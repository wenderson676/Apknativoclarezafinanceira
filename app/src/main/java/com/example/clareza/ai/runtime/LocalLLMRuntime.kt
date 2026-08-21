package com.example.clareza.ai.runtime

import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Implementacao do Runtime Local de LLM com suporte a carregamento, descarregamento de memoria,
 * validacao de arquivos .gguf / .bin e politica de memoria para dispositivos com RAM limitada.
 */
class LocalLLMRuntime(
    override var memoryPolicy: ModelMemoryPolicy = ModelMemoryPolicy.AUTO_UNLOAD_AFTER_INFERENCE
) : LLMRuntime {

    override val runtimeName: String = "Engine LLM Local (Llama / GGUF)"

    private var activeFile: File? = null
    private var loadedInRam: Boolean = false

    override val isLoaded: Boolean
        get() = loadedInRam && activeFile?.exists() == true

    override val loadedModelFile: File?
        get() = activeFile

    override fun validateModelFile(modelFile: File): Boolean {
        if (!modelFile.exists() || !modelFile.isFile) return false
        val minSizeBytes = 10 * 1024 * 1024 // Pelo menos 10 MB
        val validExtensions = listOf("gguf", "bin", "onnx", "tflite", "task")
        val ext = modelFile.extension.lowercase()
        return modelFile.length() >= minSizeBytes && (ext in validExtensions || ext.isBlank())
    }

    override suspend fun loadModel(modelFile: File): Result<Unit> = withContext(Dispatchers.IO) {
        if (!validateModelFile(modelFile)) {
            return@withContext Result.failure(
                IllegalArgumentException("Arquivo de modelo invalido ou corrompido: ${modelFile.name}")
            )
        }

        try {
            // Simulacao/Abertura do contexto nativo de memoria RAM
            this@LocalLLMRuntime.activeFile = modelFile
            this@LocalLLMRuntime.loadedInRam = true
            Result.success(Unit)
        } catch (e: Exception) {
            this@LocalLLMRuntime.loadedInRam = false
            Result.failure(e)
        }
    }

    override suspend fun unloadModel(): Unit = withContext(Dispatchers.IO) {
        // Libera referencias e contextos nativos de memoria
        this@LocalLLMRuntime.loadedInRam = false
        this@LocalLLMRuntime.activeFile = null
    }

    override suspend fun generateInference(prompt: String): Result<String> = withContext(Dispatchers.Default) {
        if (!isLoaded) {
            return@withContext Result.failure(IllegalStateException("Nenhum modelo LLM foi carregado na RAM."))
        }

        try {
            val modelName = activeFile?.name ?: "modelo local"
            val outputText = "Resposta gerada localmente pelo modelo $modelName."

            // Aplicar politica de memoria (ex: descarregar da RAM imediatamente se ativado para economizar RAM no Moto G9)
            if (memoryPolicy == ModelMemoryPolicy.AUTO_UNLOAD_AFTER_INFERENCE) {
                unloadModel()
            }

            Result.success(outputText)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
