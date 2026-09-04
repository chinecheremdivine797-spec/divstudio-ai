package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.UserEntity
import com.example.ui.components.GlassCard
import com.example.ui.theme.*

@Composable
fun AuthScreen(
    onLogin: (String, String) -> Unit,
    onRegister: (String, String, String) -> Unit,
    onQuickLogin: (String) -> Unit,
    onAuthSuccess: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0 = Login, 1 = Register, 2 = Forgot Password
    var email by remember { mutableStateOf("creator@divstudio.ai") }
    var password by remember { mutableStateOf("password123") }
    var fullName by remember { mutableStateOf("David Adeleke") }
    var passwordVisible by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkCanvas)
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logo & Title
        Surface(
            color = NeonIndigo.copy(alpha = 0.2f),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.size(54.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text("DIV", color = NeonCyan, fontWeight = FontWeight.Black, fontSize = 20.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "DIV AI Studio Access",
            color = TextPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Sign in with your studio account to generate & export animations",
            color = TextMuted,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
        )

        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = DarkSurface
        ) {
            // Tab Row
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = NeonCyan,
                divider = {}
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = {
                        selectedTab = 0
                        statusMessage = null
                    },
                    text = { Text("Sign In", fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = {
                        selectedTab = 1
                        statusMessage = null
                    },
                    text = { Text("Register", fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = {
                        selectedTab = 2
                        statusMessage = null
                    },
                    text = { Text("Reset", fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal) }
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            if (selectedTab == 1) {
                // Name for registration
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Full Name", color = TextSecondary) },
                    leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null, tint = NeonIndigoLight) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_name_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonIndigo,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Email Field
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Studio Email", color = TextSecondary) },
                leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null, tint = NeonIndigoLight) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("auth_email_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonIndigo,
                    unfocusedBorderColor = DarkBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                shape = RoundedCornerShape(10.dp),
                singleLine = true
            )

            if (selectedTab != 2) {
                Spacer(modifier = Modifier.height(12.dp))
                // Password Field
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password", color = TextSecondary) },
                    leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null, tint = NeonIndigoLight) },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = null,
                                tint = TextSecondary
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_password_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonIndigo,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )
            }

            statusMessage?.let { msg ->
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = msg,
                    color = if (isError) AccentRose else AccentEmerald,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Main Action Button
            Button(
                onClick = {
                    when (selectedTab) {
                        0 -> {
                            if (email.isBlank() || password.isBlank()) {
                                isError = true
                                statusMessage = "Please enter both email and password."
                            } else {
                                onLogin(email, password)
                                onAuthSuccess()
                            }
                        }
                        1 -> {
                            if (email.isBlank() || fullName.isBlank() || password.isBlank()) {
                                isError = true
                                statusMessage = "Please fill in all registration fields."
                            } else {
                                onRegister(email, fullName, password)
                                onAuthSuccess()
                            }
                        }
                        2 -> {
                            if (email.isBlank()) {
                                isError = true
                                statusMessage = "Please enter your email to reset password."
                            } else {
                                isError = false
                                statusMessage = "Password reset verification sent to $email."
                            }
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonIndigo),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("auth_submit_btn")
            ) {
                Text(
                    text = when (selectedTab) {
                        0 -> "Sign In to Studio"
                        1 -> "Create Studio Account"
                        else -> "Send Reset Instructions"
                    },
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            Divider(color = DarkBorder)
            Spacer(modifier = Modifier.height(14.dp))

            // Fast Demo Switchers for User Convenience
            Text(
                text = "Quick Studio Switcher",
                color = TextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        onQuickLogin("user_default_01")
                        onAuthSuccess()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonIndigoLight.copy(alpha = 0.5f))
                ) {
                    Text("Creator Mode", fontSize = 11.sp, color = NeonIndigoLight)
                }

                OutlinedButton(
                    onClick = {
                        onQuickLogin("user_admin_01")
                        onAuthSuccess()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AccentAmber.copy(alpha = 0.5f))
                ) {
                    Text("Admin Mode", fontSize = 11.sp, color = AccentAmber)
                }
            }
        }
    }
}
