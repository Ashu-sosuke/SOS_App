package com.example.sos

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// -------------------- DATA --------------------

data class TrustedContact(
    val name: String,
    val phone: String,
    val relation: String,
    val relationColor: Color,
    val active: Boolean,
    val initials: String
)

// -------------------- SCREEN --------------------

@Composable
fun TrustedContactsScreen(onBack: () -> Unit) {

    BackHandler { onBack() }

    val contacts = remember {
        mutableStateListOf(
            TrustedContact("Sarah Jenkins", "(555) 123-4567", "Sister", Color(0xFF8B5CF6), true, "SJ"),
            TrustedContact("Michael Chen", "(555) 987-6543", "Partner", Color(0xFF3B82F6), true, "MC"),
            TrustedContact("John Doe", "(555) 555-0199", "Friend", Color(0xFFF59E0B), false, "JD")
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B1220))
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {

            TrustedContactsTopBar(onBack)

            Spacer(Modifier.height(16.dp))
            DescriptionText()
            Spacer(Modifier.height(16.dp))
            SearchBar()
            Spacer(Modifier.height(16.dp))
            AddNewContactCard()
            Spacer(Modifier.height(24.dp))

            Text(
                text = "MY CIRCLE (${contacts.size})",
                color = Color(0xFF94A3B8),
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(Modifier.height(12.dp))

            contacts.forEachIndexed { index, contact ->
                ContactItem(
                    contact = contact,
                    onToggle = {
                        contacts[index] = contact.copy(active = !contact.active)
                    }
                )
                Spacer(Modifier.height(12.dp))
            }

            Spacer(Modifier.height(24.dp))
            EmptyStateCard()
            Spacer(Modifier.height(80.dp))
        }

        FloatingActionButton(
            onClick = { /* Add contact */ },
            containerColor = Color(0xFF2563EB),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White)
        }
    }
}

// -------------------- COMPONENTS --------------------

@Composable
fun TrustedContactsTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint = Color.White,
            modifier = Modifier.clickable { onBack() }
        )

        Spacer(Modifier.weight(1f))

        Text(
            text = "Trusted Contacts",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.weight(1f))

        Text(
            text = "Edit",
            color = Color(0xFF3B82F6),
            fontSize = 14.sp
        )
    }
}

@Composable
fun DescriptionText() {
    Text(
        text = "Manage who receives your alerts. Active contacts will get an instant SOS notification with your live location and audio recording.",
        color = Color(0xFF94A3B8),
        fontSize = 14.sp,
        modifier = Modifier.padding(horizontal = 20.dp)
    )
}

@Composable
fun SearchBar() {
    Box(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .background(Color(0xFF1E293B), RoundedCornerShape(12.dp))
            .fillMaxWidth()
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF64748B))
            Spacer(Modifier.width(8.dp))
            Text("Search name or number", color = Color(0xFF64748B))
        }
    }
}

@Composable
fun AddNewContactCard() {
    Row(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .background(Color(0xFF2563EB), RoundedCornerShape(14.dp))
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(painterResource(R.drawable.baseline_person_add_alt_1_24), null, tint = Color.White)
        Spacer(Modifier.width(12.dp))
        Column {
            Text("Add New Contact", color = Color.White, fontWeight = FontWeight.Bold)
            Text("Select from your phonebook", color = Color(0xFFBFDBFE), fontSize = 12.sp)
        }
        Spacer(Modifier.weight(1f))
        Icon(painterResource(R.drawable.outline_arrow_forward_ios_24), null, tint = Color.White)
    }
}

@Composable
fun ContactItem(contact: TrustedContact, onToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .background(Color(0xFF0F172A), RoundedCornerShape(14.dp))
            .fillMaxWidth()
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .size(42.dp)
                .background(Color(0xFF1E293B), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(contact.initials, color = Color.White, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.width(12.dp))

        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(contact.name, color = Color.White, fontWeight = FontWeight.Medium)
                Spacer(Modifier.width(8.dp))
                RelationChip(contact.relation, contact.relationColor)
            }
            Text(contact.phone, color = Color(0xFF94A3B8), fontSize = 12.sp)
        }

        Spacer(Modifier.weight(1f))

        Switch(
            checked = contact.active,
            onCheckedChange = { onToggle() }
        )
    }
}

@Composable
fun RelationChip(text: String, color: Color) {
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(text, color = color, fontSize = 10.sp)
    }
}

@Composable
fun EmptyStateCard() {
    Box(
        modifier = Modifier
            .padding(20.dp)
            .fillMaxWidth()
            .height(140.dp)
            .border(1.dp, Color(0xFF334155), RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(painterResource(R.drawable.baseline_group_add_24), null, tint = Color(0xFF64748B))
            Spacer(Modifier.height(8.dp))
            Text("Expand your safety net", color = Color.White)
            Text(
                "Add more trusted contacts to\nincrease the chance of immediate response.",
                color = Color(0xFF94A3B8),
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
