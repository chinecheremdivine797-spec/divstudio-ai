package com.aistudio.divai

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import com.example.BuildConfig
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/** Routes animation through the authenticated DIVSTUDIO backend; provider keys never enter the APK. */
class BackendImageToVideoRepository(
    private val context: Context,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .build()
) {
    suspend fun generateVideoFromImage(
        imageFile: File,
        prompt: String,
        aspectRatio: String = "16:9",
        resolution: String = "720p",
        onStatus: (String) -> Unit = {}
    ): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            require(imageFile.exists() && imageFile.length() > 0) { "Selected image could not be read." }
            require(prompt.isNotBlank()) { "Motion prompt cannot be blank." }
            val baseUrl = BuildConfig.DIVSTUDIO_BACKEND_URL.trimEnd('/')
            require(baseUrl.isNotBlank()) { "DIVSTUDIO_BACKEND_URL is not configured." }
            val user = auth.currentUser ?: throw IllegalStateException("Sign in to DIVSTUDIO AI before generating video.")
            val token = user.idToken()

            onStatus("Preparing image…")
            val dataUri = imageAsDataUri(imageFile)
            val body = JSONObject().apply {
                put("prompt", prompt.take(10000))
                put("provider", "runway")
                put("model", "gen4.5")
                put("image_url", dataUri)
                put("aspect_ratio", if (aspectRatio == "9:16") "9:16" else "16:9")
                put("resolution", if (resolution == "1080p") "1080p" else "720p")
                put("duration", 5)
            }.toString().toRequestBody("application/json; charset=utf-8".toMediaType())

            onStatus("Submitting to the DIVSTUDIO AI router…")
            val submitted = executeJson(
                Request.Builder().url("$baseUrl/v1/video/generate").header("Authorization", "Bearer $token").post(body).build()
            )
            val provider = submitted.optString("provider", "runway")
            val taskId = submitted.optString("task_id")
            require(taskId.isNotBlank()) { "Backend returned no video task ID." }

            var videoUrl: String? = null
            var attempts = 0
            while (videoUrl == null && attempts++ < 120) {
                delay(5_000)
                val status = executeJson(
                    Request.Builder()
                        .url("$baseUrl/v1/video/status/$provider/$taskId")
                        .header("Authorization", "Bearer $token")
                        .get().build()
                )
                when (val state = status.optString("status", "PENDING")) {
                    "SUCCEEDED" -> videoUrl = status.optString("video_url").takeIf { it.isNotBlank() }
                    "FAILED", "CANCELED" -> throw IllegalStateException(status.optString("error", "Video generation failed with status $state."))
                    else -> onStatus("Generating… $state")
                }
            }
            require(videoUrl != null) { "Video generation timed out after 10 minutes." }

            onStatus("Downloading MP4…")
            val output = File(context.filesDir, "divstudio_${System.currentTimeMillis()}_${UUID.randomUUID()}.mp4")
            client.newCall(Request.Builder().url(videoUrl!!).get().build()).execute().use { response ->
                if (!response.isSuccessful) throw IllegalStateException("MP4 download failed (${response.code}).")
                val stream = response.body ?: throw IllegalStateException("Backend returned an empty MP4.")
                stream.byteStream().use { input -> output.outputStream().use { input.copyTo(it) } }
            }
            require(output.exists() && output.length() > 0) { "Generated MP4 is empty." }
            onStatus("Ready")
            output
        }
    }

    private fun executeJson(request: Request): JSONObject = client.newCall(request).execute().use { response ->
        val text = response.body?.string().orEmpty()
        val json = if (text.isNotBlank()) JSONObject(text) else JSONObject()
        if (!response.isSuccessful) throw IllegalStateException(json.optString("error", "Backend request failed (${response.code})."))
        json
    }

    private fun imageAsDataUri(file: File): String {
        val original = file.readBytes()
        val mime = when (file.extension.lowercase()) {
            "jpg", "jpeg" -> "image/jpeg"
            "webp" -> "image/webp"
            else -> "image/png"
        }
        if (original.size <= 3_300_000) return "data:$mime;base64,${Base64.encodeToString(original, Base64.NO_WRAP)}"
        val bitmap = BitmapFactory.decodeByteArray(original, 0, original.size) ?: throw IllegalStateException("Could not prepare the selected image.")
        var working = bitmap
        try {
            if (working.width > 2048 || working.height > 2048) {
                val scale = minOf(2048f / working.width, 2048f / working.height)
                val resized = Bitmap.createScaledBitmap(working, (working.width * scale).toInt(), (working.height * scale).toInt(), true)
                if (resized !== working) working.recycle()
                working = resized
            }
            var quality = 88
            var bytes: ByteArray
            do {
                val output = ByteArrayOutputStream()
                working.compress(Bitmap.CompressFormat.JPEG, quality, output)
                bytes = output.toByteArray()
                quality -= 8
            } while (bytes.size > 3_300_000 && quality >= 48)
            require(bytes.size <= 3_300_000) { "Selected image is too large. Please choose a smaller image." }
            return "data:image/jpeg;base64,${Base64.encodeToString(bytes, Base64.NO_WRAP)}"
        } finally {
            if (!working.isRecycled) working.recycle()
        }
    }

    private suspend fun com.google.firebase.auth.FirebaseUser.idToken(): String = suspendCancellableCoroutine { continuation ->
        getIdToken(false).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result?.token
                if (!token.isNullOrBlank()) continuation.resume(token) else continuation.resumeWithException(IllegalStateException("Firebase token is empty."))
            } else {
                continuation.resumeWithException(task.exception ?: IllegalStateException("Could not get Firebase token."))
            }
        }
    }
}
