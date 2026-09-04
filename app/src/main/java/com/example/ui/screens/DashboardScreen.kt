package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.example.data.local.entities.GenerationJobEntity
import com.example.data.local.entities.ProjectEntity
import com.example.data.local.entities.UserEntity
import com.example.ui.components.GlassCard
import com.example.ui.components.StatusBadge
import com.example.ui.navigation.NavDestination
import com.example.ui.theme.*

@Composable
fun DashboardScreen(
    currentUser: UserEntity?,
    projects: List<ProjectEntity>,
    activeJobs: List<GenerationJobEntity>,
    onNavigate: (NavDestination) -> Unit,
    onSelectProject: (String) -> Unit
) {
    val completedProjects = projects.filter { it.status == "completed" }
    val draftProjects = projects.filter { it.status == "draft" }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkCanvas)
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 90.dp)
    ) {
        // Welcome Header & Main Action
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = DarkSurfaceVariant
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Welcome back, ${currentUser?.fullName ?: "Creator"}",
                            color = TextPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${currentUser?.planName ?: "Studio Pro"} • ${currentUser?.creditsRemaining ?: 100} credits remaining",
                            color = NeonCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    Button(
                        onClick = { onNavigate(NavDestination.CREATE) },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonIndigo),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("dashboard_create_anim_btn")
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Create Animation", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Active Generation Job Banner (Requirement 22)
        if (activeJobs.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(14.dp))
                activeJobs.forEach { job ->
                    Surface(
                        color = NeonIndigo.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .testTag("active_generation_banner")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.5.dp,
                                        color = NeonCyan
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = job.projectName,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                                StatusBadge(status = job.status)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = job.currentStepMessage,
                                color = NeonCyanLight,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { job.progress / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = NeonCyan,
                                trackColor = DarkSurfaceElevated
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Progress: ${job.progress}%",
                                color = TextMuted,
                                fontSize = 10.sp,
                                modifier = Modifier.align(Alignment.End)
                            )
                        }
                    }
                }
            }
        }

        // Quick Actions Grid (Requirement 4)
        item {
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = "Quick Actions",
                color = TextPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))

            val quickActions = listOf(
                Triple("Text → Animation", Icons.Filled.TextFields, NeonIndigoLight),
                Triple("Image → Animation", Icons.Filled.Image, NeonCyan),
                Triple("Script → Animation", Icons.Filled.Description, NeonViolet),
                Triple("Create Character", Icons.Filled.Face, AccentAmber),
                Triple("Create Scene", Icons.Filled.Landscape, AccentEmerald),
                Triple("Open Editor", Icons.Filled.MovieCreation, AccentRose)
            )

            quickActions.chunked(3).forEach { rowActions ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowActions.forEach { (title, icon, tint) ->
                        Surface(
                            color = DarkSurface,
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                            modifier = Modifier
                                .weight(1f)
                                .height(80.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    when (title) {
                                        "Create Character" -> onNavigate(NavDestination.CHARACTERS)
                                        "Create Scene" -> onNavigate(NavDestination.SCENES)
                                        "Open Editor" -> onNavigate(NavDestination.EDITOR)
                                        else -> onNavigate(NavDestination.CREATE)
                                    }
                                }
                                .testTag("quick_action_${title.replace(" ", "_")}")
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = tint,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = title,
                                    color = TextPrimary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }

        // Usage Information
        item {
            Spacer(modifier = Modifier.height(14.dp))
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = DarkSurfaceVariant
            ) {
                Text(
                    text = "Studio Resource Usage",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("AI Generation Credits", color = TextSecondary, fontSize = 11.sp)
                        Text("${currentUser?.creditsRemaining ?: 100} / 500", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Column {
                        Text("Render Engine", color = TextSecondary, fontSize = 11.sp)
                        Text("Veo 3.1 & AudioRig", color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Column {
                        Text("Cloud Storage", color = TextSecondary, fontSize = 11.sp)
                        Text("1.2 GB / 50 GB", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }

        // Recent Completed Projects
        item {
            Spacer(modifier = Modifier.height(18.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Completed Animations",
                    color = TextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = { onNavigate(NavDestination.PROJECTS) }) {
                    Text("View all (${projects.size})", color = NeonCyan, fontSize = 12.sp)
                }
            }
        }

        if (completedProjects.isEmpty()) {
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = DarkSurface
                ) {
                    Text("No completed animations yet. Create your first one!", color = TextMuted, fontSize = 13.sp)
                }
            }
        } else {
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(completedProjects) { proj ->
                        Surface(
                            color = DarkSurface,
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                            modifier = Modifier
                                .width(220.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    onSelectProject(proj.id)
                                    onNavigate(NavDestination.EDITOR)
                                }
                        ) {
                            Column {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(110.dp)
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
                                            .padding(6.dp)
                                    ) {
                                        Text(
                                            text = "${proj.durationSeconds}s • ${proj.aspectRatio}",
                                            color = Color.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = proj.name,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = proj.style,
                                        color = NeonIndigoLight,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Draft Projects Section
        item {
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = "Drafts & Production Queue",
                color = TextPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (draftProjects.isEmpty() && projects.none { it.status != "completed" }) {
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = DarkSurface
                ) {
                    Text("No pending drafts. All projects rendered.", color = TextMuted, fontSize = 13.sp)
                }
            }
        } else {
            val pendingProjects = projects.filter { it.status != "completed" }
            items(pendingProjects) { proj ->
                Surface(
                    color = DarkSurface,
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable {
                            onSelectProject(proj.id)
                            onNavigate(NavDestination.EDITOR)
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(proj.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("${proj.totalScenes} scenes • ${proj.style}", color = TextSecondary, fontSize = 11.sp)
                        }
                        StatusBadge(status = proj.status)
                    }
                }
            }
        }
    }
}
