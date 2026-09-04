package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.NotificationEntity
import com.example.data.local.entities.UserEntity
import com.example.ui.navigation.NavDestination
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DivAiTopBar(
    currentDestination: NavDestination,
    currentUser: UserEntity?,
    unreadNotifCount: Int,
    onNavigate: (NavDestination) -> Unit,
    onOpenNotifications: () -> Unit,
    onAuthClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Surface(
        color = DarkSurface,
        tonalElevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Logo & Brand
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onNavigate(NavDestination.LANDING) }
                        .padding(4.dp)
                        .testTag("topbar_brand_logo")
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                Brush.linearGradient(listOf(NeonIndigo, NeonViolet, NeonCyan))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "DIV",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "DIV AI",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = NeonIndigo.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "STUDIO",
                                    color = NeonIndigoLight,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Text(
                            text = "Powered by DIVSTUDIO",
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                    }
                }

                // Actions
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Notifications
                    IconButton(
                        onClick = onOpenNotifications,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("topbar_notifications_btn")
                    ) {
                        BadgedBox(
                            badge = {
                                if (unreadNotifCount > 0) {
                                    Badge(containerColor = NeonCyan) {
                                        Text(unreadNotifCount.toString(), color = Color.Black, fontSize = 10.sp)
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Notifications,
                                contentDescription = "Notifications",
                                tint = TextSecondary
                            )
                        }
                    }

                    // User Menu / Profile
                    Box {
                        Surface(
                            shape = CircleShape,
                            color = if (currentUser?.role == "admin") NeonViolet.copy(alpha = 0.3f) else NeonIndigo.copy(alpha = 0.25f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                if (currentUser?.role == "admin") AccentAmber else NeonIndigo
                            ),
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .clickable { showMenu = true }
                                .testTag("topbar_user_profile_btn")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = currentUser?.fullName?.take(1)?.uppercase() ?: "G",
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            modifier = Modifier.background(DarkSurfaceVariant)
                        ) {
                            currentUser?.let { user ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(user.fullName, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Text(user.email, color = TextMuted, fontSize = 11.sp)
                                            Text("Credits: ${user.creditsRemaining}", color = NeonCyan, fontSize = 11.sp)
                                        }
                                    },
                                    onClick = {
                                        showMenu = false
                                        onNavigate(NavDestination.PROFILE)
                                    }
                                )
                                Divider(color = DarkBorder)
                            }

                            DropdownMenuItem(
                                text = { Text("Dashboard", color = TextPrimary) },
                                leadingIcon = { Icon(Icons.Outlined.Dashboard, contentDescription = null, tint = NeonIndigoLight) },
                                onClick = {
                                    showMenu = false
                                    onNavigate(NavDestination.DASHBOARD)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Character Studio", color = TextPrimary) },
                                leadingIcon = { Icon(Icons.Outlined.Face, contentDescription = null, tint = NeonCyan) },
                                onClick = {
                                    showMenu = false
                                    onNavigate(NavDestination.CHARACTERS)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Scene Studio", color = TextPrimary) },
                                leadingIcon = { Icon(Icons.Outlined.Landscape, contentDescription = null, tint = AccentAmber) },
                                onClick = {
                                    showMenu = false
                                    onNavigate(NavDestination.SCENES)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Storyboard", color = TextPrimary) },
                                leadingIcon = { Icon(Icons.Outlined.ViewCarousel, contentDescription = null, tint = NeonViolet) },
                                onClick = {
                                    showMenu = false
                                    onNavigate(NavDestination.STORYBOARD)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Video Editor", color = TextPrimary) },
                                leadingIcon = { Icon(Icons.Outlined.MovieCreation, contentDescription = null, tint = AccentEmerald) },
                                onClick = {
                                    showMenu = false
                                    onNavigate(NavDestination.EDITOR)
                                }
                            )

                            if (currentUser?.role == "admin") {
                                Divider(color = DarkBorder)
                                DropdownMenuItem(
                                    text = { Text("Admin Console", color = AccentAmber, fontWeight = FontWeight.Bold) },
                                    leadingIcon = { Icon(Icons.Filled.AdminPanelSettings, contentDescription = null, tint = AccentAmber) },
                                    onClick = {
                                        showMenu = false
                                        onNavigate(NavDestination.ADMIN)
                                    }
                                )
                            }

                            Divider(color = DarkBorder)
                            DropdownMenuItem(
                                text = { Text("Help Center", color = TextSecondary) },
                                leadingIcon = { Icon(Icons.Outlined.HelpOutline, contentDescription = null, tint = TextSecondary) },
                                onClick = {
                                    showMenu = false
                                    onNavigate(NavDestination.HELP)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Settings & Providers", color = TextSecondary) },
                                leadingIcon = { Icon(Icons.Outlined.Settings, contentDescription = null, tint = TextSecondary) },
                                onClick = {
                                    showMenu = false
                                    onNavigate(NavDestination.PROFILE)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Switch / Auth", color = NeonIndigoLight) },
                                leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null, tint = NeonIndigoLight) },
                                onClick = {
                                    showMenu = false
                                    onAuthClick()
                                }
                            )
                        }
                    }
                }
            }
            Divider(color = DarkBorder, thickness = 1.dp)
        }
    }
}

@Composable
fun DivAiBottomBar(
    currentDestination: NavDestination,
    onNavigate: (NavDestination) -> Unit
) {
    val items = listOf(
        NavDestination.LANDING,
        NavDestination.DASHBOARD,
        NavDestination.CREATE,
        NavDestination.PROJECTS,
        NavDestination.EDITOR
    )

    Surface(
        color = DarkSurface,
        tonalElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        NavigationBar(
            containerColor = DarkSurface,
            contentColor = TextPrimary,
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
        ) {
            items.forEach { dest ->
                val selected = currentDestination == dest
                NavigationBarItem(
                    selected = selected,
                    onClick = { onNavigate(dest) },
                    icon = {
                        Icon(
                            imageVector = if (selected) dest.iconFilled else dest.iconOutlined,
                            contentDescription = dest.title,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = {
                        Text(
                            text = dest.title,
                            fontSize = 11.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = NeonCyan,
                        indicatorColor = NeonIndigo,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    ),
                    modifier = Modifier.testTag("nav_item_${dest.route}")
                )
            }
        }
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = DarkSurfaceVariant,
    borderColor: Color = DarkBorder,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

@Composable
fun StatusBadge(status: String) {
    val (bgColor, textColor, label) = when (status.lowercase()) {
        "completed", "ready" -> Triple(AccentEmerald.copy(alpha = 0.2f), AccentEmerald, "COMPLETED")
        "rendering" -> Triple(NeonViolet.copy(alpha = 0.2f), NeonViolet, "RENDERING")
        "processing" -> Triple(NeonCyan.copy(alpha = 0.2f), NeonCyan, "PROCESSING")
        "queued" -> Triple(AccentAmber.copy(alpha = 0.2f), AccentAmber, "QUEUED")
        "failed" -> Triple(AccentRose.copy(alpha = 0.2f), AccentRose, "FAILED")
        else -> Triple(TextMuted.copy(alpha = 0.2f), TextSecondary, status.uppercase())
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(6.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, textColor.copy(alpha = 0.4f))
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun NotificationDialog(
    notifications: List<NotificationEntity>,
    onDismiss: () -> Unit,
    onMarkAllRead: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurfaceElevated,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Studio Notifications",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onMarkAllRead) {
                    Text("Mark all read", color = NeonCyan, fontSize = 12.sp)
                }
            }
        },
        text = {
            if (notifications.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No notifications yet", color = TextMuted)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 350.dp)
                ) {
                    notifications.forEach { notif ->
                        Surface(
                            color = if (notif.isRead) DarkSurfaceVariant else NeonIndigo.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (notif.type == "generation") Icons.Filled.MovieCreation else Icons.Filled.Info,
                                        contentDescription = null,
                                        tint = if (notif.type == "generation") NeonCyan else NeonIndigoLight,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = notif.title,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = notif.message,
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = NeonIndigo)
            ) {
                Text("Close", color = Color.White)
            }
        }
    )
}
