package com.example.clareza.ai.runtime

import java.io.File

/**
 * Formato estrutural do arquivo de modelo LLM.
 */
enum class ModelFormat(val extension: String, val displayName: String) {
    GGUF("gguf", "Llama / GGUF (Quantizado)"),
    UNKNOWN("", "Desconhecido / Incompatível")
}

/**
 * Política de gerenciamento de memória RAM/VRAM para execução de LLM Local em dispositivos móveis.
 */
enum class ModelMemoryPolicy {
    KEEP_LOADED,                  // Mantém o modelo na RAM após inferências
    AUTO_UNLOAD_AFTER_INFERENCE,  // Descarrega o modelo da RAM imediatamente após responder (ideal para aparelhos com RAM reduzida como Moto G9)
    UNLOAD_ON_LOW_MEMORY          // Descarrega automaticamente se o sistema operacional emitir alerta de pouca memória
}

/**
 * Interface padronizada para Runtimes Nativos de LLM (focado em GGUF / Llama.cpp).
 */
interface LLMRuntime {
    val runtimeName: String
    val supportedFormats: Set<ModelFormat>
    val isLoaded: Boolean
    val loadedModelFile: File?
    var memoryPolicy: ModelMemoryPolicy

    /**
     * Valida o formato, cabeçalho e integridade do arquivo de modelo antes de alocar na RAM.
     */
    fun validateModelFile(modelFile: File): Pair<Boolean, ModelFormat>

    /**
     * Carrega o modelo na memória RAM/VRAM do dispositivo via Native Bridge.
     */
    suspend fun loadModel(modelFile: File): Result<Unit>

    /**
     * Descarrega o modelo da RAM, liberando tensores, threads, KV-cache e memória nativa.
     */
    suspend fun unloadModel()

    /**
     * Reage a eventos de pouca memória do sistema operacional Android (ComponentCallbacks2).
     */
    suspend fun onLowMemory()

    /**
     * Executa a inferência do prompt gerado.
     */
    suspend fun generateInference(prompt: String): Result<String>
}

