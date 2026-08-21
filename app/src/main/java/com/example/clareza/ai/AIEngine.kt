package com.example.clareza.ai

import com.example.clareza.ai.model.AIRequest
import com.example.clareza.ai.model.AIResponse
import com.example.clareza.domain.FinancialContext

class AIEngine(
    private var provider: AIProvider = OfflineAIProvider()
) {

    fun setProvider(newProvider: AIProvider) {
        this.provider = newProvider
    }

    fun getActiveProvider(): AIProvider = provider

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
            userMessage = userMessage
        )

        return provider.generateResponse(fullPrompt, request)
    }
}
