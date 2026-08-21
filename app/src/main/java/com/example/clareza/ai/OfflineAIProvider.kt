package com.example.clareza.ai

import com.example.clareza.ai.model.AIRequest
import com.example.clareza.ai.model.AIResponse

/**
 * Provedor offline do Clareza (subclasse retrocompatível do RuleBasedOfflineProvider).
 */
class OfflineAIProvider : RuleBasedOfflineProvider() {
    override val name: String = "Motor Offline Clareza (Regras)"
}
