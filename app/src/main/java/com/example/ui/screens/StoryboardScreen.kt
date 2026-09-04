package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.example.data.local.entities.ProjectEntity
import com.example.data.local.entities.SceneEntity
import com.example.ui.components.GlassCard
import com.example.ui.navigation.NavDestination
import com.example.ui.theme.*
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoryboardScreen(
    currentProject: ProjectEntity?,
    scenes: List<SceneEntity>,
    onAddScene: (SceneEntity) -> Unit,
    onUpdateScene: (SceneEntity) -> Unit,
    onDeleteScene: (SceneEntity) -> Unit,
    onReorderScenes: (List<SceneEntity>) -> Unit,
    onNavigate: (NavDestination) -> Unit
) {
    var editingScene by remember { mutableStateOf<SceneEntity?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkCanvas)
            .testTag("storyboard_screen"),
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
                        text = "Storyboard Studio",
                        color = TextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = currentProject?.name ?: "Active Animation Project",
                        color = NeonCyan,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                Button(
                    onClick = { showAddDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonIndigo),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("storyboard_add_scene_btn")
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Scene", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action bar: Generate full video, preview in editor
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onNavigate(NavDestination.EDITOR) },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonViolet),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.MovieCreation, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Open Video Editor", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { onNavigate(NavDestination.DASHBOARD) },
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.Visibility, contentDescription = null, tint = TextSecondary)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Preview Full", color = TextSecondary, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        if (scenes.isEmpty()) {
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = DarkSurface
                ) {
                    Text("No scenes in this storyboard. Click 'Add Scene' to start.", color = TextMuted)
                }
            }
        } else {
            itemsIndexed(scenes) { index, scene ->
                Surface(
                    color = DarkSurface,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .testTag("storyboard_scene_card_$index")
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = NeonIndigo.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "Scene ${scene.sceneNumber}",
                                    color = NeonIndigoLight,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }

                            // Reorder controls
                            Row {
                                IconButton(
                                    onClick = {
                                        if (index > 0) {
                                            val reordered = scenes.toMutableList()
                                            val item = reordered.removeAt(index)
                                            reordered.add(index - 1, item)
                                            onReorderScenes(reordered)
                                        }
                                    },
                                    enabled = index > 0,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Filled.ArrowUpward, contentDescription = "Move Up", tint = if (index > 0) TextSecondary else DarkBorder)
                                }
                                IconButton(
                                    onClick = {
                                        if (index < scenes.size - 1) {
                                            val reordered = scenes.toMutableList()
                                            val item = reordered.removeAt(index)
                                            reordered.add(index + 1, item)
                                            onReorderScenes(reordered)
                                        }
                                    },
                                    enabled = index < scenes.size - 1,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Filled.ArrowDownward, contentDescription = "Move Down", tint = if (index < scenes.size - 1) TextSecondary else DarkBorder)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            // Thumbnail
                            Box(
                                modifier = Modifier
                                    .size(width = 110.dp, height = 75.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            ) {
                                Image(
                                    painter = painterResource(
                                        if (scene.thumbnailResName == "scene_lagos_sunset") R.drawable.scene_lagos_sunset
                                        else R.drawable.hero_animation_art
                                    ),
                                    contentDescription = scene.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                Surface(
                                    color = Color.Black.copy(alpha = 0.7f),
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(4.dp)
                                ) {
                                    Text("${scene.durationSeconds}s", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(scene.title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(scene.description, color = TextSecondary, fontSize = 11.sp, maxLines = 2, modifier = Modifier.padding(top = 2.dp))
                                Text("Characters: ${scene.characters}", color = NeonCyan, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
                                if (scene.dialogue.isNotBlank()) {
                                    Text("\"${scene.dialogue}\"", color = AccentAmber, fontSize = 11.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Divider(color = DarkBorder, thickness = 0.5.dp)
                        Spacer(modifier = Modifier.height(4.dp))

                        // Action Buttons: Edit, Duplicate, Delete, Regenerate
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Camera: ${scene.cameraDirection} • Motion: ${scene.characterMovement}",
                                color = TextMuted,
                                fontSize = 10.sp
                            )

                            Row {
                                TextButton(onClick = { editingScene = scene }) {
                                    Text("Edit", color = NeonIndigoLight, fontSize = 11.sp)
                                }
                                TextButton(onClick = {
                                    val copy = scene.copy(
                                        id = UUID.randomUUID().toString(),
                                        title = "${scene.title} (Copy)",
                                        sceneNumber = scenes.size + 1,
                                        orderIndex = scenes.size
                                    )
                                    onAddScene(copy)
                                }) {
                                    Text("Duplicate", color = NeonCyan, fontSize = 11.sp)
                                }
                                TextButton(onClick = { onDeleteScene(scene) }) {
                                    Text("Delete", color = AccentRose, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Edit Scene Dialog
    editingScene?.let { scene ->
        var editTitle by remember { mutableStateOf(scene.title) }
        var editDesc by remember { mutableStateOf(scene.description) }
        var editDialogue by remember { mutableStateOf(scene.dialogue) }
        var editDuration by remember { mutableStateOf(scene.durationSeconds.toString()) }

        AlertDialog(
            onDismissRequest = { editingScene = null },
            containerColor = DarkSurfaceElevated,
            title = { Text("Edit Scene ${scene.sceneNumber}", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = editTitle,
                        onValueChange = { editTitle = it },
                        label = { Text("Scene Title", color = TextSecondary) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editDesc,
                        onValueChange = { editDesc = it },
                        label = { Text("Description", color = TextSecondary) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editDialogue,
                        onValueChange = { editDialogue = it },
                        label = { Text("Dialogue", color = TextSecondary) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editDuration,
                        onValueChange = { editDuration = it },
                        label = { Text("Duration (seconds)", color = TextSecondary) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val dur = editDuration.toIntOrNull() ?: scene.durationSeconds
                        onUpdateScene(scene.copy(title = editTitle, description = editDesc, dialogue = editDialogue, durationSeconds = dur))
                        editingScene = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonIndigo)
                ) {
                    Text("Save Changes", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { editingScene = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    // Add Scene Dialog
    if (showAddDialog) {
        var newTitle by remember { mutableStateOf("New Scene") }
        var newDesc by remember { mutableStateOf("David continues through the animated landscape.") }
        var newDialogue by remember { mutableStateOf("Let's keep moving forward.") }
        var newDuration by remember { mutableStateOf("5") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            containerColor = DarkSurfaceElevated,
            title = { Text("Add New Scene", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        label = { Text("Scene Title", color = TextSecondary) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newDesc,
                        onValueChange = { newDesc = it },
                        label = { Text("Description", color = TextSecondary) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newDialogue,
                        onValueChange = { newDialogue = it },
                        label = { Text("Dialogue", color = TextSecondary) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newDuration,
                        onValueChange = { newDuration = it },
                        label = { Text("Duration (sec)", color = TextSecondary) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val dur = newDuration.toIntOrNull() ?: 5
                        val newScene = SceneEntity(
                            id = UUID.randomUUID().toString(),
                            projectId = currentProject?.id ?: "proj_demo_01",
                            sceneNumber = scenes.size + 1,
                            title = newTitle,
                            description = newDesc,
                            characters = "David",
                            location = "Lagos",
                            dialogue = newDialogue,
                            cameraDirection = "Zoom In",
                            characterMovement = "Walk Forward",
                            durationSeconds = dur,
                            thumbnailResName = "scene_lagos_sunset",
                            orderIndex = scenes.size
                        )
                        onAddScene(newScene)
                        showAddDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonIndigo)
                ) {
                    Text("Add Scene", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}
