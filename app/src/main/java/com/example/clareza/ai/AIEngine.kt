package com.example.clareza.ai

import com.example.clareza.ai.model.AIRequest
import com.example.clareza.ai.model.AIResponse
import com.example.clareza.domain.FinancialContext

class AIEngine(
    private var primaryProvider: AIProvider = RuleBasedOfflineProvider(),
    private val fallbackProvider: AIProvider = RuleBasedOfflineProvider()
) {

    fun setProvider(newProvider: AIProvider) {
        this.primaryProvider = newProvider
    }

    fun getActiveProvider(): AIProvider = primaryProvider

    suspend fun processUserMessage(
        userMessage: String,
        financialContext: FinancialContext?,
        financialMemory: String? = null
    ): AIResponse {
        val fullPrompt = AIContextBuilder.buildPrompt(
            financialContext = financialContext,
            userQuery = userMessage,
            financialMemory = financialMemory
        )

        val request = AIRequest(
            userMessage = userMessage,
            financialContext = financialContext
        )

        return try {
            if (primaryProvider.isAvailable) {
                android.util.Log.d("ClarezaAI", "[AIEngine] Primary provider is available. Routing to ${primaryProvider.name}...")
                primaryProvider.generateResponse(fullPrompt, request)
            } else {
                android.util.Log.d("ClarezaAI", "[AIEngine] Primary provider is unavailable. Routing to fallback provider...")
                fallbackProvider.generateResponse(fullPrompt, request)
            }
        } catch (e: Throwable) {
            android.util.Log.e("ClarezaAI", "[AIEngine] Error in primary provider: ${e.localizedMessage}. Routing to fallback provider...")
            fallbackProvider.generateResponse(fullPrompt, request)
        }
    }
}
