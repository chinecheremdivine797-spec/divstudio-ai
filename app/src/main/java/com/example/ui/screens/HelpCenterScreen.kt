package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassCard
import com.example.ui.theme.*

@Composable
fun HelpCenterScreen() {
    val guides = listOf(
        "Getting Started with DIV AI Studio" to "Learn the foundational workflow: creating characters, scripting scenes, generating optical keyframes, and exporting high-definition MP4 animations.",
        "Text to Animation Guide" to "Write evocative scene descriptions with specific camera movements (Drone Orbit, Pan, Zoom) and character motions (Walk, Dance, Fight).",
        "Script to Animation & Storyboard Automation" to "DIV AI automatically partitions your multi-scene screenplay into dialogue cues, camera directions, and timeline durations.",
        "Character Studio & Consistent Rigs" to "Save persistent character profiles with customized clothing, hair, voice, and personality for serial animations.",
        "African Storytelling & Native Voices" to "DIV AI features dedicated voice presets for Nigerian English, Pidgin English, Igbo, Yoruba, and Hausa, plus Lagos sunset and Baobab backdrops.",
        "Multi-Track Timeline Editing & Lip Sync" to "Trim scenes, overlay sound effects and background music, and customize synchronized subtitles."
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkCanvas)
            .testTag("help_center_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp)
    ) {
        item {
            Text("Studio Help & Knowledge Base", color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text("Complete guides and reference documentation for DIV AI creators.", color = TextMuted, fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp, bottom = 14.dp))
        }

        items(guides.size) { idx ->
            val (title, body) = guides[idx]
            var expanded by remember { mutableStateOf(false) }

            Surface(
                color = DarkSurface,
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .clickable { expanded = !expanded }
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.weight(1f))
                        Icon(
                            imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                            contentDescription = null,
                            tint = NeonCyan
                        )
                    }
                    if (expanded) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(body, color = TextSecondary, fontSize = 12.sp, lineHeight = 18.sp)
                    }
                }
            }
        }
    }
}
