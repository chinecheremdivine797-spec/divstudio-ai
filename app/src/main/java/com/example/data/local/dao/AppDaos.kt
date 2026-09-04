package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entities.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    fun getUserById(userId: String): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int
}

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects WHERE userId = :userId ORDER BY updatedAt DESC")
    fun getProjectsForUser(userId: String): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE id = :projectId LIMIT 1")
    fun getProjectById(projectId: String): Flow<ProjectEntity?>

    @Query("SELECT * FROM projects WHERE id = :projectId LIMIT 1")
    suspend fun getProjectByIdDirect(projectId: String): ProjectEntity?

    @Query("SELECT * FROM projects ORDER BY updatedAt DESC")
    fun getAllProjects(): Flow<List<ProjectEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ProjectEntity)

    @Update
    suspend fun updateProject(project: ProjectEntity)

    @Delete
    suspend fun deleteProject(project: ProjectEntity)

    @Query("DELETE FROM projects WHERE id = :projectId")
    suspend fun deleteProjectById(projectId: String)

    @Query("SELECT COUNT(*) FROM projects")
    suspend fun getTotalProjectCount(): Int

    @Query("SELECT COUNT(*) FROM projects WHERE status = 'completed'")
    suspend fun getCompletedProjectCount(): Int
}

@Dao
interface SceneDao {
    @Query("SELECT * FROM scenes WHERE projectId = :projectId ORDER BY orderIndex ASC")
    fun getScenesForProject(projectId: String): Flow<List<SceneEntity>>

    @Query("SELECT * FROM scenes WHERE projectId = :projectId ORDER BY orderIndex ASC")
    suspend fun getScenesForProjectDirect(projectId: String): List<SceneEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScene(scene: SceneEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScenes(scenes: List<SceneEntity>)

    @Update
    suspend fun updateScene(scene: SceneEntity)

    @Delete
    suspend fun deleteScene(scene: SceneEntity)

    @Query("DELETE FROM scenes WHERE projectId = :projectId")
    suspend fun deleteScenesForProject(projectId: String)
}

@Dao
interface CharacterDao {
    @Query("SELECT * FROM characters WHERE userId = :userId ORDER BY createdAt DESC")
    fun getCharactersForUser(userId: String): Flow<List<CharacterEntity>>

    @Query("SELECT * FROM characters WHERE id = :characterId LIMIT 1")
    suspend fun getCharacterById(characterId: String): CharacterEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCharacter(character: CharacterEntity)

    @Update
    suspend fun updateCharacter(character: CharacterEntity)

    @Delete
    suspend fun deleteCharacter(character: CharacterEntity)

    @Query("DELETE FROM characters WHERE id = :characterId")
    suspend fun deleteCharacterById(characterId: String)
}

@Dao
interface SceneTemplateDao {
    @Query("SELECT * FROM scene_templates WHERE userId = :userId ORDER BY createdAt DESC")
    fun getTemplatesForUser(userId: String): Flow<List<SceneTemplateEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplate(template: SceneTemplateEntity)

    @Delete
    suspend fun deleteTemplate(template: SceneTemplateEntity)
}

@Dao
interface GenerationJobDao {
    @Query("SELECT * FROM generation_jobs ORDER BY startedAt DESC")
    fun getAllJobs(): Flow<List<GenerationJobEntity>>

    @Query("SELECT * FROM generation_jobs WHERE status IN ('queued', 'processing', 'rendering') ORDER BY startedAt DESC")
    fun getActiveJobs(): Flow<List<GenerationJobEntity>>

    @Query("SELECT * FROM generation_jobs WHERE id = :jobId LIMIT 1")
    suspend fun getJobById(jobId: String): GenerationJobEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJob(job: GenerationJobEntity)

    @Update
    suspend fun updateJob(job: GenerationJobEntity)

    @Query("SELECT COUNT(*) FROM generation_jobs")
    suspend fun getTotalGenerationsCount(): Int
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY timestamp DESC")
    fun getNotificationsForUser(userId: String): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Query("UPDATE notifications SET isRead = 1 WHERE userId = :userId")
    suspend fun markAllAsRead(userId: String)

    @Query("SELECT COUNT(*) FROM notifications WHERE userId = :userId AND isRead = 0")
    fun getUnreadCount(userId: String): Flow<Int>
}

@Dao
interface AdminLogDao {
    @Query("SELECT * FROM admin_logs ORDER BY timestamp DESC LIMIT 50")
    fun getAllLogs(): Flow<List<AdminLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: AdminLogEntity)
}
