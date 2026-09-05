package com.example.data.repository

import android.content.Context
import com.example.data.local.dao.GenerationJobDao
import com.example.data.local.dao.NotificationDao
import com.example.data.local.dao.ProjectDao
import com.example.data.local.entities.GenerationJobEntity
import com.example.data.local.entities.NotificationEntity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class GenerationRepository(
    private val generationJobDao: GenerationJobDao,
    private val projectDao: ProjectDao,
    private val notificationDao: NotificationDao,
    private val coroutineScope: CoroutineScope,
    context: Context
) {
    private val appContext = context.applicationContext
    private val veoVideoRepo = VeoVideoRepository(appContext)
    private val seedanceVideoRepo = SeedanceVideoRepository(appContext)
    private val grokVideoRepo = GrokVideoRepository(appContext)

    fun getAllJobs(): Flow<List<GenerationJobEntity>> = generationJobDao.getAllJobs()
    fun getActiveJobs(): Flow<List<GenerationJobEntity>> = generationJobDao.getActiveJobs()
    suspend fun getTotalGenerationsCount(): Int = generationJobDao.getTotalGenerationsCount()

    fun startGenerationJob(projectId: String, projectName: String, mode: String, totalScenes: Int = 1, userId: String): String {
        val jobId = "job_${UUID.randomUUID().toString().take(8)}"
        val initialJob = GenerationJobEntity(id = jobId, projectId = projectId, projectName = projectName, mode = mode, status = "queued", progress = 1, currentStepMessage = "Queued for real AI video generation...", startedAt = System.currentTimeMillis())
        coroutineScope.launch(Dispatchers.IO) {
            generationJobDao.insertJob(initialJob)
            val project = projectDao.getProjectByIdDirect(projectId)
            if (project == null) { failJob(jobId, projectId, "Project could not be loaded."); return@launch }
            updateJobAndProject(jobId, projectId, "processing", 5, "Preparing your animation prompt...")
            val prompt = buildPrompt(project.description, project.style, project.cameraMovement, project.characterMovement)
            val provider = appContext.getSharedPreferences("divstudio_ai_settings", Context.MODE_PRIVATE).getString("video_provider", "veo").orEmpty().lowercase()
            val result = when (provider) {
                "seedance" -> {
                    updateJobAndProject(jobId, projectId, "generating", 10, "Sending animation to Seedance 2.5...")
                    seedanceVideoRepo.generateVideo(prompt, project.aspectRatio, project.durationSeconds.coerceIn(4, 30), if (project.resolution == "480p") "480p" else "720p", true)
                }
                "grok" -> {
                    updateJobAndProject(jobId, projectId, "generating", 10, "Sending animation to Grok Imagine Video 1.5...")
                    grokVideoRepo.generateVideo(prompt, project.aspectRatio, project.durationSeconds.coerceIn(1, 15), if (project.resolution == "1080p") "1080p" else "720p")
                }
                else -> {
                    updateJobAndProject(jobId, projectId, "generating", 10, "Sending animation to Google Veo 3.1 Fast...")
                    veoVideoRepo.generateVideo(prompt, project.aspectRatio, if (project.resolution == "1080p") "1080p" else "720p")
                }
            }
            result.onSuccess { videoFile ->
                updateJobAndProject(jobId, projectId, "processing", 95, "AI video generated. Saving the MP4 locally...")
                generationJobDao.getJobById(jobId)?.let { generationJobDao.updateJob(it.copy(status = "completed", progress = 100, currentStepMessage = "Animation MP4 is ready for preview and export.", completedAt = System.currentTimeMillis())) }
                projectDao.getProjectByIdDirect(projectId)?.let { projectDao.updateProject(it.copy(status = "completed", progress = 100, currentStep = "Real MP4 ready - Preview & export", videoUrl = videoFile.absolutePath, updatedAt = System.currentTimeMillis())) }
                notificationDao.insertNotification(NotificationEntity(id = UUID.randomUUID().toString(), userId = userId, title = "Animation MP4 Ready", message = "Your animation '$projectName' was generated successfully.", type = "generation", timestamp = System.currentTimeMillis()))
            }.onFailure { error -> failJob(jobId, projectId, error.message ?: "Video generation failed.") }
        }
        return jobId
    }

    private fun buildPrompt(description: String, style: String, camera: String, characterMovement: String): String = """
        Create a polished animated cartoon video for DIVSTUDIO AI.
        Visual style: ${style.ifBlank { "2D cartoon" }}.
        Camera movement: ${camera.ifBlank { "smooth cinematic camera" }}.
        Character movement: ${characterMovement.ifBlank { "natural expressive movement" }}.
        Story/action:
        ${description.ifBlank { "A lively character begins an engaging animated story." }}
        Keep characters visually coherent throughout the shot. Use clear readable action, expressive faces,
        smooth motion, strong composition, and a finished professional animation look. This is an animated/cartoon production, not live action.
    """.trimIndent()

    private suspend fun failJob(jobId: String, projectId: String, message: String) {
        generationJobDao.getJobById(jobId)?.let { generationJobDao.updateJob(it.copy(status = "failed", progress = 0, currentStepMessage = message, completedAt = System.currentTimeMillis())) }
        projectDao.getProjectByIdDirect(projectId)?.let { projectDao.updateProject(it.copy(status = "failed", progress = 0, currentStep = message, updatedAt = System.currentTimeMillis())) }
    }

    private suspend fun updateJobAndProject(jobId: String, projectId: String, status: String, progress: Int, message: String) {
        generationJobDao.getJobById(jobId)?.let { generationJobDao.updateJob(it.copy(status = status, progress = progress, currentStepMessage = message)) }
        projectDao.getProjectByIdDirect(projectId)?.let { projectDao.updateProject(it.copy(status = status, progress = progress, currentStep = message)) }
    }
}
