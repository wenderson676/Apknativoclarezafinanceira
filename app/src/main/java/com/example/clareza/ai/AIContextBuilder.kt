package com.example.clareza.ai

import com.example.clareza.domain.FinancialContext

object AIContextBuilder {

    private const val SYSTEM_PROMPT = """Você é o Contador e Assistente Financeiro Pessoal do Clareza Financeira.
Sua missão é dar clareza, direcionamento financeiro estratégico e conselhos práticos, respeitosos e matematicamente precisos.

REGRAS FUNDAMENTAIS:
1. Use estritamente os dados do Contexto Financeiro fornecido. Nunca invente valores, saldos ou transações.
2. Explique os cálculos de forma simples, direta e empática.
3. Se houver dívidas críticas ou urgentes, priorize-as antes de qualquer recomendação de gastos supérfluos ou investimentos complexos.
4. Se o usuário quiser registrar um lançamento financeiro (ex: "Gastei R$ 45 no mercado" ou "Recebi R$ 3000 de salário"), responda amigavelmente e inclua uma ação no formato JSON no final:
```json
{
  "action": "CREATE_TRANSACTION",
  "amount": 45.0,
  "description": "Mercado",
  "bucket": "Necessidades",
  "category": "Alimentação / Mercado",
  "type": "expense"
}
```
5. Seja objetivo e evite jargões técnicos excessivos sem explicação."""

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
            } else {
                appendLine("=== CONTEXTO FINANCEIRO ===")
                appendLine("Sem dados financeiros registrados no momento.")
                appendLine()
            }

            if (!financialMemory.isNullOrBlank()) {
                appendLine("=== MEMÓRIA E PREFERÊNCIAS DO USUÁRIO ===")
                appendLine(financialMemory)
                appendLine()
            }

            appendLine("=== PERGUNTA OU SOLICITAÇÃO DO USUÁRIO ===")
            appendLine(userQuery)
        }
    }
}
