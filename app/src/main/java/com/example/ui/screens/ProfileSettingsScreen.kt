package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.UserEntity
import com.example.data.repository.ProviderStatus
import com.example.ui.components.GlassCard
import com.example.ui.navigation.NavDestination
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSettingsScreen(
    currentUser: UserEntity?,
    providerStatuses: List<ProviderStatus>,
    isAiConfigured: Boolean,
    onSaveProfile: (fullName: String, defaultStyle: String, defaultRatio: String, defaultVoice: String) -> Unit,
    onSaveGeminiApiKey: (String) -> Unit,
    onClearGeminiApiKey: () -> Unit,
    onLogout: () -> Unit,
    onNavigate: (NavDestination) -> Unit
) {
    var fullName by remember { mutableStateOf(currentUser?.fullName ?: "") }
    var selectedStyle by remember { mutableStateOf(currentUser?.defaultStyle ?: "African Animation") }
    var selectedRatio by remember { mutableStateOf(currentUser?.defaultRatio ?: "16:9") }
    var selectedVoice by remember { mutableStateOf(currentUser?.defaultVoice ?: "Amaka") }
    var geminiApiKey by remember { mutableStateOf("") }
    var showKey by remember { mutableStateOf(false) }
    var showSavedMessage by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkCanvas)
            .testTag("profile_settings_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp)
    ) {
        item {
            GlassCard(modifier = Modifier.fillMaxWidth(), backgroundColor = DarkSurfaceVariant) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(NeonIndigo.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = currentUser?.fullName?.take(1)?.uppercase() ?: "D",
                            color = NeonCyan,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(currentUser?.fullName ?: "Creator", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(currentUser?.email ?: "", color = TextSecondary, fontSize = 12.sp)
                        Surface(
                            color = NeonCyan.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Text(
                                text = "${currentUser?.planName ?: "Studio Pro"} • ${currentUser?.creditsRemaining ?: 100} Credits",
                                color = NeonCyanLight,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text("Studio Defaults & Preferences", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Display Name", color = TextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonIndigo,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))
                Text("Default Animation Style", color = TextSecondary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("African Animation", "3D Animation", "Anime", "Cinematic").forEach { style ->
                        FilterChip(
                            selected = selectedStyle == style,
                            onClick = { selectedStyle = style },
                            label = { Text(style, fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = NeonIndigo)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text("Default Aspect Ratio", color = TextSecondary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("16:9", "9:16", "1:1", "4:5").forEach { ratio ->
                        FilterChip(
                            selected = selectedRatio == ratio,
                            onClick = { selectedRatio = ratio },
                            label = { Text(ratio, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = NeonCyan, selectedLabelColor = Color.Black)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = {
                        onSaveProfile(fullName, selectedStyle, selectedRatio, selectedVoice)
                        showSavedMessage = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonIndigo),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save Studio Preferences", color = Color.White, fontWeight = FontWeight.Bold)
                }

                if (showSavedMessage) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Studio preferences saved successfully!", color = AccentEmerald, fontSize = 12.sp)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text("Real AI Generation", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Connect Google Gemini and Veo for real AI script, image and video generation. Your key is stored only in this app's private storage and is never committed to GitHub.",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = geminiApiKey,
                    onValueChange = { geminiApiKey = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Gemini API Key", color = TextSecondary) },
                    placeholder = { Text("Paste your Google AI Studio API key", color = TextMuted) },
                    singleLine = true,
                    visualTransformation = if (showKey) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showKey = !showKey }) {
                            Icon(if (showKey) Icons.Filled.VisibilityOff else Icons.Filled.Visibility, contentDescription = "Toggle key visibility", tint = TextSecondary)
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonIndigo,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = NeonCyan
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            if (geminiApiKey.isNotBlank()) {
                                onSaveGeminiApiKey(geminiApiKey.trim())
                                geminiApiKey = ""
                            }
                        },
                        enabled = geminiApiKey.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonIndigo),
                        modifier = Modifier.weight(1f)
                    ) { Text("Connect AI") }
                    OutlinedButton(
                        onClick = {
                            onClearGeminiApiKey()
                            geminiApiKey = ""
                        },
                        enabled = isAiConfigured,
                        modifier = Modifier.weight(1f)
                    ) { Text("Clear Key") }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    if (isAiConfigured) "● Gemini/Veo connection configured" else "○ Gemini/Veo connection not configured",
                    color = if (isAiConfigured) AccentEmerald else AccentAmber,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            Text("AI Generation Providers & Engine", color = TextPrimary, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            Text("Secure provider abstraction with real Google generation support", color = TextMuted, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp, bottom = 8.dp))
        }

        items(providerStatuses) { provider ->
            Surface(
                color = DarkSurface,
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (provider.isConfigured) DarkBorder else AccentAmber.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(provider.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Surface(
                            color = if (provider.isConfigured) AccentEmerald.copy(alpha = 0.2f) else AccentAmber.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = if (provider.isConfigured) "ACTIVE" else "NOT CONFIGURED",
                                color = if (provider.isConfigured) AccentEmerald else AccentAmber,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text("Status: ${provider.statusMessage}", color = if (provider.isConfigured) TextSecondary else AccentAmber, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
                    Text("Config location: ${provider.configGuideLocation}", color = TextMuted, fontSize = 10.sp, modifier = Modifier.padding(top = 2.dp))
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(
                onClick = {
                    onLogout()
                    onNavigate(NavDestination.AUTH)
                },
                border = androidx.compose.foundation.BorderStroke(1.dp, AccentRose.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.Logout, contentDescription = null, tint = AccentRose)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Sign Out of Studio", color = AccentRose)
            }
        }
    }
}
