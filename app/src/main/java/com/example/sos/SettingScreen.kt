package com.example.sos

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SafetySettingsScreen(onBack: () -> Unit) {
    val background = Color(0xFF0B1220)
    val card = Color(0xFF151E30)
    val primaryBlue = Color(0xFF1F5EFF)
    val scrollState = rememberScrollState()

    var stressDetection by remember { mutableStateOf(true) }
    var autoSos by remember { mutableStateOf(true) }
    var backgroundTracking by remember { mutableStateOf(true) }
    var micSensitivity by remember { mutableFloatStateOf(0.75f) }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {

        // Top Bar
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color.White,
                modifier = Modifier.clickable { onBack() }
            )

            Spacer(Modifier.width(12.dp))
            Text(
                text = "Safety Settings",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(Modifier.height(16.dp))

        // Protected Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(primaryBlue, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("Active", color = Color.White, fontSize = 12.sp)
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Protected",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "AI monitoring and emergency\nSOS are ready.",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 14.sp
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(36.dp)
                    .background(Color.White.copy(alpha = 0.15f), CircleShape)
            )
        }

        Spacer(Modifier.height(24.dp))

        SectionTitle("AI PROTECTION")

        // Voice Stress Detection
        SettingCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Voice Stress Detection", color = Color.White, fontWeight = FontWeight.Medium)
                    Text(
                        "Analyzes audio for screams",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
                Switch(
                    checked = stressDetection,
                    onCheckedChange = { stressDetection = it }
                )
            }

            Spacer(Modifier.height(16.dp))

            Text("Microphone Sensitivity", color = Color.White, fontSize = 14.sp)

            Slider(
                value = micSensitivity,
                onValueChange = { micSensitivity = it },
                valueRange = 0f..1f
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Low", color = Color.Gray, fontSize = 12.sp)
                Text("Medium", color = Color.Gray, fontSize = 12.sp)
                Text("High", color = Color(0xFF4CAF50), fontSize = 12.sp)
            }
        }

        Spacer(Modifier.height(24.dp))

        SectionTitle("EMERGENCY RESPONSE")

        SettingToggle(
            title = "Auto-SOS Trigger",
            subtitle = "Alert contacts when threat\nverified",
            checked = autoSos,
            onCheckedChange = { autoSos = it }
        )

        SettingToggle(
            title = "Background Tracking",
            subtitle = "Share location during SOS events",
            checked = backgroundTracking,
            onCheckedChange = { backgroundTracking = it }
        )

        Spacer(Modifier.height(24.dp))

        SectionTitle("SYSTEM HEALTH")

        NavigationRow(
            title = "Permission Manager",
            subtitle = "Mic • On • Loc • Always"
        )

        NavigationRow(
            title = "Privacy Center",
            subtitle = "Manage data & history"
        )

        Spacer(Modifier.height(24.dp))

        // Run Simulation
        OutlinedButton(
            onClick = {},
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Run Safety Simulation", color = Color.White)
        }

        Spacer(Modifier.height(12.dp))

        Text(
            text = "Version 2.4.1 • Secure Connection Active",
            color = Color.Gray,
            fontSize = 12.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        color = Color.Gray,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold
    )
}

@Composable
fun SettingCard(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF151E30), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        content()
    }
}

@Composable
fun SettingToggle(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(Color(0xFF151E30), RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, color = Color.White, fontWeight = FontWeight.Medium)
            Text(subtitle, color = Color.Gray, fontSize = 12.sp)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun NavigationRow(title: String, subtitle: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .background(Color(0xFF151E30), RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, color = Color.White)
            Text(subtitle, color = Color.Gray, fontSize = 12.sp)
        }
        Icon(
            imageVector = Icons.Default.KeyboardArrowRight,
            contentDescription = null,
            tint = Color.Gray
        )
    }
}
