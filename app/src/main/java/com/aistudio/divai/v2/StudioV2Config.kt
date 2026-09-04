package com.aistudio.divai.v2

/** Version 2 feature switches. No payment or Supabase features are included. */
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
    val firebaseBackend: Boolean = true,
    val paymentsEnabled: Boolean = false,
    val supabaseEnabled: Boolean = false
)
