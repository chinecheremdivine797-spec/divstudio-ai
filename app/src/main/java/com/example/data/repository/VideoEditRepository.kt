package com.example.data.repository

import android.content.Context
import android.net.Uri
import com.aistudio.divai.BuildConfig
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import kotlinx.coroutines.tasks.await
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.util.concurrent.TimeUnit

/** Connects the Android editor to the DIVSTUDIO AI FFmpeg backend. */
class VideoEditRepository(
    private val context: Context,
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(5, TimeUnit.MINUTES)
        .readTimeout(5, TimeUnit.MINUTES)
        .build()
) {
    data class Result(val url: String, val objectName: String, val id: String)
    private data class BackendResponse(val ok: Boolean = false, val url: String? = null, val objectName: String? = null, val id: String? = null, val error: String? = null)

    suspend fun transform(
        source: Uri,
        startSeconds: Float,
        endSeconds: Float,
        speed: Float,
        volume: Float,
        mute: Boolean,
        rotate: String? = null,
        flip: String? = null
    ): Result {
        val baseUrl = BuildConfig.DIVSTUDIO_BACKEND_URL.trim().trimEnd('/')
        require(baseUrl.startsWith("https://")) { "FFmpeg backend URL is not configured. Set DIVSTUDIO_BACKEND_URL to the deployed HTTPS backend URL." }

        val user = FirebaseAuth.getInstance().currentUser ?: FirebaseAuth.getInstance().signInAnonymously().await().user
        val token = user?.getIdToken(false)?.await()?.token ?: error("Firebase authentication is unavailable.")
        val input = copyUriToCache(source)
        try {
            val fileBody = input.asRequestBody("video/mp4".toMediaType())
            val multipart = MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("video", input.name, fileBody)
                .addFormDataPart("start", startSeconds.coerceAtLeast(0f).toString())
                .addFormDataPart("end", endSeconds.coerceAtLeast(0f).toString())
                .addFormDataPart("speed", speed.coerceIn(0.25f, 4f).toString())
                .addFormDataPart("volume", volume.coerceIn(0f, 4f).toString())
                .addFormDataPart("mute", mute.toString())
                .apply { rotate?.let { addFormDataPart("rotate", it) }; flip?.let { addFormDataPart("flip", it) } }
                .build()

            val request = Request.Builder()
                .url("$baseUrl/v1/edit/transform")
                .header("Authorization", "Bearer $token")
                .post(multipart)
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                val parsed = runCatching { Gson().fromJson(body, BackendResponse::class.java) }.getOrNull()
                if (!response.isSuccessful || parsed?.ok != true || parsed.url.isNullOrBlank()) {
                    throw IllegalStateException(parsed?.error ?: "FFmpeg export failed (HTTP ${response.code}).")
                }
                return Result(parsed.url!!, parsed.objectName.orEmpty(), parsed.id.orEmpty())
            }
        } finally {
            input.delete()
        }
    }

    suspend fun downloadResult(url: String, destinationName: String = "divstudio-export-${System.currentTimeMillis()}.mp4"): File {
        val output = File(context.cacheDir, destinationName)
        val request = Request.Builder().url(url).get().build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IllegalStateException("Could not download exported MP4 (HTTP ${response.code}).")
            response.body?.byteStream()?.use { input -> output.outputStream().use { input.copyTo(it) } }
                ?: throw IllegalStateException("Export returned an empty file.")
        }
        return output
    }

    private fun copyUriToCache(uri: Uri): File {
        val file = File(context.cacheDir, "editor-input-${System.currentTimeMillis()}.mp4")
        context.contentResolver.openInputStream(uri)?.use { input -> file.outputStream().use { input.copyTo(it) } }
            ?: throw IllegalStateException("Could not read the selected video.")
        return file
    }
}
