package com.example.sos.location

import android.app.Application
import android.content.*
import android.content.Context.POWER_SERVICE
import android.content.pm.PackageManager
import android.os.Build
import android.os.Looper
import android.os.PowerManager
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID

data class LocationData(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float
)

class SafetyModeViewModel(application: Application) : AndroidViewModel(application) {

    private val appContext = getApplication<Application>()

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var currentIncidentId: String = ""

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _location = MutableStateFlow<LocationData?>(null)
    val location: StateFlow<LocationData?> = _location

    private val _locationHistory = MutableStateFlow<List<LocationData>>(emptyList())
    val locationHistory: StateFlow<List<LocationData>> = _locationHistory

    private var receiverRegistered = false

    // ============================
    // Broadcast Receiver
    // ============================

    private val locationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {

            if (intent == null) return

            val lat = intent.getDoubleExtra("latitude", Double.MIN_VALUE)
            if (lat == Double.MIN_VALUE) return

            val lng = intent.getDoubleExtra("longitude", 0.0)
            val accuracy = intent.getFloatExtra("accuracy", 0f)

            val newLocation = LocationData(lat, lng, accuracy)

            viewModelScope.launch {

                _location.value = newLocation

                _locationHistory.value =
                    (_locationHistory.value + newLocation)
                        .takeLast(200)

                uploadLocationToFirebase(newLocation)
            }
        }
    }

    private fun uploadLocationToFirebase(location: LocationData) {

        val userId = auth.currentUser?.uid ?: return
        if (currentIncidentId.isEmpty()) return

        val incidentRef =
            firestore.collection("users")
                .document(userId)
                .collection("incidents")
                .document(currentIncidentId)

        val locationData = hashMapOf(
            "lat" to location.latitude,
            "lng" to location.longitude,
            "accuracy" to location.accuracy,
            "timestamp" to System.currentTimeMillis()
        )

        // Update current location
        incidentRef.update("lastLocation", locationData)

        // Save history
        incidentRef
            .collection("locationHistory")
            .add(locationData)
    }


    fun uploadAudio(audioUrl: String) {

        val userId = auth.currentUser?.uid ?: return

        firestore.collection("users")
            .document(userId)
            .collection("incidents")
            .document(currentIncidentId)
            .collection("media")
            .document("audio")
            .collection("files")
            .add(
                mapOf(
                    "url" to audioUrl,
                    "timestamp" to System.currentTimeMillis()
                )
            )
    }
    private fun hasLocationPermission(): Boolean {

        return ContextCompat.checkSelfPermission(
            appContext,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    // ============================
    // START SOS
    // ============================

    fun startSOS(): String {

        if (!hasLocationPermission()) return ""

        val userId = auth.currentUser?.uid ?: return ""

        val incidentId = UUID.randomUUID().toString()
        currentIncidentId = incidentId

        val data = hashMapOf(
            "startTime" to System.currentTimeMillis(),
            "active" to true
        )

        firestore.collection("users")
            .document(userId)
            .collection("incidents")
            .document(incidentId)
            .set(data)

        registerReceiverIfNeeded()

        startSOSService()

        return incidentId
    }


    fun stopSOS() {

        val userId = auth.currentUser?.uid ?: return

        val intent = Intent(appContext, ForegroundLocationService::class.java)
        appContext.stopService(intent)

        unregisterReceiverIfNeeded()

        firestore.collection("users")
            .document(userId)
            .collection("incidents")
            .document(currentIncidentId)
            .update(
                mapOf(
                    "active" to false,
                    "endTime" to System.currentTimeMillis()
                )
            )

        currentIncidentId = ""
    }

    // ============================
    // Foreground Service
    // ============================

     fun startSOSService() {

        val intent = Intent(appContext, ForegroundLocationService::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            appContext.startForegroundService(intent)
        } else {
            appContext.startService(intent)
        }
    }

    fun stopSOSService() {

        val intent = Intent(appContext, ForegroundLocationService::class.java)
        appContext.stopService(intent)
    }

    // ============================
    // Broadcast Receiver control
    // ============================

    private fun registerReceiverIfNeeded() {

        if (receiverRegistered) return

        val filter = IntentFilter(ACTION_SOS_LOCATION_UPDATE)

        ContextCompat.registerReceiver(
            appContext,
            locationReceiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )

        receiverRegistered = true
    }

    private fun unregisterReceiverIfNeeded() {

        if (!receiverRegistered) return

        appContext.unregisterReceiver(locationReceiver)

        receiverRegistered = false
    }



}