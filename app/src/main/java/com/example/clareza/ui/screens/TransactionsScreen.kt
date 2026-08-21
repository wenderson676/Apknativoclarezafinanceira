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
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.clareza.data.model.Transaction
import com.example.clareza.ui.ClarezaUiState
import com.example.clareza.ui.theme.*
import com.example.clareza.util.FinanceUtils
import java.time.LocalDate

@Composable
fun TransactionsScreen(
    state: ClarezaUiState,
    onEditTransaction: (Transaction) -> Unit,
    onDeleteTransaction: (String) -> Unit,
    onTogglePending: (String, Boolean) -> Unit,
    onSaveNote: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchTerm by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("all") } // "all", "expense", "income", "transfer", "pending"
    var isEditingNote by remember { mutableStateOf(false) }
    var noteText by remember(state.monthNote) { mutableStateOf(state.monthNote) }

    val filteredTransactions = remember(state.currentMonthTransactions, searchTerm, selectedFilter) {
        state.currentMonthTransactions.filter { tx ->
            val matchesSearch = searchTerm.isBlank() ||
                    tx.description.contains(searchTerm, ignoreCase = true) ||
                    tx.category.contains(searchTerm, ignoreCase = true) ||
                    tx.bucket.contains(searchTerm, ignoreCase = true)

            val matchesFilter = when (selectedFilter) {
                "income" -> tx.type == "income" || tx.type == "transfer_from_savings"
                "expense" -> tx.type == "expense"
                "transfer" -> tx.type.startsWith("transfer")
                "pending" -> tx.isPending
                else -> true
            }

            matchesSearch && matchesFilter
        }.sortedByDescending { it.date }
    }

    val groupedTransactions = remember(filteredTransactions) {
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)

        filteredTransactions.groupBy { it.date }.map { (dateStr, txs) ->
            val date = try { LocalDate.parse(dateStr) } catch (e: Exception) { today }
            val label = when {
                date.isEqual(today) -> "Hoje, ${FinanceUtils.formatDateBr(dateStr)}"
                date.isEqual(yesterday) -> "Ontem, ${FinanceUtils.formatDateBr(dateStr)}"
                else -> FinanceUtils.formatDateBr(dateStr)
            }
            label to txs
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Page Header & Statement Summary
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Extrato Financeiro",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                        letterSpacing = (-0.5).sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${filteredTransactions.size} lançamentos encontrados",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Mini KPI Summary Strip
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            shadowElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Entradas", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = EmeraldPrimary)
                    Text(
                        FinanceUtils.formatCurrency(state.totalIncome),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = EmeraldPrimary
                    )
                }

                Box(
                    modifier = Modifier
                        .height(26.dp)
                        .width(1.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )

                Column {
                    Text("Saídas", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = RoseExpense)
                    Text(
                        FinanceUtils.formatCurrency(state.totalExpenses),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = RoseExpense
                    )
                }

                Box(
                    modifier = Modifier
                        .height(26.dp)
                        .width(1.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )

                Column(horizontalAlignment = Alignment.End) {
                    val isPos = state.monthResult >= 0
                    Text("Resultado", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        FinanceUtils.formatCurrency(state.monthResult),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = if (isPos) EmeraldPrimary else RoseExpense
                    )
                }
            }
        }

        // Search Bar
        OutlinedTextField(
            value = searchTerm,
            onValueChange = { searchTerm = it },
            placeholder = { Text("Buscar por descrição, categoria ou pote...", fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
            trailingIcon = {
                if (searchTerm.isNotBlank()) {
                    IconButton(onClick = { searchTerm = "" }) {
                        Icon(Icons.Default.Close, contentDescription = "Limpar")
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                focusedBorderColor = EmeraldPrimary
            )
        )

        // Filter Chips Horizontal Scroll
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                "all" to "Todos",
                "expense" to "Despesas",
                "income" to "Receitas",
                "transfer" to "Transferências",
                "pending" to "Futuros / Pendentes"
            ).forEach { (key, label) ->
                val isSelected = selectedFilter == key
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedFilter = key },
                    label = {
                        Text(
                            label,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 12.sp
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = when (key) {
                            "income" -> EmeraldContainerLight
                            "expense" -> RoseExpenseLight
                            "transfer" -> IndigoTransferLight
                            "pending" -> AmberPendingLight
                            else -> MaterialTheme.colorScheme.primaryContainer
                        },
                        selectedLabelColor = when (key) {
                            "income" -> EmeraldDark
                            "expense" -> Color(0xFF9F1239)
                            "transfer" -> Color(0xFF312E81)
                            "pending" -> Color(0xFF92400E)
                            else -> MaterialTheme.colorScheme.onPrimaryContainer
                        }
                    )
                )
            }
        }

        // Devotional / Month Reflections Note Card
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = AmberPending.copy(alpha = 0.15f),
                            modifier = Modifier.size(30.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.EditNote,
                                    contentDescription = null,
                                    tint = AmberPending,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Text(
                            text = "Anotações do Mês",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (!isEditingNote) {
                        TextButton(
                            onClick = { isEditingNote = true },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp), tint = EmeraldPrimary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Editar", fontSize = 12.sp, color = EmeraldPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (isEditingNote) {
                    OutlinedTextField(
                        value = noteText,
                        onValueChange = { noteText = it },
                        placeholder = { Text("Escreva aqui aprendizados, compromissos ou orações financeiras...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp),
                        shape = RoundedCornerShape(14.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = {
                            noteText = state.monthNote
                            isEditingNote = false
                        }) {
                            Text("Cancelar")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                onSaveNote(noteText)
                                isEditingNote = false
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                        ) {
                            Text("Salvar", fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    Text(
                        text = if (state.monthNote.isNotBlank()) state.monthNote else "Nenhuma anotação registrada para este mês ainda. Toque em Editar para registrar metas e reflexões.",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 18.sp),
                        color = if (state.monthNote.isNotBlank()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.clickable { isEditingNote = true }
                    )
                }
            }
        }

        // Transactions List Grouped By Date
        if (groupedTransactions.isEmpty()) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.size(54.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.ReceiptLong,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                    Text(
                        text = "Nenhum lançamento no filtro",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Toque no botão central '+' para registrar sua primeira receita, despesa ou transferência.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            groupedTransactions.forEach { (dateLabel, txs) ->
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = dateLabel,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        shadowElevation = 1.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            txs.forEachIndexed { index, tx ->
                                val isIncome = tx.type == "income" || tx.type == "transfer_from_savings"
                                val isTransfer = tx.type.startsWith("transfer")
                                val accountObj = state.accounts.find { it.id == tx.account }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onEditTransaction(tx) }
                                        .padding(horizontal = 14.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        // Type Icon / Pending Toggle
                                        Surface(
                                            onClick = { onTogglePending(tx.id, tx.isPending) },
                                            shape = RoundedCornerShape(12.dp),
                                            color = if (tx.isPending) AmberPending.copy(alpha = 0.15f)
                                            else if (isTransfer) IndigoTransfer.copy(alpha = 0.15f)
                                            else if (isIncome) EmeraldPrimary.copy(alpha = 0.15f)
                                            else RoseExpense.copy(alpha = 0.15f),
                                            modifier = Modifier.size(42.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = if (tx.isPending) Icons.Default.Schedule
                                                    else if (isTransfer) Icons.Default.SyncAlt
                                                    else if (isIncome) Icons.Default.ArrowDownward
                                                    else Icons.Default.ArrowOutward,
                                                    contentDescription = "Status",
                                                    tint = if (tx.isPending) AmberPending
                                                    else if (isTransfer) IndigoTransfer
                                                    else if (isIncome) EmeraldPrimary
                                                    else RoseExpense,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }

                                        Column(
                                            modifier = Modifier.weight(1f),
                                            verticalArrangement = Arrangement.spacedBy(2.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(
                                                    text = tx.description,
                                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    maxLines = 1
                                                )
                                                if (tx.isPending) {
                                                    Surface(
                                                        shape = RoundedCornerShape(4.dp),
                                                        color = AmberPending.copy(alpha = 0.15f)
                                                    ) {
                                                        Text(
                                                            "PENDENTE",
                                                            fontSize = 8.sp,
                                                            fontWeight = FontWeight.Black,
                                                            color = AmberPending,
                                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                }
                                            }

                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(
                                                    text = "${accountObj?.icon ?: "💳"} ${tx.category}",
                                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                Text("•", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                Text(
                                                    text = tx.bucket,
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.SemiBold
                                                    ),
                                                    color = EmeraldPrimary
                                                )
                                            }
                                        }
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = "${if (isIncome) "+" else if (isTransfer) "" else "-"} ${FinanceUtils.formatCurrency(tx.amount)}",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Black,
                                                fontSize = 14.sp
                                            ),
                                            color = if (isIncome) EmeraldPrimary else if (isTransfer) IndigoTransfer else RoseExpense
                                        )

                                        IconButton(
                                            onClick = { onDeleteTransaction(tx.id) },
                                            modifier = Modifier.size(30.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.Delete,
                                                contentDescription = "Excluir",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                                modifier = Modifier.size(17.dp)
                                            )
                                        }
                                    }
                                }

                                if (index < txs.size - 1) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(horizontal = 14.dp),
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(70.dp))
    }
}
