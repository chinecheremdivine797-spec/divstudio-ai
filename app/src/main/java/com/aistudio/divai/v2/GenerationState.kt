package com.aistudio.divai.v2

sealed interface GenerationState {
    data object Idle : GenerationState
    data class Queued(val requestId: String) : GenerationState
    data class Generating(val requestId: String, val statusMessage: String = "Generating…") : GenerationState
    data class Processing(val requestId: String, val statusMessage: String = "Processing…") : GenerationState
    data class Rendering(val requestId: String, val statusMessage: String = "Rendering…") : GenerationState
    data class Completed(val requestId: String, val outputUri: String) : GenerationState
    data class Failed(val requestId: String, val message: String) : GenerationState
}
