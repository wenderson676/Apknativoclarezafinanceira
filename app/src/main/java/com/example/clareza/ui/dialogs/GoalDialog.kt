package com.example.clareza.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.clareza.data.model.Goal
import com.example.clareza.ui.theme.EmeraldPrimary
import com.example.clareza.util.FinanceUtils

@Composable
fun GoalDialog(
    goal: Goal? = null,
    onDismiss: () -> Unit,
    onSave: (Goal) -> Unit,
    onDelete: ((String) -> Unit)? = null
) {
    var title by remember { mutableStateOf(goal?.title ?: "") }
    var targetAmountText by remember {
        mutableStateOf(if (goal != null) String.format(java.util.Locale.US, "%.2f", goal.targetAmount) else "")
    }
    var currentAmountText by remember {
        mutableStateOf(if (goal != null) String.format(java.util.Locale.US, "%.2f", goal.currentAmount) else "")
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (goal != null) "Editar Meta" else "Nova Meta / Sonho",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar")
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Nome da Meta") },
                    placeholder = { Text("Ex: Reserva de Emergência, Viagem, Carro") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = targetAmountText,
                    onValueChange = { targetAmountText = it },
                    label = { Text("Valor Alvo (R$)") },
                    placeholder = { Text("0,00") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = currentAmountText,
                    onValueChange = { currentAmountText = it },
                    label = { Text("Valor Inicial Já Guardado (R$)") },
                    placeholder = { Text("0,00") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (goal != null && onDelete != null) {
                        OutlinedButton(
                            onClick = {
                                onDelete(goal.id)
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
                            val target = targetAmountText.replace(",", ".").toDoubleOrNull() ?: 0.0
                            val current = currentAmountText.replace(",", ".").toDoubleOrNull() ?: 0.0
                            if (title.isBlank() || target <= 0.0) return@Button

                            onSave(
                                Goal(
                                    id = goal?.id ?: "",
                                    title = title.trim(),
                                    targetAmount = target,
                                    currentAmount = current
                                )
                            )
                            onDismiss()
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        modifier = Modifier.weight(if (goal != null) 1.5f else 1f)
                    ) {
                        Text(if (goal != null) "Salvar" else "Criar Meta", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun GoalActionDialog(
    goal: Goal,
    isDeposit: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var amountText by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = if (isDeposit) "Guardar Dinheiro na Meta" else "Resgatar Valor da Meta",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${goal.title} • Acumulado: ${FinanceUtils.formatCurrency(goal.currentAmount)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Valor (R$)") },
                    placeholder = { Text("0,00") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancelar")
                    }

                    Button(
                        onClick = {
                            val amount = amountText.replace(",", ".").toDoubleOrNull() ?: 0.0
                            if (amount > 0.0) {
                                onConfirm(amount)
                                onDismiss()
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Confirmar", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
