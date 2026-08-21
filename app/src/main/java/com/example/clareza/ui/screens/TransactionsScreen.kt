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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.clareza.data.model.Account
import com.example.clareza.data.model.Transaction
import com.example.clareza.ui.ClarezaUiState
import com.example.clareza.ui.theme.AmberPending
import com.example.clareza.ui.theme.EmeraldPrimary
import com.example.clareza.ui.theme.IndigoTransfer
import com.example.clareza.ui.theme.RoseExpense
import com.example.clareza.util.FinanceUtils
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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
    var selectedFilter by remember { mutableStateOf("all") } // "all", "income", "expense", "transfer", "pending"
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
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Extrato & Lançamentos",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )

        // Devotional / Month Notes Card
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.25f),
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
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = null,
                            tint = AmberPending,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Anotações & Reflexões do Mês",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (!isEditingNote) {
                        TextButton(
                            onClick = { isEditingNote = true },
                            contentPadding = PaddingValues(0.dp)
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
                        placeholder = { Text("Escreva aqui os aprendizados, metas e observações deste mês...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
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
                        text = if (state.monthNote.isNotBlank()) state.monthNote else "Nenhuma anotação registrada para este mês ainda. Clique em Editar para adicionar.",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (state.monthNote.isNotBlank()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.clickable { isEditingNote = true }
                    )
                }
            }
        }

        // Search Bar
        OutlinedTextField(
            value = searchTerm,
            onValueChange = { searchTerm = it },
            placeholder = { Text("Buscar lançamentos...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchTerm.isNotBlank()) {
                    IconButton(onClick = { searchTerm = "" }) {
                        Icon(Icons.Default.Close, contentDescription = "Limpar")
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            singleLine = true
        )

        // Filters horizontal scroll
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                "all" to "Todos",
                "income" to "Receitas",
                "expense" to "Despesas",
                "transfer" to "Transferências",
                "pending" to "Futuros / Pendentes"
            ).forEach { (key, label) ->
                val isSelected = selectedFilter == key
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedFilter = key },
                    label = { Text(label, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = when (key) {
                            "income" -> EmeraldPrimary.copy(alpha = 0.2f)
                            "expense" -> RoseExpense.copy(alpha = 0.2f)
                            "transfer" -> IndigoTransfer.copy(alpha = 0.2f)
                            "pending" -> AmberPending.copy(alpha = 0.2f)
                            else -> MaterialTheme.colorScheme.primaryContainer
                        }
                    )
                )
            }
        }

        // Transactions list grouped by date
        if (groupedTransactions.isEmpty()) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Nenhum lançamento encontrado para este período.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            groupedTransactions.forEach { (dateLabel, txs) ->
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = dateLabel,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 2.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            txs.forEachIndexed { index, tx ->
                                val isIncome = tx.type == "income" || tx.type == "transfer_from_savings"
                                val isTransfer = tx.type.startsWith("transfer")
                                val accountObj = state.accounts.find { it.id == tx.account }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onEditTransaction(tx) }
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        // Pending toggle button
                                        IconButton(
                                            onClick = { onTogglePending(tx.id, tx.isPending) },
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(
                                                    if (tx.isPending) AmberPending.copy(alpha = 0.15f)
                                                    else if (isTransfer) IndigoTransfer.copy(alpha = 0.15f)
                                                    else if (isIncome) EmeraldPrimary.copy(alpha = 0.15f)
                                                    else RoseExpense.copy(alpha = 0.15f)
                                                )
                                        ) {
                                            Icon(
                                                imageVector = if (tx.isPending) Icons.Default.Schedule
                                                else if (isTransfer) Icons.Default.SwapHoriz
                                                else if (isIncome) Icons.Default.ArrowDownward
                                                else Icons.Default.ArrowOutward,
                                                contentDescription = null,
                                                tint = if (tx.isPending) AmberPending
                                                else if (isTransfer) IndigoTransfer
                                                else if (isIncome) EmeraldPrimary
                                                else RoseExpense,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }

                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Text(
                                                    text = tx.description,
                                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    maxLines = 1
                                                )
                                                if (tx.isPending) {
                                                    Text(
                                                        "FUTURO",
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Black,
                                                        color = AmberPending,
                                                        modifier = Modifier
                                                            .background(AmberPending.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }

                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(
                                                    text = "${accountObj?.icon ?: "🏦"} ${tx.category}",
                                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                Text("•", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                Text(
                                                    text = tx.bucket,
                                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                                    color = EmeraldPrimary
                                                )
                                            }
                                        }
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = "${if (isIncome) "+" else if (isTransfer) "" else "-"} ${FinanceUtils.formatCurrency(tx.amount)}",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = if (isIncome) EmeraldPrimary else if (isTransfer) IndigoTransfer else RoseExpense
                                        )

                                        IconButton(
                                            onClick = { onDeleteTransaction(tx.id) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.DeleteOutline,
                                                contentDescription = "Excluir",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }

                                if (index < txs.size - 1) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(horizontal = 16.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
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
