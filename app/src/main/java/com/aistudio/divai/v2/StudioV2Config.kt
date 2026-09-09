package com.aistudio.divai.v2

/** DIVSTUDIO AI v2 feature switches. Secrets remain server-side. */
data class StudioV2Config(
    val multiModelHub: Boolean = true,
    val cartoonMode: Boolean = true,
    val realisticMode: Boolean = true,
    val imageToVideo: Boolean = true,
    val textToVideo: Boolean = true,
    val scriptToVideo: Boolean = true,
    val characterContinuity: Boolean = true,
    val sceneContinuity: Boolean = true,
    val storyboardPipeline: Boolean = true,
    val realMp4Export: Boolean = true,
    val firebaseBackend: Boolean = false,
    val paymentsEnabled: Boolean = false,
    val supabaseEnabled: Boolean = true,
    val providerFallback: Boolean = true,
    val serverSideProviderKeys: Boolean = true
)
