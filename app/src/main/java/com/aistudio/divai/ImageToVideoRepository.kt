package com.aistudio.divai

import android.content.Context
import android.util.Base64
import com.example.BuildConfig
import com.google.gson.JsonArray
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
import java.util.concurrent.TimeUnit

/** Real Google Veo 3.1 Fast image-to-video generation. */
class ImageToVideoRepository(
    private val context: Context,
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .build()
) {
    private val baseUrl = "https://generativelanguage.googleapis.com/v1beta"
    private val model = "veo-3.1-fast-generate-preview"

    private fun apiKey(): String {
        val saved = context.getSharedPreferences("divstudio_ai_settings", Context.MODE_PRIVATE)
            .getString("gemini_api_key", "")
            .orEmpty().trim()
        if (saved.isNotBlank()) return saved
        return try {
            BuildConfig.GEMINI_API_KEY.takeUnless { it.isBlank() || it == "MY_GEMINI_API_KEY" }
                ?.trim().orEmpty()
        } catch (_: Throwable) { "" }
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
            require(key.isNotBlank()) {
                "Gemini API key is missing. Open Profile → Settings → Real AI Generation and connect your key."
            }
            require(imageFile.exists() && imageFile.length() > 0) { "Selected image could not be read." }

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
                add("instances", JsonArray().apply { add(instance) })
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
                if (!response.isSuccessful) throw apiException(response.code, body)
                JsonParser.parseString(body).asJsonObject.get("name")?.asString
                    ?: error("Google returned no generation operation.")
            }

            onStatus("Generating video with Veo 3.1 Fast…")
            var videoUri: String? = null
            var attempts = 0
            while (videoUri == null && attempts++ < 60) {
                delay(10_000)
                val poll = Request.Builder()
                    .url("$baseUrl/$operationName")
                    .header("x-goog-api-key", key)
                    .get().build()
                val json = client.newCall(poll).execute().use { response ->
                    val body = response.body?.string().orEmpty()
                    if (!response.isSuccessful) throw apiException(response.code, body)
                    JsonParser.parseString(body).asJsonObject
                }
                if (json.get("done")?.asBoolean == true) {
                    json.getAsJsonObject("error")?.let { apiError ->
                        error("Veo generation failed: ${apiError.get("message")?.asString ?: apiError}")
                    }
                    videoUri = json.getAsJsonObject("response")
                        ?.getAsJsonObject("generateVideoResponse")
                        ?.getAsJsonArray("generatedSamples")
                        ?.firstOrNull()?.asJsonObject
                        ?.getAsJsonObject("video")?.get("uri")?.asString
                    if (videoUri == null) error("Veo finished without a video URI.")
                } else {
                    onStatus("Generating video with Veo 3.1 Fast…")
                }
            }
            require(videoUri != null) { "Veo generation timed out after 10 minutes." }

            onStatus("Downloading MP4…")
            val output = File(context.filesDir, "divstudio_${System.currentTimeMillis()}_${UUID.randomUUID()}.mp4")
            val download = Request.Builder()
                .url(videoUri!!).header("x-goog-api-key", key).get().build()
            client.newCall(download).execute().use { response ->
                if (!response.isSuccessful) throw apiException(response.code, response.body?.string().orEmpty())
                val body = response.body ?: error("Google returned an empty video response.")
                body.byteStream().use { input -> output.outputStream().use { input.copyTo(it) } }
            }
            require(output.exists() && output.length() > 0) { "Veo returned an empty video." }
            onStatus("Ready")
            output
        }
    }

    private fun apiException(code: Int, body: String): IllegalStateException {
        val lower = body.lowercase()
        val message = when {
            code == 401 || lower.contains("api key not valid") ->
                "Gemini API key is invalid or expired. Create/check the key in Google AI Studio and reconnect it in the app."
            code == 403 || lower.contains("billing") || lower.contains("paid") || lower.contains("permission") ->
                "Veo 3.1 requires an eligible paid Gemini API project. Enable billing for the Google project linked to this API key, then try again."
            code == 429 -> "Gemini/Veo rate limit reached. Wait a little and try again."
            else -> "Veo request failed ($code): ${body.take(800)}"
        }
        return IllegalStateException(message)
    }
}
