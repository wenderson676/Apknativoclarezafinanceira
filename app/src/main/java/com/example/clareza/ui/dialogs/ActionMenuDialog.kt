package com.example.clareza.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.clareza.ui.theme.AmberPending
import com.example.clareza.ui.theme.EmeraldPrimary
import com.example.clareza.ui.theme.IndigoTransfer
import com.example.clareza.ui.theme.RoseExpense

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActionMenuModal(
    onDismiss: () -> Unit,
    onAddExpense: () -> Unit,
    onAddIncome: () -> Unit,
    onAddTransfer: () -> Unit,
    onAddGoal: () -> Unit,
    onAddDebt: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(),
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "O que você deseja registrar?",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Mantenha sua gestão financeira atualizada",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ActionMenuItem(
                    modifier = Modifier.weight(1f),
                    title = "Despesa",
                    subtitle = "Gasto realizado",
                    icon = Icons.Default.ArrowOutward,
                    backgroundColor = RoseExpense.copy(alpha = 0.12f),
                    iconColor = RoseExpense,
                    onClick = {
                        onDismiss()
                        onAddExpense()
                    }
                )

                ActionMenuItem(
                    modifier = Modifier.weight(1f),
                    title = "Receita",
                    subtitle = "Entrada de valor",
                    icon = Icons.Default.ArrowDownward,
                    backgroundColor = EmeraldPrimary.copy(alpha = 0.12f),
                    iconColor = EmeraldPrimary,
                    onClick = {
                        onDismiss()
                        onAddIncome()
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ActionMenuItem(
                    modifier = Modifier.weight(1f),
                    title = "Transferência",
                    subtitle = "Entre contas",
                    icon = Icons.Default.SwapHoriz,
                    backgroundColor = IndigoTransfer.copy(alpha = 0.12f),
                    iconColor = IndigoTransfer,
                    onClick = {
                        onDismiss()
                        onAddTransfer()
                    }
                )

                ActionMenuItem(
                    modifier = Modifier.weight(1f),
                    title = "Nova Meta",
                    subtitle = "Sonho / Cofrinho",
                    icon = Icons.Default.TrackChanges,
                    backgroundColor = AmberPending.copy(alpha = 0.12f),
                    iconColor = AmberPending,
                    onClick = {
                        onDismiss()
                        onAddGoal()
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                onClick = {
                    onDismiss()
                    onAddDebt()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(RoseExpense.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Dívida",
                            tint = RoseExpense,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Cadastrar Dívida",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Monte seu Plano de Ataque e elimine juros",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ActionMenuItem(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    icon: ImageVector,
    backgroundColor: Color,
    iconColor: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        border = null
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(backgroundColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
