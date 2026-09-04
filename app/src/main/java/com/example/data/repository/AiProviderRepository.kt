package com.example.data.repository

import android.content.Context
import com.example.BuildConfig
import com.example.data.local.entities.SceneEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import java.util.concurrent.TimeUnit

data class ProviderStatus(
    val id: String,
    val name: String,
    val serviceType: String,
    val isConfigured: Boolean,
    val statusMessage: String,
    val configGuideLocation: String
)

class AiProviderRepository(private val context: Context) {

    private val prefs = context.getSharedPreferences("divstudio_ai_settings", Context.MODE_PRIVATE)

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    /**
     * Reads a user-configured key first, then the build-time secret injected by
     * the Secrets Gradle plugin. The actual secret value is never committed to Git.
     */
    fun getApiKey(): String {
        val saved = prefs.getString("gemini_api_key", "").orEmpty().trim()
        if (saved.isNotBlank()) return saved

        return try {
            BuildConfig.GEMINI_API_KEY
                .takeUnless { it.isBlank() || it == "MY_GEMINI_API_KEY" }
                ?: ""
        } catch (_: Exception) {
            ""
        }
    }

    fun saveApiKey(apiKey: String) {
        prefs.edit().putString("gemini_api_key", apiKey.trim()).apply()
    }

    fun clearApiKey() {
        prefs.edit().remove("gemini_api_key").apply()
    }

    fun isGeminiConfigured(): Boolean = getApiKey().isNotBlank()

    fun getProviderStatuses(): List<ProviderStatus> {
        val configured = isGeminiConfigured()
        return listOf(
            ProviderStatus(
                id = "gemini_script",
                name = "Google Gemini — Script & Storyboard AI",
                serviceType = "Text & Storyboard Generation",
                isConfigured = configured,
                statusMessage = if (configured) "Connected" else "Gemini API key not configured",
                configGuideLocation = "Settings → Gemini API Key or build secret"
            ),
            ProviderStatus(
                id = "veo_video",
                name = "Google Veo 3.1 Fast — Video Engine",
                serviceType = "Real Generative Video",
                isConfigured = configured,
                statusMessage = if (configured) "Ready for real MP4 generation" else "Gemini API key not configured",
                configGuideLocation = "Settings → Gemini API Key or build secret"
            ),
            ProviderStatus(
                id = "div_tts_lipsync",
                name = "DIV Voice & Lip Sync",
                serviceType = "Voice & Character Dialogue",
                isConfigured = true,
                statusMessage = "Studio voice layer available",
                configGuideLocation = "Built-in Studio Engine"
            )
        )
    }

    suspend fun analyzeScriptIntoScenes(projectId: String, script: String): List<SceneEntity> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isNotBlank()) {
            try {
                val prompt = """
                    You are DIVSTUDIO AI's script breakdown assistant. Analyze this animation script and divide it into sequential visual scenes.
                    Return ONLY a valid JSON array with keys: sceneNumber, title, description, characters, location, dialogue,
                    cameraDirection, characterMovement, durationSeconds.

                    Script:
                    $script
                """.trimIndent()

                val requestJson = JSONObject().apply {
                    put("contents", JSONArray().put(JSONObject().apply {
                        put("parts", JSONArray().put(JSONObject().apply { put("text", prompt) }))
                    }))
                }

                val request = Request.Builder()
                    .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey")
                    .post(requestJson.toString().toRequestBody("application/json".toMediaType()))
                    .build()

                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val root = JSONObject(response.body?.string().orEmpty())
                        val text = root.getJSONArray("candidates")
                            .getJSONObject(0).getJSONObject("content")
                            .getJSONArray("parts").getJSONObject(0).getString("text")
                        val cleanJson = text.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
                        val jsonArray = JSONArray(cleanJson)
                        val scenes = mutableListOf<SceneEntity>()
                        for (i in 0 until jsonArray.length()) {
                            val obj = jsonArray.getJSONObject(i)
                            scenes.add(
                                SceneEntity(
                                    id = UUID.randomUUID().toString(), projectId = projectId,
                                    sceneNumber = obj.optInt("sceneNumber", i + 1),
                                    title = obj.optString("title", "Scene ${i + 1}"),
                                    description = obj.optString("description", "Animation scene"),
                                    characters = obj.optString("characters", "Main Character"),
                                    location = obj.optString("location", "Animation Studio"),
                                    dialogue = obj.optString("dialogue", ""),
                                    cameraDirection = obj.optString("cameraDirection", "Smooth Zoom In"),
                                    characterMovement = obj.optString("characterMovement", "Walk Forward"),
                                    durationSeconds = obj.optInt("durationSeconds", 6),
                                    thumbnailResName = if (i % 2 == 0) "scene_lagos_sunset" else "hero_animation_art",
                                    status = "ready", orderIndex = i
                                )
                            )
                        }
                        if (scenes.isNotEmpty()) return@withContext scenes
                    }
                }
            } catch (_: Exception) {
                // Use local fallback below.
            }
        }
        fallbackScriptBreakdown(projectId, script)
    }

    private fun fallbackScriptBreakdown(projectId: String, script: String): List<SceneEntity> {
        val lines = script.split("\n").filter { it.isNotBlank() }
        if (lines.isEmpty()) {
            return listOf(
                SceneEntity(
                    id = UUID.randomUUID().toString(), projectId = projectId, sceneNumber = 1,
                    title = "Opening Sequence", description = "Dynamic animated introduction to the story.",
                    characters = "Main Character", location = "Lagos", dialogue = "",
                    cameraDirection = "Smooth Zoom In", characterMovement = "Walk Forward",
                    durationSeconds = 6, thumbnailResName = "scene_lagos_sunset", orderIndex = 0
                )
            )
        }

        return lines.take(12).mapIndexed { index, line ->
            SceneEntity(
                id = UUID.randomUUID().toString(), projectId = projectId, sceneNumber = index + 1,
                title = "Scene ${index + 1}", description = line.trim(), characters = "Main Character",
                location = "Animation World", dialogue = "", cameraDirection = if (index % 2 == 0) "Smooth Zoom In" else "Pan",
                characterMovement = if (index % 2 == 0) "Walk Forward" else "Wave Hand", durationSeconds = 6,
                thumbnailResName = if (index % 2 == 0) "scene_lagos_sunset" else "hero_animation_art", orderIndex = index
            )
        }
    }
}
