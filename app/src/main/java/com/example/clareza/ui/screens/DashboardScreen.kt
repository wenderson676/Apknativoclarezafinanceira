package com.example.clareza.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.clareza.data.model.Account
import com.example.clareza.data.model.Debt
import com.example.clareza.data.model.Goal
import com.example.clareza.ui.ClarezaUiState
import com.example.clareza.ui.theme.AmberPending
import com.example.clareza.ui.theme.EmeraldDark
import com.example.clareza.ui.theme.EmeraldPrimary
import com.example.clareza.ui.theme.IndigoTransfer
import com.example.clareza.ui.theme.RoseExpense
import com.example.clareza.util.FinanceUtils

@Composable
fun DashboardScreen(
    state: ClarezaUiState,
    onManageAccounts: () -> Unit,
    onOpenBudgetModes: () -> Unit,
    onOpenGoalDialog: (Goal?) -> Unit,
    onGoalDeposit: (Goal) -> Unit,
    onGoalWithdraw: (Goal) -> Unit,
    onOpenDebtDialog: (Debt?) -> Unit,
    onRefreshVerse: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome / Greeting
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (state.userName.isNullOrBlank()) "Paz e Graça!" else "Olá, ${state.userName}!",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Mordomia e clareza no seu dia a dia",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(
                onClick = onOpenBudgetModes,
                shape = RoundedCornerShape(12.dp),
                color = EmeraldPrimary.copy(alpha = 0.12f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = state.budgetMode,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = EmeraldPrimary
                    )
                }
            }
        }

        // Account Balances Carousel
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Minhas Contas",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                TextButton(
                    onClick = onManageAccounts,
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                ) {
                    Text("Gerenciar", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Total Balance Card
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = EmeraldDark,
                    modifier = Modifier.width(190.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("💎", fontSize = 16.sp)
                            Text(
                                "Patrimônio Total",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                        Text(
                            text = FinanceUtils.formatCurrency(state.totalBalance),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp
                            ),
                            color = Color.White
                        )
                        Text(
                            text = "Líquido: ${FinanceUtils.formatCurrency(state.liquidBalance)}",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }

                // Individual Accounts
                state.accounts.forEach { acc ->
                    val balance = state.accountBalances[acc.id] ?: 0.0
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.width(160.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(acc.icon, fontSize = 16.sp)
                                Text(
                                    acc.name,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                            Text(
                                text = FinanceUtils.formatCurrency(balance),
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (acc.type == "reserva") "Cofrinho / Reserva" else "Conta Corrente",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }

        // Monthly Summary Overview Card
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
                        text = "Balanço do Mês",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (state.monthResult >= 0) "Superávit: +${FinanceUtils.formatCurrency(state.monthResult)}" else "Déficit: ${FinanceUtils.formatCurrency(state.monthResult)}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (state.monthResult >= 0) EmeraldPrimary else RoseExpense
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Receitas
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(EmeraldPrimary))
                            Text("Receitas", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(
                            text = FinanceUtils.formatCurrency(state.totalIncome),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = EmeraldPrimary
                        )
                    }

                    // Despesas
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(RoseExpense))
                            Text("Despesas", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(
                            text = FinanceUtils.formatCurrency(state.totalExpenses),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = RoseExpense
                        )
                    }

                    // Reserva / Aportes
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(IndigoTransfer))
                            Text("Poupado", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(
                            text = FinanceUtils.formatCurrency(state.netSavingsTransfer),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = IndigoTransfer
                        )
                    }
                }
            }
        }

        // 50-30-20 Buckets Allocation
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
                        text = "Divisão dos Potes (${state.budgetMode})",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                state.bucketProgress.forEach { bucket ->
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${bucket.bucketName} (${(bucket.ratio * 100).toInt()}%)",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${FinanceUtils.formatCurrency(bucket.spent)} / ${FinanceUtils.formatCurrency(bucket.limit)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Progress bar
                        LinearProgressIndicator(
                            progress = { (bucket.percentage / 100.0).toFloat().coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = when (bucket.status) {
                                "danger" -> RoseExpense
                                "warning" -> AmberPending
                                else -> EmeraldPrimary
                            },
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }
            }
        }

        // Daily Verse Banner
        Surface(
            onClick = onRefreshVerse,
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MenuBook,
                    contentDescription = null,
                    tint = EmeraldPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = state.dailyVerse,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontStyle = FontStyle.Italic,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Trocar Versículo",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Goals / Metas Card
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
                        text = "Metas & Cofrinhos",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(
                        onClick = { onOpenGoalDialog(null) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Nova Meta", tint = EmeraldPrimary)
                    }
                }

                if (state.goals.isEmpty()) {
                    Text(
                        text = "Nenhuma meta cadastrada ainda. Crie metas para reserva de emergência, viagens ou conquistas.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    state.goals.forEach { goal ->
                        val pct = if (goal.targetAmount > 0) (goal.currentAmount / goal.targetAmount) * 100.0 else 0.0
                        Surface(
                            onClick = { onOpenGoalDialog(goal) },
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(goal.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("${pct.toInt()}%", fontWeight = FontWeight.Bold, color = EmeraldPrimary, fontSize = 12.sp)
                                }

                                LinearProgressIndicator(
                                    progress = { (pct / 100.0).toFloat().coerceIn(0f, 1f) },
                                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                    color = EmeraldPrimary,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "${FinanceUtils.formatCurrency(goal.currentAmount)} de ${FinanceUtils.formatCurrency(goal.targetAmount)}",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Surface(
                                            onClick = { onGoalDeposit(goal) },
                                            shape = RoundedCornerShape(8.dp),
                                            color = EmeraldPrimary.copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                "+ Guardar",
                                                color = EmeraldPrimary,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }

                                        Surface(
                                            onClick = { onGoalWithdraw(goal) },
                                            shape = RoundedCornerShape(8.dp),
                                            color = RoseExpense.copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                "- Resgatar",
                                                color = RoseExpense,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Debts Attack Plan Card
        if (state.debts.isNotEmpty() || state.debtPlan != null) {
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
                        Column {
                            Text(
                                text = "Plano de Ataque a Dívidas",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Método Avalanche de eliminação rápida de juros",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(
                            onClick = { onOpenDebtDialog(null) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Nova Dívida", tint = RoseExpense)
                        }
                    }

                    state.debtPlan?.let { plan ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = RoseExpense.copy(alpha = 0.1f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Total Devido", style = MaterialTheme.typography.labelSmall, color = RoseExpense)
                                    Text(
                                        FinanceUtils.formatCurrency(plan.totalDebt),
                                        fontWeight = FontWeight.Black,
                                        fontSize = 16.sp,
                                        color = RoseExpense
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Previsão de Quitação", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        "${plan.payoffMonths} meses",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        // Debts list
                        plan.sortedDebts.forEach { debt ->
                            val typeInfo = FinanceUtils.DEBT_TYPES_INFO[debt.type]
                            Surface(
                                onClick = { onOpenDebtDialog(debt) },
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Text(debt.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            if (typeInfo != null) {
                                                Text(
                                                    typeInfo.priority,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = when (typeInfo.priority) {
                                                        "Máxima" -> RoseExpense
                                                        "Média" -> MaterialTheme.colorScheme.tertiary
                                                        else -> EmeraldPrimary
                                                    }
                                                )
                                            }
                                        }
                                        Text(
                                            "Credor: ${debt.creditor} • Juros: ${debt.interestRate}% a.m.",
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Text(
                                        FinanceUtils.formatCurrency(debt.totalAmount),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = RoseExpense
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
