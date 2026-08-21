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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.clareza.data.model.Account
import com.example.clareza.data.model.CustomCategories
import com.example.clareza.data.model.Transaction
import com.example.clareza.ui.theme.AmberPending
import com.example.clareza.ui.theme.EmeraldPrimary
import com.example.clareza.ui.theme.IndigoTransfer
import com.example.clareza.ui.theme.RoseExpense
import com.example.clareza.util.FinanceUtils
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDialog(
    initialType: String = "expense", // "expense", "income", "transfer"
    editingTransaction: Transaction? = null,
    accounts: List<Account>,
    customCategories: CustomCategories,
    onDismiss: () -> Unit,
    onSave: (
        amount: Double,
        description: String,
        bucket: String,
        category: String,
        date: String,
        isPending: Boolean,
        account: String,
        toAccount: String?,
        type: String,
        repeatCount: Int,
        frequency: String
    ) -> Unit,
    onUpdate: (Transaction) -> Unit,
    onAddCustomCategory: (bucket: String, name: String) -> Unit
) {
    var type by remember {
        mutableStateOf(
            editingTransaction?.type ?: when (initialType) {
                "income" -> "income"
                "transfer" -> "transfer_between_accounts"
                else -> "expense"
            }
        )
    }

    var amountText by remember {
        mutableStateOf(
            if (editingTransaction != null) {
                String.format(java.util.Locale.US, "%.2f", editingTransaction.amount)
            } else ""
        )
    }
    var description by remember { mutableStateOf(editingTransaction?.description ?: "") }
    var date by remember {
        mutableStateOf(
            editingTransaction?.date ?: LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        )
    }
    var isPending by remember { mutableStateOf(editingTransaction?.isPending ?: false) }

    var selectedBucket by remember {
        mutableStateOf(
            editingTransaction?.bucket ?: when (type) {
                "income" -> "Renda"
                "transfer_between_accounts", "transfer_to_savings", "transfer_from_savings" -> "Transferência"
                else -> "Necessidades"
            }
        )
    }

    var selectedCategory by remember {
        mutableStateOf(
            editingTransaction?.category ?: ""
        )
    }

    var selectedAccount by remember {
        mutableStateOf(
            editingTransaction?.account ?: accounts.find { it.isMain }?.id ?: accounts.firstOrNull()?.id ?: "banco"
        )
    }

    var selectedToAccount by remember {
        mutableStateOf(
            editingTransaction?.toAccount ?: accounts.find { it.id == "reserva" }?.id ?: accounts.lastOrNull()?.id ?: "reserva"
        )
    }

    var repeatCountText by remember { mutableStateOf("1") }
    var frequency by remember { mutableStateOf("none") } // "none", "weekly", "biweekly", "monthly"

    var showNewCatDialog by remember { mutableStateOf(false) }
    var newCatName by remember { mutableStateOf("") }

    val categoriesList = remember(selectedBucket, customCategories, type) {
        val base = FinanceUtils.CATEGORIES[selectedBucket] ?: emptyList()
        val custom = when (selectedBucket) {
            "Renda" -> customCategories.income
            "Transferência" -> customCategories.transfer
            else -> customCategories.expense
        }
        (base + custom).distinct()
    }

    LaunchedEffect(categoriesList) {
        if (selectedCategory.isBlank() || !categoriesList.contains(selectedCategory)) {
            selectedCategory = categoriesList.firstOrNull() ?: ""
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.92f),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (editingTransaction != null) "Editar Lançamento" else "Novo Lançamento",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar")
                    }
                }

                // Type Tabs (Despesa, Receita, Transferência)
                if (editingTransaction == null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TabButton(
                            title = "Despesa",
                            isSelected = type == "expense",
                            activeColor = RoseExpense,
                            onClick = {
                                type = "expense"
                                selectedBucket = "Necessidades"
                            },
                            modifier = Modifier.weight(1f)
                        )
                        TabButton(
                            title = "Receita",
                            isSelected = type == "income",
                            activeColor = EmeraldPrimary,
                            onClick = {
                                type = "income"
                                selectedBucket = "Renda"
                            },
                            modifier = Modifier.weight(1f)
                        )
                        TabButton(
                            title = "Transferência",
                            isSelected = type.startsWith("transfer"),
                            activeColor = IndigoTransfer,
                            onClick = {
                                type = "transfer_between_accounts"
                                selectedBucket = "Transferência"
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Scrollable Form Fields
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Amount input
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        label = { Text("Valor (R$)") },
                        placeholder = { Text("0,00") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        leadingIcon = {
                            Text(
                                "R$",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(start = 12.dp)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true
                    )

                    // Description input
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Descrição") },
                        placeholder = { Text("Ex: Supermercado, Aluguel, Salário") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true
                    )

                    // Date input & Future toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = date,
                            onValueChange = { date = it },
                            label = { Text("Data (AAAA-MM-DD)") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true
                        )
                    }

                    // Pending switch
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .clickable { isPending = !isPending }
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = AmberPending,
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    "Lançamento Futuro / Pendente",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    "Não afeta o saldo atual até ser confirmado",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = isPending,
                            onCheckedChange = { isPending = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = AmberPending)
                        )
                    }

                    // Bucket Selector (only for Expense)
                    if (type == "expense") {
                        Text(
                            "Destinação do Orçamento (Regra de Ouro)",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("Necessidades", "Desejos", "Reserva/Dívidas").forEach { b ->
                                val selected = selectedBucket == b
                                Surface(
                                    onClick = { selectedBucket = b },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                    border = if (selected) null else null
                                ) {
                                    Text(
                                        text = b,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 11.sp
                                        ),
                                        color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    // Category Chips
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Categoria",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            TextButton(onClick = { showNewCatDialog = true }) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Nova", fontSize = 12.sp)
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            categoriesList.forEach { cat ->
                                val isSelected = selectedCategory == cat
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedCategory = cat },
                                    label = { Text(cat, fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                )
                            }
                        }
                    }

                    // Account Selection
                    Column {
                        Text(
                            if (type.startsWith("transfer")) "Conta de Origem" else "Conta / Carteira",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            accounts.forEach { acc ->
                                val isSelected = selectedAccount == acc.id
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedAccount = acc.id },
                                    label = { Text("${acc.icon} ${acc.name}", fontSize = 12.sp) }
                                )
                            }
                        }
                    }

                    // Destination Account for Transfer
                    if (type.startsWith("transfer")) {
                        Column {
                            Text(
                                "Conta de Destino",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                accounts.forEach { acc ->
                                    val isSelected = selectedToAccount == acc.id
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { selectedToAccount = acc.id },
                                        label = { Text("${acc.icon} ${acc.name}", fontSize = 12.sp) }
                                    )
                                }
                            }
                        }
                    }

                    // Recurrence options (only for new transactions)
                    if (editingTransaction == null) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                "Repetição / Parcelamento",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(
                                    "none" to "Único",
                                    "monthly" to "Mensal",
                                    "weekly" to "Semanal",
                                    "biweekly" to "Quinzenal"
                                ).forEach { (key, label) ->
                                    val isSel = frequency == key
                                    FilterChip(
                                        selected = isSel,
                                        onClick = { frequency = key },
                                        label = { Text(label, fontSize = 11.sp) }
                                    )
                                }
                            }

                            if (frequency != "none") {
                                OutlinedTextField(
                                    value = repeatCountText,
                                    onValueChange = { repeatCountText = it },
                                    label = { Text("Número de Parcelas / Meses") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = true
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Submit Button
                Button(
                    onClick = {
                        val amount = amountText.replace(",", ".").toDoubleOrNull() ?: 0.0
                        if (amount <= 0.0 || description.isBlank()) return@Button

                        val repCount = repeatCountText.toIntOrNull()?.coerceIn(1, 48) ?: 1

                        if (editingTransaction != null) {
                            onUpdate(
                                editingTransaction.copy(
                                    amount = amount,
                                    description = description,
                                    bucket = selectedBucket,
                                    category = selectedCategory.ifBlank { "Outros" },
                                    date = date,
                                    isPending = isPending,
                                    account = selectedAccount,
                                    toAccount = if (type.startsWith("transfer")) selectedToAccount else null,
                                    type = type
                                )
                            )
                        } else {
                            onSave(
                                amount,
                                description,
                                selectedBucket,
                                selectedCategory.ifBlank { "Outros" },
                                date,
                                isPending,
                                selectedAccount,
                                if (type.startsWith("transfer")) selectedToAccount else null,
                                type,
                                repCount,
                                frequency
                            )
                        }
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = when (type) {
                            "expense" -> RoseExpense
                            "income" -> EmeraldPrimary
                            else -> IndigoTransfer
                        }
                    )
                ) {
                    Text(
                        if (editingTransaction != null) "Atualizar Lançamento" else "Salvar Lançamento",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }

    // New Category Sub-dialog
    if (showNewCatDialog) {
        AlertDialog(
            onDismissRequest = { showNewCatDialog = false },
            title = { Text("Nova Categoria") },
            text = {
                OutlinedTextField(
                    value = newCatName,
                    onValueChange = { newCatName = it },
                    label = { Text("Nome da Categoria") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newCatName.isNotBlank()) {
                            onAddCustomCategory(selectedBucket, newCatName.trim())
                            selectedCategory = newCatName.trim()
                            newCatName = ""
                            showNewCatDialog = false
                        }
                    }
                ) {
                    Text("Adicionar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewCatDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun TabButton(
    title: String,
    isSelected: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        color = if (isSelected) activeColor else Color.Transparent,
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier.padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 13.sp
                ),
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
