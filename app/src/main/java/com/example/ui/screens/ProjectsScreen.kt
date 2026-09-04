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
import com.example.data.local.entities.ProjectEntity
import com.example.ui.components.GlassCard
import com.example.ui.components.StatusBadge
import com.example.ui.navigation.NavDestination
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectsScreen(
    projects: List<ProjectEntity>,
    onSelectProject: (String) -> Unit,
    onDuplicateProject: (ProjectEntity) -> Unit,
    onRenameProject: (ProjectEntity, String) -> Unit,
    onDeleteProject: (String) -> Unit,
    onNavigate: (NavDestination) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }
    var projectToRename by remember { mutableStateOf<ProjectEntity?>(null) }
    var shareProjectLink by remember { mutableStateOf<String?>(null) }

    val filteredProjects = projects.filter { proj ->
        val matchesSearch = proj.name.contains(searchQuery, ignoreCase = true) ||
                proj.description.contains(searchQuery, ignoreCase = true) ||
                proj.style.contains(searchQuery, ignoreCase = true)

        val matchesFilter = when (selectedFilter) {
            "Drafts" -> proj.status == "draft"
            "Generating" -> proj.status in listOf("queued", "processing", "rendering")
            "Completed" -> proj.status == "completed"
            "Failed" -> proj.status == "failed"
            else -> true
        }
        matchesSearch && matchesFilter
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkCanvas)
            .testTag("projects_screen"),
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
                        text = "Studio Projects",
                        color = TextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${projects.size} total animation projects in workspace",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                Button(
                    onClick = { onNavigate(NavDestination.CREATE) },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonIndigo),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("projects_create_btn")
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Create", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by title, prompt or style...", color = TextMuted) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = TextMuted) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Filled.Close, contentDescription = "Clear", tint = TextMuted)
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonIndigo,
                    unfocusedBorderColor = DarkBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("projects_search_field")
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Filter Chips
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("All", "Completed", "Generating", "Drafts", "Failed").forEach { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NeonIndigo,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        if (filteredProjects.isEmpty()) {
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text("No projects found matching the criteria.", color = TextMuted)
                }
            }
        } else {
            items(filteredProjects) { proj ->
                Surface(
                    color = DarkSurface,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .testTag("project_item_${proj.id}")
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            // Thumbnail
                            Box(
                                modifier = Modifier
                                    .size(width = 110.dp, height = 75.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        onSelectProject(proj.id)
                                        onNavigate(NavDestination.EDITOR)
                                    }
                            ) {
                                Image(
                                    painter = painterResource(
                                        if (proj.thumbnailResName == "scene_lagos_sunset") R.drawable.scene_lagos_sunset
                                        else R.drawable.hero_animation_art
                                    ),
                                    contentDescription = proj.name,
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
                                    Text(
                                        text = "${proj.durationSeconds}s",
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = proj.name,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        maxLines = 1
                                    )
                                    StatusBadge(status = proj.status)
                                }

                                Text(
                                    text = "${proj.totalScenes} scenes • ${proj.style} • ${proj.aspectRatio}",
                                    color = NeonCyan,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(top = 2.dp)
                                )

                                Text(
                                    text = proj.description,
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    maxLines = 2,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Divider(color = DarkBorder, thickness = 0.5.dp)
                        Spacer(modifier = Modifier.height(4.dp))

                        // Actions Row: Open, Duplicate, Rename, Delete, Share
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = {
                                    onSelectProject(proj.id)
                                    onNavigate(NavDestination.EDITOR)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonIndigo),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text("Open Studio", fontSize = 11.sp, color = Color.White)
                            }

                            Row {
                                IconButton(
                                    onClick = { onDuplicateProject(proj) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Filled.ContentCopy, contentDescription = "Duplicate", tint = TextSecondary, modifier = Modifier.size(16.dp))
                                }
                                IconButton(
                                    onClick = { projectToRename = proj },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Filled.Edit, contentDescription = "Rename", tint = TextSecondary, modifier = Modifier.size(16.dp))
                                }
                                IconButton(
                                    onClick = { shareProjectLink = "https://div.ai/share/${proj.id}" },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Filled.Share, contentDescription = "Share", tint = NeonCyan, modifier = Modifier.size(16.dp))
                                }
                                IconButton(
                                    onClick = { onDeleteProject(proj.id) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = AccentRose, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Rename Project Dialog
    projectToRename?.let { proj ->
        var newName by remember { mutableStateOf(proj.name) }
        AlertDialog(
            onDismissRequest = { projectToRename = null },
            containerColor = DarkSurfaceElevated,
            title = { Text("Rename Project", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("Project Name", color = TextSecondary) },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onRenameProject(proj, newName)
                        projectToRename = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonIndigo)
                ) {
                    Text("Save", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { projectToRename = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    // Share Link Modal
    shareProjectLink?.let { link ->
        AlertDialog(
            onDismissRequest = { shareProjectLink = null },
            containerColor = DarkSurfaceElevated,
            title = { Text("Share Animation Project", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Your public viewer & collaboration link is active:", color = TextSecondary, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = DarkSurfaceVariant,
                        shape = RoundedCornerShape(6.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                    ) {
                        Text(
                            text = link,
                            color = NeonCyanLight,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { shareProjectLink = null },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonIndigo)
                ) {
                    Text("Copy & Close", color = Color.White)
                }
            }
        )
    }
}
