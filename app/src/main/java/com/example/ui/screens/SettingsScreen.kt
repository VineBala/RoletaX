package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Sync
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
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsStateWithLifecycle()

    var showClearConfirm by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showChangelogDialog by remember { mutableStateOf(false) }
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

        // Section: Notificações
        item {
            SettingsGroup(title = "NOTIFICAÇÕES NO CELULAR") {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(NeonCyan.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = null,
                                    tint = NeonCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Alertas de Novos Episódios",
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Notificar o celular quando houver novas temporadas ou episódios",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Switch(
                            checked = notificationsEnabled,
                            onCheckedChange = { viewModel.setNotificationsEnabled(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = NeonCyan,
                                checkedTrackColor = NeonPurple
                            )
                        )
                    }

                    SettingsActionItem(
                        icon = Icons.Default.NotificationsActive,
                        title = "Testar Notificação Agora",
                        subtitle = "Dispara uma notificação imediata para testar no aparelho",
                        iconTint = NeonPurple,
                        onClick = { viewModel.sendTestNotification() }
                    )
                }
            }
        }

        // Section: Gerenciamento da Biblioteca & Backup
        item {
            SettingsGroup(title = "GERENCIAMENTO DA BIBLIOTECA & ATUALIZAÇÕES") {
                Column {
                    SettingsActionItem(
                        icon = Icons.Default.Sync,
                        title = "Verificar Atualizações de Todos os Títulos",
                        subtitle = "Procura na internet novos episódios e temporadas para os ${allTitles.size} títulos salvos",
                        iconTint = NeonCyan,
                        onClick = { viewModel.syncAllTitles() }
                    )

                    SettingsActionItem(
                        icon = Icons.Default.AutoAwesome,
                        title = "Simular Chegada de Novo Episódio (Teste)",
                        subtitle = "Adiciona +1 episódio oficial e dispara a notificação no celular",
                        iconTint = NeonPurple,
                        onClick = { viewModel.simulateUpdateForTesting() }
                    )

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

        // Section: Sobre o Aplicativo & Assinatura VineBalla
        item {
            SettingsGroup(title = "SOBRE & ATUALIZAÇÕES") {
                Column {
                    SettingsActionItem(
                        icon = Icons.Default.Info,
                        title = "Sobre o Vortex",
                        subtitle = "Versão 2.0 • Por VineBalla",
                        iconTint = NeonCyan,
                        onClick = { showAboutDialog = true }
                    )

                    SettingsActionItem(
                        icon = Icons.Default.AutoAwesome,
                        title = "O Que Há de Novo na Atualização",
                        subtitle = "Novidades da versão 2.0 Vortex (Busca de episódios, notificações)",
                        iconTint = NeonPurple,
                        onClick = { showChangelogDialog = true }
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
                        val clip = ClipData.newPlainText("Vortex Backup", exportedJsonText)
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
                    Text("VORTEX", color = TextPrimary, fontWeight = FontWeight.Black)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(NeonPurple.copy(alpha = 0.2f))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Versão 2.0",
                            color = NeonCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Por: VineBalla",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "Vortex é uma biblioteca inteligente de entretenimento projetada para amantes de filmes, séries e animes.",
                        color = TextPrimary,
                        fontSize = 13.sp
                    )

                    Text(
                        text = "Assinatura do Desenvolvedor:\n" +
                                "Criado e assinado por VineBalla.\n\n" +
                                "Destaques do Sistema:\n" +
                                "• Armazenamento Local Robusto com Room Database (Offline-First)\n" +
                                "• Busca Automática Oficial de Episódios e Temporadas (TVMaze & Jikan)\n" +
                                "• Roleta Aleatória Interativa com 3 Modos (Geral, Categoria e Personalizada)\n" +
                                "• Alertas e Notificações nativas no Android\n" +
                                "• Atualização contínua sem conflito de versão (Vortex APK)\n" +
                                "• Controle automatizado de status e progresso de episódios\n" +
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

    // Dialog: O que sempre vem de novo na atualização (Changelog)
    if (showChangelogDialog) {
        AlertDialog(
            onDismissRequest = { showChangelogDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = NeonPurple, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("NOVIDADES DA ATUALIZAÇÃO", color = TextPrimary, fontWeight = FontWeight.Black, fontSize = 16.sp)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(androidx.compose.foundation.rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(NeonPurple.copy(alpha = 0.15f))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "Versão 2.0 (Vortex) • Assinado por VineBalla",
                            color = NeonCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "Confira o que há de novo nesta versão:\n\n" +
                                "1. 🔍 Busca Automática Completa de Episódios:\n" +
                                "Agora, ao pesquisar qualquer anime ou série, o aplicativo busca e preenche automaticamente o total real de episódios, temporadas e episódios por temporada diretamente da internet (TVMaze e Jikan/MyAnimeList).\n\n" +
                                "2. 🎬 Episódio Atual Inicial no Ep 1:\n" +
                                "O episódio atual agora começa no episódio 1 por padrão para séries e animes (sem confundir o episódio assistido com o total de episódios da obra).\n\n" +
                                "3. 🔄 Atualização em Lote da Biblioteca:\n" +
                                "Botão de sincronização rápida para buscar se saíram novos episódios ou temporadas na internet para todos os títulos salvos.\n\n" +
                                "4. 🔔 Notificações no Celular:\n" +
                                "Seu aparelho avisa quando novos episódios ou temporadas de suas obras salvas forem encontrados!\n\n" +
                                "5. ⚡ Nome do APK Vortex e Compatibilidade Sem Conflito:\n" +
                                "O aplicativo agora gera o pacote Vortex com incremento contínuo de versão (v2.0), permitindo atualizar diretamente sem precisar apagar a versão antiga.\n\n" +
                                "6. ✍️ Assinatura:\n" +
                                "Desenvolvido com carinho por VineBalla.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showChangelogDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonPurple)
                ) {
                    Text("Fechar", color = Color.White)
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
