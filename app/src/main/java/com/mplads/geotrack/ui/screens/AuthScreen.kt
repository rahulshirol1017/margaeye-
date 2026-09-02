package com.mplads.geotrack.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mplads.geotrack.ui.theme.*
import com.mplads.geotrack.utils.AuthManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current

    var showPasswordForm by remember { mutableStateOf(false) }

    // Google Profile Defaults
    var googleEmail by remember { mutableStateOf(AuthManager.getUserEmail(context).ifBlank { "rahul.shirol@gov.in" }) }
    var googleName by remember { mutableStateOf(AuthManager.getOfficerName(context).ifBlank { "Rahul Shirol" }) }

    // Manual Form Defaults
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var workId by remember { mutableStateOf(AuthManager.getWorkId(context).ifBlank { "WRK-2026-8942" }) }
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
            // 1. Google App Style Header & Logo
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(top = 32.dp)
            ) {
                // Official Badge Header
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
                        text = "GOVERNMENT SURVEILLANCE",
                        color = White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // App Symbol Logo
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(Emerald500.copy(alpha = 0.15f))
                        .border(1.5.dp, Emerald400.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Camera,
                        contentDescription = null,
                        tint = Emerald400,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Text(
                    text = "Welcome to Marga-eyes",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = White
                )

                Text(
                    text = "Geo-Spatial Infrastructure & Road Tracking",
                    fontSize = 13.sp,
                    color = Zinc400
                )
            }

            // 2. Google-Style One-Tap Sign-In Card
            if (!showPasswordForm) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Google Account Profile Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Zinc900),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Zinc800),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Avatar Circle
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF4285F4)), // Google Blue
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = googleName.take(1).uppercase(),
                                    color = White,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = googleName,
                                    color = White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = googleEmail,
                                    color = Zinc400,
                                    fontSize = 12.sp
                                )
                            }

                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Emerald400,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Primary Button: "Continue as [Google User]"
                    Button(
                        onClick = {
                            val success = AuthManager.loginWithGoogle(
                                context = context,
                                googleEmail = googleEmail.trim(),
                                googleDisplayName = googleName.trim(),
                                workId = workId,
                                description = ""
                            )
                            if (success) {
                                Toast.makeText(context, "Signed in as $googleName", Toast.LENGTH_SHORT).show()
                                onLoginSuccess()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = White,
                            contentColor = Black
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = null,
                                tint = Color(0xFF4285F4),
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Continue as $googleName",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Secondary Option: Use Password Credentials
                    Text(
                        text = "Use password or another account",
                        color = Emerald400,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { showPasswordForm = true }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            } else {
                // Password Login Form
                Card(
                    colors = CardDefaults.cardColors(containerColor = Zinc900),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Zinc800),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Password Sign-In",
                                color = White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Google Sign-In",
                                color = Emerald400,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.clickable { showPasswordForm = false }
                            )
                        }

                        OutlinedTextField(
                            value = googleEmail,
                            onValueChange = { googleEmail = it; errorMessage = "" },
                            label = { Text("Email address") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Emerald400) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Emerald400,
                                unfocusedBorderColor = Zinc800,
                                focusedLabelColor = Emerald400,
                                unfocusedLabelColor = Zinc400
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it; errorMessage = "" },
                            label = { Text("Password") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Emerald400) },
                            trailingIcon = {
                                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                    Icon(
                                        imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = null,
                                        tint = Zinc400
                                    )
                                }
                            },
                            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Emerald400,
                                unfocusedBorderColor = Zinc800,
                                focusedLabelColor = Emerald400,
                                unfocusedLabelColor = Zinc400
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (errorMessage.isNotEmpty()) {
                            Text(text = errorMessage, color = Rose500, fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                if (googleEmail.isBlank() || password.isBlank()) {
                                    errorMessage = "Please enter both Email and Password."
                                } else {
                                    val success = AuthManager.loginWithCredentials(
                                        context = context,
                                        email = googleEmail.trim(),
                                        password = password.trim(),
                                        officerName = googleEmail.substringBefore("@").replace(".", " "),
                                        workId = workId,
                                        description = ""
                                    )
                                    if (success) {
                                        Toast.makeText(context, "Authenticated successfully", Toast.LENGTH_SHORT).show()
                                        onLoginSuccess()
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Emerald500, contentColor = Black),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Text("Sign In", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            // 3. Official Footer Notice
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = Zinc500,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "Secured 24-Hour Token Session",
                        color = Zinc500,
                        fontSize = 11.sp
                    )
                }

                Text(
                    text = "GOVERNMENT OF INDIA • MARGA-EYES V3.0",
                    color = Zinc500,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}
