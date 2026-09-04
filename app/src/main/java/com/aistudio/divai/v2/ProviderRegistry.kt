package com.aistudio.divai.v2

/** Central registry for enabled and future AI providers. */
class ProviderRegistry(providers: List<AiProvider>) {
    private val providersById = providers.associateBy { it.id }

    fun all(): List<AiProvider> = providersById.values.toList()
    fun find(id: String): AiProvider? = providersById[id]
    fun supporting(capability: Capability): List<AiProvider> =
        all().filter { capability in it.capabilities }
}
