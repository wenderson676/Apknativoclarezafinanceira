package com.example.clareza.util

import com.example.clareza.data.model.Account
import com.example.clareza.data.model.Debt
import com.example.clareza.data.model.Transaction
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.roundToInt

data class BudgetModeInfo(
    val key: String,
    val name: String,
    val description: String,
    val explanation: String,
    val ratios: Map<String, Double>
)

data class DebtTypeInfo(
    val key: String,
    val label: String,
    val priority: String, // "Máxima", "Média", "Baixa"
    val desc: String
)

data class ExpenseCutSuggestion(
    val category: String,
    val currentAmount: Double,
    val suggestedCutAmount: Double,
    val newAmount: Double,
    val percentageCut: Double,
    val impactLevel: String, // "Alto", "Médio", "Baixo"
    val reasoning: String
)

data class ModeEligibility(
    val modeKey: String,
    val name: String,
    val needsRatio: Double,
    val wantsRatio: Double,
    val savingsRatio: Double,
    val isEligible: Boolean,
    val ineligibilityReason: String? = null,
    val fitsNeeds: Boolean
)

data class DiagnosticResult(
    val totalIncome: Double,
    val totalExpenses: Double,
    val needs: Double,
    val wants: Double,
    val savings: Double,
    val needsPercentage: Double,
    val wantsPercentage: Double,
    val savingsPercentage: Double,
    val healthScore: Int,
    val eligibleModes: List<ModeEligibility>,
    val recommendedMode: String?,
    val recommendationReason: String,
    val cutSuggestions: List<ExpenseCutSuggestion>,
    val totalPotentialSavings: Double
)

object FinanceUtils {

    val BRL_LOCALE = Locale("pt", "BR")

    fun formatCurrency(amount: Double): String {
        val format = NumberFormat.getCurrencyInstance(BRL_LOCALE)
        return format.format(amount)
    }

    fun formatDateBr(dateStr: String): String {
        return try {
            val date = LocalDate.parse(dateStr)
            val formatter = DateTimeFormatter.ofPattern("dd 'de' MMMM", BRL_LOCALE)
            date.format(formatter)
        } catch (e: Exception) {
            dateStr
        }
    }

    val CATEGORIES = mapOf(
        "Necessidades" to listOf(
            "Moradia (Aluguel/Cond.)",
            "Supermercado & Feira",
            "Contas Fixas (Água/Luz/Net)",
            "Transporte & Combustível",
            "Saúde & Medicamentos",
            "Educação",
            "Manutenção da Casa",
            "Impostos & Taxas"
        ),
        "Desejos" to listOf(
            "Lazer & Passeios",
            "Restaurantes & Delivery",
            "Streaming & Assinaturas",
            "Compras & Vestuário",
            "Cuidados Pessoais & Salão",
            "Viagens & Férias",
            "Presentes & Mimos",
            "Hobbies & Eletrônicos"
        ),
        "Reserva/Dívidas" to listOf(
            "Reserva de Emergência",
            "Quitação de Dívidas",
            "Aporte de Investimento",
            "Previdência Privada",
            "Meta / Sonho Específico",
            "Fundo para Imprevistos"
        ),
        "Renda" to listOf(
            "Salário Principal",
            "Adiantamento / 13º",
            "Renda Extra / Freelance",
            "Rendimentos / Dividendos",
            "Venda de Itens",
            "Reembolsos",
            "Presente / Doação Recebida",
            "Outras Entradas"
        ),
        "Transferência" to listOf(
            "Aporte Cofrinho",
            "Resgate de Reserva",
            "Transferência de Saldos",
            "Investimentos",
            "Ajuste de Conta"
        )
    )

    val BUDGET_MODES_INFO = mapOf(
        "50-30-20" to BudgetModeInfo(
            key = "50-30-20",
            name = "Padrão (50/30/20)",
            description = "50% Necessidades • 30% Desejos • 20% Reserva",
            explanation = "O equilíbrio clássico ideal para quem tem renda estável, contas sob controle e quer poupar com consistência.",
            ratios = mapOf("Necessidades" to 0.50, "Desejos" to 0.30, "Reserva/Dívidas" to 0.20)
        ),
        "80-10-10" to BudgetModeInfo(
            key = "80-10-10",
            name = "Sobrevivência (80/10/10)",
            description = "80% Necessidades • 10% Desejos • 10% Reserva",
            explanation = "Desenvolvido para orçamentos apertados com custo de vida elevado, mantendo um respiro essencial.",
            ratios = mapOf("Necessidades" to 0.80, "Desejos" to 0.10, "Reserva/Dívidas" to 0.10)
        ),
        "90-5-5" to BudgetModeInfo(
            key = "90-5-5",
            name = "Crise / Reestruturação (90/5/5)",
            description = "90% Necessidades • 5% Desejos • 5% Reserva",
            explanation = "Para momentos de emergência e aperto extremo, garantindo alimentação, moradia e serviços básicos.",
            ratios = mapOf("Necessidades" to 0.90, "Desejos" to 0.05, "Reserva/Dívidas" to 0.05)
        ),
        "70-0-30" to BudgetModeInfo(
            key = "70-0-30",
            name = "Quitar Dívidas (70/0/30)",
            description = "70% Necessidades • 0% Desejos • 30% Dívidas",
            explanation = "Foco total e radical em eliminar juros e dívidas acumuladas no menor tempo possível.",
            ratios = mapOf("Necessidades" to 0.70, "Desejos" to 0.00, "Reserva/Dívidas" to 0.30)
        ),
        "50-20-30" to BudgetModeInfo(
            key = "50-20-30",
            name = "Prosperar / Acelerar (50/20/30)",
            description = "50% Necessidades • 20% Desejos • 30% Investimentos",
            explanation = "Para quem busca acelerar a independência financeira e construir patrimônio sólido rapidamente.",
            ratios = mapOf("Necessidades" to 0.50, "Desejos" to 0.20, "Reserva/Dívidas" to 0.30)
        )
    )

    val DEBT_TYPES_INFO = mapOf(
        "rent_late" to DebtTypeInfo("rent_late", "Aluguel Atrasado", "Máxima", "Risco de despejo ou perda de moradia."),
        "utility_risk" to DebtTypeInfo("utility_risk", "Água/Luz em Risco de Corte", "Máxima", "Serviços essenciais que podem ser interrompidos."),
        "pension" to DebtTypeInfo("pension", "Pensão Alimentícia", "Máxima", "Risco de sanções legais graves."),
        "loan_shark" to DebtTypeInfo("loan_shark", "Agiota / Empréstimo Informal urgente", "Máxima", "Altíssimo risco pessoal ou juros abusivos de curto prazo."),
        "card_revolving" to DebtTypeInfo("card_revolving", "Cartão de Crédito (Rotativo/Atrasado)", "Máxima", "Os maiores juros do mercado financeiro brasileiro."),
        "loan_installments" to DebtTypeInfo("loan_installments", "Empréstimos Parcelados (Banco)", "Média", "Empréstimo pessoal com parcelas recorrentes e juros médios."),
        "card_installments" to DebtTypeInfo("card_installments", "Fatura de Cartão Parcelada", "Média", "Financiamento da fatura com taxa de juros parcelada."),
        "store_installments" to DebtTypeInfo("store_installments", "Carnê ou Parcelamento de Loja", "Média", "Financiamento de consumo direto com estabelecimento."),
        "no_interest" to DebtTypeInfo("no_interest", "Dívida Sem Juros", "Baixa", "Compras parceladas sem cobrança de encargos ativos."),
        "family" to DebtTypeInfo("family", "Empréstimo com Familiar ou Amigo", "Baixa", "Dívida de relacionamento, sem pressão agressiva de juros."),
        "other" to DebtTypeInfo("other", "Outras Parcelas Leves", "Baixa", "Outros compromissos financeiros de baixo impacto.")
    )

    val VERSES = listOf(
        "\"O prudente percebe o perigo e busca refúgio; o inexperiente segue adiante e sofre as consequências.\" - Provérbios 27:12",
        "\"Os planos bem elaborados levam à fartura; mas o apressado sempre acaba na miséria.\" - Provérbios 21:5",
        "\"O rico domina sobre o pobre, e o que toma emprestado é servo do que empresta.\" - Provérbios 22:7",
        "\"Honra ao Senhor com os teus bens e com as primícias de toda a tua renda.\" - Provérbios 3:9",
        "\"Quem é fiel no pouco também é fiel no muito.\" - Lucas 16:10",
        "\"Não andeis ansiosos pela vossa vida, quanto ao que haveis de comer ou beber... vosso Pai sabe que precisais.\" - Mateus 6:25,32",
        "\"Mais vale o pouco com o temor do Senhor do que um grande tesouro onde há inquietação.\" - Provérbios 15:16"
    )

    fun getRandomVerse(): String {
        return VERSES.random()
    }

    fun calculateDiagnostic(
        transactions: List<Transaction>,
        currentBalance: Double,
        budgetMode: String,
        accounts: List<Account>,
        debts: List<Debt>
    ): DiagnosticResult {
        val isReserva = { accId: String? ->
            accId == "reserva" || accounts.find { it.id == accId }?.type == "reserva"
        }

        val totalIncome = transactions
            .filter { it.type == "income" && !it.isPending && !isReserva(it.account) }
            .sumOf { it.amount }

        val totalExpenses = transactions
            .filter { it.type == "expense" && !it.isPending && !isReserva(it.account) }
            .sumOf { it.amount }

        val needs = transactions
            .filter { it.type == "expense" && it.bucket == "Necessidades" && !it.isPending && !isReserva(it.account) }
            .sumOf { it.amount }

        val wants = transactions
            .filter { it.type == "expense" && it.bucket == "Desejos" && !it.isPending && !isReserva(it.account) }
            .sumOf { it.amount }

        val savings = transactions
            .filter { 
                !it.isPending && !isReserva(it.account) && (
                    (it.type == "expense" && it.bucket == "Reserva/Dívidas") ||
                    it.type == "transfer_to_savings"
                )
            }
            .sumOf { it.amount }

        val activeDebtsValue = debts.sumOf { it.totalAmount }
        val budgetBase = if (totalIncome > 0) totalIncome else max(1.0, currentBalance)

        val needsPercentage = if (budgetBase > 0) (needs / budgetBase) else 0.0
        val wantsPercentage = if (budgetBase > 0) (wants / budgetBase) else 0.0
        val savingsPercentage = if (budgetBase > 0) (savings / budgetBase) else 0.0

        // Mode evaluations
        val allModes = listOf("50-30-20", "80-10-10", "90-5-5", "70-0-30", "50-20-30")
        val modeEvaluations = allModes.map { key ->
            val info = BUDGET_MODES_INFO[key] ?: BUDGET_MODES_INFO["50-30-20"]!!
            val maxNeedsRatio = info.ratios["Necessidades"] ?: 0.5
            val maxWantsRatio = info.ratios["Desejos"] ?: 0.3
            val maxSavingsRatio = info.ratios["Reserva/Dívidas"] ?: 0.2

            val fitsNeeds = needsPercentage <= maxNeedsRatio + 0.02
            var isEligible = true
            var reason = ""

            if (!fitsNeeds) {
                isEligible = false
                reason = "Contas essenciais (${(needsPercentage * 100).roundToInt()}%) ultrapassam o limite de ${(maxNeedsRatio * 100).roundToInt()}% deste modo."
            } else if (key == "70-0-30" && wantsPercentage > 0.15) {
                isEligible = false
                reason = "O modo 70/0/30 exige zerar supérfluos, mas você tem ${(wantsPercentage * 100).roundToInt()}% comprometidos com lazer."
            } else if (needsPercentage + wantsPercentage > 0.98 && maxSavingsRatio > 0.10) {
                isEligible = false
                reason = "Seus gastos totais não deixam margem para os ${(maxSavingsRatio * 100).roundToInt()}% de reservas deste modo."
            }

            ModeEligibility(
                modeKey = key,
                name = info.name,
                needsRatio = maxNeedsRatio,
                wantsRatio = maxWantsRatio,
                savingsRatio = maxSavingsRatio,
                isEligible = isEligible,
                ineligibilityReason = if (isEligible) null else reason,
                fitsNeeds = fitsNeeds
            )
        }

        val eligibleModes = modeEvaluations.filter { it.isEligible }
        var recommendedMode: String? = null
        var modeRecommendationReason = ""

        if (eligibleModes.isNotEmpty()) {
            if (activeDebtsValue > 0 && eligibleModes.any { it.modeKey == "70-0-30" }) {
                recommendedMode = "70-0-30"
                modeRecommendationReason = "Você possui dívidas ativas e suas contas fixas cabem dentro de 70%. Recomendamos o modo Quitar Dívidas (70/0/30)."
            } else if (eligibleModes.any { it.modeKey == "50-30-20" }) {
                recommendedMode = "50-30-20"
                modeRecommendationReason = "Suas contas essenciais consomem menos de 50% da renda. O modo Padrão (50/30/20) é o mais equilibrado para você!"
            } else if (eligibleModes.any { it.modeKey == "50-20-30" }) {
                recommendedMode = "50-20-30"
                modeRecommendationReason = "Suas contas cabem em 50% e você pode acelerar a construção de patrimônio com o modo Prosperar (50/20/30)."
            } else if (eligibleModes.any { it.modeKey == "80-10-10" }) {
                recommendedMode = "80-10-10"
                modeRecommendationReason = "Seus custos essenciais exigem até 80% da renda. Os modos de 50% foram desqualificados por não cobrirem suas contas."
            } else if (eligibleModes.any { it.modeKey == "90-5-5" }) {
                recommendedMode = "90-5-5"
                modeRecommendationReason = "Suas contas essenciais exigem quase toda a renda. O modo Crise (90/5/5) é o plano mais realista que suporta suas necessidades."
            } else {
                recommendedMode = eligibleModes.first().modeKey
                modeRecommendationReason = "O modo ${BUDGET_MODES_INFO[recommendedMode]?.name} é o mais adequado para seu perfil de gastos atual."
            }
        } else {
            recommendedMode = null
            modeRecommendationReason = "Suas contas essenciais consomem ${(needsPercentage * 100).roundToInt()}% da renda, ultrapassando os limites padrões. Sugerimos aplicar cortes de gastos."
        }

        // Expense cuts suggestions
        val nonEssentialCategories = listOf("Lazer", "Restaurantes", "Compras", "Assinaturas", "Cuidados Pessoais", "Presentes", "Delivery", "Entretenimento")
        val categoryExpenses = mutableMapOf<String, Double>()

        transactions.forEach { t ->
            if (t.type == "expense" && !t.isPending && !isReserva(t.account)) {
                if (t.bucket == "Desejos" || nonEssentialCategories.any { t.category.contains(it, ignoreCase = true) }) {
                    categoryExpenses[t.category] = (categoryExpenses[t.category] ?: 0.0) + t.amount
                }
            }
        }

        val cutSuggestions = mutableListOf<ExpenseCutSuggestion>()
        var totalPotentialSavings = 0.0

        categoryExpenses.forEach { (cat, amount) ->
            if (amount > 0) {
                var cutPercent = 0.40
                var reasoning = "Gasto flexível de estilo de vida que pode ser reduzido temporariamente."
                var impact = "Médio"

                if (cat.contains("Assinatura", ignoreCase = true) || cat.contains("Streaming", ignoreCase = true)) {
                    cutPercent = 0.50
                    reasoning = "Cancele ou pause streamings e assinaturas que não são usados semanalmente."
                    impact = "Baixo"
                } else if (cat.contains("Delivery", ignoreCase = true) || cat.contains("Restaurante", ignoreCase = true)) {
                    cutPercent = 0.45
                    reasoning = "Substitua parte das refeições fora por preparo em casa para economizar."
                    impact = "Médio"
                } else if (cat.contains("Compras", ignoreCase = true) || cat.contains("Vestuário", ignoreCase = true)) {
                    cutPercent = 0.60
                    reasoning = "Adie a compra de roupas e eletrônicos não urgentes para os próximos meses."
                    impact = "Alto"
                }

                val cutAmount = amount * cutPercent
                totalPotentialSavings += cutAmount

                cutSuggestions.add(
                    ExpenseCutSuggestion(
                        category = cat,
                        currentAmount = amount,
                        suggestedCutAmount = cutAmount,
                        newAmount = amount - cutAmount,
                        percentageCut = cutPercent * 100,
                        impactLevel = impact,
                        reasoning = reasoning
                    )
                )
            }
        }

        // Calculate financial health score (0-100)
        var score = 100
        if (totalIncome > 0) {
            val expenseRatio = totalExpenses / totalIncome
            if (expenseRatio > 1.0) score -= 35
            else if (expenseRatio > 0.9) score -= 20
            else if (expenseRatio > 0.8) score -= 10
        } else {
            score -= 20
        }

        if (needsPercentage > 0.7) score -= 15
        if (activeDebtsValue > 0) score -= 15
        if (savingsPercentage < 0.1) score -= 15
        if (currentBalance < 0) score -= 20

        score = max(0, score)

        return DiagnosticResult(
            totalIncome = totalIncome,
            totalExpenses = totalExpenses,
            needs = needs,
            wants = wants,
            savings = savings,
            needsPercentage = needsPercentage,
            wantsPercentage = wantsPercentage,
            savingsPercentage = savingsPercentage,
            healthScore = score,
            eligibleModes = modeEvaluations,
            recommendedMode = recommendedMode,
            recommendationReason = modeRecommendationReason,
            cutSuggestions = cutSuggestions.sortedByDescending { it.suggestedCutAmount },
            totalPotentialSavings = totalPotentialSavings
        )
    }
}
