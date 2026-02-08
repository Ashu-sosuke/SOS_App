package com.example.sos.loginCred

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.sos.R

@Composable
fun EditProfileDialog(
    authViewModel: AuthViewModel,
    onDismiss: () -> Unit
) {
    val profile = authViewModel.profile

    var name by remember { mutableStateOf(profile.name) }
    var phone by remember { mutableStateOf(profile.phone ?: "") }
    var email by remember { mutableStateOf(profile.email ?: "") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> imageUri = uri }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF151E30),
        shape = RoundedCornerShape(20.dp),
        title = {
            Text("Edit Profile", color = Color.White, fontWeight = FontWeight.SemiBold)
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {

                AsyncImage(
                    model = imageUri ?: profile.photoUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .clickable { imagePicker.launch("image/*") },
                    placeholder = painterResource(R.drawable.baseline_person_2_24),
                    error = painterResource(R.drawable.baseline_person_2_24)
                )

                Text(
                    "Change photo",
                    color = Color(0xFF4CAF50),
                    fontSize = 12.sp,
                    modifier = Modifier.clickable { imagePicker.launch("image/*") }
                )

                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color.White,
                    )
                )

                Spacer(Modifier.height(12.dp))

                if (profile.phone == null) {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it.filter(Char::isDigit) },
                        label = { Text("Phone") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Color.White,
                        )

                    )
                }

                if (profile.email == null) {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Color.White,
                        )
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    authViewModel.saveProfile(
                        updated = profile.copy(
                            name = if (name.isBlank()) "User01" else name,
                            phone = phone.ifBlank { profile.phone },
                            email = email.ifBlank { profile.email }
                        ),
                        imageUri = imageUri
                    ) {
                        onDismiss()
                    }
                }
            ) {
                Text("Save", color = Color(0xFF4CAF50))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        }
    )
}
