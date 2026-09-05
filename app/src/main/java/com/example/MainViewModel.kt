package com.example

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entities.*
import com.example.data.repository.*
import com.example.ui.navigation.NavDestination
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application, viewModelScope)
    val authRepo = AuthRepository(db.userDao())
    val projectRepo = ProjectRepository(db.projectDao(), db.sceneDao())
    val studioRepo = StudioRepository(db.characterDao(), db.sceneTemplateDao())
    val generationRepo = GenerationRepository(db.generationJobDao(), db.projectDao(), db.notificationDao(), viewModelScope, application.applicationContext)
    val aiProviderRepo = AiProviderRepository(application.applicationContext)
    private val settings = application.getSharedPreferences("divstudio_ai_settings", Application.MODE_PRIVATE)

    private val _currentDestination = MutableStateFlow(NavDestination.LANDING)
    val currentDestination = _currentDestination.asStateFlow()
    private val _activeProjectId = MutableStateFlow("proj_demo_01")
    val activeProjectId = _activeProjectId.asStateFlow()

    val currentUser: StateFlow<UserEntity?> = authRepo.getCurrentUserFlow().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val allUsers: StateFlow<List<UserEntity>> = authRepo.getAllUsersFlow().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val projects: StateFlow<List<ProjectEntity>> = authRepo.currentUserId.flatMapLatest { uid -> if (uid != null) projectRepo.getProjectsForUser(uid) else flowOf(emptyList()) }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val activeProject: StateFlow<ProjectEntity?> = _activeProjectId.flatMapLatest { id -> projectRepo.getProjectById(id) }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val activeProjectScenes: StateFlow<List<SceneEntity>> = _activeProjectId.flatMapLatest { id -> projectRepo.getScenesForProject(id) }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val characters: StateFlow<List<CharacterEntity>> = authRepo.currentUserId.flatMapLatest { uid -> if (uid != null) studioRepo.getCharactersForUser(uid) else flowOf(emptyList()) }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val sceneTemplates: StateFlow<List<SceneTemplateEntity>> = authRepo.currentUserId.flatMapLatest { uid -> if (uid != null) studioRepo.getTemplatesForUser(uid) else flowOf(emptyList()) }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val activeJobs: StateFlow<List<GenerationJobEntity>> = generationRepo.getActiveJobs().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val notifications: StateFlow<List<NotificationEntity>> = authRepo.currentUserId.flatMapLatest { uid -> if (uid != null) db.notificationDao().getNotificationsForUser(uid) else flowOf(emptyList()) }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val unreadNotifCount: StateFlow<Int> = authRepo.currentUserId.flatMapLatest { uid -> if (uid != null) db.notificationDao().getUnreadCount(uid) else flowOf(0) }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val adminLogs: StateFlow<List<AdminLogEntity>> = db.adminLogDao().getAllLogs().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun navigateTo(destination: NavDestination) { if (destination == NavDestination.ADMIN && currentUser.value?.role != "admin") _currentDestination.value = NavDestination.AUTH else _currentDestination.value = destination }
    fun selectProject(projectId: String) { _activeProjectId.value = projectId }
    fun login(email: String, pass: String) { viewModelScope.launch { authRepo.login(email, pass); _currentDestination.value = NavDestination.DASHBOARD } }
    fun register(email: String, name: String, pass: String) { viewModelScope.launch { authRepo.register(email, name, pass); _currentDestination.value = NavDestination.DASHBOARD } }
    fun quickSwitchUser(userId: String) { authRepo.switchUser(userId) }
    fun logout() { authRepo.logout(); _currentDestination.value = NavDestination.AUTH }
    fun updateProfile(name: String, style: String, ratio: String, voice: String) { viewModelScope.launch { authRepo.updateProfile(name, style, ratio, voice) } }
    fun saveGeminiApiKey(apiKey: String) { aiProviderRepo.saveApiKey(apiKey) }
    fun clearGeminiApiKey() { aiProviderRepo.clearApiKey() }
    fun saveSeedanceApiKey(apiKey: String) { settings.edit().putString("seedance_api_key", apiKey).apply() }
    fun clearSeedanceApiKey() { settings.edit().remove("seedance_api_key").apply() }
    fun isSeedanceConfigured(): Boolean = settings.getString("seedance_api_key", "").orEmpty().isNotBlank()
    fun getVideoProvider(): String = settings.getString("video_provider", "veo").orEmpty().ifBlank { "veo" }
    fun saveVideoProvider(provider: String) { settings.edit().putString("video_provider", if (provider == "seedance") "seedance" else "veo").apply() }

    fun startAnimationGeneration(projectName: String, prompt: String, mode: String, style: String, duration: Int, ratio: String, camera: String, charMove: String, voice: String, language: String, scenes: List<SceneEntity>) {
        val uid = authRepo.currentUserId.value ?: "user_default_01"
        val projId = "proj_${UUID.randomUUID().toString().take(8)}"
        val newProject = ProjectEntity(id = projId, userId = uid, name = projectName.ifBlank { "Animation $projId" }, description = prompt, mode = mode, status = "queued", progress = 1, currentStep = "Queued for real AI video generation", durationSeconds = duration, aspectRatio = ratio, resolution = "720p", style = style, cameraMovement = camera, characterMovement = charMove, voiceName = voice, voiceLanguage = language, totalScenes = scenes.size.coerceAtLeast(1), thumbnailResName = if (scenes.size % 2 == 1) "scene_lagos_sunset" else "hero_animation_art")
        viewModelScope.launch {
            val linkedScenes = scenes.map { it.copy(projectId = projId) }
            projectRepo.createProjectWithScenes(newProject, linkedScenes)
            _activeProjectId.value = projId
            generationRepo.startGenerationJob(projId, projectName, mode, linkedScenes.size, uid)
            _currentDestination.value = NavDestination.DASHBOARD
        }
    }
    fun addScene(scene: SceneEntity) { viewModelScope.launch { projectRepo.addScene(scene) } }
    fun updateScene(scene: SceneEntity) { viewModelScope.launch { projectRepo.updateScene(scene) } }
    fun deleteScene(scene: SceneEntity) { viewModelScope.launch { projectRepo.deleteScene(scene) } }
    fun reorderScenes(scenes: List<SceneEntity>) { viewModelScope.launch { projectRepo.reorderScenes(_activeProjectId.value, scenes) } }
    fun duplicateProject(project: ProjectEntity) { viewModelScope.launch { _activeProjectId.value = projectRepo.duplicateProject(project) } }
    fun renameProject(project: ProjectEntity, newName: String) { viewModelScope.launch { projectRepo.updateProject(project.copy(name = newName)) } }
    fun deleteProject(projectId: String) { viewModelScope.launch { projectRepo.deleteProject(projectId) } }
    fun saveProject(project: ProjectEntity) { viewModelScope.launch { projectRepo.updateProject(project) } }
    fun saveCharacter(character: CharacterEntity) { viewModelScope.launch { studioRepo.saveCharacter(character.copy(userId = authRepo.currentUserId.value ?: "user_default_01")) } }
    fun duplicateCharacter(character: CharacterEntity) { viewModelScope.launch { studioRepo.duplicateCharacter(character) } }
    fun deleteCharacter(characterId: String) { viewModelScope.launch { studioRepo.deleteCharacter(characterId) } }
    fun saveSceneTemplate(template: SceneTemplateEntity) { viewModelScope.launch { studioRepo.saveTemplate(template.copy(userId = authRepo.currentUserId.value ?: "user_default_01")) } }
    fun deleteSceneTemplate(template: SceneTemplateEntity) { viewModelScope.launch { studioRepo.deleteTemplate(template) } }
    fun adjustUserCredits(userId: String, credits: Int) { viewModelScope.launch { db.userDao().getUserByEmail(userId)?.let { db.userDao().updateUser(it.copy(creditsRemaining = credits)) } } }
    fun markNotificationsRead() { val uid = authRepo.currentUserId.value ?: return; viewModelScope.launch { db.notificationDao().markAllAsRead(uid) } }
}
