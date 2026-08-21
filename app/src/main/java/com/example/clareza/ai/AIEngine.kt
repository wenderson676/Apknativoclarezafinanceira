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
                primaryProvider.generateResponse(fullPrompt, request)
            } else {
                fallbackProvider.generateResponse(fullPrompt, request)
            }
        } catch (e: Throwable) {
            // Em caso de exceção no provedor primário, recorre com segurança ao provedor de regras determinístico
            fallbackProvider.generateResponse(fullPrompt, request)
        }
    }
}
