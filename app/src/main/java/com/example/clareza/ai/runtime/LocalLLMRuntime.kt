package com.example.clareza.ai.runtime

import java.io.File
import java.io.FileInputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Implementação do Runtime Local de LLM focado na arquitetura GGUF (Llama.cpp).
 *
 * Conectado diretamente à biblioteca nativa libllama_jni.so via NDK/JNI.
 * Não utiliza simulações ou handles genéricos.
 */
class LocalLLMRuntime(
    override var memoryPolicy: ModelMemoryPolicy = ModelMemoryPolicy.KEEP_LOADED
) : LLMRuntime {

    companion object {
        private var isNativeLibraryLoaded = false

        init {
            try {
                System.loadLibrary("llama_jni")
                isNativeLibraryLoaded = true
            } catch (e: Throwable) {
                android.util.Log.e("ClarezaAI", "[LocalLLMRuntime] Failed to load library", e)
                isNativeLibraryLoaded = false
            }
        }
    }

    override val runtimeName: String = "Engine LLM Local GGUF (Llama.cpp Native)"
    override val supportedFormats: Set<ModelFormat> = setOf(ModelFormat.GGUF)

    private var activeFile: File? = null
    private var nativeContextHandle: Long = 0L
    private var loadedInRam: Boolean = false

    override val isLoaded: Boolean
        get() = isNativeLibraryLoaded && loadedInRam && activeFile?.exists() == true && nativeContextHandle != 0L

    override val loadedModelFile: File?
        get() = activeFile

    /**
     * Valida o arquivo do modelo verificando os Magic Bytes do formato GGUF (0x46554747 = "GGUF").
     */
    override fun validateModelFile(modelFile: File): Pair<Boolean, ModelFormat> {
        if (!modelFile.exists() || !modelFile.isFile) return Pair(false, ModelFormat.UNKNOWN)
        
        val ext = modelFile.extension.lowercase()
        val minSizeBytes = 10 * 1024 * 1024 // 10MB
        if (modelFile.length() < minSizeBytes) return Pair(false, ModelFormat.UNKNOWN)

        if (ext == "gguf") {
            val isGgufHeaderValid = checkGgufMagicHeader(modelFile)
            if (isGgufHeaderValid) {
                return Pair(true, ModelFormat.GGUF)
            }
        }

        return Pair(false, ModelFormat.UNKNOWN)
    }

    private fun checkGgufMagicHeader(file: File): Boolean {
        return try {
            FileInputStream(file).use { stream ->
                val header = ByteArray(4)
                val bytesRead = stream.read(header)
                if (bytesRead == 4) {
                    val magicStr = String(header, Charsets.US_ASCII)
                    magicStr == "GGUF"
                } else false
            }
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun loadModel(modelFile: File): Result<Unit> = withContext(Dispatchers.IO) {
        if (!isNativeLibraryLoaded) {
            android.util.Log.e("ClarezaAI", "[LocalLLMRuntime] native library libllama_jni.so not available.")
            return@withContext Result.failure(
                IllegalStateException("A biblioteca nativa llama_jni não está disponível no dispositivo.")
            )
        }

        val (isValid, format) = validateModelFile(modelFile)
        if (!isValid || format != ModelFormat.GGUF) {
            android.util.Log.e("ClarezaAI", "[LocalLLMRuntime] File ${modelFile.name} is invalid or not GGUF header.")
            return@withContext Result.failure(
                IllegalArgumentException("Arquivo incompatível ou corrompido para o Runtime GGUF: ${modelFile.name}")
            )
        }

        try {
            if (loadedInRam) {
                unloadModelInternal()
            }

            android.util.Log.d("ClarezaAI", "[LocalLLMRuntime] Initializing native model context for ${modelFile.name}...")
            val handle = nativeInitModelContext(modelFile.absolutePath)
            if (handle != 0L) {
                android.util.Log.i("ClarezaAI", "[LocalLLMRuntime] Native context allocated successfully. Handle: $handle")
                this@LocalLLMRuntime.activeFile = modelFile
                this@LocalLLMRuntime.nativeContextHandle = handle
                this@LocalLLMRuntime.loadedInRam = true
                Result.success(Unit)
            } else {
                android.util.Log.w("ClarezaAI", "[LocalLLMRuntime] nativeInitModelContext returned 0L (e.g., STUB in use or allocation failed).")
                this@LocalLLMRuntime.loadedInRam = false
                this@LocalLLMRuntime.nativeContextHandle = 0L
                Result.failure(
                    IllegalStateException("O runtime nativo llama.cpp não conseguiu alocar o modelo na memória RAM.")
                )
            }
        } catch (e: Exception) {
            android.util.Log.e("ClarezaAI", "[LocalLLMRuntime] Exception in loadModel: ${e.localizedMessage}")
            this@LocalLLMRuntime.loadedInRam = false
            this@LocalLLMRuntime.nativeContextHandle = 0L
            Result.failure(e)
        }
    }

    override suspend fun unloadModel(): Unit = withContext(Dispatchers.IO) {
        unloadModelInternal()
    }

    private fun unloadModelInternal() {
        if (nativeContextHandle != 0L && isNativeLibraryLoaded) {
            try {
                android.util.Log.d("ClarezaAI", "[LocalLLMRuntime] Freeing native model context...")
                nativeFreeModelContext(nativeContextHandle)
            } catch (e: Exception) {
                // Ignore native cleanup exception
            }
        }
        this@LocalLLMRuntime.loadedInRam = false
        this@LocalLLMRuntime.activeFile = null
        this@LocalLLMRuntime.nativeContextHandle = 0L
    }

    override suspend fun onLowMemory(): Unit = withContext(Dispatchers.IO) {
        unloadModelInternal()
    }

    override suspend fun generateInference(prompt: String): Result<String> = withContext(Dispatchers.Default) {
        if (!isLoaded || nativeContextHandle == 0L || !isNativeLibraryLoaded) {
            android.util.Log.w("ClarezaAI", "[LocalLLMRuntime] generateInference called but isLoaded=$isLoaded, handle=$nativeContextHandle")
            return@withContext Result.failure(IllegalStateException("Nenhum modelo GGUF está carregado na memória nativa."))
        }

        try {
            android.util.Log.d("ClarezaAI", "[LocalLLMRuntime] Calling nativeGenerateInference with prompt length ${prompt.length}...")
            val rawOutput = nativeGenerateInference(nativeContextHandle, prompt)
            if (rawOutput.isNullOrBlank()) {
                android.util.Log.w("ClarezaAI", "[LocalLLMRuntime] nativeGenerateInference returned empty/null output.")
                return@withContext Result.failure(
                    IllegalStateException("O runtime nativo llama.cpp não retornou tokens de resposta válidos.")
                )
            }

            android.util.Log.d("ClarezaAI", "[LocalLLMRuntime] nativeGenerateInference succeeded! Length: ${rawOutput.length}")

            if (memoryPolicy == ModelMemoryPolicy.AUTO_UNLOAD_AFTER_INFERENCE) {
                unloadModelInternal()
            }

            Result.success(rawOutput)
        } catch (e: Exception) {
            android.util.Log.e("ClarezaAI", "[LocalLLMRuntime] Exception in generateInference: ${e.localizedMessage}")
            Result.failure(e)
        }
    }

    // --- Funções de Ponte JNI Nativas (llama_jni) ---
    private external fun nativeInitModelContext(modelPath: String): Long
    private external fun nativeFreeModelContext(handle: Long)
    private external fun nativeGenerateInference(handle: Long, prompt: String): String?
}
