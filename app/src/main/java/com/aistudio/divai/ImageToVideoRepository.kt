package com.aistudio.divai

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

/** Real Veo image-to-video generation. No simulated progress. */
class ImageToVideoRepository(
    private val client: OkHttpClient = OkHttpClient()
) {
    private val baseUrl = "https://generativelanguage.googleapis.com/v1beta"
    private val model = "veo-3.1-generate-preview"

    suspend fun generateVideoFromImage(
        imageFile: File,
        prompt: String,
        aspectRatio: String = "16:9",
        resolution: String = "720p",
        onStatus: (String) -> Unit = {}
    ): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            val apiKey = BuildConfig.GEMINI_API_KEY
            require(apiKey.isNotBlank()) { "GEMINI_API_KEY is not configured." }
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
                addProperty("prompt", prompt)
                add("image", JsonObject().apply {
                    add("inlineData", JsonObject().apply {
                        addProperty("mimeType", mime)
                        addProperty("data", encoded)
                    })
                })
            }
            val parameters = JsonObject().apply {
                addProperty("aspectRatio", aspectRatio)
                addProperty("resolution", resolution)
                addProperty("numberOfVideos", 1)
            }
            val payload = JsonObject().apply {
                add("instances", com.google.gson.JsonArray().apply { add(instance) })
                add("parameters", parameters)
            }

            val request = Request.Builder()
                .url("$baseUrl/models/$model:predictLongRunning")
                .header("x-goog-api-key", apiKey)
                .header("Content-Type", "application/json")
                .post(payload.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val operationName = client.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (!response.isSuccessful) error("Veo request failed (${response.code}): $body")
                JsonParser.parseString(body).asJsonObject.get("name")?.asString
                    ?: error("Veo did not return an operation name.")
            }

            onStatus("Generating video…")
            var videoUri: String? = null
            while (videoUri == null) {
                delay(10_000)
                val poll = Request.Builder()
                    .url("$baseUrl/$operationName")
                    .header("x-goog-api-key", apiKey)
                    .get()
                    .build()
                val json = client.newCall(poll).execute().use { response ->
                    val body = response.body?.string().orEmpty()
                    if (!response.isSuccessful) error("Veo polling failed (${response.code}): $body")
                    JsonParser.parseString(body).asJsonObject
                }
                if (json.get("done")?.asBoolean == true) {
                    json.getAsJsonObject("response")?.let { response ->
                        videoUri = response.getAsJsonObject("generateVideoResponse")
                            ?.getAsJsonArray("generatedSamples")
                            ?.firstOrNull()?.asJsonObject
                            ?.getAsJsonObject("video")?.get("uri")?.asString
                    }
                    if (videoUri == null) {
                        val error = json.getAsJsonObject("error")
                        error("Veo generation failed: ${error ?: json}")
                    }
                } else {
                    onStatus("Generating video…")
                }
            }

            onStatus("Downloading MP4…")
            val output = File(
                imageFile.parentFile ?: File(System.getProperty("java.io.tmpdir")!!),
                "divstudio_${System.currentTimeMillis()}_${UUID.randomUUID()}.mp4"
            )
            val download = Request.Builder()
                .url(videoUri!!)
                .header("x-goog-api-key", apiKey)
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
