package com.example.clareza.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.clareza.ai.AIController
import com.example.clareza.ai.model.AIActionType
import com.example.clareza.ai.model.AIChatMessage
import com.example.clareza.ai.model.AISuggestedAction
import com.example.clareza.domain.FinancialContext
import com.example.clareza.ui.ClarezaViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ChatUiState(
    val messages: List<AIChatMessage> = emptyList(),
    val inputText: String = "",
    val isGenerating: Boolean = false,
    val isLLMLoaded: Boolean = false,
    val activeModelName: String? = null,
    val activeAction: AISuggestedAction? = null,
    val actionSuccessMessage: String? = null,
    val errorMessage: String? = null
)

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val aiController = AIController.getInstance(application)

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var activeGenerationJob: Job? = null

    init {
        viewModelScope.launch {
            aiController.chatMessages.collect { msgs ->
                val isLoaded = aiController.offlineLLMProvider.isAvailable
                val modelName = aiController.offlineLLMProvider.getActiveModelName()
                _uiState.value = _uiState.value.copy(
                    messages = msgs,
                    isLLMLoaded = isLoaded,
                    activeModelName = modelName
                )
            }
        }
    }

    fun updateInput(text: String) {
        _uiState.value = _uiState.value.copy(inputText = text)
    }

    fun refreshLLMStatus() {
        val isLoaded = aiController.offlineLLMProvider.isAvailable
        val modelName = aiController.offlineLLMProvider.getActiveModelName()
        _uiState.value = _uiState.value.copy(
            isLLMLoaded = isLoaded,
            activeModelName = modelName
        )
    }

    fun autoLoadModelIfAvailable() {
        viewModelScope.launch {
            val available = aiController.availableModels.value
            if (!aiController.offlineLLMProvider.isAvailable && available.isNotEmpty()) {
                val modelToLoad = available.firstOrNull()
                if (modelToLoad != null) {
                    aiController.loadModel(modelToLoad.file)
                }
            }
            refreshLLMStatus()
        }
    }

    fun onChatClosed() {
        cancelGeneration()
        viewModelScope.launch {
            if (aiController.offlineLLMProvider.isAvailable) {
                aiController.unloadModel()
            }
            refreshLLMStatus()
        }
    }

    fun sendMessage(financialContext: FinancialContext?) {
        val text = _uiState.value.inputText.trim()
        if (text.isBlank() || _uiState.value.isGenerating) return

        _uiState.value = _uiState.value.copy(
            inputText = "",
            isGenerating = true,
            errorMessage = null,
            actionSuccessMessage = null
        )

        activeGenerationJob = viewModelScope.launch {
            try {
                val response = aiController.processUserMessage(text, financialContext)
                val isLoaded = aiController.offlineLLMProvider.isAvailable
                val modelName = aiController.offlineLLMProvider.getActiveModelName()

                _uiState.value = _uiState.value.copy(
                    isGenerating = false,
                    isLLMLoaded = isLoaded,
                    activeModelName = modelName,
                    activeAction = if (response.suggestedAction?.type != AIActionType.NONE) response.suggestedAction else null
                )
            } catch (e: Throwable) {
                _uiState.value = _uiState.value.copy(
                    isGenerating = false,
                    errorMessage = "Erro ao processar mensagem: ${e.localizedMessage ?: "Falha desconhecida"}"
                )
            } finally {
                _uiState.value = _uiState.value.copy(isGenerating = false)
            }
        }
    }

    fun cancelGeneration() {
        activeGenerationJob?.cancel()
        _uiState.value = _uiState.value.copy(
            isGenerating = false,
            errorMessage = "Geração de resposta cancelada pelo usuário."
        )
    }

    fun confirmSuggestedAction(action: AISuggestedAction, clarezaViewModel: ClarezaViewModel) {
        when (action.type) {
            AIActionType.CREATE_TRANSACTION -> {
                action.transactionPayload?.let { p ->
                    clarezaViewModel.addTransaction(
                        amount = p.amount,
                        description = p.description,
                        bucket = p.bucket,
                        category = p.category,
                        date = java.time.LocalDate.now().toString(),
                        isPending = false,
                        account = "banco",
                        toAccount = null,
                        type = p.type
                    )
                    _uiState.value = _uiState.value.copy(
                        activeAction = null,
                        actionSuccessMessage = "✓ Transação de ${p.description} (R$ ${String.format("%.2f", p.amount)}) lançada com sucesso!"
                    )
                }
            }
            AIActionType.CREATE_GOAL -> {
                action.goalPayload?.let { g ->
                    clarezaViewModel.saveGoal(
                        com.example.clareza.data.model.Goal(
                            id = "",
                            title = g.title,
                            targetAmount = g.targetAmount,
                            currentAmount = 0.0
                        )
                    )
                    _uiState.value = _uiState.value.copy(
                        activeAction = null,
                        actionSuccessMessage = "✓ Meta \"${g.title}\" criada com sucesso!"
                    )
                }
            }
            AIActionType.CREATE_DEBT -> {
                action.debtPayload?.let { d ->
                    clarezaViewModel.saveDebt(
                        com.example.clareza.data.model.Debt(
                            id = "",
                            name = d.name,
                            totalAmount = d.totalAmount,
                            monthlyPayment = d.monthlyPayment,
                            interestRate = d.interestRate,
                            isLate = false,
                            creditor = d.name,
                            type = d.urgencyLevel
                        )
                    )
                    _uiState.value = _uiState.value.copy(
                        activeAction = null,
                        actionSuccessMessage = "✓ Dívida \"${d.name}\" cadastrada no plano com sucesso!"
                    )
                }
            }
            AIActionType.ADJUST_BUDGET_MODE -> {
                action.budgetModePayload?.let { mode ->
                    clarezaViewModel.setBudgetMode(mode)
                    _uiState.value = _uiState.value.copy(
                        activeAction = null,
                        actionSuccessMessage = "✓ Modelo de orçamento ajustado para \"$mode\"!"
                    )
                }
            }
            else -> {
                _uiState.value = _uiState.value.copy(activeAction = null)
            }
        }
    }

    fun dismissSuggestedAction() {
        _uiState.value = _uiState.value.copy(activeAction = null)
    }

    fun clearHistory() {
        aiController.clearChatHistory()
        _uiState.value = _uiState.value.copy(
            messages = emptyList(),
            activeAction = null,
            actionSuccessMessage = null
        )
    }

    fun clearSuccessMessage() {
        _uiState.value = _uiState.value.copy(actionSuccessMessage = null)
    }
}
