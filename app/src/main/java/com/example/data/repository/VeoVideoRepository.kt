package com.example.data.repository

import android.content.Context
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import java.util.concurrent.TimeUnit

/** Real Google Veo 3.1 Fast text-to-video client. */
class VeoVideoRepository(private val context: Context) {
    private val client = OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS).readTimeout(120, TimeUnit.SECONDS).writeTimeout(120, TimeUnit.SECONDS).build()
    private val baseUrl = "https://generativelanguage.googleapis.com/v1beta"
    private val model = "veo-3.1-fast-generate-preview"

    private fun apiKey(): String {
        val saved = context.getSharedPreferences("divstudio_ai_settings", Context.MODE_PRIVATE).getString("gemini_api_key", "").orEmpty().trim()
        if (saved.isNotBlank()) return saved
        return try { BuildConfig.GEMINI_API_KEY.takeUnless { it.isBlank() || it == "MY_GEMINI_API_KEY" }?.trim().orEmpty() } catch (_: Throwable) { "" }
    }

    suspend fun generateVideo(prompt: String, aspectRatio: String = "16:9", resolution: String = "720p"): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            val key = apiKey()
            require(key.isNotBlank()) { "Gemini API key is missing. Open Profile → Settings → Real AI Generation and connect your key." }
            val payload = JSONObject().apply {
                put("instances", JSONArray().put(JSONObject().apply { put("prompt", prompt.take(10000)) }))
                put("parameters", JSONObject().apply {
                    put("aspectRatio", if (aspectRatio == "9:16") "9:16" else "16:9")
                    put("resolution", if (resolution == "1080p") "1080p" else "720p")
                    put("numberOfVideos", 1)
                })
            }
            val request = Request.Builder().url("$baseUrl/models/$model:predictLongRunning").header("x-goog-api-key", key).header("Content-Type", "application/json").post(payload.toString().toRequestBody("application/json".toMediaType())).build()
            val operationName = client.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (!response.isSuccessful) throw apiException(response.code, body)
                JSONObject(body).optString("name").takeIf { it.isNotBlank() } ?: error("Google returned no generation operation.")
            }
            var videoUri: String? = null
            repeat(60) {
                if (videoUri == null) {
                    delay(10_000)
                    val poll = Request.Builder().url("$baseUrl/$operationName").header("x-goog-api-key", key).get().build()
                    client.newCall(poll).execute().use { response ->
                        val body = response.body?.string().orEmpty()
                        if (!response.isSuccessful) throw apiException(response.code, body)
                        val json = JSONObject(body)
                        if (json.optBoolean("done", false)) {
                            json.optJSONObject("error")?.let { error("Veo generation failed: ${it.optString("message", it.toString())}") }
                            videoUri = json.optJSONObject("response")?.optJSONObject("generateVideoResponse")?.optJSONArray("generatedSamples")?.optJSONObject(0)?.optJSONObject("video")?.optString("uri")?.takeIf { it.isNotBlank() }
                            if (videoUri == null) error("Veo finished without a video URI.")
                        }
                    }
                }
            }
            val uri = videoUri ?: error("Veo generation timed out after 10 minutes.")
            val output = File(context.filesDir, "divstudio_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(8)}.mp4")
            client.newCall(Request.Builder().url(uri).header("x-goog-api-key", key).get().build()).execute().use { response ->
                if (!response.isSuccessful) throw apiException(response.code, response.body?.string().orEmpty())
                val body = response.body ?: error("Google returned an empty video response.")
                FileOutputStream(output).use { out -> body.byteStream().use { it.copyTo(out) } }
            }
            require(output.exists() && output.length() > 0) { "Veo returned an empty MP4." }
            output
        }
    }

    private fun apiException(code: Int, body: String): IllegalStateException {
        val lower = body.lowercase()
        val message = when {
            code == 401 || lower.contains("api key not valid") -> "Gemini API key is invalid or expired. Reconnect a valid key in the app."
            code == 403 || lower.contains("billing") || lower.contains("paid") || lower.contains("permission") -> "Veo 3.1 is not available on the free Gemini API tier. Enable billing on the Google project linked to this key, then try again."
            code == 429 -> "Gemini/Veo rate limit reached. Wait and try again."
            else -> "Veo request failed ($code): ${body.take(800)}"
        }
        return IllegalStateException(message)
    }
}
