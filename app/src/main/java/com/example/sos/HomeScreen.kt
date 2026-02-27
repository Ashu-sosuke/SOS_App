package com.example.sos

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sos.modelCread.SosViewModel
import com.example.sos.modelCread.SosViewModelFactory
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.android.gms.location.LocationServices


@Composable
fun HomeScreen() {

    val context = LocalContext.current
    val systemUiController = rememberSystemUiController()

    val viewModel: SosViewModel = viewModel(
        factory = SosViewModelFactory(context)
    )

    SideEffect {
        systemUiController.setStatusBarColor(
            color = Color.Transparent,
            darkIcons = false
        )
    }

    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0B1220),
            Color(0xFF070B14)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            TopBar()
            Spacer(modifier = Modifier.height(24.dp))
            StatusChip()
            Spacer(modifier = Modifier.height(40.dp))
            TitleSection()
            Spacer(modifier = Modifier.height(40.dp))
            SosButton(viewModel)
            Spacer(modifier = Modifier.height(32.dp))
            AIVoiceGuardCard()
        }
    }
}


@Composable
fun TopBar() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "SAFETY SHIELD",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.weight(1f))

        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = "Profile",
            tint = Color.White
        )
    }
}
@Composable
fun StatusChip() {
    Box(
        modifier = Modifier
            .background(
                color = Color(0xFF0E3B2E),
                shape = RoundedCornerShape(50)
            )
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = "SYSTEM ARMED & MONITORING",
            color = Color(0xFF3CFFB3),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun TitleSection() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Tap ",
            color = Color.White,
            fontSize = 22.sp
        )
        Text(
            text = "SOS",
            color = Color(0xFFFF4B4B),
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "if you feel unsafe",
            color = Color.White,
            fontSize = 22.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Press to instantly alert contacts\nand share your live location.",
            color = Color(0xFF9AA4B2),
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun SosButton(viewModel: SosViewModel) {

    val context = LocalContext.current
    var loading by remember { mutableStateOf(false) }

    val fusedLocationClient =
        LocationServices.getFusedLocationProviderClient(context)

    // 🔥 Define function FIRST
    fun startSosFlow() {

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->

                if (location != null) {

                    loading = true

                    viewModel.triggerSos(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        onComplete = {
                            loading = false
                        }
                    )

                } else {
                    loading = false
                }
            }
            .addOnFailureListener {
                loading = false
            }
    }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->

            val granted = permissions.values.all { it }

            if (granted) {
                startSosFlow()
            }
        }

    fun checkAndStart() {

        val audioGranted =
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED

        val locationGranted =
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        if (audioGranted && locationGranted) {
            startSosFlow()
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            )
        }
    }

    Button(
        onClick = { checkAndStart() },
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor =
                if (loading) Color.Gray
                else Color(0xFFFF3B3B)
        ),
        modifier = Modifier.size(160.dp)
    ) {
        Text(
            text = if (loading) "PROCESSING..." else "SOS",
            color = Color.White,
        )
    }
}


@Composable
fun AIVoiceGuardCard() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color(0xFF0E1626),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Icon(
            painter = painterResource(R.drawable.baseline_graphic_eq_24),
            contentDescription = null,
            tint = Color(0xFF3B82F6)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = "AI Voice Guard",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "Stress Level: Low",
                color = Color(0xFF22C55E),
                fontSize = 12.sp
            )
        }
    }
}
