package com.example.clareza.ai

import com.example.clareza.ai.model.AIRequest
import com.example.clareza.ai.model.AIResponse

interface AIProvider {
    val name: String
    val isAvailable: Boolean
    val isOffline: Boolean

    suspend fun generateResponse(
        prompt: String,
        request: AIRequest
    ): AIResponse
}
