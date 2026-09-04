package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

enum class NavDestination(
    val route: String,
    val title: String,
    val iconOutlined: ImageVector,
    val iconFilled: ImageVector,
    val showInBottomBar: Boolean = false
) {
    LANDING("landing", "Home", Icons.Outlined.Home, Icons.Filled.Home, showInBottomBar = true),
    DASHBOARD("dashboard", "Dashboard", Icons.Outlined.Dashboard, Icons.Filled.Dashboard, showInBottomBar = true),
    CREATE("create", "Create", Icons.Outlined.AutoAwesome, Icons.Filled.AutoAwesome, showInBottomBar = true),
    PROJECTS("projects", "Projects", Icons.Outlined.Folder, Icons.Filled.Folder, showInBottomBar = true),
    CHARACTERS("characters", "Characters", Icons.Outlined.Face, Icons.Filled.Face, showInBottomBar = false),
    SCENES("scenes", "Scenes", Icons.Outlined.Landscape, Icons.Filled.Landscape, showInBottomBar = false),
    STORYBOARD("storyboard", "Storyboard", Icons.Outlined.ViewCarousel, Icons.Filled.ViewCarousel, showInBottomBar = false),
    EDITOR("editor", "Editor", Icons.Outlined.MovieCreation, Icons.Filled.MovieCreation, showInBottomBar = true),
    ADMIN("admin", "Admin", Icons.Outlined.AdminPanelSettings, Icons.Filled.AdminPanelSettings, showInBottomBar = false),
    PROFILE("profile", "Settings", Icons.Outlined.Settings, Icons.Filled.Settings, showInBottomBar = false),
    HELP("help", "Help", Icons.Outlined.HelpOutline, Icons.Filled.Help, showInBottomBar = false),
    AUTH("auth", "Sign In", Icons.Outlined.Lock, Icons.Filled.Lock, showInBottomBar = false)
}
