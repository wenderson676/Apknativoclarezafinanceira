package com.example.clareza.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.clareza.data.model.Account
import com.example.clareza.data.model.Debt
import com.example.clareza.data.model.Goal
import com.example.clareza.data.model.Transaction
import com.example.clareza.ui.dialogs.*
import com.example.clareza.ui.screens.ComparisonScreen
import com.example.clareza.ui.screens.DashboardScreen
import com.example.clareza.ui.screens.TransactionsScreen
import com.example.clareza.ui.theme.EmeraldPrimary
import com.example.clareza.ui.theme.RoseExpense
import com.example.clareza.util.FinanceUtils
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: ClarezaViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    var currentTab by remember { mutableIntStateOf(0) } // 0: Dashboard, 1: Extrato, 2: Análise

    // Dialog states
    var showActionMenu by remember { mutableStateOf(false) }
    var transactionDialogType by remember { mutableStateOf<String?>(null) } // "expense", "income", "transfer"
    var editingTransaction by remember { mutableStateOf<Transaction?>(null) }

    var selectedGoalForEdit by remember { mutableStateOf<Goal?>(null) }
    var showGoalDialog by remember { mutableStateOf(false) }
    var goalActionType by remember { mutableStateOf<Pair<Goal, Boolean>?>(null) } // (Goal, isDeposit)

    var selectedDebtForEdit by remember { mutableStateOf<Debt?>(null) }
    var showDebtDialog by remember { mutableStateOf(false) }

    var showAccountsDialog by remember { mutableStateOf(false) }
    var showDonationDialog by remember { mutableStateOf(false) }
    var showFeedbackDialog by remember { mutableStateOf(false) }
    var showBackupDialog by remember { mutableStateOf(false) }
    var showBudgetModeDialog by remember { mutableStateOf(false) }
    var showTutorialDialog by remember { mutableStateOf(false) }
    var showResetConfirmDialog by remember { mutableStateOf(false) }
    var showUserNameDialog by remember { mutableStateOf(false) }

    val monthFormatter = remember { DateTimeFormatter.ofPattern("MMMM 'de' yyyy", FinanceUtils.BRL_LOCALE) }
    val monthTitle = state.selectedYearMonth.format(monthFormatter).replaceFirstChar { it.uppercase() }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(310.dp),
                drawerContainerColor = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        // Profile & App branding
                        Surface(
                            onClick = { showUserNameDialog = true },
                            shape = RoundedCornerShape(18.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(EmeraldPrimary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccountBalanceWallet,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = state.userName?.ifBlank { "Definir Nome" } ?: "Paz e Graça",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Toque para alterar",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                        // Navigation Items
                        NavigationDrawerItem(
                            icon = { Icon(Icons.Default.Tune, contentDescription = null, tint = EmeraldPrimary) },
                            label = { Text("Modelo de Orçamento (${state.budgetMode})") },
                            selected = false,
                            onClick = {
                                scope.launch { drawerState.close() }
                                showBudgetModeDialog = true
                            }
                        )

                        NavigationDrawerItem(
                            icon = { Icon(Icons.Default.AccountBalance, contentDescription = null) },
                            label = { Text("Minhas Contas & Carteiras") },
                            selected = false,
                            onClick = {
                                scope.launch { drawerState.close() }
                                showAccountsDialog = true
                            }
                        )

                        NavigationDrawerItem(
                            icon = { Icon(Icons.Default.Backup, contentDescription = null) },
                            label = { Text("Backup & Restauração") },
                            selected = false,
                            onClick = {
                                scope.launch { drawerState.close() }
                                showBackupDialog = true
                            }
                        )

                        NavigationDrawerItem(
                            icon = { Icon(Icons.Default.Info, contentDescription = null) },
                            label = { Text("Tutorial & Princípios") },
                            selected = false,
                            onClick = {
                                scope.launch { drawerState.close() }
                                showTutorialDialog = true
                            }
                        )

                        NavigationDrawerItem(
                            icon = { Icon(Icons.Default.Chat, contentDescription = null) },
                            label = { Text("Fale Conosco (WhatsApp)") },
                            selected = false,
                            onClick = {
                                scope.launch { drawerState.close() }
                                showFeedbackDialog = true
                            }
                        )

                        NavigationDrawerItem(
                            icon = { Icon(Icons.Default.Favorite, contentDescription = null, tint = RoseExpense) },
                            label = { Text("Apoiar com PIX") },
                            selected = false,
                            onClick = {
                                scope.launch { drawerState.close() }
                                showDonationDialog = true
                            }
                        )

                        NavigationDrawerItem(
                            icon = { Icon(Icons.Default.DeleteForever, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                            label = { Text("Zerar Todos os Dados", color = MaterialTheme.colorScheme.error) },
                            selected = false,
                            onClick = {
                                scope.launch { drawerState.close() }
                                showResetConfirmDialog = true
                            }
                        )
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Clareza Financeira v1.0",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Criado com dedicação por Wenderson Gomes",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            IconButton(onClick = { viewModel.previousMonth() }) {
                                Icon(Icons.Default.ChevronLeft, contentDescription = "Mês Anterior")
                            }
                            Text(
                                text = monthTitle,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                            IconButton(onClick = { viewModel.nextMonth() }) {
                                Icon(Icons.Default.ChevronRight, contentDescription = "Próximo Mês")
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu Lateral")
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.toggleDarkMode() }) {
                            Icon(
                                imageVector = if (state.isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = "Alternar Tema"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        selected = currentTab == 0,
                        onClick = { currentTab = 0 },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Início") },
                        label = { Text("Início", fontWeight = if (currentTab == 0) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = EmeraldPrimary,
                            selectedTextColor = EmeraldPrimary,
                            indicatorColor = EmeraldPrimary.copy(alpha = 0.15f)
                        )
                    )

                    NavigationBarItem(
                        selected = currentTab == 1,
                        onClick = { currentTab = 1 },
                        icon = { Icon(Icons.Default.ReceiptLong, contentDescription = "Extrato") },
                        label = { Text("Extrato", fontWeight = if (currentTab == 1) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = EmeraldPrimary,
                            selectedTextColor = EmeraldPrimary,
                            indicatorColor = EmeraldPrimary.copy(alpha = 0.15f)
                        )
                    )

                    // Center Floating Add Button in NavigationBar
                    FloatingActionButton(
                        onClick = { showActionMenu = true },
                        containerColor = EmeraldPrimary,
                        contentColor = Color.White,
                        shape = CircleShape,
                        elevation = FloatingActionButtonDefaults.elevation(6.dp),
                        modifier = Modifier.size(54.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Adicionar Lançamento", modifier = Modifier.size(28.dp))
                    }

                    NavigationBarItem(
                        selected = currentTab == 2,
                        onClick = { currentTab = 2 },
                        icon = { Icon(Icons.Default.BarChart, contentDescription = "Análise") },
                        label = { Text("Análise", fontWeight = if (currentTab == 2) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = EmeraldPrimary,
                            selectedTextColor = EmeraldPrimary,
                            indicatorColor = EmeraldPrimary.copy(alpha = 0.15f)
                        )
                    )
                }
            }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                when (currentTab) {
                    0 -> DashboardScreen(
                        state = state,
                        onManageAccounts = { showAccountsDialog = true },
                        onOpenBudgetModes = { showBudgetModeDialog = true },
                        onOpenGoalDialog = { goal ->
                            selectedGoalForEdit = goal
                            showGoalDialog = true
                        },
                        onGoalDeposit = { goal ->
                            goalActionType = goal to true
                        },
                        onGoalWithdraw = { goal ->
                            goalActionType = goal to false
                        },
                        onOpenDebtDialog = { debt ->
                            selectedDebtForEdit = debt
                            showDebtDialog = true
                        },
                        onRefreshVerse = { viewModel.refreshVerse() }
                    )
                    1 -> TransactionsScreen(
                        state = state,
                        onEditTransaction = { tx ->
                            editingTransaction = tx
                            transactionDialogType = tx.type
                        },
                        onDeleteTransaction = { id -> viewModel.deleteTransaction(id) },
                        onTogglePending = { id, currentPending -> viewModel.toggleTransactionPending(id, currentPending) },
                        onSaveNote = { note ->
                            val monthId = state.selectedYearMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"))
                            viewModel.saveMonthNote(monthId, note)
                        }
                    )
                    2 -> ComparisonScreen(
                        state = state,
                        onSetBudgetMode = { mode -> viewModel.setBudgetMode(mode) }
                    )
                }
            }
        }
    }

    // --- DIALOG MODALS ---

    // Action Menu Modal
    if (showActionMenu) {
        ActionMenuModal(
            onDismiss = { showActionMenu = false },
            onAddExpense = { transactionDialogType = "expense" },
            onAddIncome = { transactionDialogType = "income" },
            onAddTransfer = { transactionDialogType = "transfer" },
            onAddGoal = {
                selectedGoalForEdit = null
                showGoalDialog = true
            },
            onAddDebt = {
                selectedDebtForEdit = null
                showDebtDialog = true
            }
        )
    }

    // Transaction Create / Edit Dialog
    if (transactionDialogType != null) {
        TransactionDialog(
            initialType = transactionDialogType ?: "expense",
            editingTransaction = editingTransaction,
            accounts = state.accounts,
            customCategories = state.customCategories,
            onDismiss = {
                transactionDialogType = null
                editingTransaction = null
            },
            onSave = { amount, desc, bucket, cat, date, isPending, account, toAccount, type, repCount, freq ->
                viewModel.addTransaction(
                    amount = amount,
                    description = desc,
                    bucket = bucket,
                    category = cat,
                    date = date,
                    isPending = isPending,
                    account = account,
                    toAccount = toAccount,
                    type = type,
                    repeatCount = repCount,
                    frequency = freq
                )
            },
            onUpdate = { tx ->
                viewModel.updateTransaction(tx)
            },
            onAddCustomCategory = { bucket, name ->
                viewModel.addCustomCategory(bucket, name)
            }
        )
    }

    // Goal Dialog
    if (showGoalDialog) {
        GoalDialog(
            goal = selectedGoalForEdit,
            onDismiss = {
                showGoalDialog = false
                selectedGoalForEdit = null
            },
            onSave = { goal ->
                viewModel.saveGoal(goal)
            },
            onDelete = { id ->
                viewModel.deleteGoal(id)
            }
        )
    }

    // Goal Deposit / Withdraw Dialog
    goalActionType?.let { (goal, isDeposit) ->
        GoalActionDialog(
            goal = goal,
            isDeposit = isDeposit,
            onDismiss = { goalActionType = null },
            onConfirm = { amount ->
                if (isDeposit) {
                    viewModel.depositToGoal(goal, amount)
                } else {
                    viewModel.withdrawFromGoal(goal, amount)
                }
            }
        )
    }

    // Debt Dialog
    if (showDebtDialog) {
        DebtDialog(
            debt = selectedDebtForEdit,
            onDismiss = {
                showDebtDialog = false
                selectedDebtForEdit = null
            },
            onSave = { debt ->
                viewModel.saveDebt(debt)
            },
            onDelete = { id ->
                viewModel.deleteDebt(id)
            }
        )
    }

    // Account Management Dialog
    if (showAccountsDialog) {
        AccountManagementDialog(
            accounts = state.accounts,
            balances = state.accountBalances,
            onDismiss = { showAccountsDialog = false },
            onSaveAccount = { acc -> viewModel.saveAccount(acc) },
            onDeleteAccount = { id -> viewModel.deleteAccount(id) }
        )
    }

    // Donation Dialog
    if (showDonationDialog) {
        DonationDialog(onDismiss = { showDonationDialog = false })
    }

    // Feedback Dialog
    if (showFeedbackDialog) {
        FeedbackDialog(onDismiss = { showFeedbackDialog = false })
    }

    // Backup Dialog
    if (showBackupDialog) {
        BackupDialog(
            onDismiss = { showBackupDialog = false },
            onExport = { viewModel.exportData() },
            onImport = { json -> viewModel.importData(json) }
        )
    }

    // Budget Mode Selector
    if (showBudgetModeDialog) {
        BudgetModeSelectorDialog(
            currentMode = state.budgetMode,
            onDismiss = { showBudgetModeDialog = false },
            onSelectMode = { mode -> viewModel.setBudgetMode(mode) }
        )
    }

    // Tutorial Dialog
    if (showTutorialDialog) {
        TutorialDialog(onDismiss = { showTutorialDialog = false })
    }

    // Reset Confirmation Dialog
    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            title = { Text("Zerar Todos os Dados?") },
            text = { Text("Esta ação apagará todos os lançamentos, contas personalizadas, metas e dívidas do banco de dados local. Tem certeza?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetAllData()
                        showResetConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Zerar Tudo")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // User Name Dialog
    if (showUserNameDialog) {
        var tempName by remember { mutableStateOf(state.userName ?: "") }
        AlertDialog(
            onDismissRequest = { showUserNameDialog = false },
            title = { Text("Seu Nome") },
            text = {
                OutlinedTextField(
                    value = tempName,
                    onValueChange = { tempName = it },
                    label = { Text("Como prefere ser chamado?") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.setUserName(tempName.trim())
                        showUserNameDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("Salvar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showUserNameDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
