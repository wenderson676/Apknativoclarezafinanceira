package com.example.clareza.ui.dialogs

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.clareza.ui.theme.EmeraldPrimary

@Composable
fun FeedbackDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    fun openWhatsApp() {
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://wa.me/5531983470840?text=Ol%C3%A1%2C+estou+usando+o+aplicativo+Clareza+Financeira+e+gostaria+de+enviar+um+feedback%2Fsugest%C3%A3o%3A")
        )
        context.startActivity(intent)
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar")
                    }
                }

                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(EmeraldPrimary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Chat,
                        contentDescription = null,
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Text(
                    text = "Fale com o Desenvolvedor",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Sua opinião é fundamental para aprimorarmos o Clareza Financeira. Encontrou algum problema ou tem sugestões de melhorias? Mande uma mensagem direta no WhatsApp!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Button(
                    onClick = {
                        openWhatsApp()
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Icon(Icons.Default.Chat, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Abrir WhatsApp", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
