package com.example.clareza.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.clareza.data.model.Debt
import com.example.clareza.ui.theme.EmeraldPrimary
import com.example.clareza.ui.theme.RoseExpense
import com.example.clareza.util.FinanceUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebtDialog(
    debt: Debt? = null,
    onDismiss: () -> Unit,
    onSave: (Debt) -> Unit,
    onDelete: ((String) -> Unit)? = null
) {
    var name by remember { mutableStateOf(debt?.name ?: "") }
    var totalAmountText by remember {
        mutableStateOf(if (debt != null) String.format(java.util.Locale.US, "%.2f", debt.totalAmount) else "")
    }
    var monthlyPaymentText by remember {
        mutableStateOf(if (debt != null) String.format(java.util.Locale.US, "%.2f", debt.monthlyPayment) else "")
    }
    var interestRateText by remember {
        mutableStateOf(if (debt != null) String.format(java.util.Locale.US, "%.2f", debt.interestRate) else "")
    }
    var creditor by remember { mutableStateOf(debt?.creditor ?: "") }
    var selectedType by remember { mutableStateOf(debt?.type ?: "card_revolving") }
    var isLate by remember { mutableStateOf(debt?.isLate ?: false) }

    var expandedTypeDropdown by remember { mutableStateOf(false) }

    val debtTypeInfo = FinanceUtils.DEBT_TYPES_INFO[selectedType] ?: FinanceUtils.DEBT_TYPES_INFO["other"]!!

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.90f),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (debt != null) "Editar Dívida" else "Cadastrar Dívida",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Type dropdown
                    ExposedDropdownMenuBox(
                        expanded = expandedTypeDropdown,
                        onExpandedChange = { expandedTypeDropdown = !expandedTypeDropdown }
                    ) {
                        OutlinedTextField(
                            value = debtTypeInfo.label,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Tipo de Dívida / Gravidade") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTypeDropdown) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = expandedTypeDropdown,
                            onDismissRequest = { expandedTypeDropdown = false }
                        ) {
                            FinanceUtils.DEBT_TYPES_INFO.values.forEach { info ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(info.label, fontWeight = FontWeight.Bold)
                                                Text(
                                                    info.priority,
                                                    color = when (info.priority) {
                                                        "Máxima" -> RoseExpense
                                                        "Média" -> MaterialTheme.colorScheme.tertiary
                                                        else -> EmeraldPrimary
                                                    },
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp
                                                )
                                            }
                                            Text(info.desc, style = MaterialTheme.typography.bodySmall)
                                        }
                                    },
                                    onClick = {
                                        selectedType = info.key
                                        expandedTypeDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    // Priority Banner
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                when (debtTypeInfo.priority) {
                                    "Máxima" -> RoseExpense.copy(alpha = 0.12f)
                                    "Média" -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f)
                                    else -> EmeraldPrimary.copy(alpha = 0.12f)
                                }
                            )
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "Prioridade: ${debtTypeInfo.priority} • ${debtTypeInfo.desc}",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = when (debtTypeInfo.priority) {
                                "Máxima" -> RoseExpense
                                "Média" -> MaterialTheme.colorScheme.tertiary
                                else -> EmeraldPrimary
                            }
                        )
                    }

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nome da Dívida") },
                        placeholder = { Text("Ex: Cartão Nubank, Empréstimo Caixa") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = totalAmountText,
                        onValueChange = { totalAmountText = it },
                        label = { Text("Valor Total Devido (R$)") },
                        placeholder = { Text("0,00") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = monthlyPaymentText,
                        onValueChange = { monthlyPaymentText = it },
                        label = { Text("Parcela Mensal Atual (R$)") },
                        placeholder = { Text("0,00") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = interestRateText,
                        onValueChange = { interestRateText = it },
                        label = { Text("Taxa de Juros Mensal (%)") },
                        placeholder = { Text("Ex: 3.5") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = creditor,
                        onValueChange = { creditor = it },
                        label = { Text("Credor / Instituição") },
                        placeholder = { Text("Ex: Banco do Brasil, Imobiliária, Amigo") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Dívida em Atraso / Negativada", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Switch(checked = isLate, onCheckedChange = { isLate = it })
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (debt != null && onDelete != null) {
                        OutlinedButton(
                            onClick = {
                                onDelete(debt.id)
                                onDismiss()
                            },
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Excluir")
                        }
                    }

                    Button(
                        onClick = {
                            val total = totalAmountText.replace(",", ".").toDoubleOrNull() ?: 0.0
                            val monthly = monthlyPaymentText.replace(",", ".").toDoubleOrNull() ?: 0.0
                            val rate = interestRateText.replace(",", ".").toDoubleOrNull() ?: 0.0
                            if (name.isBlank() || total <= 0.0) return@Button

                            onSave(
                                Debt(
                                    id = debt?.id ?: "",
                                    name = name.trim(),
                                    totalAmount = total,
                                    monthlyPayment = monthly,
                                    interestRate = rate,
                                    isLate = isLate,
                                    creditor = creditor.ifBlank { "Não informado" },
                                    type = selectedType
                                )
                            )
                            onDismiss()
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RoseExpense),
                        modifier = Modifier.weight(if (debt != null) 1.5f else 1f)
                    ) {
                        Text(if (debt != null) "Salvar" else "Cadastrar Dívida", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
