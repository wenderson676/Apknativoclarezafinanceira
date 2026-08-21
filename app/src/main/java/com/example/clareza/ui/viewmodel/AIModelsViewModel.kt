package com.example.clareza.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.clareza.ai.AIController
import com.example.clareza.ai.GGUFModelInfo
import com.example.clareza.ai.ModelLoadingState
import com.example.clareza.ai.RecommendedModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

data class AIModelsUiState(
    val availableModels: List<GGUFModelInfo> = emptyList(),
    val recommendedModels: List<RecommendedModel> = emptyList(),
    val activeModelName: String? = null,
    val loadingState: ModelLoadingState = ModelLoadingState.IDLE,
    val statusMessage: String? = null,
    val errorMessage: String? = null,
    val isImporting: Boolean = false
)

class AIModelsViewModel(application: Application) : AndroidViewModel(application) {

    private val aiController = AIController.getInstance(application)

    private val _uiState = MutableStateFlow(AIModelsUiState())
    val uiState: StateFlow<AIModelsUiState> = _uiState.asStateFlow()

    init {
        _uiState.value = _uiState.value.copy(
            recommendedModels = aiController.recommendedModels
        )

        viewModelScope.launch {
            aiController.availableModels.collect { models ->
                val activeName = aiController.activeModelName.value
                _uiState.value = _uiState.value.copy(
                    availableModels = models,
                    activeModelName = activeName
                )
            }
        }

        viewModelScope.launch {
            aiController.loadingState.collect { state ->
                _uiState.value = _uiState.value.copy(loadingState = state)
            }
        }
    }

    fun refreshModels() {
        aiController.refreshAvailableModels()
    }

    fun importGGUF(uri: Uri) {
        _uiState.value = _uiState.value.copy(
            isImporting = true,
            statusMessage = "Importando e validando modelo .GGUF...",
            errorMessage = null
        )

        viewModelScope.launch {
            val result = aiController.importGGUFFromUri(uri)
            result.onSuccess { info ->
                _uiState.value = _uiState.value.copy(
                    isImporting = false,
                    statusMessage = "✓ Modelo \"${info.name}\" importado e validado com sucesso!",
                    errorMessage = null
                )
                refreshModels()
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isImporting = false,
                    statusMessage = null,
                    errorMessage = error.message ?: "Falha ao importar o arquivo GGUF."
                )
            }
        }
    }

    fun loadModel(file: File) {
        _uiState.value = _uiState.value.copy(
            statusMessage = "Carregando modelo \"${file.name}\" na memória RAM...",
            errorMessage = null
        )

        viewModelScope.launch {
            val result = aiController.loadModel(file)
            result.onSuccess {
                _uiState.value = _uiState.value.copy(
                    statusMessage = "✓ Modelo \"${file.name}\" alocado e pronto para inferência!",
                    errorMessage = null
                )
                refreshModels()
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    statusMessage = null,
                    errorMessage = "Falha ao carregar modelo: ${error.localizedMessage}"
                )
                refreshModels()
            }
        }
    }

    fun unloadModel() {
        viewModelScope.launch {
            aiController.unloadModel()
            _uiState.value = _uiState.value.copy(
                statusMessage = "Modelo descarregado da memória RAM.",
                errorMessage = null
            )
            refreshModels()
        }
    }

    fun deleteModel(file: File) {
        viewModelScope.launch {
            aiController.deleteModel(file)
            _uiState.value = _uiState.value.copy(
                statusMessage = "Modelo \"${file.name}\" removido.",
                errorMessage = null
            )
            refreshModels()
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            statusMessage = null,
            errorMessage = null
        )
    }
}
