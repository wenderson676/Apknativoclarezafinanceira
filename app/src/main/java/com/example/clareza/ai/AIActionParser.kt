package com.example.clareza.ai

import com.example.clareza.ai.model.AIActionType
import com.example.clareza.ai.model.AIDebtPayload
import com.example.clareza.ai.model.AIGoalPayload
import com.example.clareza.ai.model.AISuggestedAction
import com.example.clareza.ai.model.AITransactionPayload
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.util.regex.Pattern

object AIActionParser {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    /**
     * Extrai e valida acoes estruturadas em blocos ```json ... ``` do texto retornado pela IA.
     * Suporta: CREATE_TRANSACTION, CREATE_GOAL, CREATE_DEBT, ADJUST_BUDGET_MODE.
     */
    fun extractActionFromText(text: String): AISuggestedAction? {
        val jsonPattern = Pattern.compile("```(?:json)?\\s*(\\{.*?\\})\\s*```", Pattern.DOTALL)
        val matcher = jsonPattern.matcher(text)
        if (!matcher.find()) return null

        val jsonStr = matcher.group(1) ?: return null

        return try {
            val jsonObject = json.parseToJsonElement(jsonStr).jsonObject
            val actionTypeStr = jsonObject["action"]?.jsonPrimitive?.content ?: return null

            when (actionTypeStr) {
                "CREATE_TRANSACTION" -> {
                    val amt = jsonObject["amount"]?.jsonPrimitive?.content?.toDoubleOrNull() ?: 0.0
                    val desc = jsonObject["description"]?.jsonPrimitive?.content ?: "Lançamento"
                    val bucket = jsonObject["bucket"]?.jsonPrimitive?.content ?: "Necessidades"
                    val category = jsonObject["category"]?.jsonPrimitive?.content ?: "Outros"
                    val type = jsonObject["type"]?.jsonPrimitive?.content ?: "expense"

                    if (amt <= 0) return null

                    AISuggestedAction(
                        type = AIActionType.CREATE_TRANSACTION,
                        description = "Registrar $desc no valor de R$ ${"%.2f".format(amt)}",
                        transactionPayload = AITransactionPayload(
                            amount = amt,
                            description = desc,
                            bucket = bucket,
                            category = category,
                            type = type
                        )
                    )
                }

                "CREATE_GOAL" -> {
                    val title = jsonObject["title"]?.jsonPrimitive?.content ?: "Nova Meta"
                    val targetAmount = jsonObject["targetAmount"]?.jsonPrimitive?.content?.toDoubleOrNull() ?: 0.0
                    val deadline = jsonObject["deadline"]?.jsonPrimitive?.content

                    if (targetAmount <= 0) return null

                    AISuggestedAction(
                        type = AIActionType.CREATE_GOAL,
                        description = "Criar meta '$title' com objetivo de R$ ${"%.2f".format(targetAmount)}",
                        goalPayload = AIGoalPayload(
                            title = title,
                            targetAmount = targetAmount,
                            deadline = deadline
                        )
                    )
                }

                "CREATE_DEBT" -> {
                    val name = jsonObject["name"]?.jsonPrimitive?.content ?: "Nova Dívida"
                    val totalAmount = jsonObject["totalAmount"]?.jsonPrimitive?.content?.toDoubleOrNull() ?: 0.0
                    val monthlyPayment = jsonObject["monthlyPayment"]?.jsonPrimitive?.content?.toDoubleOrNull() ?: 0.0
                    val interestRate = jsonObject["interestRate"]?.jsonPrimitive?.content?.toDoubleOrNull() ?: 0.0
                    val urgencyLevel = jsonObject["urgencyLevel"]?.jsonPrimitive?.content ?: "media"

                    if (totalAmount <= 0) return null

                    AISuggestedAction(
                        type = AIActionType.CREATE_DEBT,
                        description = "Cadastrar dívida '$name' de R$ ${"%.2f".format(totalAmount)}",
                        debtPayload = AIDebtPayload(
                            name = name,
                            totalAmount = totalAmount,
                            monthlyPayment = monthlyPayment,
                            interestRate = interestRate,
                            urgencyLevel = urgencyLevel
                        )
                    )
                }

                "ADJUST_BUDGET_MODE" -> {
                    val mode = jsonObject["mode"]?.jsonPrimitive?.content ?: "50-30-20"
                    AISuggestedAction(
                        type = AIActionType.ADJUST_BUDGET_MODE,
                        description = "Alterar modelo de orçamento para $mode",
                        budgetModePayload = mode
                    )
                }

                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }
}
