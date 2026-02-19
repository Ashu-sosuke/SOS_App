package com.example.sos.location

import android.app.Application
import android.content.*
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class LocationData(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float
)

class SafetyModeViewModel(application: Application) :
    AndroidViewModel(application) {

    private val appContext = getApplication<Application>()

    private val _location = MutableStateFlow<LocationData?>(null)
    val location: StateFlow<LocationData?> = _location

    private val _locationHistory = MutableStateFlow<List<LocationData>>(emptyList())
    val locationHistory: StateFlow<List<LocationData>> = _locationHistory

    private var receiverRegistered = false

    // ===============================
    // Broadcast Receiver
    // ===============================

    private val locationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {

            Log.d("SOS_RECEIVER", "Broadcast received")

            if (intent == null) return

            val lat = intent.getDoubleExtra("latitude", Double.MIN_VALUE)
            if (lat == Double.MIN_VALUE) return

            val lng = intent.getDoubleExtra("longitude", 0.0)
            val accuracy = intent.getFloatExtra("accuracy", 0f)

            val newLocation = LocationData(lat, lng, accuracy)

            viewModelScope.launch {
                _location.value = newLocation

                // Limit history size (memory safe)
                _locationHistory.value =
                    (_locationHistory.value + newLocation)
                        .takeLast(200)
            }
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            appContext,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }


    // ===============================
    // SERVICE CONTROL
    // ===============================

    fun startSOSService() {

        if (!hasLocationPermission()) return

        registerReceiverIfNeeded()

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

        unregisterReceiverIfNeeded()
    }

    // ===============================
    // RECEIVER MANAGEMENT
    // ===============================

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

    override fun onCleared() {
        super.onCleared()
        unregisterReceiverIfNeeded()
    }
}
