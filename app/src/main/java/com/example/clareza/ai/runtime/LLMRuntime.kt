package com.example.clareza.ai.runtime

import java.io.File

/**
 * Politica de gerenciamento de memoria RAM/VRAM para execucao de LLM Local em dispositivos moveis.
 */
enum class ModelMemoryPolicy {
    KEEP_LOADED,                  // Mantem o modelo na RAM apos inferencias
    AUTO_UNLOAD_AFTER_INFERENCE,  // Descarrega o modelo da RAM imediatamente apos responder (ideal para aparelhos com RAM reduzida)
    UNLOAD_ON_LOW_MEMORY          // Descarrega se o sistema operacional emitir alerta de pouca memoria
}

/**
 * Interface padronizada para Runtimes Nativos de LLM (ex: llama.cpp JNI, MediaPipe LLM, ONNX Runtime).
 */
interface LLMRuntime {
    val runtimeName: String
    val isLoaded: Boolean
    val loadedModelFile: File?
    var memoryPolicy: ModelMemoryPolicy

    /**
     * Valida a integridade e formato do arquivo de modelo antes de alocar na RAM.
     */
    fun validateModelFile(modelFile: File): Boolean

    /**
     * Carrega o modelo na memoria RAM/VRAM do dispositivo.
     */
    suspend fun loadModel(modelFile: File): Result<Unit>

    /**
     * Descarrega o modelo da RAM, liberando tensores, threads, KV-cache e memoria nativa.
     */
    suspend fun unloadModel()

    /**
     * Executa a inferencia do prompt gerado.
     */
    suspend fun generateInference(prompt: String): Result<String>
}
