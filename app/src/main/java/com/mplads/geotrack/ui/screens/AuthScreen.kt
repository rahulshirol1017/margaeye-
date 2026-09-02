package com.mplads.geotrack.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mplads.geotrack.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    initialWorkerName: String,
    initialWorkId: String,
    initialDescription: String,
    onLoginSuccess: (String, String, String) -> Unit
) {
    var workerName by remember { mutableStateOf(initialWorkerName) }
    var workId by remember { mutableStateOf(initialWorkId) }
    var description by remember { mutableStateOf(initialDescription) }
    var errorMessage by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Zinc950)
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. Header & Government Emblem
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Zinc900)
                        .border(1.dp, Emerald500.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = Emerald400,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "GOVERNMENT OF INDIA",
                        color = White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Emerald500.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Badge,
                        contentDescription = null,
                        tint = Emerald400,
                        modifier = Modifier.size(30.dp)
                    )
                }

                Text(
                    text = "Marga-eyes Sign-In",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = White
                )

                Text(
                    text = "Field Officer Authentication & Site Details",
                    fontSize = 13.sp,
                    color = Zinc400,
                    fontWeight = FontWeight.Normal
                )
            }

            // 2. Authentication Form Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Zinc900),
                border = androidx.compose.foundation.BorderStroke(1.dp, Zinc800),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Field 1: Officer Name
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Field Officer Name",
                            color = Zinc300,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        OutlinedTextField(
                            value = workerName,
                            onValueChange = {
                                workerName = it
                                errorMessage = ""
                            },
                            placeholder = { Text("e.g. Rahul Shirol", color = Zinc500) },
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = null, tint = Emerald400)
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Emerald400,
                                unfocusedBorderColor = Zinc800,
                                focusedLabelColor = Emerald400,
                                unfocusedLabelColor = Zinc400
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Field 2: Work / Project ID
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Work / Project ID",
                            color = Zinc300,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        OutlinedTextField(
                            value = workId,
                            onValueChange = {
                                workId = it
                                errorMessage = ""
                            },
                            placeholder = { Text("e.g. WRK-2026-8942", color = Zinc500) },
                            leadingIcon = {
                                Icon(Icons.AutoMirrored.Filled.Notes, contentDescription = null, tint = Emerald400)
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Emerald400,
                                unfocusedBorderColor = Zinc800,
                                focusedLabelColor = Emerald400,
                                unfocusedLabelColor = Zinc400
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Field 3: Work Description & Site Notes
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Work / Site Description",
                            color = Zinc300,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        OutlinedTextField(
                            value = description,
                            onValueChange = {
                                description = it
                                errorMessage = ""
                            },
                            placeholder = { Text("e.g. Road repair & drainage inspection", color = Zinc500) },
                            leadingIcon = {
                                Icon(Icons.Default.Description, contentDescription = null, tint = Emerald400)
                            },
                            singleLine = false,
                            maxLines = 3,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Emerald400,
                                unfocusedBorderColor = Zinc800,
                                focusedLabelColor = Emerald400,
                                unfocusedLabelColor = Zinc400
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    if (errorMessage.isNotEmpty()) {
                        Text(
                            text = errorMessage,
                            color = Rose500,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Sign-In Button
                    Button(
                        onClick = {
                            if (workerName.trim().isEmpty() || workId.trim().isEmpty()) {
                                errorMessage = "Please enter both Field Officer Name and Work ID."
                            } else {
                                onLoginSuccess(workerName.trim(), workId.trim(), description.trim())
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Emerald500,
                            contentColor = Black
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text(
                            text = "Start Field Inspection",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // 3. Footer Notice
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = Zinc500,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "Authenticated session with MediaStore & GPS Geotagging",
                    color = Zinc500,
                    fontSize = 11.sp
                )
            }
        }
    }
}
