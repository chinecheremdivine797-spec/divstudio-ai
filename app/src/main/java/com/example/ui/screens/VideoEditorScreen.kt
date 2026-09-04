package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.entities.ProjectEntity
import com.example.data.local.entities.SceneEntity
import com.example.data.model.VoicePresets
import com.example.ui.components.GlassCard
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoEditorScreen(
    project: ProjectEntity?,
    scenes: List<SceneEntity>,
    onSaveProject: (ProjectEntity) -> Unit
) {
    var isPlaying by remember { mutableStateOf(false) }
    var currentPlayTimeSeconds by remember { mutableStateOf(0f) }
    val totalDurationSeconds = if (scenes.isNotEmpty()) scenes.sumOf { it.durationSeconds }.toFloat() else 18f
    var activeTab by remember { mutableStateOf(0) } // 0: Timeline, 1: Subtitles, 2: Voice & Lip Sync, 3: Export

    // Subtitle states
    var subtitlesText by remember {
        mutableStateOf(
            project?.subtitleText?.ifBlank {
                "[00:01] Look at that sunset over Lagos island.\n[00:06] One day, our animations will fly across the sky!\n[00:12] They already are, David."
            } ?: "[00:01] Welcome to DIV AI studio animation."
        )
    }
    var subtitleColor by remember { mutableStateOf(Color.White) }
    var subtitleSize by remember { mutableStateOf(16f) }

    // Voice states
    var selectedVoice by remember { mutableStateOf(VoicePresets.allVoices[0]) }
    var voiceSpeed by remember { mutableStateOf(1.0f) }
    var voicePitch by remember { mutableStateOf(1.0f) }

    // Export states
    var exportResolution by remember { mutableStateOf("1080p") }
    var exportFps by remember { mutableStateOf("60 FPS") }
    var exportFormat by remember { mutableStateOf("MP4 (H.264)") }
    var isExporting by remember { mutableStateOf(false) }
    var exportProgress by remember { mutableStateOf(0) }
    var exportCompletedNotice by remember { mutableStateOf<String?>(null) }

    // Playback ticker simulation
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            delay(100)
            currentPlayTimeSeconds += 0.1f
            if (currentPlayTimeSeconds >= totalDurationSeconds) {
                currentPlayTimeSeconds = 0f
                isPlaying = false
            }
        }
    }

    // Active scene determination
    var cumulative = 0f
    var activeSceneIndex = 0
    for ((idx, sc) in scenes.withIndex()) {
        cumulative += sc.durationSeconds
        if (currentPlayTimeSeconds <= cumulative) {
            activeSceneIndex = idx
            break
        }
    }
    val currentActiveScene = if (scenes.isNotEmpty()) scenes[activeSceneIndex.coerceIn(0, scenes.size - 1)] else null

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkCanvas)
            .testTag("video_editor_screen"),
        contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 14.dp, bottom = 100.dp)
    ) {
        // Video Preview Player Canvas
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.Black)
                    .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
                    .testTag("editor_video_player")
            ) {
                // Video frame display
                Image(
                    painter = painterResource(
                        if (currentActiveScene?.thumbnailResName == "character_david") R.drawable.character_david
                        else if (activeSceneIndex % 2 == 1) R.drawable.hero_animation_art
                        else R.drawable.scene_lagos_sunset
                    ),
                    contentDescription = "Playback Frame",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Subtitle Overlay
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 20.dp, start = 16.dp, end = 16.dp)
                ) {
                    if (currentActiveScene?.dialogue?.isNotBlank() == true) {
                        Surface(
                            color = Color.Black.copy(alpha = 0.75f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = currentActiveScene.dialogue,
                                color = subtitleColor,
                                fontSize = subtitleSize.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // Studio Watermark / Tag
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.65f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "${project?.name ?: "DIV Animation"} • Scene ${activeSceneIndex + 1}/${scenes.size.coerceAtLeast(1)}",
                            color = NeonCyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                // Lip Sync Active Tag
                if (isPlaying) {
                    Surface(
                        color = NeonViolet.copy(alpha = 0.8f),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "● Neural Lip Sync Live",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Player Scrub & Transport Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Timecode
                val currentMins = (currentPlayTimeSeconds / 60).toInt()
                val currentSecs = (currentPlayTimeSeconds % 60).toInt()
                val totalMins = (totalDurationSeconds / 60).toInt()
                val totalSecs = (totalDurationSeconds % 60).toInt()
                val timeString = String.format("%02d:%02d / %02d:%02d", currentMins, currentSecs, totalMins, totalSecs)

                Text(
                    text = timeString,
                    color = NeonCyanLight,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )

                // Buttons: Rewind, Play/Pause, Fast Forward
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { currentPlayTimeSeconds = (currentPlayTimeSeconds - 2f).coerceAtLeast(0f) }) {
                        Icon(Icons.Filled.Replay, contentDescription = "Rewind 2s", tint = TextSecondary)
                    }

                    IconButton(
                        onClick = { isPlaying = !isPlaying },
                        modifier = Modifier
                            .background(NeonIndigo, CircleShape)
                            .size(38.dp)
                            .testTag("editor_play_pause_btn")
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = Color.White
                        )
                    }

                    IconButton(onClick = { currentPlayTimeSeconds = (currentPlayTimeSeconds + 2f).coerceAtMost(totalDurationSeconds) }) {
                        Icon(Icons.Filled.Forward10, contentDescription = "Forward 2s", tint = TextSecondary)
                    }
                }

                Surface(
                    color = DarkSurfaceVariant,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("1080p 60fps", color = TextMuted, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
            }

            // Scrub slider
            Slider(
                value = currentPlayTimeSeconds,
                onValueChange = { currentPlayTimeSeconds = it },
                valueRange = 0f..totalDurationSeconds.coerceAtLeast(1f),
                colors = SliderDefaults.colors(
                    thumbColor = NeonCyan,
                    activeTrackColor = NeonIndigo,
                    inactiveTrackColor = DarkBorder
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("editor_scrub_slider")
            )
        }

        // Sub-Tabs: Timeline, Subtitles, Voice & Lip Sync, Export
        item {
            TabRow(
                selectedTabIndex = activeTab,
                containerColor = DarkSurface,
                contentColor = NeonCyan,
                divider = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
            ) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = { Text("Timeline", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    icon = { Icon(Icons.Filled.ViewTimeline, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = { Text("Subtitles", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    icon = { Icon(Icons.Filled.Subtitles, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
                Tab(
                    selected = activeTab == 2,
                    onClick = { activeTab = 2 },
                    text = { Text("Voice", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    icon = { Icon(Icons.Filled.RecordVoiceOver, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
                Tab(
                    selected = activeTab == 3,
                    onClick = { activeTab = 3 },
                    text = { Text("Export", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    icon = { Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // TAB 0: Multi-Track Timeline (Requirement 12)
        if (activeTab == 0) {
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Multi-Track Production Timeline", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Row {
                            TextButton(onClick = { /* Split */ }) { Text("Split", color = NeonCyan, fontSize = 11.sp) }
                            TextButton(onClick = { /* Trim */ }) { Text("Trim", color = NeonIndigoLight, fontSize = 11.sp) }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Track 1: Video Clips
                    Text("Track 1: Video Clips (${scenes.size} scenes)", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        itemsIndexed(scenes) { idx, scene ->
                            val isCurrent = idx == activeSceneIndex
                            Surface(
                                color = if (isCurrent) NeonIndigo.copy(alpha = 0.4f) else DarkSurfaceElevated,
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isCurrent) NeonCyan else DarkBorder),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier
                                    .width((scene.durationSeconds * 20).coerceIn(60, 160).dp)
                                    .height(50.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Filled.Movie, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Column {
                                        Text("S${scene.sceneNumber}: ${scene.title.take(10)}", color = TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                        Text("${scene.durationSeconds}s • ${scene.cameraDirection.take(6)}", color = TextMuted, fontSize = 9.sp)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Track 2: Dialogue & Voice Track
                    Text("Track 2: Character Dialogue & Lip Sync", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        color = NeonViolet.copy(alpha = 0.2f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NeonViolet.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(38.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.RecordVoiceOver, contentDescription = null, tint = NeonViolet, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Amaka & David (Nigerian English Neural Pitch - Synced)", color = Color.White, fontSize = 10.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Track 3: Sound Effects (SFX)
                    Text("Track 3: SFX (Ambient City, Footsteps, Whoosh)", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        color = AccentAmber.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AccentAmber.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(34.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.VolumeUp, contentDescription = null, tint = AccentAmber, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Lagos Waterfront Ambience + Footsteps", color = AccentAmber, fontSize = 10.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Track 4: Background Music (BGM)
                    Text("Track 4: Background Music (Afrobeats Cinematic Fusion)", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        color = AccentEmerald.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AccentEmerald.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(34.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.MusicNote, contentDescription = null, tint = AccentEmerald, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("DIV Original Score - Neo Africa Sunset Beat (Looped)", color = AccentEmerald, fontSize = 10.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Track 5: Subtitles Track
                    Text("Track 5: Subtitles / Captions (Synchronized SRT)", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        color = NeonCyan.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(34.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.Subtitles, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Auto-generated captions (English)", color = NeonCyanLight, fontSize = 10.sp)
                        }
                    }
                }
            }
        }

        // TAB 1: Subtitle Editor (Requirement 13)
        if (activeTab == 1) {
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text("Automatic Subtitles & Caption Styling", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = subtitlesText,
                        onValueChange = { subtitlesText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonIndigo,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Subtitle Text Size (${subtitleSize.toInt()} sp)", color = TextSecondary, fontSize = 12.sp)
                    Slider(
                        value = subtitleSize,
                        onValueChange = { subtitleSize = it },
                        valueRange = 12f..24f,
                        colors = SliderDefaults.colors(thumbColor = NeonCyan, activeTrackColor = NeonCyan)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                project?.let { onSaveProject(it.copy(subtitleText = subtitlesText)) }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonIndigo),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Save Subtitles", color = Color.White)
                        }

                        OutlinedButton(
                            onClick = { /* Export SRT */ },
                            border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Export .SRT", color = NeonCyan)
                        }
                    }
                }
            }
        }

        // TAB 2: Voice & Lip Sync Studio (Requirement 14)
        if (activeTab == 2) {
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text("Voice & Neural Lip Sync Rig", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("DIV AI animates lip and jaw keyframes matching the phonetic waveforms of selected voices.", color = TextMuted, fontSize = 11.sp, modifier = Modifier.padding(bottom = 10.dp))

                    VoicePresets.allVoices.forEach { voice ->
                        val isSel = selectedVoice.id == voice.id
                        Surface(
                            color = if (isSel) NeonIndigo.copy(alpha = 0.25f) else DarkSurface,
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) NeonCyan else DarkBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { selectedVoice = voice }
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isSel) Icons.Filled.CheckCircle else Icons.Filled.RecordVoiceOver,
                                    contentDescription = null,
                                    tint = if (isSel) NeonCyan else TextMuted
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("${voice.name} (${voice.gender})", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text("${voice.language.displayName} • Accent: ${voice.accent}", color = TextSecondary, fontSize = 10.sp)
                                }
                                TextButton(onClick = { /* preview voice */ }) {
                                    Text("Preview", color = NeonIndigoLight, fontSize = 11.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Speech Rate (${voiceSpeed}x)", color = TextSecondary, fontSize = 12.sp)
                    Slider(
                        value = voiceSpeed,
                        onValueChange = { voiceSpeed = it },
                        valueRange = 0.75f..1.5f,
                        colors = SliderDefaults.colors(thumbColor = NeonViolet, activeTrackColor = NeonViolet)
                    )
                }
            }
        }

        // TAB 3: Export System (Requirement 15)
        if (activeTab == 3) {
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text("Render & Export Animation", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    // Resolution Chips
                    Text("Export Resolution", color = TextSecondary, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("720p", "1080p", "4K Ultra").forEach { res ->
                            FilterChip(
                                selected = exportResolution == res,
                                onClick = { exportResolution = res },
                                label = { Text(res, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = NeonCyan, selectedLabelColor = Color.Black)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Frame rate Chips
                    Text("Framerate", color = TextSecondary, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("24 FPS (Cinematic)", "30 FPS", "60 FPS (Smooth)").forEach { fps ->
                            FilterChip(
                                selected = exportFps == fps,
                                onClick = { exportFps = fps },
                                label = { Text(fps, fontSize = 11.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Watermark status
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Verified, contentDescription = null, tint = AccentEmerald, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("No Watermark • Studio Pro Commercial License", color = AccentEmerald, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (isExporting) {
                        Column {
                            Text("Rendering Master Video: $exportProgress%", color = NeonCyanLight, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { exportProgress / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = NeonCyan
                            )
                        }
                    } else {
                        Button(
                            onClick = {
                                isExporting = true
                                exportProgress = 15
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonIndigo),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("export_video_btn")
                        ) {
                            Icon(Icons.Filled.Download, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Export MP4 Video", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Simulated export progress effect
                    LaunchedEffect(isExporting) {
                        if (isExporting) {
                            while (exportProgress < 100) {
                                delay(300)
                                exportProgress += 20
                            }
                            isExporting = false
                            exportCompletedNotice = "MP4 video ready for download!"
                        }
                    }

                    exportCompletedNotice?.let { notice ->
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            color = AccentEmerald.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AccentEmerald)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = AccentEmerald)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(notice, color = AccentEmerald, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text("Saved to Downloads • 1080p 60fps MP4", color = TextSecondary, fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
