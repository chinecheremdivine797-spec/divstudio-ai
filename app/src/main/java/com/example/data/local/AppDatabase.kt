package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.*
import com.example.data.local.entities.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        ProjectEntity::class,
        SceneEntity::class,
        CharacterEntity::class,
        SceneTemplateEntity::class,
        GenerationJobEntity::class,
        NotificationEntity::class,
        AdminLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun projectDao(): ProjectDao
    abstract fun sceneDao(): SceneDao
    abstract fun characterDao(): CharacterDao
    abstract fun sceneTemplateDao(): SceneTemplateDao
    abstract fun generationJobDao(): GenerationJobDao
    abstract fun notificationDao(): NotificationDao
    abstract fun adminLogDao(): AdminLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "div_ai_database"
                )
                    .addCallback(AppDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class AppDatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database)
                    }
                }
            }
        }

        private suspend fun populateInitialData(db: AppDatabase) {
            val defaultUser = UserEntity(
                id = "user_default_01",
                email = "creator@divstudio.ai",
                fullName = "David Adeleke",
                role = "user",
                avatarUrl = "",
                defaultStyle = "African Animation",
                defaultRatio = "16:9",
                defaultVoice = "Amaka",
                creditsRemaining = 120,
                planName = "Studio Creator Pro"
            )

            val adminUser = UserEntity(
                id = "user_admin_01",
                email = "divstudio03@gmail.com",
                fullName = "DIVSTUDIO Admin",
                role = "admin",
                avatarUrl = "",
                defaultStyle = "Cinematic",
                defaultRatio = "16:9",
                defaultVoice = "Chidi",
                creditsRemaining = 9999,
                planName = "Executive Studio Enterprise"
            )

            db.userDao().insertUser(defaultUser)
            db.userDao().insertUser(adminUser)

            val charDavid = CharacterEntity(
                id = "char_david_01",
                userId = "user_default_01",
                name = "David",
                age = "19",
                gender = "Male",
                skinTone = "Warm Melanin Bronze",
                hair = "Clean Fade Afro",
                clothing = "Futuristic yellow hoodie with smart cyber headphones",
                bodyType = "Athletic & expressive",
                personality = "Brilliant animator, quick-witted, adventurous",
                voice = "Chidi",
                animationStyle = "African Animation",
                description = "David traverses the vibrant metropolis of Lagos crafting virtual reality animations.",
                avatarResName = "character_david"
            )

            val charAmaka = CharacterEntity(
                id = "char_amaka_02",
                userId = "user_default_01",
                name = "Amaka",
                age = "21",
                gender = "Female",
                skinTone = "Deep Mahogany Glow",
                hair = "Braided crown with neon beads",
                clothing = "Modern Ankara bomber jacket & cybernetic goggles",
                bodyType = "Graceful & bold",
                personality = "Visionary tech lead, confident, loyal",
                voice = "Amaka",
                animationStyle = "African Animation",
                description = "Amaka leads the sound synthesis team at Neo-Lagos creative hub.",
                avatarResName = "hero_animation_art"
            )

            db.characterDao().insertCharacter(charDavid)
            db.characterDao().insertCharacter(charAmaka)

            val sceneLagos = SceneTemplateEntity(
                id = "scene_temp_01",
                userId = "user_default_01",
                name = "Lagos Sunset Lekki Bridge",
                location = "Lagos, Nigeria",
                timeOfDay = "Golden Hour Sunset",
                weather = "Warm Tropical Breeze",
                description = "Gleaming suspension bridge overlooking sparkling lagoon waters with animated yellow Danfo buses.",
                artStyle = "African Animation",
                previewResName = "scene_lagos_sunset"
            )

            val sceneVillage = SceneTemplateEntity(
                id = "scene_temp_02",
                userId = "user_default_01",
                name = "Enchanted Baobab Village",
                location = "Enugu Heartland",
                timeOfDay = "Starlit Twilight",
                weather = "Ethereal Bioluminescent Fireflies",
                description = "Ancient monumental baobab tree surrounded by circular clay architecture glowing with ancestral symbols.",
                artStyle = "African Animation",
                previewResName = "hero_animation_art"
            )

            db.sceneTemplateDao().insertTemplate(sceneLagos)
            db.sceneTemplateDao().insertTemplate(sceneVillage)

            val sampleProject = ProjectEntity(
                id = "proj_demo_01",
                userId = "user_default_01",
                name = "Lagos 2099: Flight of David",
                description = "A young Nigerian boy walks through Lagos at sunset while talking to his friend about flying cars.",
                mode = "text",
                status = "completed",
                progress = 100,
                currentStep = "Export ready",
                durationSeconds = 18,
                aspectRatio = "16:9",
                resolution = "1080p",
                style = "African Animation",
                cameraMovement = "Drone Orbit",
                characterMovement = "Walk Forward",
                environment = "Lagos sunset",
                voiceName = "Amaka",
                voiceLanguage = "Nigerian English",
                scriptText = "David looks out at the sunset over Lagos island. 'One day, our animations will fly across the sky!' Amaka laughs and replies, 'They already are!'",
                thumbnailResName = "scene_lagos_sunset",
                videoUrl = "https://divstudio.ai/renders/lagos_2099.mp4",
                shareLink = "https://div.ai/share/lg-2099-flight",
                isPublic = true,
                totalScenes = 3,
                subtitleText = "[00:01] Look at that sunset over Lagos island.\n[00:06] One day, our animations will fly across the sky!\n[00:12] They already are, David."
            )

            db.projectDao().insertProject(sampleProject)

            val scene1 = SceneEntity(
                id = "scene_p1_01",
                projectId = "proj_demo_01",
                sceneNumber = 1,
                title = "Golden Hour on Lekki Bridge",
                description = "David walks forward as the sun sets over Lagos horizon.",
                characters = "David",
                location = "Lekki-Ikoyi Link Bridge",
                dialogue = "Look at that sunset over Lagos island.",
                cameraDirection = "Smooth Zoom In",
                characterMovement = "Walk Forward",
                durationSeconds = 6,
                thumbnailResName = "scene_lagos_sunset",
                status = "completed",
                orderIndex = 0
            )

            val scene2 = SceneEntity(
                id = "scene_p1_02",
                projectId = "proj_demo_01",
                sceneNumber = 2,
                title = "The Dream of Flight",
                description = "David turns toward the camera, pointing toward solar-powered sky taxis.",
                characters = "David, Amaka",
                location = "Marina Waterfront Promenade",
                dialogue = "One day, our animations will fly across the sky!",
                cameraDirection = "Drone Orbit",
                characterMovement = "Wave Hand",
                durationSeconds = 6,
                thumbnailResName = "hero_animation_art",
                status = "completed",
                orderIndex = 1
            )

            val scene3 = SceneEntity(
                id = "scene_p1_03",
                projectId = "proj_demo_01",
                sceneNumber = 3,
                title = "Creative Triumph",
                description = "Amaka smiles in approval with holographic art tablets glowing.",
                characters = "Amaka",
                location = "Studio Rooftop",
                dialogue = "They already are, David.",
                cameraDirection = "Pan Right",
                characterMovement = "Talk & Explain",
                durationSeconds = 6,
                thumbnailResName = "character_david",
                status = "completed",
                orderIndex = 2
            )

            db.sceneDao().insertScenes(listOf(scene1, scene2, scene3))

            val welcomeNotif = NotificationEntity(
                id = "notif_01",
                userId = "user_default_01",
                title = "Welcome to DIV AI Studio!",
                message = "Your studio workspace is ready. You have 120 AI rendering credits loaded.",
                type = "system",
                timestamp = System.currentTimeMillis()
            )
            val readyNotif = NotificationEntity(
                id = "notif_02",
                userId = "user_default_01",
                title = "Animation Render Ready",
                message = "Project 'Lagos 2099: Flight of David' has finished rendering in 1080p.",
                type = "generation",
                timestamp = System.currentTimeMillis() - 3600000L
            )
            db.notificationDao().insertNotification(welcomeNotif)
            db.notificationDao().insertNotification(readyNotif)

            val initLog = AdminLogEntity(
                id = "log_01",
                adminEmail = "system@divstudio.ai",
                action = "SYSTEM_INITIALIZE",
                target = "DIV AI Studio Engine",
                details = "Platform initialized with Room Local Persistence and AI Provider Architecture"
            )
            db.adminLogDao().insertLog(initLog)
        }
    }
}
