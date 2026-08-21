package com.example.clareza.ui.dialogs

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.clareza.ui.theme.EmeraldPrimary
import kotlinx.coroutines.launch

@Composable
fun BackupDialog(
    onDismiss: () -> Unit,
    onExport: suspend () -> String,
    onImport: suspend (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var tabIndex by remember { mutableStateOf(0) } // 0: Export, 1: Import
    var exportedJson by remember { mutableStateOf("") }
    var importJsonText by remember { mutableStateOf("") }
    var isExporting by remember { mutableStateOf(false) }
    var isImporting by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isExporting = true
        exportedJson = onExport()
        isExporting = false
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(28.dp),
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
                            text = "Backup & Restauração",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Seus dados 100% sob seu controle",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                TabRow(selectedTabIndex = tabIndex) {
                    Tab(
                        selected = tabIndex == 0,
                        onClick = { tabIndex = 0 },
                        text = { Text("Exportar Dados", fontWeight = FontWeight.Bold) },
                        icon = { Icon(Icons.Default.Upload, contentDescription = null) }
                    )
                    Tab(
                        selected = tabIndex == 1,
                        onClick = { tabIndex = 1 },
                        text = { Text("Restaurar Backup", fontWeight = FontWeight.Bold) },
                        icon = { Icon(Icons.Default.Download, contentDescription = null) }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (tabIndex == 0) {
                    // Export Tab
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "Gere um arquivo ou copie o código de backup completo para transferir seus lançamentos, contas e metas para outro dispositivo:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        OutlinedTextField(
                            value = exportedJson,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("JSON de Backup") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            shape = RoundedCornerShape(14.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Clareza Backup", exportedJson)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "Backup copiado para a área de transferência!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Copiar")
                            }

                            Button(
                                onClick = {
                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, exportedJson)
                                        type = "text/plain"
                                    }
                                    val shareIntent = Intent.createChooser(sendIntent, "Compartilhar Backup Clareza")
                                    context.startActivity(shareIntent)
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Compartilhar")
                            }
                        }
                    }
                } else {
                    // Import Tab
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "Cole o código JSON do seu backup anterior para restaurar todas as suas transações e dados:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        OutlinedTextField(
                            value = importJsonText,
                            onValueChange = { importJsonText = it },
                            placeholder = { Text("Cole o JSON de backup aqui...") },
                            label = { Text("Código de Backup") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            shape = RoundedCornerShape(14.dp)
                        )

                        Button(
                            onClick = {
                                if (importJsonText.isBlank()) return@Button
                                scope.launch {
                                    try {
                                        isImporting = true
                                        onImport(importJsonText.trim())
                                        Toast.makeText(context, "Backup restaurado com sucesso!", Toast.LENGTH_SHORT).show()
                                        onDismiss()
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Erro ao restaurar backup: formato inválido.", Toast.LENGTH_LONG).show()
                                    } finally {
                                        isImporting = false
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                            enabled = importJsonText.isNotBlank() && !isImporting
                        ) {
                            Text(if (isImporting) "Restaurando..." else "Restaurar Dados Agora", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
