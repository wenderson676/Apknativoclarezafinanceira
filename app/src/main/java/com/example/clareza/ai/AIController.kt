package com.example.clareza.ai

import android.content.Context
import android.net.Uri
import com.example.clareza.ai.model.AIChatMessage
import com.example.clareza.ai.model.AIRequest
import com.example.clareza.ai.model.AIResponse
import com.example.clareza.ai.runtime.LocalLLMRuntime
import com.example.clareza.domain.FinancialContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

data class GGUFModelInfo(
    val file: File,
    val name: String,
    val sizeBytes: Long,
    val isLoaded: Boolean
)

data class RecommendedModel(
    val name: String,
    val sizeText: String,
    val format: String = "GGUF",
    val description: String,
    val targetDevice: String
)

enum class ModelLoadingState {
    IDLE,
    LOADING,
    LOADED,
    ERROR
}

class AIController private constructor(private val context: Context) {

    companion object {
        @Volatile
        private var INSTANCE: AIController? = null

        fun getInstance(context: Context): AIController {
            return INSTANCE ?: synchronized(this) {
                val instance = AIController(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }

    val offlineLLMProvider = OfflineLLMProvider(LocalLLMRuntime())
    val ruleBasedProvider = RuleBasedOfflineProvider()
    val aiEngine = AIEngine(
        primaryProvider = offlineLLMProvider,
        fallbackProvider = ruleBasedProvider
    )

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    private val modelsDir: File
        get() {
            val dir = File(context.filesDir, "models")
            if (!dir.exists()) dir.mkdirs()
            return dir
        }

    private val chatHistoryFile: File
        get() = File(context.filesDir, "chat_history.json")

    private val _loadingState = MutableStateFlow(ModelLoadingState.IDLE)
    val loadingState: StateFlow<ModelLoadingState> = _loadingState.asStateFlow()

    private val _activeModelName = MutableStateFlow<String?>(null)
    val activeModelName: StateFlow<String?> = _activeModelName.asStateFlow()

    private val _availableModels = MutableStateFlow<List<GGUFModelInfo>>(emptyList())
    val availableModels: StateFlow<List<GGUFModelInfo>> = _availableModels.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<AIChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<AIChatMessage>> = _chatMessages.asStateFlow()

    val recommendedModels = listOf(
        RecommendedModel(
            name = "LFM2 350M Instruct",
            sizeText = "~350 MB",
            description = "Modelo ultra leve focado em dispositivos móveis e baixos recursos de RAM.",
            targetDevice = "Recomendado para Moto G9 Power e aparelhos de 2GB a 4GB RAM"
        ),
        RecommendedModel(
            name = "Qwen2.5 0.5B Instruct",
            sizeText = "~390 MB",
            description = "Excelente raciocínio lógico e suporte multimodal a português.",
            targetDevice = "Recomendado para smartphones intermediários (4GB RAM)"
        ),
        RecommendedModel(
            name = "TinyLlama 1.1B Chat (Q4_K_M)",
            sizeText = "~650 MB",
            description = "Mais capacidade de conversação fluida e análise detalhada.",
            targetDevice = "Recomendado para aparelhos com 4GB+ RAM disponível"
        )
    )

    init {
        refreshAvailableModels()
        loadChatHistory()
    }

    fun refreshAvailableModels() {
        val files = modelsDir.listFiles { _, name -> name.lowercase().endsWith(".gguf") } ?: emptyArray()
        val currentActive = offlineLLMProvider.getActiveModelName()
        val list = files.map { file ->
            GGUFModelInfo(
                file = file,
                name = file.name,
                sizeBytes = file.length(),
                isLoaded = file.name == currentActive && offlineLLMProvider.isAvailable
            )
        }
        _availableModels.value = list
        _activeModelName.value = currentActive
    }

    suspend fun importGGUFFromUri(uri: Uri): Result<GGUFModelInfo> = withContext(Dispatchers.IO) {
        try {
            val contentResolver = context.contentResolver
            var fileName = "imported_model_${System.currentTimeMillis()}.gguf"

            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1 && cursor.moveToFirst()) {
                    val displayName = cursor.getString(nameIndex)
                    if (!displayName.isNullOrBlank()) {
                        fileName = displayName
                    }
                }
            }

            if (!fileName.lowercase().endsWith(".gguf")) {
                fileName = "$fileName.gguf"
            }

            val destFile = File(modelsDir, fileName)
            contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            } ?: return@withContext Result.failure(Exception("Não foi possível abrir o arquivo selecionado."))

            // Validate imported GGUF header and minimum size
            val (isValid, _) = offlineLLMProvider.validateAndIdentifyModel(destFile)
            if (!isValid) {
                destFile.delete()
                return@withContext Result.failure(IllegalArgumentException("O arquivo selecionado não é um modelo GGUF válido ou está corrompido."))
            }

            refreshAvailableModels()
            val info = GGUFModelInfo(
                file = destFile,
                name = destFile.name,
                sizeBytes = destFile.length(),
                isLoaded = false
            )
            Result.success(info)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loadModel(file: File): Result<Unit> = withContext(Dispatchers.IO) {
        _loadingState.value = ModelLoadingState.LOADING
        try {
            val success = offlineLLMProvider.loadModel(file)
            if (success) {
                _loadingState.value = ModelLoadingState.LOADED
                _activeModelName.value = file.name
                refreshAvailableModels()
                Result.success(Unit)
            } else {
                _loadingState.value = ModelLoadingState.ERROR
                refreshAvailableModels()
                Result.failure(Exception("O runtime llama.cpp não conseguiu alocar o modelo na memória RAM."))
            }
        } catch (e: Exception) {
            _loadingState.value = ModelLoadingState.ERROR
            refreshAvailableModels()
            Result.failure(e)
        }
    }

    suspend fun unloadModel() = withContext(Dispatchers.IO) {
        offlineLLMProvider.unloadModel()
        _loadingState.value = ModelLoadingState.IDLE
        _activeModelName.value = null
        refreshAvailableModels()
    }

    suspend fun deleteModel(file: File) = withContext(Dispatchers.IO) {
        if (file.name == offlineLLMProvider.getActiveModelName()) {
            unloadModel()
        }
        if (file.exists()) {
            file.delete()
        }
        refreshAvailableModels()
    }

    suspend fun processUserMessage(
        userMessage: String,
        financialContext: FinancialContext?
    ): AIResponse = withContext(Dispatchers.Default) {

        // Construct short memory from recent chat history
        val recentHistory = _chatMessages.value.takeLast(6)
        val conversationMemory = if (recentHistory.isNotEmpty()) {
            recentHistory.joinToString("\n") { "${it.role.uppercase()}: ${it.content}" }
        } else null

        // Add user message to UI state
        val userMsg = AIChatMessage(role = "user", content = userMessage)
        val updatedList = _chatMessages.value + userMsg
        _chatMessages.value = updatedList
        saveChatHistory(updatedList)

        // Process message through AIEngine
        val response = aiEngine.processUserMessage(
            userMessage = userMessage,
            financialContext = financialContext,
            financialMemory = conversationMemory
        )

        // Add assistant response to UI state
        val assistantMsg = AIChatMessage(role = "assistant", content = response.text)
        val finalList = _chatMessages.value + assistantMsg
        _chatMessages.value = finalList
        saveChatHistory(finalList)

        response
    }

    fun clearChatHistory() {
        _chatMessages.value = emptyList()
        if (chatHistoryFile.exists()) {
            chatHistoryFile.delete()
        }
    }

    private fun saveChatHistory(messages: List<AIChatMessage>) {
        try {
            val jsonStr = json.encodeToString(messages)
            chatHistoryFile.writeText(jsonStr)
        } catch (e: Exception) {
            // Non-critical background save failure
        }
    }

    private fun loadChatHistory() {
        try {
            if (chatHistoryFile.exists()) {
                val jsonStr = chatHistoryFile.readText()
                if (jsonStr.isNotBlank()) {
                    val list = json.decodeFromString<List<AIChatMessage>>(jsonStr)
                    _chatMessages.value = list
                }
            }
        } catch (e: Exception) {
            _chatMessages.value = emptyList()
        }
    }
}
