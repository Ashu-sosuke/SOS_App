package com.example.sos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.SegmentedButtonDefaults.Icon
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
import androidx.navigation.compose.rememberNavController
import com.example.sos.utils.BottomNavBar
import androidx.compose.runtime.SideEffect
import com.google.accompanist.systemuicontroller.rememberSystemUiController


@Composable
fun HomeScreen() {

    val systemUiController = rememberSystemUiController()

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
            SosButton()
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
fun SosButton() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(220.dp)
    ) {
        Box(
            modifier = Modifier
                .size(200.dp)
                .background(
                    color = Color(0x44FF3B3B),
                    shape = CircleShape
                )
        )

        Button(
            onClick = { /* Trigger SOS */ },
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFF3B3B)
            ),
            modifier = Modifier.size(160.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "SOS",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "SOS",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = "Hold for 3 seconds to cancel accidental taps",
        color = Color(0xFF6B7280),
        fontSize = 12.sp
    )
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
