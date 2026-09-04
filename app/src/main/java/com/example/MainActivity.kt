package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.DivAiBottomBar
import com.example.ui.components.DivAiTopBar
import com.example.ui.components.NotificationDialog
import com.example.ui.navigation.NavDestination
import com.example.ui.screens.*
import com.example.ui.theme.DarkCanvas
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val currentDestination by viewModel.currentDestination.collectAsStateWithLifecycle()
                val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
                val projects by viewModel.projects.collectAsStateWithLifecycle()
                val activeProject by viewModel.activeProject.collectAsStateWithLifecycle()
                val activeScenes by viewModel.activeProjectScenes.collectAsStateWithLifecycle()
                val characters by viewModel.characters.collectAsStateWithLifecycle()
                val sceneTemplates by viewModel.sceneTemplates.collectAsStateWithLifecycle()
                val activeJobs by viewModel.activeJobs.collectAsStateWithLifecycle()
                val notifications by viewModel.notifications.collectAsStateWithLifecycle()
                val unreadNotifCount by viewModel.unreadNotifCount.collectAsStateWithLifecycle()
                val allUsers by viewModel.allUsers.collectAsStateWithLifecycle()
                val adminLogs by viewModel.adminLogs.collectAsStateWithLifecycle()

                var showNotificationsDialog by remember { mutableStateOf(false) }
                var isAiConfigured by remember { mutableStateOf(viewModel.aiProviderRepo.isGeminiConfigured()) }
                val providerStatuses = remember(isAiConfigured) {
                    viewModel.aiProviderRepo.getProviderStatuses()
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = DarkCanvas,
                    topBar = {
                        DivAiTopBar(
                            currentDestination = currentDestination,
                            currentUser = currentUser,
                            unreadNotifCount = unreadNotifCount,
                            onNavigate = { viewModel.navigateTo(it) },
                            onOpenNotifications = { showNotificationsDialog = true },
                            onAuthClick = { viewModel.navigateTo(NavDestination.AUTH) }
                        )
                    },
                    bottomBar = {
                        DivAiBottomBar(
                            currentDestination = currentDestination,
                            onNavigate = { viewModel.navigateTo(it) }
                        )
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .background(DarkCanvas)
                    ) {
                        when (currentDestination) {
                            NavDestination.LANDING -> LandingScreen(onNavigate = { viewModel.navigateTo(it) })
                            NavDestination.AUTH -> AuthScreen(
                                onLogin = { email, pass -> viewModel.login(email, pass) },
                                onRegister = { email, name, pass -> viewModel.register(email, name, pass) },
                                onQuickLogin = { uid -> viewModel.quickSwitchUser(uid) },
                                onAuthSuccess = { viewModel.navigateTo(NavDestination.DASHBOARD) }
                            )
                            NavDestination.DASHBOARD -> DashboardScreen(
                                currentUser = currentUser,
                                projects = projects,
                                activeJobs = activeJobs,
                                onNavigate = { viewModel.navigateTo(it) },
                                onSelectProject = { viewModel.selectProject(it) }
                            )
                            NavDestination.CREATE -> CreateAnimationScreen(
                                characters = characters,
                                sceneTemplates = sceneTemplates,
                                providerStatuses = providerStatuses,
                                isAiConfigured = isAiConfigured,
                                onGenerateAnimation = { name, prompt, mode, style, duration, ratio, camera, charMove, voice, lang, scenes ->
                                    viewModel.startAnimationGeneration(
                                        name, prompt, mode, style, duration, ratio, camera, charMove, voice, lang, scenes
                                    )
                                },
                                onNavigate = { viewModel.navigateTo(it) }
                            )
                            NavDestination.STORYBOARD -> StoryboardScreen(
                                currentProject = activeProject,
                                scenes = activeScenes,
                                onAddScene = { viewModel.addScene(it) },
                                onUpdateScene = { viewModel.updateScene(it) },
                                onDeleteScene = { viewModel.deleteScene(it) },
                                onReorderScenes = { viewModel.reorderScenes(it) },
                                onNavigate = { viewModel.navigateTo(it) }
                            )
                            NavDestination.EDITOR -> RealVideoPreviewScreen(
                                project = activeProject,
                                onSaveProject = { viewModel.saveProject(it) }
                            )
                            NavDestination.CHARACTERS -> CharacterStudioScreen(
                                characters = characters,
                                onSaveCharacter = { viewModel.saveCharacter(it) },
                                onDuplicateCharacter = { viewModel.duplicateCharacter(it) },
                                onDeleteCharacter = { viewModel.deleteCharacter(it) },
                                onUseInProject = { viewModel.navigateTo(NavDestination.CREATE) },
                                onNavigate = { viewModel.navigateTo(it) }
                            )
                            NavDestination.SCENES -> SceneStudioScreen(
                                customTemplates = sceneTemplates,
                                onSaveTemplate = { viewModel.saveSceneTemplate(it) },
                                onDeleteTemplate = { viewModel.deleteSceneTemplate(it) },
                                onUseEnvironment = { viewModel.navigateTo(NavDestination.CREATE) },
                                onNavigate = { viewModel.navigateTo(it) }
                            )
                            NavDestination.PROJECTS -> ProjectsScreen(
                                projects = projects,
                                onSelectProject = { viewModel.selectProject(it) },
                                onDuplicateProject = { viewModel.duplicateProject(it) },
                                onRenameProject = { proj, newName -> viewModel.renameProject(proj, newName) },
                                onDeleteProject = { viewModel.deleteProject(it) },
                                onNavigate = { viewModel.navigateTo(it) }
                            )
                            NavDestination.ADMIN -> AdminDashboardScreen(
                                currentUser = currentUser,
                                allUsers = allUsers,
                                totalProjectsCount = projects.size,
                                totalGenerationsCount = activeJobs.size + projects.size,
                                completedProjectsCount = projects.count { it.status == "completed" },
                                providerStatuses = providerStatuses,
                                adminLogs = adminLogs,
                                onUpdateUserCredits = { uid, credits -> viewModel.adjustUserCredits(uid, credits) },
                                onNavigate = { viewModel.navigateTo(it) }
                            )
                            NavDestination.PROFILE -> ProfileSettingsScreen(
                                currentUser = currentUser,
                                providerStatuses = providerStatuses,
                                isAiConfigured = isAiConfigured,
                                onSaveProfile = { name, style, ratio, voice -> viewModel.updateProfile(name, style, ratio, voice) },
                                onSaveGeminiApiKey = { key ->
                                    viewModel.saveGeminiApiKey(key)
                                    isAiConfigured = true
                                },
                                onClearGeminiApiKey = {
                                    viewModel.clearGeminiApiKey()
                                    isAiConfigured = false
                                },
                                onLogout = { viewModel.logout() },
                                onNavigate = { viewModel.navigateTo(it) }
                            )
                            NavDestination.HELP -> HelpCenterScreen()
                        }

                        if (showNotificationsDialog) {
                            NotificationDialog(
                                notifications = notifications,
                                onDismiss = { showNotificationsDialog = false },
                                onMarkAllRead = { viewModel.markNotificationsRead() }
                            )
                        }
                    }
                }
            }
        }
    }
}
