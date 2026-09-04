package com.example.data.repository

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
    private val coroutineScope: CoroutineScope
) {
    fun getAllJobs(): Flow<List<GenerationJobEntity>> = generationJobDao.getAllJobs()

    fun getActiveJobs(): Flow<List<GenerationJobEntity>> = generationJobDao.getActiveJobs()

    suspend fun getTotalGenerationsCount(): Int = generationJobDao.getTotalGenerationsCount()

    fun startGenerationJob(
        projectId: String,
        projectName: String,
        mode: String,
        totalScenes: Int = 3,
        userId: String
    ): String {
        val jobId = "job_${UUID.randomUUID().toString().take(8)}"
        val initialJob = GenerationJobEntity(
            id = jobId,
            projectId = projectId,
            projectName = projectName,
            mode = mode,
            status = "queued",
            progress = 5,
            currentStepMessage = "Queued in DIV AI studio cluster...",
            startedAt = System.currentTimeMillis()
        )

        coroutineScope.launch(Dispatchers.IO) {
            generationJobDao.insertJob(initialJob)

            // Step 1: Queued
            delay(1200)
            updateJobAndProject(
                jobId = jobId,
                projectId = projectId,
                status = "processing",
                progress = 20,
                message = "Analyzing prompt & establishing camera keyframes..."
            )

            // Step 2: Processing Character & Scene Rigs
            delay(1800)
            updateJobAndProject(
                jobId = jobId,
                projectId = projectId,
                status = "processing",
                progress = 45,
                message = "Synthesizing character motion & lighting passes..."
            )

            // Step 3: Rendering Scenes
            for (sceneIdx in 1..totalScenes) {
                val sceneProgress = 45 + ((sceneIdx.toFloat() / totalScenes.toFloat()) * 40).toInt()
                delay(1500)
                updateJobAndProject(
                    jobId = jobId,
                    projectId = projectId,
                    status = "rendering",
                    progress = sceneProgress,
                    message = "Generating Scene $sceneIdx of $totalScenes (Optical Flow & Color Grade)..."
                )
            }

            // Step 4: Audio & Lip Sync Synthesis
            delay(1400)
            updateJobAndProject(
                jobId = jobId,
                projectId = projectId,
                status = "rendering",
                progress = 92,
                message = "Synchronizing neural voice dialogue & subtitles..."
            )

            // Step 5: Completed
            delay(1200)
            val completedJob = generationJobDao.getJobById(jobId)?.copy(
                status = "completed",
                progress = 100,
                currentStepMessage = "Animation render completed successfully!",
                completedAt = System.currentTimeMillis()
            )
            if (completedJob != null) {
                generationJobDao.updateJob(completedJob)
            }

            val project = projectDao.getProjectByIdDirect(projectId)
            if (project != null) {
                projectDao.updateProject(
                    project.copy(
                        status = "completed",
                        progress = 100,
                        currentStep = "Render finished - Ready for preview & export",
                        videoUrl = "https://divstudio.ai/renders/$projectId.mp4",
                        updatedAt = System.currentTimeMillis()
                    )
                )
            }

            notificationDao.insertNotification(
                NotificationEntity(
                    id = UUID.randomUUID().toString(),
                    userId = userId,
                    title = "Animation Render Ready!",
                    message = "Your animation '$projectName' has completed rendering.",
                    type = "generation",
                    timestamp = System.currentTimeMillis()
                )
            )
        }

        return jobId
    }

    private suspend fun updateJobAndProject(
        jobId: String,
        projectId: String,
        status: String,
        progress: Int,
        message: String
    ) {
        val job = generationJobDao.getJobById(jobId)
        if (job != null) {
            generationJobDao.updateJob(
                job.copy(status = status, progress = progress, currentStepMessage = message)
            )
        }
        val project = projectDao.getProjectByIdDirect(projectId)
        if (project != null) {
            projectDao.updateProject(
                project.copy(status = status, progress = progress, currentStep = message)
            )
        }
    }
}
