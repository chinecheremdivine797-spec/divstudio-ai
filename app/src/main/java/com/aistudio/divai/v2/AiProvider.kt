package com.aistudio.divai.v2

/** A provider adapter keeps DIVSTUDIO AI independent from any single AI vendor. */
interface AiProvider {
    val id: String
    val displayName: String
    val capabilities: Set<AiCapability>
    suspend fun generate(request: AiGenerationRequest): AiGenerationResult
}

enum class AiCapability {
    STORY,
    PROMPT,
    IMAGE_GENERATION,
    IMAGE_TO_VIDEO,
    TEXT_TO_VIDEO,
    SCRIPT_TO_VIDEO,
    REALISTIC_VIDEO,
    CARTOON_VIDEO,
    VOICE,
    LIP_SYNC,
    SUBTITLES
}

enum class VisualMode { CARTOON, REALISTIC }

enum class VideoAspectRatio { LANDSCAPE_16_9, PORTRAIT_9_16 }

data class AiGenerationRequest(
    val prompt: String,
    val visualMode: VisualMode,
    val aspectRatio: VideoAspectRatio = VideoAspectRatio.LANDSCAPE_16_9,
    val referenceImageUris: List<String> = emptyList(),
    val projectId: String? = null
)

data class AiGenerationResult(
    val providerId: String,
    val operationId: String,
    val status: GenerationStatus
)

enum class GenerationStatus { QUEUED, GENERATING, PROCESSING, RENDERING, COMPLETED, FAILED }
