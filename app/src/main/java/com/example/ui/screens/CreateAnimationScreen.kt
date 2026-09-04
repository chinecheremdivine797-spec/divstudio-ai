package com.example.ui.screens

import androidx.compose.foundation.*
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.entities.CharacterEntity
import com.example.data.local.entities.SceneEntity
import com.example.data.local.entities.SceneTemplateEntity
import com.example.data.model.*
import com.example.data.repository.ProviderStatus
import com.example.ui.components.GlassCard
import com.example.ui.navigation.NavDestination
import com.example.ui.theme.*
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAnimationScreen(
    characters: List<CharacterEntity>,
    sceneTemplates: List<SceneTemplateEntity>,
    providerStatuses: List<ProviderStatus>,
    isAiConfigured: Boolean,
    onGenerateAnimation: (
        projectName: String,
        prompt: String,
        mode: String,
        style: String,
        duration: Int,
        ratio: String,
        camera: String,
        charMove: String,
        voice: String,
        language: String,
        scenes: List<SceneEntity>
    ) -> Unit,
    onNavigate: (NavDestination) -> Unit
) {
    var selectedModeTab by remember { mutableStateOf(0) } // 0: Text, 1: Image, 2: Script, 3: Character, 4: Scene

    // Form states
    var projectName by remember { mutableStateOf("New Studio Animation") }
    var promptText by remember {
        mutableStateOf("A young Nigerian boy walks through Lagos at sunset while talking to his friend about his creative dreams.")
    }
    var selectedStyle by remember { mutableStateOf(AnimationStyle.AFRICAN_ANIMATION) }
    var selectedRatio by remember { mutableStateOf(AspectRatioOption.RATIO_16_9) }
    var selectedDuration by remember { mutableStateOf(15) } // seconds
    var selectedCamera by remember { mutableStateOf(CameraMovement.ZOOM_IN) }
    var selectedCharMove by remember { mutableStateOf(CharacterMovement.WALK) }
    var selectedVoice by remember { mutableStateOf(VoicePresets.allVoices[0]) }
    var selectedResolution by remember { mutableStateOf("1080p") }
    var motionStrength by remember { mutableStateOf(7f) }

    // Script to Animation states
    var rawScript by remember {
        mutableStateOf(
            "Scene 1: David walks forward through the bustling streets of Lagos at sunset.\nDavid: Look at how bright our city shines today!\n\nScene 2: David meets an old wise craftsman near the Lekki Lagoon.\nCraftsman: True stories are built with heart and vision, young man."
        )
    }
    var parsedScenes by remember { mutableStateOf<List<SceneEntity>>(emptyList()) }
    var isAnalyzingScript by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkCanvas)
            .testTag("create_animation_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp)
    ) {
        // Header
        item {
            Text(
                text = "Animation Studio Workspace",
                color = TextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Turn prompts, scripts, images & character rigs into cinema animations.",
                color = TextSecondary,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
            )

            // AI Provider Banner Notice
            if (!isAiConfigured) {
                Surface(
                    color = AccentAmber.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AccentAmber.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Info, contentDescription = null, tint = AccentAmber, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "AI generation provider not configured",
                                color = AccentAmber,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "Set GEMINI_API_KEY in Settings or Secrets. Built-in studio synthesis engine is active for rapid storyboard & rig testing.",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        // Mode Selector (5 Modes)
        item {
            ScrollableTabRow(
                selectedTabIndex = selectedModeTab,
                containerColor = DarkSurface,
                contentColor = NeonCyan,
                edgePadding = 0.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
            ) {
                val modes = listOf("Text → Animation", "Image → Animation", "Script → Animation", "Character Rig", "Scene Backdrop")
                modes.forEachIndexed { index, mode ->
                    Tab(
                        selected = selectedModeTab == index,
                        onClick = { selectedModeTab = index },
                        text = {
                            Text(
                                text = mode,
                                fontSize = 12.sp,
                                fontWeight = if (selectedModeTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        modifier = Modifier.testTag("mode_tab_$index")
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // MODE 0: Text to Animation
        if (selectedModeTab == 0) {
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text("Project Title", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = projectName,
                        onValueChange = { projectName = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("text_anim_title_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonIndigo,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text("Animation Description Prompt", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = promptText,
                        onValueChange = { promptText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .testTag("text_anim_prompt_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonIndigo,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )

                    // Prompt Presets
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Prompt Presets:", color = TextMuted, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val presets = listOf(
                            "Lagos Sunset Friend Walk",
                            "Anansi & the Golden Stool",
                            "Cyberpunk Nairobi Courier",
                            "Enugu Baobab Spirits",
                            "Afrobeats High School Dance"
                        )
                        presets.forEach { preset ->
                            SuggestionChip(
                                onClick = {
                                    promptText = when (preset) {
                                        "Lagos Sunset Friend Walk" -> "A young Nigerian boy walks through Lagos at sunset while talking to his friend about flying cars."
                                        "Anansi & the Golden Stool" -> "The mythical spider Anansi weaves a glowing gold web in an ancient bioluminescent African rainforest."
                                        "Cyberpunk Nairobi Courier" -> "A futuristic animated courier darts across skyscraper skybridges on a solar hoverboard in Neo-Nairobi."
                                        "Enugu Baobab Spirits" -> "Glowing ancestral spirits rise from the roots of an ancient Baobab tree under starry African skies."
                                        else -> "Two African high school students prepare their robotic dance choreography for the national festival."
                                    }
                                    projectName = preset
                                },
                                label = { Text(preset, fontSize = 11.sp, color = NeonCyanLight) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Animation Style Dropdown
                    Text("Animation Style", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        AnimationStyle.values().forEach { style ->
                            FilterChip(
                                selected = selectedStyle == style,
                                onClick = { selectedStyle = style },
                                label = { Text(style.displayName, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = NeonIndigo,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Aspect Ratio
                    Text("Aspect Ratio", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        AspectRatioOption.values().forEach { ratio ->
                            FilterChip(
                                selected = selectedRatio == ratio,
                                onClick = { selectedRatio = ratio },
                                label = { Text(ratio.label.substringBefore(" "), fontSize = 11.sp) },
                                modifier = Modifier.weight(1f),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = NeonCyan,
                                    selectedLabelColor = Color.Black
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Duration & Resolution
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Duration", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                listOf(5, 10, 15, 30).forEach { dur ->
                                    FilterChip(
                                        selected = selectedDuration == dur,
                                        onClick = { selectedDuration = dur },
                                        label = { Text("${dur}s", fontSize = 11.sp) },
                                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = NeonViolet)
                                    )
                                }
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text("Resolution", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                listOf("720p", "1080p", "4K").forEach { res ->
                                    FilterChip(
                                        selected = selectedResolution == res,
                                        onClick = { selectedResolution = res },
                                        label = { Text(res, fontSize = 11.sp) },
                                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = AccentAmber)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Camera Movement & Character Motion
                    Text("Camera Movement", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CameraMovement.values().forEach { cam ->
                            FilterChip(
                                selected = selectedCamera == cam,
                                onClick = { selectedCamera = cam },
                                label = { Text(cam.label, fontSize = 11.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Character Movement", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CharacterMovement.values().forEach { mov ->
                            FilterChip(
                                selected = selectedCharMove == mov,
                                onClick = { selectedCharMove = mov },
                                label = { Text(mov.label, fontSize = 11.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Voice & Language Selection
                    Text("Voice & Dialogue Rig", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        VoicePresets.allVoices.forEach { voice ->
                            FilterChip(
                                selected = selectedVoice == voice,
                                onClick = { selectedVoice = voice },
                                label = { Text("${voice.name} (${voice.language.displayName.substringBefore(" ")})", fontSize = 11.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Generate Button
                    Button(
                        onClick = {
                            val projectId = "proj_${UUID.randomUUID().toString().take(8)}"
                            val sceneCount = 3
                            val scenes = (1..sceneCount).map { i ->
                                SceneEntity(
                                    id = UUID.randomUUID().toString(),
                                    projectId = projectId,
                                    sceneNumber = i,
                                    title = "$projectName - Scene $i",
                                    description = "Scene $i: $promptText",
                                    characters = "David",
                                    location = "Lagos",
                                    dialogue = "Look at this world we are building.",
                                    cameraDirection = selectedCamera.label,
                                    characterMovement = selectedCharMove.label,
                                    durationSeconds = selectedDuration / sceneCount,
                                    thumbnailResName = if (i % 2 == 1) "scene_lagos_sunset" else "hero_animation_art",
                                    orderIndex = i - 1
                                )
                            }
                            onGenerateAnimation(
                                projectName,
                                promptText,
                                "text",
                                selectedStyle.displayName,
                                selectedDuration,
                                selectedRatio.label.substringBefore(" "),
                                selectedCamera.label,
                                selectedCharMove.label,
                                selectedVoice.name,
                                selectedVoice.language.displayName,
                                scenes
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonIndigo),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("generate_animation_btn")
                    ) {
                        Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Generate Animation", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }
        }

        // MODE 1: Image to Animation
        if (selectedModeTab == 1) {
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text("Source Image", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .border(1.dp, DarkBorder, RoundedCornerShape(10.dp))
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.character_david),
                            contentDescription = "Source Image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Surface(
                            color = Color.Black.copy(alpha = 0.7f),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(8.dp)
                        ) {
                            Text("JPG • PNG • WEBP Ready", color = NeonCyan, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Describe How The Image Should Move", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = promptText,
                        onValueChange = { promptText = it },
                        placeholder = { Text("Make the character walk forward, wave his hand and smile.") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonIndigo,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Motion Strength (${motionStrength.toInt()} / 10)", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Slider(
                        value = motionStrength,
                        onValueChange = { motionStrength = it },
                        valueRange = 1f..10f,
                        steps = 8,
                        colors = SliderDefaults.colors(thumbColor = NeonCyan, activeTrackColor = NeonCyan)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            val projectId = "proj_${UUID.randomUUID().toString().take(8)}"
                            val scene = SceneEntity(
                                id = UUID.randomUUID().toString(),
                                projectId = projectId,
                                sceneNumber = 1,
                                title = "Image Animation Scene",
                                description = promptText,
                                characters = "Source Character",
                                location = "Portrait Studio",
                                dialogue = "",
                                cameraDirection = selectedCamera.label,
                                characterMovement = "Wave Hand",
                                durationSeconds = 10,
                                thumbnailResName = "character_david",
                                orderIndex = 0
                            )
                            onGenerateAnimation(
                                "Image Motion Animation",
                                promptText,
                                "image",
                                selectedStyle.displayName,
                                10,
                                "16:9",
                                selectedCamera.label,
                                "Wave Hand",
                                selectedVoice.name,
                                selectedVoice.language.displayName,
                                listOf(scene)
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("animate_image_btn")
                    ) {
                        Text("Animate Image", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // MODE 2: Script to Animation (Section 8)
        if (selectedModeTab == 2) {
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text("Paste Your Story or Screenplay", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Text(
                        "DIV AI automatically analyzes dialogue, actions, camera angles, and durations into distinct scenes.",
                        color = TextMuted,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    OutlinedTextField(
                        value = rawScript,
                        onValueChange = { rawScript = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .testTag("script_input_field"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonIndigo,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                isAnalyzingScript = true
                                // Simulated parsing
                                parsedScenes = listOf(
                                    SceneEntity(
                                        id = UUID.randomUUID().toString(),
                                        projectId = "pending",
                                        sceneNumber = 1,
                                        title = "Scene 1: Lagos Sunset Walk",
                                        description = "David walks forward through the bustling streets of Lagos at sunset.",
                                        characters = "David",
                                        location = "Lagos Marina",
                                        dialogue = "Look at how bright our city shines today!",
                                        cameraDirection = "Smooth Zoom In",
                                        characterMovement = "Walk Forward",
                                        durationSeconds = 6,
                                        thumbnailResName = "scene_lagos_sunset",
                                        orderIndex = 0
                                    ),
                                    SceneEntity(
                                        id = UUID.randomUUID().toString(),
                                        projectId = "pending",
                                        sceneNumber = 2,
                                        title = "Scene 2: Meeting the Craftsman",
                                        description = "David meets an old wise craftsman near the Lekki Lagoon.",
                                        characters = "David, Craftsman",
                                        location = "Lekki Lagoon Crafts Market",
                                        dialogue = "True stories are built with heart and vision, young man.",
                                        cameraDirection = "Drone Orbit",
                                        characterMovement = "Talk & Explain",
                                        durationSeconds = 8,
                                        thumbnailResName = "hero_animation_art",
                                        orderIndex = 1
                                    )
                                )
                                isAnalyzingScript = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonViolet),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("generate_storyboard_btn")
                        ) {
                            Icon(Icons.Filled.AutoStories, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Generate Storyboard", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        if (parsedScenes.isNotEmpty()) {
                            Button(
                                onClick = {
                                    val projId = "proj_${UUID.randomUUID().toString().take(8)}"
                                    val linkedScenes = parsedScenes.map { it.copy(projectId = projId) }
                                    onGenerateAnimation(
                                        "Script: Lagos Awakening",
                                        rawScript,
                                        "script",
                                        selectedStyle.displayName,
                                        linkedScenes.sumOf { it.durationSeconds },
                                        "16:9",
                                        "Zoom In",
                                        "Walk Forward",
                                        selectedVoice.name,
                                        selectedVoice.language.displayName,
                                        linkedScenes
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonIndigo),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("generate_script_anim_btn")
                            ) {
                                Icon(Icons.Filled.MovieCreation, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Generate Animation", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // Parsed Scene Cards
            if (parsedScenes.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Divided Storyboard Scenes (${parsedScenes.size})",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }

                items(parsedScenes) { scene ->
                    Surface(
                        color = DarkSurface,
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Scene ${scene.sceneNumber}: ${scene.title}",
                                    color = NeonCyan,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text("${scene.durationSeconds}s", color = TextMuted, fontSize = 11.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "Characters: ${scene.characters} • Location: ${scene.location}", color = TextSecondary, fontSize = 11.sp)
                            Text(text = "Dialogue: \"${scene.dialogue}\"", color = TextPrimary, fontSize = 12.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "Camera: ${scene.cameraDirection} • Motion: ${scene.characterMovement}", color = NeonIndigoLight, fontSize = 10.sp)
                        }
                    }
                }
            }
        }

        // MODE 3: Character Animation Shortcut
        if (selectedModeTab == 3) {
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text("Select Character Rig to Animate", color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(10.dp))
                    characters.forEach { char ->
                        Surface(
                            color = DarkSurface,
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    promptText = "Animate ${char.name} performing active dialogue in ${char.animationStyle} style."
                                    selectedModeTab = 0
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.character_david),
                                        contentDescription = char.name,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(char.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("${char.clothing} • Voice: ${char.voice}", color = TextSecondary, fontSize = 11.sp)
                                }
                                TextButton(onClick = {
                                    promptText = "Animate ${char.name} walking through Lagos."
                                    selectedModeTab = 0
                                }) {
                                    Text("Animate", color = NeonCyan, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // MODE 4: Scene Backdrop Shortcut
        if (selectedModeTab == 4) {
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text("Select Environment Backdrop to Animate", color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(10.dp))
                    sceneTemplates.forEach { temp ->
                        Surface(
                            color = DarkSurface,
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.scene_lagos_sunset),
                                        contentDescription = temp.name,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(temp.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("${temp.location} • ${temp.timeOfDay}", color = TextSecondary, fontSize = 11.sp)
                                }
                                TextButton(onClick = {
                                    promptText = "Render scenic cinematic animation in ${temp.name} with camera pan."
                                    selectedModeTab = 0
                                }) {
                                    Text("Use", color = NeonCyan, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
