package com.example.sos.utils

import android.telephony.SmsManager
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

fun sendSosSms(
    phone: String,
    latitude: Double,
    longitude: Double,
    link: String
) {

    val message = """
🚨 SOS ALERT

I may be in danger.

My location:
$link
    """.trimIndent()

    try {
        val smsManager = SmsManager.getDefault()
        smsManager.sendTextMessage(
            phone,
            null,
            message,
            null,
            null
        )
        Log.d("SOS sms", "Sent SMS to $phone")
    }
    catch (e: Exception) {

        Log.e("SOS sms", "SMS Failed: ${e.message}")

    }
}

fun getTrustedContacts(onResult: (List<String>) -> Unit) {

    val userId = FirebaseAuth.getInstance().currentUser?.uid

    if (userId == null) {
        onResult(emptyList())
        return
    }

    FirebaseFirestore.getInstance()
        .collection("users")
        .document(userId)
        .collection("trusted_contacts")
        .get()
        .addOnSuccessListener { result ->

            val phones = result.documents.mapNotNull {
                it.getString("phone")
            }

            onResult(phones)
        }
}