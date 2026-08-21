package com.example.clareza.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.clareza.data.model.Account
import com.example.clareza.ui.theme.EmeraldPrimary
import com.example.clareza.util.FinanceUtils

@Composable
fun AccountManagementDialog(
    accounts: List<Account>,
    balances: Map<String, Double>,
    onDismiss: () -> Unit,
    onSaveAccount: (Account) -> Unit,
    onDeleteAccount: (String) -> Unit
) {
    var editingAccount by remember { mutableStateOf<Account?>(null) }
    var isCreating by remember { mutableStateOf(false) }

    if (editingAccount != null || isCreating) {
        AccountEditDialog(
            account = editingAccount,
            onDismiss = {
                editingAccount = null
                isCreating = false
            },
            onSave = { acc ->
                onSaveAccount(acc)
                editingAccount = null
                isCreating = false
            },
            onDelete = if (editingAccount != null && editingAccount?.id !in listOf("banco", "reserva", "carteira")) {
                { id ->
                    onDeleteAccount(id)
                    editingAccount = null
                    isCreating = false
                }
            } else null
        )
        return
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.85f),
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
                    Column {
                        Text(
                            text = "Minhas Contas & Carteiras",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Gerencie seus saldos e cofrinhos",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    accounts.forEach { acc ->
                        val balance = balances[acc.id] ?: 0.0
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primaryContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(acc.icon, fontSize = 20.sp)
                                    }
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Text(acc.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            if (acc.isMain) {
                                                Text(
                                                    "Principal",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = EmeraldPrimary,
                                                    modifier = Modifier
                                                        .background(EmeraldPrimary.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            "Saldo Atual: ${FinanceUtils.formatCurrency(balance)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                IconButton(onClick = { editingAccount = acc }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { isCreating = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Adicionar Nova Conta", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AccountEditDialog(
    account: Account?,
    onDismiss: () -> Unit,
    onSave: (Account) -> Unit,
    onDelete: ((String) -> Unit)? = null
) {
    var name by remember { mutableStateOf(account?.name ?: "") }
    var icon by remember { mutableStateOf(account?.icon ?: "🏦") }
    var initialBalanceText by remember {
        mutableStateOf(if (account != null) String.format(java.util.Locale.US, "%.2f", account.initialBalance) else "0.00")
    }
    var isMain by remember { mutableStateOf(account?.isMain ?: false) }
    var isSavings by remember { mutableStateOf(account?.type == "reserva") }

    val icons = listOf("🏦", "💳", "💰", "💵", "🪙", "📈", "🛒", "🏷️")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = if (account != null) "Editar Conta" else "Nova Conta",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Icon selector
                Column {
                    Text("Ícone", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        icons.forEach { ic ->
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(if (icon == ic) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { icon = ic },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(ic, fontSize = 18.sp)
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome da Conta") },
                    placeholder = { Text("Ex: Nubank, Inter, Cofrinho") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = initialBalanceText,
                    onValueChange = { initialBalanceText = it },
                    label = { Text("Saldo Inicial (R$)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Conta Principal", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Switch(checked = isMain, onCheckedChange = { isMain = it })
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("É Cofrinho / Reserva?", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Switch(checked = isSavings, onCheckedChange = { isSavings = it })
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (onDelete != null && account != null) {
                        OutlinedButton(
                            onClick = { onDelete(account.id) },
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Excluir")
                        }
                    }

                    Button(
                        onClick = {
                            val initBal = initialBalanceText.replace(",", ".").toDoubleOrNull() ?: 0.0
                            if (name.isBlank()) return@Button

                            onSave(
                                Account(
                                    id = account?.id ?: "",
                                    name = name.trim(),
                                    icon = icon,
                                    type = if (isSavings) "reserva" else "custom",
                                    initialBalance = initBal,
                                    isMain = isMain
                                )
                            )
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Salvar", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
