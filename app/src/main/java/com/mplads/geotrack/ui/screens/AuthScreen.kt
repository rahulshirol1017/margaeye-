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

    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Password Auth, 1 = Google Sign-In

    // Credentials State
    var email by remember { mutableStateOf(AuthManager.getUserEmail(context)) }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var officerName by remember { mutableStateOf(AuthManager.getOfficerName(context)) }
    var workId by remember { mutableStateOf(AuthManager.getWorkId(context)) }
    var description by remember { mutableStateOf(AuthManager.getDescription(context)) }
    var errorMessage by remember { mutableStateOf("") }

    // Google Sign-In State
    var googleEmail by remember { mutableStateOf("officer.surveillance@gov.in") }
    var googleName by remember { mutableStateOf("Rahul Shirol (Google)") }

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
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. Header & Government Emblem
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 12.dp)
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

                Spacer(modifier = Modifier.height(2.dp))

                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Emerald500.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = Emerald400,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Text(
                    text = "Marga-eyes Portal",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = White
                )

                // 24-Hour Session Banner Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Emerald500.copy(alpha = 0.12f))
                        .border(1.dp, Emerald500.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        tint = Emerald400,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "🔒 24-Hour Authenticated Field Session",
                        color = Emerald400,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // 2. Auth Method Tab Selector
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Zinc900,
                contentColor = Emerald400,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
                    .clip(RoundedCornerShape(14.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.VpnKey, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text("Password Auth", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    },
                    selectedContentColor = Emerald400,
                    unselectedContentColor = Zinc400
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text("Google Sign-In", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    },
                    selectedContentColor = Emerald400,
                    unselectedContentColor = Zinc400
                )
            }

            // 3. Authentication Form Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Zinc900),
                border = androidx.compose.foundation.BorderStroke(1.dp, Zinc800),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    if (selectedTab == 0) {
                        // TAB 1: PASSWORD & CREDENTIALS
                        Text(
                            text = "Log In with Password Credentials",
                            color = White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )

                        // Email Field
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it; errorMessage = "" },
                            label = { Text("Email / Username") },
                            placeholder = { Text("officer@gov.in", color = Zinc500) },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Emerald400) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Emerald400,
                                unfocusedBorderColor = Zinc800,
                                focusedLabelColor = Emerald400,
                                unfocusedLabelColor = Zinc400
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Password Field with Eye Toggle
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it; errorMessage = "" },
                            label = { Text("Password") },
                            placeholder = { Text("Enter account password", color = Zinc500) },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Emerald400) },
                            trailingIcon = {
                                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                    Icon(
                                        imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Toggle password visibility",
                                        tint = Zinc400
                                    )
                                }
                            },
                            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Emerald400,
                                unfocusedBorderColor = Zinc800,
                                focusedLabelColor = Emerald400,
                                unfocusedLabelColor = Zinc400
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Officer Name
                        OutlinedTextField(
                            value = officerName,
                            onValueChange = { officerName = it; errorMessage = "" },
                            label = { Text("Field Officer Name") },
                            placeholder = { Text("e.g. Rahul Shirol", color = Zinc500) },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Emerald400) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Emerald400,
                                unfocusedBorderColor = Zinc800,
                                focusedLabelColor = Emerald400,
                                unfocusedLabelColor = Zinc400
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Work ID
                        OutlinedTextField(
                            value = workId,
                            onValueChange = { workId = it; errorMessage = "" },
                            label = { Text("Work / Project ID") },
                            placeholder = { Text("e.g. WRK-2026-8942", color = Zinc500) },
                            leadingIcon = { Icon(Icons.AutoMirrored.Filled.Notes, contentDescription = null, tint = Emerald400) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Emerald400,
                                unfocusedBorderColor = Zinc800,
                                focusedLabelColor = Emerald400,
                                unfocusedLabelColor = Zinc400
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Description
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it; errorMessage = "" },
                            label = { Text("Work / Site Description") },
                            placeholder = { Text("e.g. Road repair & drainage inspection", color = Zinc500) },
                            leadingIcon = { Icon(Icons.Default.Description, contentDescription = null, tint = Emerald400) },
                            singleLine = false,
                            maxLines = 2,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Emerald400,
                                unfocusedBorderColor = Zinc800,
                                focusedLabelColor = Emerald400,
                                unfocusedLabelColor = Zinc400
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (errorMessage.isNotEmpty()) {
                            Text(text = errorMessage, color = Rose500, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }

                        Button(
                            onClick = {
                                if (email.isBlank() || password.isBlank() || officerName.isBlank() || workId.isBlank()) {
                                    errorMessage = "Please enter Email, Password, Officer Name, and Work ID."
                                } else {
                                    val success = AuthManager.loginWithCredentials(
                                        context = context,
                                        email = email.trim(),
                                        password = password.trim(),
                                        officerName = officerName.trim(),
                                        workId = workId.trim(),
                                        description = description.trim()
                                    )
                                    if (success) {
                                        Toast.makeText(context, "Authenticated! 24-Hour session started.", Toast.LENGTH_SHORT).show()
                                        onLoginSuccess()
                                    } else {
                                        errorMessage = "Invalid credentials."
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Emerald500, contentColor = Black),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Text("Sign In (24h Session)", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                        }

                    } else {
                        // TAB 2: GOOGLE SIGN-IN
                        Text(
                            text = "Sign in with Google Account",
                            color = White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Authenticate instantly using Google single sign-on. Creates a secure 24-hour token.",
                            color = Zinc400,
                            fontSize = 12.sp
                        )

                        OutlinedTextField(
                            value = googleEmail,
                            onValueChange = { googleEmail = it },
                            label = { Text("Google Account Email") },
                            leadingIcon = { Icon(Icons.Default.AccountCircle, contentDescription = null, tint = Emerald400) },
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
                            value = workId,
                            onValueChange = { workId = it },
                            label = { Text("Work / Project ID") },
                            placeholder = { Text("e.g. GOOG-WRK-101", color = Zinc500) },
                            leadingIcon = { Icon(Icons.AutoMirrored.Filled.Notes, contentDescription = null, tint = Emerald400) },
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
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Work / Site Description") },
                            placeholder = { Text("e.g. Highway quality audit", color = Zinc500) },
                            leadingIcon = { Icon(Icons.Default.Description, contentDescription = null, tint = Emerald400) },
                            singleLine = false,
                            maxLines = 2,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Emerald400,
                                unfocusedBorderColor = Zinc800,
                                focusedLabelColor = Emerald400,
                                unfocusedLabelColor = Zinc400
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = {
                                val success = AuthManager.loginWithGoogle(
                                    context = context,
                                    googleEmail = googleEmail.trim(),
                                    googleDisplayName = googleName.trim(),
                                    workId = if (workId.isNotBlank()) workId.trim() else "GOOG-PROJECT",
                                    description = description.trim()
                                )
                                if (success) {
                                    Toast.makeText(context, "Signed in with Google! 24-Hour session started.", Toast.LENGTH_SHORT).show()
                                    onLoginSuccess()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = White, contentColor = Black),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Icon(Icons.Default.GTranslate, contentDescription = null, tint = Emerald600, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Continue with Google", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 4. Footer info
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(Icons.Default.Security, contentDescription = null, tint = Zinc500, modifier = Modifier.size(14.dp))
                Text("Token expires in 24 Hours • Encryption & Geotagging", color = Zinc500, fontSize = 11.sp)
            }
        }
    }
}
