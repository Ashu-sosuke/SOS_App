package com.example.sos.loginCred

import android.app.Activity
import android.net.Uri
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class AuthViewModel(
    private val repo: AuthRepo = AuthRepo()
) : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    var profileSaved by mutableStateOf(false)
        private set

    var profile by mutableStateOf(UserData())
        private set

    var verificationId by mutableStateOf<String?>(null)
        private set

    var timer by mutableStateOf(60)
        private set

    var loading by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    /* ---------------- OTP ---------------- */

    fun sendOtp(
        phone: String,
        activity: Activity,
        onCodeSent: () -> Unit,
        onError: (String) -> Unit
    ) {
        loading = true

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phone)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    auth.signInWithCredential(credential)
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    loading = false
                    onError(e.message ?: "OTP verification failed")
                }

                override fun onCodeSent(
                    id: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    loading = false
                    verificationId = id
                    startTimer()
                    onCodeSent()
                }
            })
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun verifyOtp(
        otp: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val id = verificationId ?: return onError("OTP not sent")

        val credential = PhoneAuthProvider.getCredential(id, otp)
        auth.signInWithCredential(credential)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it.message ?: "Invalid OTP") }
    }

    private fun startTimer() {
        timer = 60
        viewModelScope.launch {
            while (timer > 0) {
                delay(1000)
                timer--
            }
        }
    }

    /* ---------------- GOOGLE ---------------- */

    fun googleLogin(idToken: String, onSuccess: () -> Unit) {
        loading = true
        repo.firebaseAuthWithGoogle(idToken) { success, msg ->
            loading = false
            if (success) onSuccess()
            else error = msg
        }
    }

    /* ---------------- GUEST ---------------- */

    fun guestLogin(onSuccess: () -> Unit) {
        auth.signInAnonymously().addOnSuccessListener {
            val uid = it.user!!.uid
            val guestProfile = UserData(isGuest = true)

            db.collection("users").document(uid)
                .set(guestProfile)
                .addOnSuccessListener {
                    profile = guestProfile
                    onSuccess()
                }
        }
    }

    fun isUserLoggedIn(): Boolean = auth.currentUser != null

    fun logout() {
        auth.signOut()
        profile = UserData()
    }

    /* ---------------- PROFILE ---------------- */

    fun loadUserProfile() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("users").document(uid)
            .get()
            .addOnSuccessListener { doc ->
                profile = UserData(
                    name = doc.getString("name") ?: "User01",
                    phone = doc.getString("phone"),
                    email = doc.getString("email"),
                    photoUrl = doc.getString("photoUrl"),
                    isGuest = doc.getBoolean("isGuest") ?: false
                )
            }
    }

    fun saveProfile(
        updated: UserData,
        imageUri: Uri?,
        onSuccess: () -> Unit
    ) {
        val uid = auth.currentUser?.uid ?: return
        loading = true

        if (imageUri != null) {
            val ref = storage.reference.child("profile_photos/$uid.jpg")

            ref.putFile(imageUri)
                .continueWithTask { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let { throw it }
                    }
                    ref.downloadUrl
                }
                .addOnSuccessListener { downloadUrl ->
                    saveProfileData(
                        updated.copy(photoUrl = downloadUrl.toString()),
                        onSuccess
                    )
                }
                .addOnFailureListener {
                    loading = false
                    error = it.message
                }
        } else {
            saveProfileData(updated, onSuccess)
        }
    }

    private fun saveProfileData(updated: UserData, onSuccess: () -> Unit) {
        val uid = auth.currentUser?.uid ?: return

        db.collection("users").document(uid)
            .set(updated, SetOptions.merge())
            .addOnSuccessListener {
                profile = updated
                loading = false
                profileSaved = true      // ✅ CONFIRMATION FLAG
                onSuccess()
            }
            .addOnFailureListener {
                loading = false
                error = it.message
            }
    }

    fun clearProfileSavedFlag() {
        profileSaved = false
    }

}
