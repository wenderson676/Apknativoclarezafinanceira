package com.example.clareza.ai

import com.example.clareza.domain.FinancialContext

object AIContextBuilder {

    private const val SYSTEM_PROMPT = """Você é o Assistente Financeiro do Clareza Financeira.
Responda de forma clara, empática e objetiva em português.
Se o usuário solicitar um lançamento (ex: "Gastei R$ 45 no mercado"), inclua no final:
```json
{
  "action": "CREATE_TRANSACTION",
  "amount": 45.0,
  "description": "Mercado",
  "bucket": "Necessidades",
  "category": "Alimentação / Mercado",
  "type": "expense"
}
```"""

    fun buildPrompt(
        financialContext: FinancialContext?,
        userQuery: String,
        financialMemory: String? = null
    ): String {
        return buildString {
            appendLine(SYSTEM_PROMPT)
            appendLine()

            if (financialContext != null) {
                appendLine(financialContext.toCompactContext())
                appendLine()
            }

            if (!financialMemory.isNullOrBlank()) {
                appendLine("Memória: $financialMemory")
                appendLine()
            }

            appendLine("Pergunta: $userQuery")
            appendLine("Resposta:")
        }
    }
}
