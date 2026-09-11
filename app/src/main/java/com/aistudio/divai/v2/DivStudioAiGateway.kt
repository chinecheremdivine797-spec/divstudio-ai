package com.aistudio.divai.v2

import com.example.BuildConfig
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class DivStudioAiGateway(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val http: OkHttpClient = OkHttpClient()
) {
    private val jsonType = "application/json; charset=utf-8".toMediaType()

    suspend fun generate(prompt: String, taskType: String = "chat", model: String? = null, projectId: String? = null): AiGenerationResult {
        require(prompt.isNotBlank()) { "prompt cannot be blank" }
        val base = BuildConfig.DIVSTUDIO_BACKEND_URL.trimEnd('/')
        require(base.isNotBlank()) { "DIVSTUDIO_BACKEND_URL is not configured" }
        val token = auth.currentUser?.idToken() ?: throw IllegalStateException("Sign in to DIV EDIT AI before generating content")
        val body = JSONObject().apply {
            put("prompt", prompt)
            put("task_type", taskType)
            model?.let { put("model", it) }
            projectId?.let { put("project_id", it) }
        }.toString().toRequestBody(jsonType)
        val request = Request.Builder().url("$base/v1/ai/generate").header("Authorization", "Bearer $token").post(body).build()
        http.newCall(request).execute().use { response ->
            val text = response.body?.string().orEmpty()
            val payload = if (text.isNotBlank()) JSONObject(text) else JSONObject()
            if (!response.isSuccessful) throw IllegalStateException(payload.optString("error", "AI generation failed (${response.code})"))
            return AiGenerationResult(
                providerId = payload.optString("provider", "backend"),
                operationId = payload.optString("response_id", "completed"),
                status = GenerationStatus.COMPLETED
            )
        }
    }

    private suspend fun com.google.firebase.auth.FirebaseUser.idToken(): String = suspendCancellableCoroutine { continuation ->
        getIdToken(false).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result?.token
                if (!token.isNullOrBlank()) continuation.resume(token)
                else continuation.resumeWithException(IllegalStateException("Firebase token is empty"))
            } else continuation.resumeWithException(task.exception ?: IllegalStateException("Could not get Firebase token"))
        }
    }
}
