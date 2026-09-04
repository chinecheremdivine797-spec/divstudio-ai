package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val email: String,
    val fullName: String,
    val role: String = "user", // "user" or "admin"
    val avatarUrl: String = "",
    val defaultStyle: String = "African Animation",
    val defaultRatio: String = "16:9",
    val defaultVoice: String = "Amaka",
    val creditsRemaining: Int = 100,
    val planName: String = "Studio Creator Pro",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val name: String,
    val description: String,
    val mode: String = "text", // "text", "image", "script", "character", "scene"
    val status: String = "draft", // "draft", "queued", "processing", "rendering", "completed", "failed"
    val progress: Int = 0,
    val currentStep: String = "Ready to produce",
    val durationSeconds: Int = 15,
    val aspectRatio: String = "16:9",
    val resolution: String = "1080p",
    val style: String = "African Animation",
    val cameraMovement: String = "Zoom In",
    val characterMovement: String = "Walk Forward",
    val environment: String = "Lagos sunset",
    val voiceName: String = "Amaka",
    val voiceLanguage: String = "Nigerian English",
    val scriptText: String = "",
    val sourceImageUrl: String = "",
    val thumbnailResName: String = "hero_animation_art",
    val videoUrl: String = "",
    val shareLink: String = "",
    val isPublic: Boolean = false,
    val totalScenes: Int = 1,
    val subtitleText: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "scenes")
data class SceneEntity(
    @PrimaryKey val id: String,
    val projectId: String,
    val sceneNumber: Int,
    val title: String,
    val description: String,
    val characters: String,
    val location: String,
    val dialogue: String,
    val cameraDirection: String = "Zoom In",
    val characterMovement: String = "Walk Forward",
    val durationSeconds: Int = 5,
    val thumbnailResName: String = "scene_lagos_sunset",
    val videoUrl: String = "",
    val status: String = "completed",
    val orderIndex: Int = 0
)

@Entity(tableName = "characters")
data class CharacterEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val name: String,
    val age: String = "Young Adult",
    val gender: String = "Male",
    val skinTone: String = "Deep Melanin Warm",
    val hair: String = "Fade Afro with neat parting",
    val clothing: String = "Yellow hoodie with modern Ankara trim & headphones",
    val bodyType: String = "Athletic",
    val personality: String = "Optimistic, charismatic tech creator",
    val voice: String = "Chidi",
    val animationStyle: String = "African Animation",
    val description: String = "David, a young Nigerian animator exploring modern Lagos.",
    val avatarResName: String = "character_david",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "scene_templates")
data class SceneTemplateEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val name: String,
    val location: String,
    val timeOfDay: String = "Sunset",
    val weather: String = "Clear Golden",
    val description: String,
    val artStyle: String = "African Animation",
    val previewResName: String = "scene_lagos_sunset",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "generation_jobs")
data class GenerationJobEntity(
    @PrimaryKey val id: String,
    val projectId: String,
    val projectName: String,
    val mode: String,
    val status: String = "processing", // queued, processing, rendering, completed, failed
    val progress: Int = 0,
    val currentStepMessage: String = "Initializing AI generation pipeline...",
    val startedAt: Long = System.currentTimeMillis(),
    val completedAt: Long = 0L,
    val errorMessage: String = ""
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val title: String,
    val message: String,
    val type: String = "generation", // generation, system, credits
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

@Entity(tableName = "admin_logs")
data class AdminLogEntity(
    @PrimaryKey val id: String,
    val adminEmail: String,
    val action: String,
    val target: String,
    val details: String,
    val timestamp: Long = System.currentTimeMillis()
)
