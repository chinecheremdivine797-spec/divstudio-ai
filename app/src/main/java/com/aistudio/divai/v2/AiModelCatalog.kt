package com.aistudio.divai.v2

/** Central catalog for DIVSTUDIO AI's multi-provider creation hub. */
data class AiModelDescriptor(
    val id: String,
    val name: String,
    val providerId: String,
    val capabilities: Set<AiCapability>,
    val enabledByDefault: Boolean = false
)

object AiModelCatalog {
    val models: List<AiModelDescriptor> = listOf(
        AiModelDescriptor("veo-3.1", "Veo 3.1", "google", setOf(AiCapability.TEXT_TO_VIDEO, AiCapability.SCRIPT_TO_VIDEO, AiCapability.IMAGE_TO_VIDEO, AiCapability.CARTOON_VIDEO, AiCapability.REALISTIC_VIDEO), true),
        AiModelDescriptor("seedance-2.5", "Seedance 2.5", "runway", setOf(AiCapability.TEXT_TO_VIDEO, AiCapability.IMAGE_TO_VIDEO, AiCapability.SCRIPT_TO_VIDEO), true),
        AiModelDescriptor("runway-router", "Runway Model Router", "runway", setOf(AiCapability.TEXT_TO_VIDEO, AiCapability.IMAGE_TO_VIDEO, AiCapability.CARTOON_VIDEO, AiCapability.REALISTIC_VIDEO), true),
        AiModelDescriptor("kling", "Kling AI", "kling", setOf(AiCapability.TEXT_TO_VIDEO, AiCapability.IMAGE_TO_VIDEO, AiCapability.CARTOON_VIDEO, AiCapability.REALISTIC_VIDEO)),
        AiModelDescriptor("luma", "Luma Dream Machine", "luma", setOf(AiCapability.TEXT_TO_VIDEO, AiCapability.IMAGE_TO_VIDEO, AiCapability.REALISTIC_VIDEO)),
        AiModelDescriptor("gemini", "Gemini", "google", setOf(AiCapability.STORY, AiCapability.PROMPT)),
        AiModelDescriptor("grok", "Grok", "xai", setOf(AiCapability.STORY, AiCapability.PROMPT))
    )

    fun forCapability(capability: AiCapability): List<AiModelDescriptor> =
        models.filter { capability in it.capabilities }
}
