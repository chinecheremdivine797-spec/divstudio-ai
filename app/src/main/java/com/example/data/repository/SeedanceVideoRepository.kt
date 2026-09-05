package com.example.data.repository

import android.content.Context
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

/** Real BytePlus/Volcano Engine Seedance 2.5 video generation client. */
class SeedanceVideoRepository(private val context: Context) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .build()

    private val baseUrl = "https://operator.las.ap-southeast-1.bytepluses.com/api/v1/contents/generations/tasks"
    private val model = "dreamina-seedance-2-5-260628"

    private fun apiKey(): String = context
        .getSharedPreferences("divstudio_ai_settings", Context.MODE_PRIVATE)
        .getString("seedance_api_key", "")
        .orEmpty()
        .trim()

    suspend fun generateVideo(
        prompt: String,
        aspectRatio: String = "16:9",
        duration: Int = 8,
        resolution: String = "720p",
        generateAudio: Boolean = true
    ): Result<File> = withContext(Dispatchers.IO) {
        val key = apiKey()
        if (key.isBlank()) {
            return@withContext Result.failure(
                IllegalStateException("Seedance 2.5 API key is not configured. Open Profile → Settings → Seedance 2.5 and connect your BytePlus/LAS API key.")
            )
        }

        try {
            val safeRatio = when (aspectRatio) {
                "21:9", "16:9", "4:3", "1:1", "3:4", "9:16" -> aspectRatio
                else -> "16:9"
            }
            val safeDuration = duration.coerceIn(4, 30)
            val safeResolution = if (resolution == "480p") "480p" else "720p"

            val content = JSONArray().put(JSONObject().apply {
                put("type", "text")
                put("text", prompt.take(12000))
            })

            val requestJson = JSONObject().apply {
                put("model", model)
                put("content", content)
                put("ratio", safeRatio)
                put("resolution", safeResolution)
                put("duration", safeDuration)
                put("generate_audio", generateAudio)
                put("watermark", false)
            }

            val request = Request.Builder()
                .url(baseUrl)
                .addHeader("Authorization", "Bearer $key")
                .addHeader("Content-Type", "application/json")
                .post(requestJson.toString().toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    return@withContext Result.failure(
                        IllegalStateException("Seedance 2.5 request failed (${response.code}): ${body.take(1000)}")
                    )
                }

                val taskId = JSONObject(body).optString("id")
                if (taskId.isBlank()) {
                    return@withContext Result.failure(
                        IllegalStateException("Seedance 2.5 did not return a task ID.")
                    )
                }

                val videoUri = pollForVideoUri(key, taskId)
                val outputFile = File(
                    context.filesDir,
                    "divstudio_seedance_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(8)}.mp4"
                )
                downloadVideo(key, videoUri, outputFile)

                if (!outputFile.exists() || outputFile.length() == 0L) {
                    return@withContext Result.failure(
                        IllegalStateException("Seedance 2.5 completed but the downloaded MP4 is empty.")
                    )
                }
                Result.success(outputFile)
            }
        } catch (e: Exception) {
            Result.failure(IllegalStateException(e.message ?: "Seedance 2.5 video generation failed.", e))
        }
    }

    private suspend fun pollForVideoUri(apiKey: String, taskId: String): String {
        val taskUrl = "$baseUrl/$taskId"

        repeat(180) {
            val request = Request.Builder()
                .url(taskUrl)
                .addHeader("Authorization", "Bearer $apiKey")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    throw IllegalStateException("Seedance task check failed (${response.code}): ${body.take(1000)}")
                }

                val json = JSONObject(body)
                when (json.optString("status").lowercase()) {
                    "succeeded" -> {
                        val uri = json.optJSONObject("content")?.optString("video_url").orEmpty()
                        if (uri.isBlank()) throw IllegalStateException("Seedance completed without a video URL.")
                        return uri
                    }
                    "failed", "cancelled", "expired" -> {
                        val error = json.optJSONObject("error")?.optString("message").orEmpty()
                        throw IllegalStateException(
                            "Seedance 2.5 generation ${json.optString("status")}: ${error.ifBlank { "The provider rejected or expired the task." }}"
                        )
                    }
                }
            }

            delay(10_000)
        }

        throw IllegalStateException("Seedance 2.5 generation timed out while waiting for the provider.")
    }

    private fun downloadVideo(apiKey: String, uri: String, destination: File) {
        val request = Request.Builder()
            .url(uri)
            .addHeader("Authorization", "Bearer $apiKey")
            .get()
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val body = response.body?.string().orEmpty()
                throw IllegalStateException("Seedance video download failed (${response.code}): ${body.take(500)}")
            }
            val body = response.body ?: throw IllegalStateException("Seedance returned an empty video response.")
            FileOutputStream(destination).use { output ->
                body.byteStream().use { input -> input.copyTo(output) }
            }
        }
    }
}
