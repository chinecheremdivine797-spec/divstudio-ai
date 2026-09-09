package com.aistudio.divai.v2

import com.aistudio.divai.BuildConfig
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/** Secure Android client for the DIVSTUDIO AI backend.
 * Provider API keys never live in the APK; the backend owns them.
 */
class DivStudioAiGateway(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val http: OkHttpClient = OkHttpClient()
) {
    private val jsonType = "application/json; charset=utf-8".toMediaType()

    suspend fun generate(
        prompt: String,
        taskType: String = "chat",
        model: String? = null,
        projectId: String? = null
    ): AiGenerationResult {
        require(prompt.isNotBlank()) { "prompt cannot be blank" }
        val base = BuildConfig.DIVSTUDIO_BACKEND_URL.trimEnd('/')
        require(base.isNotBlank()) { "DIVSTUDIO_BACKEND_URL is not configured" }

        val token = auth.currentUser?.idToken()
            ?: throw IllegalStateException("Sign in to DIVSTUDIO AI before generating content")

        val body = JSONObject().apply {
            put("prompt", prompt)
            put("task_type", taskType)
            model?.let { put("model", it) }
            projectId?.let { put("project_id", it) }
        }.toString().toRequestBody(jsonType)

        val request = Request.Builder()
            .url("$base/v1/ai/generate")
            .header("Authorization", "Bearer $token")
            .post(body)
            .build()

        http.newCall(request).execute().use { response ->
            val text = response.body?.string().orEmpty()
            val payload = if (text.isNotBlank()) JSONObject(text) else JSONObject()
            if (!response.isSuccessful) {
                throw IllegalStateException(payload.optString("error", "AI generation failed (${response.code})"))
            }
            return AiGenerationResult(
                text = payload.optString("text"),
                provider = payload.optString("provider"),
                model = payload.optString("model"),
                responseId = payload.optString("response_id")
            )
        }
    }

    private suspend fun com.google.firebase.auth.FirebaseUser.idToken(): String =
        suspendCancellableCoroutine { continuation ->
            getIdToken(false).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result?.token
                    if (!token.isNullOrBlank()) continuation.resume(token)
                    else continuation.resumeWithException(IllegalStateException("Firebase token is empty"))
                } else {
                    continuation.resumeWithException(task.exception ?: IllegalStateException("Could not get Firebase token"))
                }
            }
        }
}

data class AiGenerationResult(
    val text: String,
    val provider: String,
    val model: String,
    val responseId: String
)
