package com.aistudio.divai.v2

/**
 * Central catalog for the multi-model studio. Providers are adapters, so new
 * vendors can be added without rewriting the creation workflow.
 */
data class AiModelDescriptor(
    val id: String,
    val name: String,
    val providerId: String,
    val capabilities: Set<AiCapability>,
    val enabledByDefault: Boolean = false
)

object AiModelCatalog {
    val models: List<AiModelDescriptor> = listOf(
        AiModelDescriptor(
            id = "gemini",
            name = "Gemini",
            providerId = "google",
            capabilities = setOf(AiCapability.STORY, AiCapability.PROMPT)
        ),
        AiModelDescriptor(
            id = "veo",
            name = "Veo",
            providerId = "google",
            capabilities = setOf(
                AiCapability.TEXT_TO_VIDEO,
                AiCapability.SCRIPT_TO_VIDEO,
                AiCapability.IMAGE_TO_VIDEO,
                AiCapability.CARTOON_VIDEO,
                AiCapability.REALISTIC_VIDEO
            ),
            enabledByDefault = true
        ),
        AiModelDescriptor(
            id = "grok",
            name = "Grok",
            providerId = "xai",
            capabilities = setOf(AiCapability.STORY, AiCapability.PROMPT)
        ),
        AiModelDescriptor(
            id = "future-video-provider",
            name = "Future Video Model",
            providerId = "custom",
            capabilities = setOf(AiCapability.TEXT_TO_VIDEO, AiCapability.IMAGE_TO_VIDEO)
        )
    )

    fun forCapability(capability: AiCapability): List<AiModelDescriptor> =
        models.filter { capability in it.capabilities }
}
