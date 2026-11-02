package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.GraphiteBlack
import com.example.ui.theme.GraphiteBorder
import com.example.ui.theme.GraphiteDark
import com.example.ui.theme.GraphiteSurface
import com.example.ui.theme.GraphiteSurfaceVariant
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.SeraAkisosViewModel

@Composable
fun SettingsScreen(
    viewModel: SeraAkisosViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDarkTheme by viewModel.isDarkTheme.collectAsStateWithLifecycle()
    val allTitles by viewModel.allTitles.collectAsStateWithLifecycle()
    val excludeCompleted by viewModel.rouletteExcludeCompleted.collectAsStateWithLifecycle()
    val avoidLastResult by viewModel.rouletteAvoidLastResult.collectAsStateWithLifecycle()

    var showClearConfirm by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var importJsonText by remember { mutableStateOf("") }
    var exportedJsonText by remember { mutableStateOf("") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "CONFIGURAÇÕES",
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Aparência, gerenciamento de dados e sobre o app",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
        }

        // Section: Aparência (Tema)
        item {
            SettingsGroup(title = "APARÊNCIA") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(NeonPurple.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isDarkTheme) Icons.Default.DarkMode else Icons.Default.LightMode,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Tema Escuro (Padrão)",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isDarkTheme) "Visual escuro com detalhes neon ativado" else "Modo claro ativado",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Switch(
                        checked = isDarkTheme,
                        onCheckedChange = { viewModel.setDarkTheme(it) },
                        modifier = Modifier.testTag("theme_switch"),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = NeonCyan,
                            checkedTrackColor = NeonPurple,
                            uncheckedThumbColor = TextSecondary,
                            uncheckedTrackColor = GraphiteSurfaceVariant
                        )
                    )
                }
            }
        }

        // Section: Configuração Padrão da Roleta
        item {
            SettingsGroup(title = "CONFIGURAÇÕES DA ROLETA") {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Excluir Concluídos", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text(text = "Não incluir obras finalizadas no sorteio", color = TextSecondary, fontSize = 11.sp)
                        }
                        Switch(
                            checked = excludeCompleted,
                            onCheckedChange = { viewModel.toggleRouletteExcludeCompleted() }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Evitar Repetições", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text(text = "Não sortear consecutivamente o mesmo título", color = TextSecondary, fontSize = 11.sp)
                        }
                        Switch(
                            checked = avoidLastResult,
                            onCheckedChange = { viewModel.toggleRouletteAvoidLastResult() }
                        )
                    }
                }
            }
        }

        // Section: Gerenciamento da Biblioteca & Backup
        item {
            SettingsGroup(title = "GERENCIAMENTO DA BIBLIOTECA & BACKUP") {
                Column {
                    SettingsActionItem(
                        icon = Icons.Default.Restore,
                        title = "Carregar Dados de Exemplo",
                        subtitle = "Adiciona obras consagradas (One Piece, Arcane, Interestelar...)",
                        iconTint = NeonCyan,
                        onClick = { viewModel.seedSampleData() }
                    )

                    SettingsActionItem(
                        icon = Icons.Default.Upload,
                        title = "Exportar Títulos (JSON)",
                        subtitle = "Gera backup completo de todos os ${allTitles.size} títulos",
                        iconTint = NeonPurple,
                        onClick = {
                            exportedJsonText = viewModel.exportBackupJson()
                            showExportDialog = true
                        }
                    )

                    SettingsActionItem(
                        icon = Icons.Default.Download,
                        title = "Importar Títulos (JSON)",
                        subtitle = "Restaura títulos a partir de código JSON",
                        iconTint = NeonCyan,
                        onClick = {
                            importJsonText = ""
                            showImportDialog = true
                        }
                    )

                    SettingsActionItem(
                        icon = Icons.Default.DeleteForever,
                        title = "Limpar Toda a Biblioteca",
                        subtitle = "Remove permanentemente todos os registros",
                        iconTint = Color(0xFFEF4444),
                        onClick = { showClearConfirm = true }
                    )
                }
            }
        }

        // Section: Sobre o Aplicativo
        item {
            SettingsGroup(title = "INFORMAÇÕES") {
                Column {
                    SettingsActionItem(
                        icon = Icons.Default.Info,
                        title = "Sobre o Sera Akisos",
                        subtitle = "Versão 1.0.0 • Biblioteca Inteligente & Roleta",
                        iconTint = NeonCyan,
                        onClick = { showAboutDialog = true }
                    )
                }
            }
        }
    }

    // Dialog: Confirm Clear Database
    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("Limpar Biblioteca?", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Todos os ${allTitles.size} títulos cadastrados serão excluídos permanentemente. Deseja continuar?",
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showClearConfirm = false
                        viewModel.clearAllTitles()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Sim, Limpar Tudo", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showClearConfirm = false }) {
                    Text("Cancelar", color = TextPrimary)
                }
            },
            containerColor = GraphiteDark
        )
    }

    // Dialog: Export JSON
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("Backup dos Títulos (JSON)", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Copie o conteúdo JSON abaixo para salvar ou transferir:",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    OutlinedTextField(
                        value = exportedJsonText,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = GraphiteSurface,
                            unfocusedContainerColor = GraphiteSurface
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Sera Akisos Backup", exportedJsonText)
                        clipboard.setPrimaryClip(clip)
                        viewModel.showMessage("Backup copiado para a área de transferência!")
                        showExportDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonPurple)
                ) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copiar JSON", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showExportDialog = false }) {
                    Text("Fechar", color = TextPrimary)
                }
            },
            containerColor = GraphiteDark
        )
    }

    // Dialog: Import JSON
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text("Importar Backup (JSON)", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Cole o JSON exportado anteriormente para importar para a biblioteca:",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    OutlinedTextField(
                        value = importJsonText,
                        onValueChange = { importJsonText = it },
                        placeholder = { Text("Cole o JSON aqui...", color = TextSecondary) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = GraphiteSurface,
                            unfocusedContainerColor = GraphiteSurface,
                            focusedBorderColor = NeonCyan
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (importJsonText.isNotBlank()) {
                            viewModel.importBackupJson(importJsonText) { success, _ ->
                                if (success) showImportDialog = false
                            }
                        }
                    },
                    enabled = importJsonText.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                ) {
                    Text("Restaurar Dados", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showImportDialog = false }) {
                    Text("Cancelar", color = TextPrimary)
                }
            },
            containerColor = GraphiteDark
        )
    }

    // Dialog: About App
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Casino, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("SERA AKISOS", color = TextPrimary, fontWeight = FontWeight.Black)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Versão 1.0.0 (Produção)",
                        color = NeonPurple,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Sera Akisos é uma biblioteca inteligente de entretenimento projetada para amantes de filmes, séries e animes.",
                        color = TextPrimary,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "Destaques do Sistema:\n" +
                                "• Armazenamento Local Robusto com Room Database (Offline-First)\n" +
                                "• Estrutura preparada para sincronização na nuvem com Firebase\n" +
                                "• Roleta Aleatória Interativa com 3 Modos (Geral, Categoria e Personalizada)\n" +
                                "• Busca de Títulos Online com dados e capas reais (Jikan, TVMaze e iTunes)\n" +
                                "• Controle completo de episódios, temporadas e status automatizados\n" +
                                "• Importação e Exportação de Backup em formato JSON",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showAboutDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonPurple)
                ) {
                    Text("Entendido", color = Color.White)
                }
            },
            containerColor = GraphiteDark
        )
    }
}

@Composable
private fun SettingsGroup(
    title: String,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = title,
            color = TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = GraphiteSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, GraphiteBorder)
        ) {
            content()
        }
    }
}

@Composable
private fun SettingsActionItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    iconTint: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(iconTint.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = TextSecondary,
                fontSize = 11.sp
            )
        }
    }
}
