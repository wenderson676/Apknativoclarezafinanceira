package com.example.clareza.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.clareza.ui.ClarezaUiState
import com.example.clareza.ui.theme.AmberPending
import com.example.clareza.ui.theme.EmeraldDark
import com.example.clareza.ui.theme.EmeraldPrimary
import com.example.clareza.ui.theme.IndigoTransfer
import com.example.clareza.ui.theme.RoseExpense
import com.example.clareza.util.FinanceUtils
import java.time.LocalDate

@Composable
fun ComparisonScreen(
    state: ClarezaUiState,
    onSetBudgetMode: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Diagnóstico, 1: Comparativo, 2: Simulador
    var simulatedAmountText by remember { mutableStateOf("") }

    val diag = state.diagnosticResult

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Header Card
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = EmeraldDark,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "DIAGNÓSTICO & INTELIGÊNCIA",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, fontSize = 10.sp),
                            color = Color(0xFFA7F3D0)
                        )
                        Text(
                            text = "Análise Financeira Avançada",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                }

                Text(
                    text = "Acompanhe a saúde do seu orçamento, elegibilidade de planos e simulações para decisões conscientes.",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 18.sp),
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
        }

        // Subtabs
        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Diagnóstico", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Comparativo", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("Simulador", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            )
        }

        if (selectedTab == 0 && diag != null) {
            // Health Score Card
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Pontuação de Saúde Financeira",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${diag.healthScore}/100",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                color = if (diag.healthScore >= 70) EmeraldPrimary else if (diag.healthScore >= 50) AmberPending else RoseExpense
                            )
                        )
                    }

                    LinearProgressIndicator(
                        progress = { (diag.healthScore / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = if (diag.healthScore >= 70) EmeraldPrimary else if (diag.healthScore >= 50) AmberPending else RoseExpense,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Necessidades: ${(diag.needsPercentage * 100).toInt()}%", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Desejos: ${(diag.wantsPercentage * 100).toInt()}%", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Reserva: ${(diag.savingsPercentage * 100).toInt()}%", fontSize = 11.sp, color = EmeraldPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Recommended Budget Mode Card
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Recomendação de Modelo",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = diag.recommendationReason,
                        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (diag.recommendedMode != null && diag.recommendedMode != state.budgetMode) {
                        Button(
                            onClick = { onSetBudgetMode(diag.recommendedMode) },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Aplicar Modelo ${FinanceUtils.BUDGET_MODES_INFO[diag.recommendedMode]?.name}", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Mode Eligibility Matrix
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Elegibilidade de Modelos",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    diag.eligibleModes.forEach { mode ->
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (mode.isEligible) EmeraldPrimary.copy(alpha = 0.08f) else RoseExpense.copy(alpha = 0.08f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(mode.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    if (!mode.isEligible && mode.ineligibilityReason != null) {
                                        Text(
                                            mode.ineligibilityReason,
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                            color = RoseExpense
                                        )
                                    } else {
                                        Text(
                                            "Compatível com seus gastos atuais",
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                            color = EmeraldPrimary
                                        )
                                    }
                                }

                                Icon(
                                    imageVector = if (mode.isEligible) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                    contentDescription = null,
                                    tint = if (mode.isEligible) EmeraldPrimary else RoseExpense,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Expense Cuts Suggestions
            if (diag.cutSuggestions.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Sugestões de Economia",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Economia: ${FinanceUtils.formatCurrency(diag.totalPotentialSavings)}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = EmeraldPrimary
                            )
                        }

                        diag.cutSuggestions.forEach { cut ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(cut.category, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(
                                            "-${cut.percentageCut.toInt()}% (${FinanceUtils.formatCurrency(cut.suggestedCutAmount)})",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = EmeraldPrimary
                                        )
                                    }
                                    Text(
                                        cut.reasoning,
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else if (selectedTab == 1) {
            // Comparativo de Períodos
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Gastos por Categoria (Este Mês)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    val categoryExpenses = remember(state.currentMonthTransactions) {
                        state.currentMonthTransactions
                            .filter { it.type == "expense" && !it.isPending }
                            .groupBy { it.category }
                            .mapValues { entry -> entry.value.sumOf { it.amount } }
                            .toList()
                            .sortedByDescending { it.second }
                    }

                    if (categoryExpenses.isEmpty()) {
                        Text("Nenhum gasto registrado neste mês.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        val maxExpense = categoryExpenses.maxOf { it.second }
                        categoryExpenses.forEach { (cat, amount) ->
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(cat, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    Text(FinanceUtils.formatCurrency(amount), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = RoseExpense)
                                }
                                LinearProgressIndicator(
                                    progress = { if (maxExpense > 0) (amount / maxExpense).toFloat().coerceIn(0f, 1f) else 0f },
                                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                    color = RoseExpense,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // Safe Purchase Simulator ("Posso Comprar Isso Hoje?")
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Simulador: Posso Comprar Hoje?",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Insira o valor da compra que está planejando para avaliar o impacto nas suas finanças:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = simulatedAmountText,
                        onValueChange = { simulatedAmountText = it },
                        label = { Text("Valor da Compra (R$)") },
                        placeholder = { Text("Ex: 350,00") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true
                    )

                    val simAmount = simulatedAmountText.replace(",", ".").toDoubleOrNull() ?: 0.0
                    if (simAmount > 0.0) {
                        val liquid = state.liquidBalance
                        val remaining = liquid - simAmount
                        val dailyIncome = if (state.totalIncome > 0) state.totalIncome / 30.0 else 0.0
                        val hoursOrDaysOfWork = if (dailyIncome > 0) (simAmount / dailyIncome) else 0.0

                        val isSafe = remaining >= (state.totalExpenses * 0.20) && remaining >= 0

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSafe) EmeraldPrimary.copy(alpha = 0.1f) else RoseExpense.copy(alpha = 0.1f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isSafe) Icons.Default.CheckCircle else Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = if (isSafe) EmeraldPrimary else RoseExpense
                                    )
                                    Text(
                                        text = if (isSafe) "Compra Segura e Aprovada" else "Atenção: Risco ao Orçamento",
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSafe) EmeraldPrimary else RoseExpense
                                    )
                                }

                                Text(
                                    text = if (isSafe) {
                                        "Após esta compra de ${FinanceUtils.formatCurrency(simAmount)}, você ainda manterá ${FinanceUtils.formatCurrency(remaining)} livres em conta."
                                    } else {
                                        "Esta compra consumirá a maior parte do seu saldo disponível, deixando apenas ${FinanceUtils.formatCurrency(remaining)}. Recomenda-se adiar ou parcelar sem juros."
                                    },
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 18.sp),
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                if (hoursOrDaysOfWork > 0) {
                                    Text(
                                        text = "⏱️ Essa compra equivale a aproximadamente ${String.format(java.util.Locale.US, "%.1f", hoursOrDaysOfWork)} dias de trabalho da sua renda.",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(60.dp))
    }
}
