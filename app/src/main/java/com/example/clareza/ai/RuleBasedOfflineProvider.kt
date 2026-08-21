package com.example.clareza.ai

import com.example.clareza.ai.model.AIActionType
import com.example.clareza.ai.model.AIRequest
import com.example.clareza.ai.model.AIResponse
import com.example.clareza.ai.model.AISuggestedAction
import com.example.clareza.ai.model.AITransactionPayload
import com.example.clareza.domain.FinancialContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.util.regex.Pattern

/**
 * Motor offline determinístico de regras e heurísticas avançadas.
 * Atua como motor ultra-rápido de resposta imediata e fallback confiável.
 */
open class RuleBasedOfflineProvider : AIProvider {

    override val name: String = "Motor Offline Determinístico (Regras)"
    override val isAvailable: Boolean = true
    override val isOffline: Boolean = true

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    override suspend fun generateResponse(
        prompt: String,
        request: AIRequest
    ): AIResponse {
        val startTime = System.currentTimeMillis()
        val query = request.userMessage.trim()
        val ctx = request.financialContext

        // 1. Tentar interpretar comando explícito de lançamento financeiro
        val quickExpenseAction = parseQuickExpense(query)
        if (quickExpenseAction != null) {
            val latency = System.currentTimeMillis() - startTime
            val tx = quickExpenseAction.transactionPayload!!
            val text = "Entendido! Identifiquei o lançamento de R$ ${"%.2f".format(tx.amount)} para '${tx.description}' no pote '${tx.bucket}' (Categoria: ${tx.category}). Deseja confirmar e salvar?"
            return AIResponse(
                text = text,
                suggestedAction = quickExpenseAction,
                isOffline = true,
                latencyMs = latency
            )
        }

        // 2. Análise contextual refinada baseada no FinancialContext do usuário
        val responseText = analyzeQueryWithContext(query, ctx, prompt)
        val extractedAction = extractActionFromText(responseText)

        val latency = System.currentTimeMillis() - startTime
        return AIResponse(
            text = responseText,
            suggestedAction = extractedAction,
            isOffline = true,
            latencyMs = latency
        )
    }

    /**
     * Parser monetário fortalecido:
     * Ignores números de parcela (ex: "parcela 3 de R$ 450")
     * Suporta "1.500,50", "1500,00", "R$ 450", "45.90"
     */
    fun parseQuickExpense(query: String): AISuggestedAction? {
        val lower = query.lowercase()
        val isExpense = lower.contains("gastei") || lower.contains("paguei") || lower.contains("comprei") || lower.contains("custou")
        val isIncome = lower.contains("recebi") || lower.contains("ganhei") || lower.contains("salário") || lower.contains("renda")

        if (!isExpense && !isIncome) return null

        // Remover padrões de parcelas para evitar capturar o número da parcela como valor
        val cleanQuery = lower
            .replace(Regex("(?i)parcela\\s*\\d+\\b"), "")
            .replace(Regex("(?i)n[ºo]?\\s*\\d+\\b"), "")
            .replace(Regex("(?i)\\d+\\s*x\\b"), "")

        // Regex que busca valores monetários explicitamente
        val monetaryPattern = Pattern.compile(
            "(?:r\\$|rs|de|valor\\s+de|custou|paguei|gastei)?\\s*(\\d{1,3}(?:\\.\\d{3})*(?:,\\d{1,2})?|\\d+(?:[.,]\\d{1,2})?)"
        )
        val matcher = monetaryPattern.matcher(cleanQuery)

        var amount: Double? = null
        while (matcher.find()) {
            val rawStr = matcher.group(1)?.trim() ?: continue
            // Converter formato brasileiro 1.500,50 ou formato padrão 1500.50
            val normalizedStr = if (rawStr.contains(",") && rawStr.contains(".")) {
                rawStr.replace(".", "").replace(",", ".")
            } else if (rawStr.contains(",")) {
                rawStr.replace(",", ".")
            } else {
                rawStr
            }
            val parsed = normalizedStr.toDoubleOrNull()
            if (parsed != null && parsed > 0) {
                amount = parsed
                break
            }
        }

        if (amount == null || amount <= 0) return null

        // Extrair descrição removendo palavras de comando
        val description = query
            .replace(Regex("(?i)(gastei|paguei|comprei|recebi|ganhei|r\\$|rs|no|na|com|de|em|para|valor|parcela|\\d+x|\\d+\\b)"), " ")
            .trim()
            .replace(Regex("\\s+"), " ")
            .ifBlank { if (isIncome) "Renda Extra" else "Despesa Geral" }

        val (bucket, category) = if (isIncome) {
            Pair("Renda", "Salário / Rendimentos")
        } else {
            classifyCategory(description)
        }

        return AISuggestedAction(
            type = AIActionType.CREATE_TRANSACTION,
            description = if (isIncome) "Adicionar Receita de R$ ${"%.2f".format(amount)}" else "Adicionar Despesa de R$ ${"%.2f".format(amount)}",
            transactionPayload = AITransactionPayload(
                amount = amount,
                description = description.replaceFirstChar { it.uppercase() },
                bucket = bucket,
                category = category,
                type = if (isIncome) "income" else "expense"
            )
        )
    }

    /**
     * Classificação refinada com taxonomia própria e específica
     */
    fun classifyCategory(desc: String): Pair<String, String> {
        val d = desc.lowercase()
        return when {
            // Moradia / Aluguel
            d.contains("aluguel") || d.contains("condomínio") || d.contains("iptu") -> {
                Pair("Necessidades", "Moradia / Aluguel")
            }
            // Habitação / Contas Residenciais
            d.contains("luz") || d.contains("água") || d.contains("energia") || d.contains("gás") || d.contains("internet") || d.contains("tv") || d.contains("telefone") -> {
                Pair("Necessidades", "Contas Residenciais")
            }
            // Alimentação / Mercado
            d.contains("mercado") || d.contains("supermercado") || d.contains("feira") || d.contains("compras") || d.contains("açougue") || d.contains("padaria") -> {
                Pair("Necessidades", "Alimentação / Mercado")
            }
            // Saúde / Farmácia
            d.contains("farmácia") || d.contains("remédio") || d.contains("médico") || d.contains("hospital") || d.contains("dentista") || d.contains("exame") || d.contains("plano de saúde") -> {
                Pair("Necessidades", "Saúde / Farmácia")
            }
            // Transporte
            d.contains("gasolina") || d.contains("combustível") || d.contains("etanol") || d.contains("diesel") || d.contains("estacionamento") || d.contains("pedágio") || d.contains("ipva") || d.contains("oficina") -> {
                Pair("Necessidades", "Transporte / Veículo")
            }
            d.contains("uber") || d.contains("99") || d.contains("táxi") -> {
                Pair("Desejos", "Transporte / Aplicativo")
            }
            // Lazer / Alimentação Fora
            d.contains("restaurante") || d.contains("ifood") || d.contains("lanche") || d.contains("pizza") || d.contains("hambúrguer") || d.contains("sushi") || d.contains("cinema") || d.contains("show") || d.contains("jogo") || d.contains("cerveja") || d.contains("bar") -> {
                Pair("Desejos", "Lazer / Alimentação Fora")
            }
            // Cuidados Pessoais / Vestuário
            d.contains("roupa") || d.contains("calçado") || d.contains("sapato") || d.contains("cabeleireiro") || d.contains("barbeiro") || d.contains("cosmético") -> {
                Pair("Desejos", "Cuidados Pessoais / Roupas")
            }
            // Educação
            d.contains("curso") || d.contains("faculdade") || d.contains("escola") || d.contains("livro") || d.contains("mensalidade") -> {
                Pair("Necessidades", "Educação")
            }
            // Dívidas / Reserva
            d.contains("dívida") || d.contains("empréstimo") || d.contains("cartão") || d.contains("fatura") || d.contains("reserva") || d.contains("cofrinho") || d.contains("quitação") -> {
                Pair("Reserva/Dívidas", "Reserva / Dívidas")
            }
            else -> {
                Pair("Necessidades", "Outros Essenciais")
            }
        }
    }

    private fun analyzeQueryWithContext(
        query: String,
        ctx: FinancialContext?,
        fullPrompt: String
    ): String {
        val q = query.lowercase()

        if (ctx == null) {
            return "Olá! Ainda não temos dados financeiros suficientes registrados. Cadastre suas primeiras receitas, despesas e saldo inicial para receber uma análise personalizada da sua saúde financeira."
        }

        return when {
            q.contains("score") || q.contains("saúde") || q.contains("pontuação") || q.contains("diagnóstico") -> {
                buildString {
                    appendLine("📊 **Diagnóstico de Saúde Financeira (Score: ${ctx.healthScore}/100)**")
                    appendLine("• **Fase Atual:** ${ctx.stage.title}")
                    appendLine("• **Nível de Risco:** ${ctx.riskLevel.label}")
                    appendLine()
                    appendLine("**Detalhamento dos 4 Pilares:**")
                    appendLine("• Fluxo de Caixa: ${ctx.healthScoreBreakdown.cashFlowScore}/25")
                    appendLine("• Reserva & Segurança: ${ctx.healthScoreBreakdown.reserveScore}/25")
                    appendLine("• Nível de Dívidas: ${ctx.healthScoreBreakdown.debtScore}/25")
                    appendLine("• Disciplina de Orçamento: ${ctx.healthScoreBreakdown.budgetDisciplineScore}/25")
                    if (ctx.healthScoreBreakdown.explanationFactors.isNotEmpty()) {
                        appendLine()
                        appendLine("**Fatores Relevantes:**")
                        ctx.healthScoreBreakdown.explanationFactors.forEach { factor ->
                            appendLine("• $factor")
                        }
                    }
                    appendLine()
                    appendLine("💡 **Foco Estratégico Recomendado:** ${ctx.primaryStrategicFocus}")
                }
            }

            q.contains("dívida") || q.contains("quitar") || q.contains("juros") || q.contains("avalanche") -> {
                buildString {
                    appendLine("💳 **Situação das Dívidas**")
                    if (ctx.totalDebt <= 0) {
                        appendLine("Parabéns! Você não possui dívidas registradas no momento.")
                    } else {
                        appendLine("• **Total em Dívidas:** R$ ${"%.2f".format(ctx.totalDebt)}")
                        appendLine("• **Mínimo Mensal:** R$ ${"%.2f".format(ctx.monthlyDebtMinimums)} (${"%.1f".format(ctx.debtToIncomeRatio)}% da sua renda)")
                        if (ctx.highPriorityDebtCount > 0) {
                            appendLine("• **Dívidas Urgentes/Prioritárias:** ${ctx.highPriorityDebtCount} (Prioridade Máxima para evitar sanções/cortes)")
                        }
                        if (ctx.estimatedDebtPayoffMonths > 0) {
                            appendLine("• **Tempo Estimado para Quitação:** ~${ctx.estimatedDebtPayoffMonths} meses")
                        }
                        appendLine()
                        appendLine("🎯 **Recomendação (Método Avalanche):** Pague o valor mínimo de todas as contas para evitar inadimplência e direcione todo valor sobressalente para quitar a dívida de maior taxa de juros.")
                    }
                }
            }

            q.contains("reserva") || q.contains("emergência") || q.contains("cofrinho") || q.contains("guardar") -> {
                buildString {
                    appendLine("🛡️ **Reserva de Emergência e Autonomia**")
                    appendLine("• **Saldo Atual na Reserva:** R$ ${"%.2f".format(ctx.savingsBalance)}")
                    if (ctx.isAutonomyDataSufficient) {
                        appendLine("• **Despesas Essenciais Médias:** R$ ${"%.2f".format(ctx.averageEssentialMonthlyExpenses)}/mês (com base em ${ctx.historicalMonthsUsed} mês/meses)")
                        appendLine("• **Autonomia de Cobertura:** ${"%.1f".format(ctx.monthsOfReserveCovered)} meses de despesas essenciais garantidos")
                    } else {
                        appendLine("• **Autonomia:** Dados históricos de despesas essenciais ainda insuficientes. Continue registrando seus gastos de 'Necessidades' para calcular sua autonomia exata.")
                    }
                    appendLine()
                    appendLine("📌 **Meta de Segurança:** O recomendado é acumular de 3 a 6 meses de despesas essenciais.")
                }
            }

            q.contains("resumo") || q.contains("como estou") || q.contains("visão geral") || q.contains("situacao") -> {
                buildString {
                    appendLine("📈 **Resumo Geral Financeiro**")
                    appendLine("• **Renda Mensal:** R$ ${"%.2f".format(ctx.totalIncome)}")
                    appendLine("• **Despesas Totais:** R$ ${"%.2f".format(ctx.totalExpenses)}")
                    appendLine("• **Resultado do Mês:** R$ ${"%.2f".format(ctx.monthResult)}")
                    appendLine("• **Potes Reais:** Necessidades (${"%.1f".format(ctx.needsPercentage)}%) | Desejos (${"%.1f".format(ctx.wantsPercentage)}%) | Poupança (${"%.1f".format(ctx.savingsPercentage)}%)")
                    if (ctx.activeGoals.isNotEmpty()) {
                        appendLine()
                        appendLine("**Metas Ativas:**")
                        ctx.activeGoals.forEach { g ->
                            appendLine("• ${g.title}: R$ ${"%.2f".format(g.currentAmount)} / R$ ${"%.2f".format(g.targetAmount)} (${"%.0f".format(g.progressPercentage)}%)")
                        }
                    }
                    appendLine()
                    appendLine("🎯 **Direcionamento:** ${ctx.primaryStrategicFocus}")
                }
            }

            else -> {
                buildString {
                    appendLine("Olá! Sou o Assistente Financeiro do Clareza.")
                    appendLine("Atualmente suas finanças estão classificadas como **${ctx.stage.title}** (Score: ${ctx.healthScore}/100).")
                    appendLine()
                    appendLine("Você pode me perguntar:")
                    appendLine("1. 'Como está meu score/diagnóstico?'")
                    appendLine("2. 'Como estão minhas dívidas?'")
                    appendLine("3. 'Qual minha autonomia de reserva?'")
                    appendLine("4. 'Gastei 150 no mercado' (para registrar um lançamento)")
                }
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
