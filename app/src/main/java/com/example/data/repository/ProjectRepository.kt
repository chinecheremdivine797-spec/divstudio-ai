package com.example.data.repository

import com.example.data.local.dao.ProjectDao
import com.example.data.local.dao.SceneDao
import com.example.data.local.entities.ProjectEntity
import com.example.data.local.entities.SceneEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class ProjectRepository(
    private val projectDao: ProjectDao,
    private val sceneDao: SceneDao
) {

    fun getProjectsForUser(userId: String): Flow<List<ProjectEntity>> =
        projectDao.getProjectsForUser(userId)

    fun getAllProjects(): Flow<List<ProjectEntity>> =
        projectDao.getAllProjects()

    fun getProjectById(projectId: String): Flow<ProjectEntity?> =
        projectDao.getProjectById(projectId)

    suspend fun getProjectByIdDirect(projectId: String): ProjectEntity? =
        projectDao.getProjectByIdDirect(projectId)

    fun getScenesForProject(projectId: String): Flow<List<SceneEntity>> =
        sceneDao.getScenesForProject(projectId)

    suspend fun createProjectWithScenes(
        project: ProjectEntity,
        scenes: List<SceneEntity>
    ) {
        projectDao.insertProject(project)
        if (scenes.isNotEmpty()) {
            sceneDao.insertScenes(scenes)
        }
    }

    suspend fun updateProject(project: ProjectEntity) {
        projectDao.updateProject(project.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteProject(projectId: String) {
        sceneDao.deleteScenesForProject(projectId)
        projectDao.deleteProjectById(projectId)
    }

    suspend fun duplicateProject(project: ProjectEntity): String {
        val newProjectId = "proj_${UUID.randomUUID().toString().take(8)}"
        val duplicatedProject = project.copy(
            id = newProjectId,
            name = "${project.name} (Copy)",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        projectDao.insertProject(duplicatedProject)

        val existingScenes = sceneDao.getScenesForProjectDirect(project.id)
        val duplicatedScenes = existingScenes.map { scene ->
            scene.copy(
                id = "scene_${UUID.randomUUID().toString().take(8)}",
                projectId = newProjectId
            )
        }
        if (duplicatedScenes.isNotEmpty()) {
            sceneDao.insertScenes(duplicatedScenes)
        }
        return newProjectId
    }

    suspend fun updateScene(scene: SceneEntity) {
        sceneDao.updateScene(scene)
    }

    suspend fun addScene(scene: SceneEntity) {
        sceneDao.insertScene(scene)
    }

    suspend fun deleteScene(scene: SceneEntity) {
        sceneDao.deleteScene(scene)
    }

    suspend fun reorderScenes(projectId: String, scenes: List<SceneEntity>) {
        scenes.forEachIndexed { index, scene ->
            sceneDao.updateScene(scene.copy(orderIndex = index, sceneNumber = index + 1))
        }
    }

    suspend fun getTotalProjectsCount(): Int = projectDao.getTotalProjectCount()

    suspend fun getCompletedProjectsCount(): Int = projectDao.getCompletedProjectCount()
}
