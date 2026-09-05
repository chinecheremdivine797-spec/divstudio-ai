package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.CharacterEntity
import com.example.data.local.entities.SceneEntity
import com.example.data.local.entities.SceneTemplateEntity
import com.example.data.model.*
import com.example.data.model.VoicePresets
import com.example.data.repository.ProviderStatus
import com.example.ui.components.GlassCard
import com.example.ui.navigation.NavDestination
import com.example.ui.theme.*
import java.util.UUID

@Composable
fun StudioCreateScreen(
    characters: List<CharacterEntity>,
    sceneTemplates: List<SceneTemplateEntity>,
    providerStatuses: List<ProviderStatus>,
    isAiConfigured: Boolean,
    onGenerateAnimation: (
        String, String, String, String, Int, String, String, String, String, List<SceneEntity>
    ) -> Unit,
    onNavigate: (NavDestination) -> Unit
) {
    var mode by remember { mutableStateOf("Text") }
    var prompt by remember { mutableStateOf("") }
    var projectName by remember { mutableStateOf("Untitled animation") }
    var model by remember { mutableStateOf("Auto") }
    var style by remember { mutableStateOf(AnimationStyle.AFRICAN_ANIMATION) }
    var ratio by remember { mutableStateOf(AspectRatioOption.RATIO_16_9) }
    var duration by remember { mutableStateOf(8) }
    var camera by remember { mutableStateOf(CameraMovement.ZOOM_IN) }
    var movement by remember { mutableStateOf(CharacterMovement.WALK) }
    var voice by remember { mutableStateOf(VoicePresets.allVoices[0]) }
    var showAdvanced by remember { mutableStateOf(false) }

    val models = listOf("Auto", "Veo 3.1 Fast", "Seedance 2.5")
    val modes = listOf("Text", "Image", "Script", "Character", "Scene")

    Column(
        modifier = Modifier.fillMaxSize().background(DarkCanvas).padding(horizontal = 18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 18.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Create", color = TextPrimary, fontSize = 27.sp, fontWeight = FontWeight.Bold)
                Text("Turn an idea into an animated scene", color = TextSecondary, fontSize = 13.sp)
            }
            IconButton(onClick = { onNavigate(NavDestination.PROJECTS) }) {
                Icon(Icons.Default.FolderOpen, contentDescription = "Projects", tint = TextSecondary)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            modes.forEach { item ->
                FilterChip(
                    selected = mode == item,
                    onClick = { mode = item },
                    label = { Text(item, fontSize = 12.sp) },
                    leadingIcon = if (mode == item) ({ Icon(Icons.Default.Check, null, Modifier.size(15.dp)) }) else null,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NeonIndigo,
                        selectedLabelColor = Color.White,
                        selectedLeadingIconColor = Color.White
                    )
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Model", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.width(8.dp))
                var expanded by remember { mutableStateOf(false) }
                Box {
                    AssistChip(onClick = { expanded = true }, label = { Text(model, fontSize = 12.sp) }, trailingIcon = { Icon(Icons.Default.ExpandMore, null) })
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        models.forEach { m -> DropdownMenuItem(text = { Text(m) }, onClick = { model = m; expanded = false }) }
                    }
                }
                Spacer(Modifier.weight(1f))
                if (isAiConfigured || providerStatuses.isNotEmpty()) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = "AI ready", tint = NeonCyan, modifier = Modifier.size(18.dp))
                }
            }

            Spacer(Modifier.height(14.dp))
            Text("What do you want to create?", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(7.dp))
            OutlinedTextField(
                value = prompt,
                onValueChange = { prompt = it },
                placeholder = { Text("Describe the scene, characters, action and camera...", color = TextMuted) },
                modifier = Modifier.fillMaxWidth().height(150.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonIndigo,
                    unfocusedBorderColor = DarkBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = NeonCyan
                ),
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                AssistChip(onClick = { }, label = { Text("+ Add image", fontSize = 11.sp) }, leadingIcon = { Icon(Icons.Default.AddPhotoAlternate, null, Modifier.size(16.dp)) })
                AssistChip(onClick = { }, label = { Text("Characters", fontSize = 11.sp) }, leadingIcon = { Icon(Icons.Default.Face, null, Modifier.size(16.dp)) })
                AssistChip(onClick = { showAdvanced = !showAdvanced }, label = { Text("Advanced", fontSize = 11.sp) }, leadingIcon = { Icon(Icons.Default.Tune, null, Modifier.size(16.dp)) })
            }
        }

        if (showAdvanced) {
            Spacer(Modifier.height(10.dp))
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text("Creative controls", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(10.dp))
                Text("Style", color = TextSecondary, fontSize = 11.sp)
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    AnimationStyle.values().forEach { s ->
                        FilterChip(selected = style == s, onClick = { style = s }, label = { Text(s.displayName, fontSize = 10.sp) })
                    }
                }
                Spacer(Modifier.height(10.dp))
                Text("Duration", color = TextSecondary, fontSize = 11.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(5, 8, 10, 15, 30).forEach { d -> FilterChip(selected = duration == d, onClick = { duration = d }, label = { Text("${d}s", fontSize = 10.sp) }) }
                }
                Spacer(Modifier.height(10.dp))
                Text("Aspect ratio", color = TextSecondary, fontSize = 11.sp)
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    AspectRatioOption.values().forEach { r -> FilterChip(selected = ratio == r, onClick = { ratio = r }, label = { Text(r.label.substringBefore(" "), fontSize = 10.sp) }) }
                }
                Spacer(Modifier.height(10.dp))
                Text("Camera", color = TextSecondary, fontSize = 11.sp)
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    CameraMovement.values().forEach { c -> FilterChip(selected = camera == c, onClick = { camera = c }, label = { Text(c.label, fontSize = 10.sp) }) }
                }
                Spacer(Modifier.height(10.dp))
                Text("Character motion", color = TextSecondary, fontSize = 11.sp)
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    CharacterMovement.values().forEach { m -> FilterChip(selected = movement == m, onClick = { movement = m }, label = { Text(m.label, fontSize = 10.sp) }) }
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = projectName,
            onValueChange = { projectName = it },
            label = { Text("Project name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonIndigo, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(Modifier.height(12.dp))
        Button(
            onClick = {
                val safeName = projectName.ifBlank { "Untitled animation" }
                val safePrompt = prompt.ifBlank { "Create a cinematic animated scene with expressive character motion and a polished camera shot." }
                val projectId = "proj_${UUID.randomUUID().toString().take(8)}"
                val scenes = (1..3).map { i ->
                    SceneEntity(
                        id = UUID.randomUUID().toString(), projectId = projectId, sceneNumber = i,
                        title = "$safeName - Scene $i", description = "Scene $i: $safePrompt", characters = "",
                        location = "", dialogue = "", cameraDirection = camera.label,
                        characterMovement = movement.label, durationSeconds = maxOf(1, duration / 3),
                        thumbnailResName = "scene_lagos_sunset", orderIndex = i - 1
                    )
                }
                onGenerateAnimation(safeName, safePrompt, mode.lowercase(), style.displayName, duration,
                    ratio.label.substringBefore(" "), camera.label, movement.label, voice.name,
                    voice.language.displayName, scenes)
            },
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = NeonIndigo)
        ) {
            Icon(Icons.Default.AutoAwesome, null)
            Spacer(Modifier.width(8.dp))
            Text("Generate", fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(8.dp))
        Text("DIVSTUDIO AI • Create, refine, and build your story", color = TextMuted, fontSize = 10.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
    }
}
