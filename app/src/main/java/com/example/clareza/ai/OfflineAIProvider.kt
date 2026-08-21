package com.example.clareza.ai

import com.example.clareza.ai.model.AIActionType
import com.example.clareza.ai.model.AIRequest
import com.example.clareza.ai.model.AIResponse
import com.example.clareza.ai.model.AISuggestedAction
import com.example.clareza.ai.model.AITransactionPayload
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.util.regex.Pattern

class OfflineAIProvider : AIProvider {

    override val name: String = "Motor Offline Clareza"
    override val isAvailable: Boolean = true
    override val isOffline: Boolean = true

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    override suspend fun generateResponse(
        prompt: String,
        request: AIRequest
    ): AIResponse {
        val startTime = System.currentTimeMillis()
        val query = request.userMessage.trim()

        // 1. Verificar se é um comando explícito de lançamento financeiro
        val quickExpenseAction = parseQuickExpense(query)
        if (quickExpenseAction != null) {
            val latency = System.currentTimeMillis() - startTime
            val tx = quickExpenseAction.transactionPayload!!
            val text = "Entendido! Identifiquei o lançamento de R$ ${"%.2f".format(tx.amount)} para '${tx.description}' na categoria '${tx.category}'. Deseja confirmar e salvar?"
            return AIResponse(
                text = text,
                suggestedAction = quickExpenseAction,
                isOffline = true,
                latencyMs = latency
            )
        }

        // 2. Análise inteligente offline baseada em regras e contexto financeiro
        val responseText = analyzeQueryWithContext(query, prompt)
        val extractedAction = extractActionFromText(responseText)

        val latency = System.currentTimeMillis() - startTime
        return AIResponse(
            text = responseText,
            suggestedAction = extractedAction,
            isOffline = true,
            latencyMs = latency
        )
    }

    private fun parseQuickExpense(query: String): AISuggestedAction? {
        val lower = query.lowercase()
        val isExpense = lower.contains("gastei") || lower.contains("paguei") || lower.contains("comprei") || lower.contains("custou")
        val isIncome = lower.contains("recebi") || lower.contains("ganhei") || lower.contains("salário") || lower.contains("renda")

        if (!isExpense && !isIncome) return null

        val matcher = Pattern.compile("(?:r\\$|rs)?\\s*(\\d+(?:[.,]\\d{1,2})?)").matcher(lower)
        if (matcher.find()) {
            val amountStr = matcher.group(1)?.replace(",", ".") ?: return null
            val amount = amountStr.toDoubleOrNull() ?: return null

            // Extrair descrição
            val description = query
                .replace(Regex("(?i)(gastei|paguei|comprei|recebi|ganhei|r\\$|rs|\\d+[.,]?\\d*|no|na|com|de|em|para)"), " ")
                .trim()
                .replace(Regex("\\s+"), " ")
                .ifBlank { if (isIncome) "Renda Extra" else "Despesa" }

            val (bucket, category) = if (isIncome) {
                Pair("Renda", "Salário / Rendimentos")
            } else {
                classifyCategory(description)
            }

            return AISuggestedAction(
                type = AIActionType.CREATE_TRANSACTION,
                description = if (isIncome) "Adicionar Receita de R$ $amount" else "Adicionar Despesa de R$ $amount",
                transactionPayload = AITransactionPayload(
                    amount = amount,
                    description = description.replaceFirstChar { it.uppercase() },
                    bucket = bucket,
                    category = category,
                    type = if (isIncome) "income" else "expense"
                )
            )
        }
        return null
    }

    private fun classifyCategory(desc: String): Pair<String, String> {
        val d = desc.lowercase()
        return when {
            d.contains("mercado") || d.contains("compras") || d.contains("aluguel") || d.contains("luz") || d.contains("água") || d.contains("energia") || d.contains("farmácia") || d.contains("remédio") || d.contains("gasolina") || d.contains("combustível") -> {
                Pair("Necessidades", "Alimentação / Mercado")
            }
            d.contains("restaurante") || d.contains("ifood") || d.contains("lanche") || d.contains("pizza") || d.contains("cinema") || d.contains("uber") || d.contains("jogo") || d.contains("roupa") || d.contains("passeio") -> {
                Pair("Desejos", "Lazer / Restaurante")
            }
            d.contains("dívida") || d.contains("empréstimo") || d.contains("cartão") || d.contains("fatura") || d.contains("reserva") || d.contains("cofrinho") -> {
                Pair("Reserva/Dívidas", "Reserva de Emergência")
            }
            else -> {
                Pair("Necessidades", "Outros Essenciais")
            }
        }
    }

    private fun analyzeQueryWithContext(query: String, fullPrompt: String): String {
        val q = query.lowercase()

        return when {
            q.contains("score") || q.contains("saúde") || q.contains("pontuação") -> {
                "Analisando seu Score e Saúde Financeira com base no diagnóstico:\n\n" +
                "• Seu Score atual é calculado com base em 4 pilares: Fluxo de Caixa, Reserva de Emergência, Nível de Dívidas e Disciplina de Gastos.\n" +
                "• Para aumentar sua nota: mantenha as despesas essenciais equilibradas e priorize amortizar dívidas com juros altos para liberar fluxo mensal."
            }
            q.contains("dívida") || q.contains("quitar") || q.contains("juros") || q.contains("avalanche") -> {
                "Sobre suas Dívidas:\n\n" +
                "• Pelo Método Avalanche, priorizamos pagar o valor mínimo de todas as contas e direcionar todo o valor extra disponível para a dívida com maior taxa de juros ou urgência essencial.\n" +
                "• Mantenha as contas essenciais em dia para evitar cortes de serviços básicos e encargos abusivos."
            }
            q.contains("reserva") || q.contains("emergência") || q.contains("cofrinho") || q.contains("guardar") -> {
                "Sobre a Reserva de Emergência:\n\n" +
                "• A recomendação do Clareza é construir um colchão de 3 a 6 meses das suas despesas essenciais reais.\n" +
                "• Guarde de forma consistente todo mês antes de realizar gastos discricionários."
            }
            q.contains("resumo") || q.contains("como estou") || q.contains("diagnóstico") || q.contains("visão geral") -> {
                "Aqui está a leitura estratégica das suas finanças:\n\n" +
                "• Seus dados consolidados estão organizados na metodologia de potes.\n" +
                "• Recomendo revisar a aba de Diagnóstico no Dashboard para ver os pontos fortes e os pontos de atenção do mês atual."
            }
            else -> {
                "Olá! Sou o Assistente Financeiro do Clareza. Posso ajudar você a:\n\n" +
                "1. Registrar gastos rápidos (ex: 'Gastei 45 no mercado')\n" +
                "2. Analisar seu plano de quitação de dívidas pelo método Avalanche\n" +
                "3. Avaliar sua reserva de emergência e saúde financeira\n\n" +
                "Como posso te orientar hoje?"
            }
        }
    }

    private fun extractActionFromText(text: String): AISuggestedAction? {
        val jsonPattern = Pattern.compile("```(?:json)?\\s*(\\{.*?\\})\\s*```", Pattern.DOTALL)
        val matcher = jsonPattern.matcher(text)
        if (matcher.find()) {
            val jsonStr = matcher.group(1) ?: return null
            return try {
                val element = json.parseToJsonElement(jsonStr).jsonObject
                val actionTypeStr = element["action"]?.jsonPrimitive?.content
                if (actionTypeStr == "CREATE_TRANSACTION") {
                    val amt = element["amount"]?.jsonPrimitive?.content?.toDoubleOrNull() ?: 0.0
                    val desc = element["description"]?.jsonPrimitive?.content ?: "Lançamento"
                    val bucket = element["bucket"]?.jsonPrimitive?.content ?: "Necessidades"
                    val category = element["category"]?.jsonPrimitive?.content ?: "Outros"
                    val type = element["type"]?.jsonPrimitive?.content ?: "expense"

                    AISuggestedAction(
                        type = AIActionType.CREATE_TRANSACTION,
                        description = "Registrar $desc no valor de R$ $amt",
                        transactionPayload = AITransactionPayload(
                            amount = amt,
                            description = desc,
                            bucket = bucket,
                            category = category,
                            type = type
                        )
                    )
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        }
        return null
    }
}
