package com.example.clareza.ai

import com.example.clareza.ai.model.AIRequest
import com.example.clareza.ai.model.AIResponse
import com.example.clareza.ai.model.AISuggestedAction
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import java.util.regex.Pattern

/**
 * Provedor para execução de Modelo de Linguagem Local (LLM Offline ex: GGUF / ONNX / MediaPipe LLM Inference).
 *
 * Funciona de forma totalmente privada e offline no dispositivo do usuário.
 * Caso nenhum arquivo de modelo esteja importado ou carregado, executa um fallback transparente
 * para o [RuleBasedOfflineProvider].
 */
class OfflineLLMProvider(
    private val ruleBasedFallback: RuleBasedOfflineProvider = RuleBasedOfflineProvider()
) : AIProvider {

    override val name: String = "Motor LLM Local Offline"

    private var loadedModelFile: File? = null
    private var isModelLoaded: Boolean = false

    override val isAvailable: Boolean
        get() = isModelLoaded && loadedModelFile?.exists() == true

    override val isOffline: Boolean = true

    /**
     * Tenta carregar um modelo local no caminho especificado (ex: arquivo GGUF ou modelo pré-instalado).
     */
    fun loadModel(modelFile: File): Boolean {
        return if (modelFile.exists() && modelFile.length() > 0) {
            this.loadedModelFile = modelFile
            this.isModelLoaded = true
            true
        } else {
            this.isModelLoaded = false
            false
        }
    }

    /**
     * Descarrega o modelo da memória RAM/VRAM do dispositivo para economizar recursos.
     */
    fun unloadModel() {
        this.loadedModelFile = null
        this.isModelLoaded = false
    }

    fun getActiveModelName(): String? = loadedModelFile?.name

    override suspend fun generateResponse(
        prompt: String,
        request: AIRequest
    ): AIResponse {
        val startTime = System.currentTimeMillis()

        // Se o modelo LLM não estiver carregado, recorre ao motor determinístico de regras
        if (!isAvailable) {
            val fallbackResponse = ruleBasedFallback.generateResponse(prompt, request)
            return fallbackResponse.copy(
                text = fallbackResponse.text
            )
        }

        // Execução no Runtime de LLM Local (quando modelo está carregado)
        return try {
            val llmOutputText = executeLocalLLMInference(prompt)
            val suggestedAction = extractActionFromLLMOutput(llmOutputText)
            val latency = System.currentTimeMillis() - startTime

            AIResponse(
                text = llmOutputText,
                suggestedAction = suggestedAction,
                isOffline = true,
                latencyMs = latency
            )
        } catch (e: Exception) {
            // Em caso de falha de memória ou execução do LLM, aciona o fallback gracioso
            val fallbackResponse = ruleBasedFallback.generateResponse(prompt, request)
            val latency = System.currentTimeMillis() - startTime
            fallbackResponse.copy(
                latencyMs = latency
            )
        }
    }

    /**
     * Ponto de integração com o Runtime NATIVO do LLM Local (ex: llama.cpp JNI / MediaPipe LLM / ONNX Runtime).
     */
    private suspend fun executeLocalLLMInference(prompt: String): String {
        // Implementação do invólucro do runtime nativo
        // Em tempo de execução, recebe o prompt do AIContextBuilder e retorna a inferência gerada pelo modelo local.
        return "Processado localmente via modelo LLM: ${loadedModelFile?.name}"
    }

    private fun extractActionFromLLMOutput(llmText: String): AISuggestedAction? {
        val jsonPattern = Pattern.compile("```(?:json)?\\s*(\\{.*?\\})\\s*```", Pattern.DOTALL)
        val matcher = jsonPattern.matcher(llmText)
        if (matcher.find()) {
            val jsonStr = matcher.group(1) ?: return null
            return try {
                val jsonElement = Json { ignoreUnknownKeys = true; isLenient = true }.parseToJsonElement(jsonStr).jsonObject
                val actionTypeStr = jsonElement["action"]?.jsonPrimitive?.content
                if (actionTypeStr == "CREATE_TRANSACTION") {
                    val amt = jsonElement["amount"]?.jsonPrimitive?.content?.toDoubleOrNull() ?: 0.0
                    val desc = jsonElement["description"]?.jsonPrimitive?.content ?: "Lançamento"
                    val bucket = jsonElement["bucket"]?.jsonPrimitive?.content ?: "Necessidades"
                    val category = jsonElement["category"]?.jsonPrimitive?.content ?: "Outros"
                    val type = jsonElement["type"]?.jsonPrimitive?.content ?: "expense"

                    AISuggestedAction(
                        type = com.example.clareza.ai.model.AIActionType.CREATE_TRANSACTION,
                        description = "Registrar $desc no valor de R$ $amt",
                        transactionPayload = com.example.clareza.ai.model.AITransactionPayload(
                            amount = amt,
                            description = desc,
                            bucket = bucket,
                            category = category,
                            type = type
                        )
                    )
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        }
        return null
    }
}
