package com.example.clareza.ai

import com.example.clareza.ai.model.AIRequest
import com.example.clareza.ai.model.AIResponse
import com.example.clareza.ai.runtime.LLMRuntime
import com.example.clareza.ai.runtime.LocalLLMRuntime
import com.example.clareza.ai.runtime.ModelMemoryPolicy
import kotlinx.coroutines.withTimeoutOrNull
import java.io.File

/**
 * Provedor para execução de Modelo de Linguagem Local (LLM Offline ex: GGUF / ONNX / MediaPipe LLM Inference).
 *
 * Funciona de forma totalmente privada e offline no dispositivo do usuário.
 * Gerencia a alocação de memória RAM e recorre ao [RuleBasedOfflineProvider] caso nenhum modelo esteja alocado.
 */
class OfflineLLMProvider(
    private val runtime: LLMRuntime = LocalLLMRuntime(),
    private val ruleBasedFallback: RuleBasedOfflineProvider = RuleBasedOfflineProvider()
) : AIProvider {

    override val name: String = "Motor LLM Local Offline (${runtime.runtimeName})"

    override val isAvailable: Boolean
        get() = runtime.isLoaded

    override val isOffline: Boolean = true

    /**
     * Valida o arquivo do modelo antes de efetuar o carregamento na memória RAM.
     */
    fun validateModelFile(modelFile: File): Boolean {
        return runtime.validateModelFile(modelFile).first
    }

    /**
     * Valida e identifica o formato do modelo.
     */
    fun validateAndIdentifyModel(modelFile: File): Pair<Boolean, com.example.clareza.ai.runtime.ModelFormat> {
        return runtime.validateModelFile(modelFile)
    }

    /**
     * Reage a eventos de pouca memória informados pelo SO.
     */
    suspend fun onLowMemory() {
        runtime.onLowMemory()
    }

    /**
     * Tenta carregar o modelo na memória RAM do dispositivo.
     */
    suspend fun loadModel(modelFile: File): Boolean {
        return runtime.loadModel(modelFile).isSuccess
    }

    /**
     * Descarrega o modelo da memória RAM/VRAM para economizar recursos no dispositivo.
     */
    suspend fun unloadModel() {
        runtime.unloadModel()
    }

    fun setMemoryPolicy(policy: ModelMemoryPolicy) {
        runtime.memoryPolicy = policy
    }

    fun getActiveModelName(): String? = runtime.loadedModelFile?.name

    override suspend fun generateResponse(
        prompt: String,
        request: AIRequest
    ): AIResponse {
        val startTime = System.currentTimeMillis()

        // Se o modelo LLM não estiver carregado na RAM, utiliza o motor determinístico de regras como fallback
        if (!isAvailable) {
            android.util.Log.d("ClarezaAI", "[OfflineLLMProvider] LLM not available/loaded. Using RuleBasedOfflineProvider fallback immediately.")
            return ruleBasedFallback.generateResponse(prompt, request)
        }

        android.util.Log.d("ClarezaAI", "[OfflineLLMProvider] LLM is available. Running local inference with 15s timeout...")

        // Execução no Runtime de LLM Local com Timeout de segurança (15s)
        return try {
            val inferenceResult = withTimeoutOrNull(15000L) {
                runtime.generateInference(prompt)
            }
            if (inferenceResult != null && inferenceResult.isSuccess && inferenceResult.getOrNull()?.isNotBlank() == true) {
                val llmOutputText = inferenceResult.getOrThrow().trim()
                val suggestedAction = AIActionParser.extractActionFromText(llmOutputText)
                val latency = System.currentTimeMillis() - startTime

                android.util.Log.d("ClarezaAI", "[OfflineLLMProvider] Inference successful in ${latency}ms.")

                AIResponse(
                    text = llmOutputText,
                    suggestedAction = suggestedAction,
                    isOffline = true,
                    latencyMs = latency
                )
            } else {
                android.util.Log.w("ClarezaAI", "[OfflineLLMProvider] Inference returned empty/null result or timed out. Falling back to RuleBasedOfflineProvider.")
                val fallbackResponse = ruleBasedFallback.generateResponse(prompt, request)
                val latency = System.currentTimeMillis() - startTime
                fallbackResponse.copy(latencyMs = latency)
            }
        } catch (e: Throwable) {
            android.util.Log.e("ClarezaAI", "[OfflineLLMProvider] Exception during inference: ${e.localizedMessage}. Falling back to RuleBasedOfflineProvider.")
            val fallbackResponse = ruleBasedFallback.generateResponse(prompt, request)
            val latency = System.currentTimeMillis() - startTime
            fallbackResponse.copy(latencyMs = latency)
        }
    }
}
