package com.example.sos.modelCread

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import java.util.*

class SosViewModel(private val context: Context) : ViewModel() {

    private val storage = FirebaseStorage.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val api = RetrofitClient.api
    private val recorder = AudioRecorder(context)

    var activeIncidentId: String? = null
        private set

    fun triggerSos(
        latitude: Double,
        longitude: Double,
        onComplete: () -> Unit
    ) {

        val user = auth.currentUser
        if (user == null) {
            Log.e("SOS", "User not logged in")
            onComplete()
            return
        }

        val userId = user.uid
        val phone = user.phoneNumber ?: "Anonymous"

        viewModelScope.launch {

            try {

                Log.d("SOS", "Starting recording...")

                val incidentId = UUID.randomUUID().toString()
                activeIncidentId = incidentId

                // Save initial location immediately
                saveLocationToIncident(
                    userId,
                    incidentId,
                    latitude,
                    longitude
                )

                // Record 15 sec
                val audioFile = recorder.startRecording()
                delay(15000)
                recorder.stopRecording()

                Log.d("SOS", "Recording finished")

                // Upload audio
                val downloadUrl = uploadAudio(audioFile, userId)

                // Prepare backend request
                val request = IncidentRequest(
                    incidentId = incidentId,
                    userId = userId,
                    phone = phone,
                    audioUrl = downloadUrl,
                    latitude = latitude,
                    longitude = longitude,
                    timestamp = System.currentTimeMillis() / 1000
                )

                Log.d("SOS", "Calling AI backend...")

                val response = api.processIncident(request)

                if (response.isSuccessful && response.body() != null) {

                    val result = response.body()!!

                    saveIncidentToFirestore(
                        userId,
                        incidentId,
                        request,
                        result
                    )

                } else {
                    Log.e("SOS", "API error: ${response.code()}")
                }

            } catch (e: Exception) {
                Log.e("SOS", "Flow failed", e)
            } finally {
                // 🔥 VERY IMPORTANT
                onComplete()
            }
        }
    }

    // ===============================
    // Upload Audio
    // ===============================

    private suspend fun uploadAudio(file: File, userId: String): String {

        val ref = storage.reference
            .child("users/$userId/incidents/${file.name}")

        ref.putFile(android.net.Uri.fromFile(file)).await()

        return ref.downloadUrl.await().toString()
    }

    // ===============================
    // Save Main Incident Data
    // ===============================

    private suspend fun saveIncidentToFirestore(
        userId: String,
        incidentId: String,
        request: IncidentRequest,
        response: IncidentResponse
    ) {

        val data = hashMapOf(
            "incidentId" to incidentId,
            "userId" to userId,
            "phone" to request.phone,
            "audioUrl" to request.audioUrl,
            "latitude" to request.latitude,
            "longitude" to request.longitude,
            "timestamp" to request.timestamp,
            "transcript" to response.transcript,
            "severityScore" to response.severityScore,
            "threatType" to response.threatType,
            "finalSeverity" to response.finalSeverity,
            "confidence" to response.confidence,
            "recommendedAction" to response.recommendedAction,
            "createdAt" to System.currentTimeMillis()
        )

        firestore.collection("users")
            .document(userId)
            .collection("incidents")
            .document(incidentId)
            .set(data)
            .await()

        Log.d("SOS", "Incident saved under user $userId")
    }

    // ===============================
    // Save Location (Subcollection)
    // ===============================

    suspend fun saveLocationToIncident(
        userId: String,
        incidentId: String,
        latitude: Double,
        longitude: Double
    ) {

        val locationData = hashMapOf(
            "latitude" to latitude,
            "longitude" to longitude,
            "timestamp" to System.currentTimeMillis()
        )

        firestore.collection("users")
            .document(userId)
            .collection("incidents")
            .document(incidentId)
            .collection("locations")
            .add(locationData)
            .await()

        Log.d("SOS", "Location saved to incident $incidentId")
    }
}