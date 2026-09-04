package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.entities.SceneTemplateEntity
import com.example.ui.components.GlassCard
import com.example.ui.navigation.NavDestination
import com.example.ui.theme.*
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SceneStudioScreen(
    customTemplates: List<SceneTemplateEntity>,
    onSaveTemplate: (SceneTemplateEntity) -> Unit,
    onDeleteTemplate: (SceneTemplateEntity) -> Unit,
    onUseEnvironment: (String) -> Unit,
    onNavigate: (NavDestination) -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }

    val presetEnvironments = listOf(
        Triple("Lagos sunset", "Golden hour over Lekki bridge and bustling city waterways", "Tropical Dusk"),
        Triple("African village", "Earth-brick compound surrounded by ancient baobabs & storytelling hearth", "Twilight Fire"),
        Triple("Futuristic city", "Neon-lined skyscrapers with flying solar transport rails", "Cyber Midnight"),
        Triple("School classroom", "Bright animated classroom with chalkboard and desks", "Morning Sunshine"),
        Triple("Hospital ward", "Clean sci-fi medical bay with holographic patient scanners", "Daylight Clean"),
        Triple("Tech Office", "Modern open creative studio with animation tablets and plants", "Afternoon Glow"),
        Triple("Marketplace", "Vibrant African market stalls bursting with Ankara fabrics & fruits", "Midday Vibrant"),
        Triple("Tropical Beach", "Golden sands with turquoise Atlantic waves crashing", "Sunset Radiance"),
        Triple("Enchanted Forest", "Mystical ancient woods with glowing bioluminescent vines", "Starry Night"),
        Triple("Fantasy world", "Floating crystal islands and aurora borealis skyways", "Celestial Dusk")
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkCanvas)
            .testTag("scene_studio_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Scene Studio",
                        color = TextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Select from pre-made worlds or design custom environmental backdrops.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                Button(
                    onClick = { showCreateDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonIndigo),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("create_scene_backdrop_btn")
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Backdrop", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Pre-made Environments
        item {
            Text(
                text = "Studio Environment Presets",
                color = TextPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(presetEnvironments) { (envName, desc, time) ->
            Surface(
                color = DarkSurface,
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(8.dp))
                    ) {
                        Image(
                            painter = painterResource(
                                if (envName.contains("Lagos") || envName.contains("Market")) R.drawable.scene_lagos_sunset
                                else R.drawable.hero_animation_art
                            ),
                            contentDescription = envName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(envName, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(color = NeonCyan.copy(alpha = 0.15f), shape = RoundedCornerShape(4.dp)) {
                                Text(time, color = NeonCyanLight, fontSize = 9.sp, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                            }
                        }
                        Text(desc, color = TextSecondary, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
                    }
                    TextButton(onClick = {
                        onUseEnvironment(envName)
                        onNavigate(NavDestination.CREATE)
                    }) {
                        Text("Use", color = NeonIndigoLight, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Custom Templates Section
        item {
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "My Custom Environments (${customTemplates.size})",
                color = TextPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(customTemplates) { temp ->
            Surface(
                color = DarkSurfaceVariant,
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(temp.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        TextButton(onClick = { onDeleteTemplate(temp) }) {
                            Text("Delete", color = AccentRose, fontSize = 11.sp)
                        }
                    }
                    Text("${temp.location} • ${temp.timeOfDay} • ${temp.weather}", color = NeonCyan, fontSize = 11.sp)
                    Text(temp.description, color = TextSecondary, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                }
            }
        }
    }

    // Create Backdrop Dialog
    if (showCreateDialog) {
        var name by remember { mutableStateOf("") }
        var location by remember { mutableStateOf("Lagos, Nigeria") }
        var timeOfDay by remember { mutableStateOf("Sunset") }
        var weather by remember { mutableStateOf("Warm Clear") }
        var desc by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            containerColor = DarkSurfaceElevated,
            title = { Text("Create Custom Backdrop", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Environment Name", color = TextSecondary) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("Location", color = TextSecondary) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = timeOfDay,
                            onValueChange = { timeOfDay = it },
                            label = { Text("Time of Day", color = TextSecondary) },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = weather,
                            onValueChange = { weather = it },
                            label = { Text("Weather", color = TextSecondary) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = desc,
                        onValueChange = { desc = it },
                        label = { Text("Visual Description", color = TextSecondary) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSaveTemplate(
                            SceneTemplateEntity(
                                id = "scene_temp_${UUID.randomUUID().toString().take(8)}",
                                userId = "user_default_01",
                                name = name.ifBlank { "Custom Studio World" },
                                location = location,
                                timeOfDay = timeOfDay,
                                weather = weather,
                                description = desc.ifBlank { "High-detail animation backdrop." },
                                artStyle = "African Animation",
                                previewResName = "scene_lagos_sunset"
                            )
                        )
                        showCreateDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonIndigo)
                ) {
                    Text("Save Backdrop", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}
