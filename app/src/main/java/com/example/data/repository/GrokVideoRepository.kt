package com.example.data.repository

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import java.util.concurrent.TimeUnit

/** Real xAI Grok Imagine Video 1.5 client. */
class GrokVideoRepository(private val context: Context) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val baseUrl = "https://api.x.ai/v1"
    private val model = "grok-imagine-video-1.5"

    private fun apiKey(): String = context
        .getSharedPreferences("divstudio_ai_settings", Context.MODE_PRIVATE)
        .getString("grok_api_key", "")
        .orEmpty()
        .trim()

    suspend fun generateVideo(
        prompt: String,
        aspectRatio: String = "16:9",
        duration: Int = 6,
        resolution: String = "720p",
        imageUrl: String? = null
    ): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            val key = apiKey()
            require(key.isNotBlank()) { "Grok API key is missing. Open Profile → Settings → Grok Imagine and connect your key." }
            val payload = JSONObject().apply {
                put("model", model)
                put("prompt", prompt.take(10000))
                put("duration", duration.coerceIn(1, 15))
                put("aspect_ratio", if (aspectRatio in setOf("16:9", "9:16", "1:1", "4:3", "3:4", "21:9")) aspectRatio else "16:9")
                put("resolution", if (resolution in setOf("480p", "720p", "1080p")) resolution else "720p")
                put("generate_audio", true)
                imageUrl?.takeIf { it.isNotBlank() }?.let { put("image", JSONObject().put("url", it)) }
            }
            val start = Request.Builder()
                .url("$baseUrl/videos/generations")
                .header("Authorization", "Bearer $key")
                .header("Content-Type", "application/json")
                .post(payload.toString().toRequestBody("application/json".toMediaType()))
                .build()
            val requestId = client.newCall(start).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (!response.isSuccessful) throw apiException(response.code, body)
                JSONObject(body).optString("request_id").takeIf { it.isNotBlank() }
                    ?: error("Grok returned no generation request ID.")
            }

            var videoUrl: String? = null
            repeat(120) {
                delay(5_000)
                val poll = Request.Builder()
                    .url("$baseUrl/videos/$requestId")
                    .header("Authorization", "Bearer $key")
                    .get().build()
                client.newCall(poll).execute().use { response ->
                    val body = response.body?.string().orEmpty()
                    if (!response.isSuccessful) throw apiException(response.code, body)
                    val json = JSONObject(body)
                    when (json.optString("status")) {
                        "done" -> videoUrl = json.optJSONObject("video")?.optString("url")?.takeIf { it.isNotBlank() }
                        "failed", "expired" -> error("Grok video generation ${json.optString("status")}: ${json.optString("error", "No additional details")}")
                    }
                }
                if (videoUrl != null) return@repeat
            }

            val url = videoUrl ?: error("Grok generation timed out after 10 minutes.")
            val output = File(context.filesDir, "divstudio_grok_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(8)}.mp4")
            client.newCall(Request.Builder().url(url).get().build()).execute().use { response ->
                if (!response.isSuccessful) throw apiException(response.code, response.body?.string().orEmpty())
                val body = response.body ?: error("Grok returned an empty video response.")
                FileOutputStream(output).use { out -> body.byteStream().use { it.copyTo(out) } }
            }
            require(output.exists() && output.length() > 0) { "Grok returned an empty MP4." }
            output
        }
    }

    private fun apiException(code: Int, body: String): IllegalStateException {
        val message = when (code) {
            401 -> "Grok API key is invalid or expired. Reconnect it in DIVSTUDIO AI settings."
            403 -> "Grok API access is not permitted for this key or account."
            429 -> "Grok rate limit reached. Wait and try again."
            else -> "Grok request failed ($code): ${body.take(800)}"
        }
        return IllegalStateException(message)
    }
}
