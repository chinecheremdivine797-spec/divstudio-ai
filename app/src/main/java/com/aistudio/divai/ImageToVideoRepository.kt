package com.aistudio.divai

import android.content.Context
import android.util.Base64
import com.example.BuildConfig
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.util.UUID

/** Real Google Veo image-to-video generation. No simulated progress. */
class ImageToVideoRepository(
    private val context: Context,
    private val client: OkHttpClient = OkHttpClient()
) {
    private val baseUrl = "https://generativelanguage.googleapis.com/v1beta"
    private val model = "veo-3.1-generate-preview"

    private fun apiKey(): String {
        val saved = context.getSharedPreferences("divstudio_ai_settings", Context.MODE_PRIVATE)
            .getString("gemini_api_key", "")
            .orEmpty()
            .trim()
        if (saved.isNotBlank()) return saved
        return try {
            BuildConfig.GEMINI_API_KEY.takeUnless { it.isBlank() || it == "MY_GEMINI_API_KEY" }.orEmpty()
        } catch (_: Throwable) {
            ""
        }
    }

    suspend fun generateVideoFromImage(
        imageFile: File,
        prompt: String,
        aspectRatio: String = "16:9",
        resolution: String = "720p",
        onStatus: (String) -> Unit = {}
    ): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            val key = apiKey()
            require(key.isNotBlank()) { "GEMINI_API_KEY is not configured. Open Profile → Settings → Real AI Generation and connect your Gemini API key." }
            require(imageFile.exists()) { "Selected image could not be found." }
            require(imageFile.length() > 0) { "Selected image is empty." }

            onStatus("Uploading image…")
            val mime = when (imageFile.extension.lowercase()) {
                "jpg", "jpeg" -> "image/jpeg"
                "webp" -> "image/webp"
                else -> "image/png"
            }
            val encoded = Base64.encodeToString(imageFile.readBytes(), Base64.NO_WRAP)

            val instance = JsonObject().apply {
                addProperty("prompt", prompt.take(10000))
                add("image", JsonObject().apply {
                    add("inlineData", JsonObject().apply {
                        addProperty("mimeType", mime)
                        addProperty("data", encoded)
                    })
                })
            }
            val parameters = JsonObject().apply {
                addProperty("aspectRatio", if (aspectRatio == "9:16") "9:16" else "16:9")
                addProperty("resolution", if (resolution == "1080p") "1080p" else "720p")
                addProperty("numberOfVideos", 1)
            }
            val payload = JsonObject().apply {
                add("instances", com.google.gson.JsonArray().apply { add(instance) })
                add("parameters", parameters)
            }

            val request = Request.Builder()
                .url("$baseUrl/models/$model:predictLongRunning")
                .header("x-goog-api-key", key)
                .header("Content-Type", "application/json")
                .post(payload.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val operationName = client.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (!response.isSuccessful) error("Veo request failed (${response.code}): ${body.take(800)}")
                JsonParser.parseString(body).asJsonObject.get("name")?.asString
                    ?: error("Veo did not return an operation name.")
            }

            onStatus("Generating video…")
            var videoUri: String? = null
            var attempts = 0
            while (videoUri == null && attempts++ < 60) {
                delay(10_000)
                val poll = Request.Builder()
                    .url("$baseUrl/$operationName")
                    .header("x-goog-api-key", key)
                    .get()
                    .build()
                val json = client.newCall(poll).execute().use { response ->
                    val body = response.body?.string().orEmpty()
                    if (!response.isSuccessful) error("Veo polling failed (${response.code}): ${body.take(800)}")
                    JsonParser.parseString(body).asJsonObject
                }
                if (json.get("done")?.asBoolean == true) {
                    val apiError = json.getAsJsonObject("error")
                    if (apiError != null) error("Veo generation failed: ${apiError}")
                    json.getAsJsonObject("response")?.let { response ->
                        videoUri = response.getAsJsonObject("generateVideoResponse")
                            ?.getAsJsonArray("generatedSamples")
                            ?.firstOrNull()?.asJsonObject
                            ?.getAsJsonObject("video")?.get("uri")?.asString
                    }
                    if (videoUri == null) error("Veo finished without a video URI.")
                } else {
                    onStatus("Generating video…")
                }
            }
            require(videoUri != null) { "Veo generation timed out after 10 minutes." }

            onStatus("Downloading MP4…")
            val output = File(context.filesDir, "divstudio_${System.currentTimeMillis()}_${UUID.randomUUID()}.mp4")
            val download = Request.Builder()
                .url(videoUri!!)
                .header("x-goog-api-key", key)
                .get()
                .build()
            client.newCall(download).execute().use { response ->
                if (!response.isSuccessful) error("Video download failed (${response.code}).")
                response.body?.byteStream()?.use { input -> output.outputStream().use { input.copyTo(it) } }
            }
            require(output.exists() && output.length() > 0) { "Veo returned an empty video." }
            onStatus("Ready")
            output
        }
    }
}
