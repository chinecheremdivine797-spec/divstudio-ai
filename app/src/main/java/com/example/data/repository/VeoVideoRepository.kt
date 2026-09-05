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

/**
 * Real Google Veo 3.1 video generation client.
 *
 * The API returns a long-running operation. This class starts the operation,
 * polls until completion, then downloads the generated MP4 into app storage.
 * No fake progress or placeholder video URL is returned.
 */
class VeoVideoRepository(private val context: Context) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .build()

    /**
     * Prefer the Gemini key configured by the user inside DIVSTUDIO AI.
     * Fall back to the build-time key only when one was supplied.
     */
    private fun apiKey(): String {
        val saved = context.getSharedPreferences("divstudio_ai_settings", Context.MODE_PRIVATE)
            .getString("gemini_api_key", "")
            .orEmpty()
            .trim()
        if (saved.isNotBlank()) return saved

        return try {
            BuildConfig.GEMINI_API_KEY
                .takeUnless { it.isBlank() || it == "MY_GEMINI_API_KEY" }
                ?.trim()
                .orEmpty()
        } catch (_: Throwable) {
            ""
        }
    }

    suspend fun generateVideo(
        prompt: String,
        aspectRatio: String = "16:9",
        resolution: String = "720p"
    ): Result<File> = withContext(Dispatchers.IO) {
        val key = apiKey()
        if (key.isBlank()) {
            return@withContext Result.failure(
                IllegalStateException("Gemini API key is not configured. Open Profile → Settings → Real AI Generation and connect your Gemini API key.")
            )
        }

        try {
            val safeRatio = if (aspectRatio == "9:16") "9:16" else "16:9"
            val safeResolution = when (resolution) {
                "1080p" -> "1080p"
                "4k" -> "4k"
                else -> "720p"
            }

            val requestJson = JSONObject().apply {
                put("instances", JSONArray().put(JSONObject().apply {
                    put("prompt", prompt.take(10000))
                }))
                put("parameters", JSONObject().apply {
                    put("aspectRatio", safeRatio)
                    put("resolution", safeResolution)
                    put("numberOfVideos", 1)
                })
            }

            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/veo-3.1-generate-preview:predictLongRunning")
                .addHeader("x-goog-api-key", key)
                .addHeader("Content-Type", "application/json")
                .post(requestJson.toString().toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    return@withContext Result.failure(
                        IllegalStateException("Veo request failed (${response.code}): ${body.take(800)}")
                    )
                }

                val operationName = JSONObject(body).optString("name")
                if (operationName.isBlank()) {
                    return@withContext Result.failure(
                        IllegalStateException("Veo did not return a generation operation.")
                    )
                }

                val videoUri = pollForVideoUri(key, operationName)
                val outputFile = File(
                    context.filesDir,
                    "divstudio_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(8)}.mp4"
                )

                downloadVideo(key, videoUri, outputFile)
                if (!outputFile.exists() || outputFile.length() == 0L) {
                    return@withContext Result.failure(
                        IllegalStateException("Veo completed but the downloaded MP4 is empty.")
                    )
                }

                Result.success(outputFile)
            }
        } catch (e: Exception) {
            Result.failure(
                IllegalStateException(e.message ?: "Veo video generation failed.", e)
            )
        }
    }

    private fun pollForVideoUri(apiKey: String, operationName: String): String {
        val operationUrl = "https://generativelanguage.googleapis.com/v1beta/$operationName"

        repeat(60) {
            val request = Request.Builder()
                .url(operationUrl)
                .addHeader("x-goog-api-key", apiKey)
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    throw IllegalStateException("Veo operation check failed (${response.code}): ${body.take(800)}")
                }

                val json = JSONObject(body)
                if (json.optBoolean("done", false)) {
                    if (json.has("error")) {
                        throw IllegalStateException(
                            "Veo generation failed: ${json.optJSONObject("error")?.optString("message") ?: "Unknown error"}"
                        )
                    }

                    val uri = json.optJSONObject("response")
                        ?.optJSONObject("generateVideoResponse")
                        ?.optJSONArray("generatedSamples")
                        ?.optJSONObject(0)
                        ?.optJSONObject("video")
                        ?.optString("uri")
                        .orEmpty()

                    if (uri.isBlank()) {
                        throw IllegalStateException("Veo finished without a video URI.")
                    }
                    return uri
                }
            }

            Thread.sleep(10_000)
        }

        throw IllegalStateException("Veo generation timed out while waiting for the operation to complete.")
    }

    private fun downloadVideo(apiKey: String, uri: String, destination: File) {
        val request = Request.Builder()
            .url(uri)
            .addHeader("x-goog-api-key", apiKey)
            .get()
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val body = response.body?.string().orEmpty()
                throw IllegalStateException("Video download failed (${response.code}): ${body.take(500)}")
            }

            val body = response.body ?: throw IllegalStateException("Veo returned an empty video response.")
            FileOutputStream(destination).use { output ->
                body.byteStream().use { input ->
                    input.copyTo(output)
                }
            }
        }
    }
}
