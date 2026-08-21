package com.example.clareza.ai.model

import kotlinx.serialization.Serializable

@Serializable
enum class AIActionType {
    CREATE_TRANSACTION,
    CREATE_GOAL,
    CREATE_DEBT,
    ADJUST_BUDGET_MODE,
    NONE
}

@Serializable
data class AITransactionPayload(
    val amount: Double,
    val description: String,
    val bucket: String, // "Necessidades", "Desejos", "Reserva/Dívidas", "Renda"
    val category: String,
    val type: String = "expense" // "expense", "income"
)

@Serializable
data class AIGoalPayload(
    val title: String,
    val targetAmount: Double,
    val deadline: String? = null
)

@Serializable
data class AIDebtPayload(
    val name: String,
    val totalAmount: Double,
    val monthlyPayment: Double,
    val interestRate: Double = 0.0,
    val urgencyLevel: String = "media"
)

@Serializable
data class AISuggestedAction(
    val type: AIActionType = AIActionType.NONE,
    val description: String = "",
    val transactionPayload: AITransactionPayload? = null,
    val goalPayload: AIGoalPayload? = null,
    val debtPayload: AIDebtPayload? = null,
    val budgetModePayload: String? = null
)

@Serializable
data class AIRequest(
    val userMessage: String,
    val conversationHistory: List<AIChatMessage> = emptyList(),
    val systemPromptOverride: String? = null
)

@Serializable
data class AIChatMessage(
    val role: String, // "user", "assistant", "system"
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
data class AIResponse(
    val text: String,
    val suggestedAction: AISuggestedAction? = null,
    val isOffline: Boolean = true,
    val latencyMs: Long = 0
)
