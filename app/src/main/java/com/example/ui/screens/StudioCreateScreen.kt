package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.*
import com.example.ui.navigation.NavDestination
import com.example.ui.theme.*
import java.util.UUID
import kotlin.math.max

@Composable
fun StudioCreateScreen(
    characters: List<CharacterEntity>,
    sceneTemplates: List<SceneTemplateEntity>,
    providerStatuses: Map<String, Boolean>,
    isAiConfigured: Boolean,
    onGenerateAnimation: (String, String, String, String, Int, String, String, String, String, List<SceneEntity>) -> Unit,
    onNavigate: (NavDestination) -> Unit
) {
    var projectName by remember { mutableStateOf("") }
    var prompt by remember { mutableStateOf("") }
    var mode by remember { mutableStateOf("Animation") }
    var style by remember { mutableStateOf("Cinematic") }
    var duration by remember { mutableStateOf(15) }
    var ratio by remember { mutableStateOf("9:16") }
    var camera by remember { mutableStateOf(CameraOption.CINEMATIC) }
    var movement by remember { mutableStateOf(MovementOption.NATURAL) }
    var voice by remember { mutableStateOf(VoiceOption.DEFAULT) }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Create Animation", color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(value = projectName, onValueChange = { projectName = it }, label = { Text("Project name") }, modifier = Modifier.fillMaxWidth(), singleLine = true, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonIndigo, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary), shape = RoundedCornerShape(12.dp))
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(value = prompt, onValueChange = { prompt = it }, label = { Text("Describe your scene") }, modifier = Modifier.fillMaxWidth(), minLines = 4, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonIndigo, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary), shape = RoundedCornerShape(12.dp))
        Spacer(Modifier.height(12.dp))
        Button(modifier = Modifier.fillMaxWidth().height(54.dp), onClick = {
            val safeName = projectName.ifBlank { "Untitled animation" }
            val safePrompt = prompt.ifBlank { "Create a cinematic animated scene with expressive character motion and a polished camera shot." }
            val projectId = "proj_${UUID.randomUUID().toString().take(8)}"
            val scenes = (1..3).map { i -> SceneEntity(id = UUID.randomUUID().toString(), projectId = projectId, sceneNumber = i, title = "$safeName - Scene $i", description = "Scene $i: $safePrompt", characters = "", location = "", dialogue = "", cameraDirection = camera.label, characterMovement = movement.label, durationSeconds = max(1, duration / 3), thumbnailResName = "scene_lagos_sunset", orderIndex = i - 1) }
            onGenerateAnimation(safeName, safePrompt, mode.lowercase(), style, duration, ratio, camera.label, movement.label, voice.name, scenes)
        }, shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = NeonIndigo)) {
            Icon(Icons.Default.AutoAwesome, null); Spacer(Modifier.width(8.dp)); Text("Generate", fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(8.dp))
        Text("DIV EDIT AI • Create, refine, and build your story", color = TextMuted, fontSize = 10.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
    }
}
