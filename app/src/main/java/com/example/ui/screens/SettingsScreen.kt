package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FolderZip
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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
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
import androidx.core.content.ContextCompat
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
    val coroutineScope = rememberCoroutineScope()
    val isDarkTheme by viewModel.isDarkTheme.collectAsStateWithLifecycle()
    val allTitles by viewModel.allTitles.collectAsStateWithLifecycle()
    val excludeCompleted by viewModel.rouletteExcludeCompleted.collectAsStateWithLifecycle()
    val avoidLastResult by viewModel.rouletteAvoidLastResult.collectAsStateWithLifecycle()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsStateWithLifecycle()

    val storagePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.exportBackupDirectlyToDownloads(context)
        } else {
            viewModel.showMessage("Permissão necessária para salvar backup na pasta Downloads.")
        }
    }

    val exportZipSafLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip")
    ) { uri ->
        if (uri != null) {
            coroutineScope.launch {
                try {
                    context.contentResolver.openOutputStream(uri)?.use { os ->
                        viewModel.exportBackupZip(os)
                    }
                } catch (e: Exception) {
                    viewModel.showMessage("Erro ao salvar arquivo: ${e.message}")
                }
            }
        }
    }

    val importZipLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.importBackupFromUri(context, uri) { success, count ->
                if (success) {
                    viewModel.showMessage("$count títulos restaurados com sucesso do backup .zip!")
                }
            }
        }
    }

    var showClearConfirm by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showChangelogDialog by remember { mutableStateOf(false) }

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
                        icon = Icons.Default.CloudDownload,
                        title = "Baixar Backup (.zip)",
                        subtitle = "Salva o backup compactado com os ${allTitles.size} títulos direto na pasta Downloads",
                        iconTint = NeonPurple,
                        onClick = {
                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                                val hasPermission = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                                ) == PackageManager.PERMISSION_GRANTED
                                if (!hasPermission) {
                                    storagePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                } else {
                                    viewModel.exportBackupDirectlyToDownloads(context)
                                }
                            } else {
                                viewModel.exportBackupDirectlyToDownloads(context)
                            }
                        }
                    )

                    SettingsActionItem(
                        icon = Icons.Default.FolderZip,
                        title = "Restaurar Backup (.zip)",
                        subtitle = "Selecione o arquivo .zip para restaurar todos os títulos, progresso e categorias",
                        iconTint = NeonCyan,
                        onClick = {
                            importZipLauncher.launch(
                                arrayOf(
                                    "application/zip",
                                    "application/x-zip-compressed",
                                    "application/octet-stream",
                                    "*/*"
                                )
                            )
                        }
                    )

                    SettingsActionItem(
                        icon = Icons.Default.AutoAwesome,
                        title = "Ver Apresentação das Novidades",
                        subtitle = "Abre a tela inicial de boas-vindas do Vortex",
                        iconTint = NeonCyan,
                        onClick = {
                            viewModel.showWelcomeScreen()
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
                        subtitle = "Versão 2.1 • Por VineBalla",
                        iconTint = NeonCyan,
                        onClick = { showAboutDialog = true }
                    )

                    SettingsActionItem(
                        icon = Icons.Default.AutoAwesome,
                        title = "O Que Há de Novo na Atualização",
                        subtitle = "Novidades da versão 2.1 Vortex (Backup ZIP, Card de Destaque)",
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
                            text = "Versão ${com.example.BuildConfig.VERSION_NAME}",
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
                                "• Sorteio Inteligente com Card de Destaque e 3 Modos (Geral, Categoria e Personalizada)\n" +
                                "• Alertas e Notificações nativas no Android\n" +
                                "• Atualização contínua sem conflito de versão (Vortex APK)\n" +
                                "• Controle automatizado de status e progresso de episódios\n" +
                                "• Importação e Exportação de Backup em formato ZIP direto para Downloads",
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
                            text = "Versão ${com.example.BuildConfig.VERSION_NAME} (Vortex) • Assinado por VineBalla",
                            color = NeonCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "Confira o que há de novo nesta versão:\n\n" +
                                "1. 🎲 Novo Card de Destaque na Roleta:\n" +
                                "O elemento gráfico da roleta girando foi removido e substituído por um card central elegante com a obra sorteada em destaque, capa, detalhes e botões de ação rápida.\n\n" +
                                "2. 📦 Sistema de Backup e Restauração em formato ZIP:\n" +
                                "Substituição completa do método de texto/JSON antigo. O botão 'Baixar Backup (.zip)' salva diretamente na pasta Downloads do celular com permissão compatível, e 'Restaurar Backup (.zip)' permite carregar todo o acervo sem limites de caracteres.\n\n" +
                                "3. ✨ Tela de Boas-Vindas e Apresentação Inicial:\n" +
                                "Apresentação limpa na primeira abertura do app com atalho visível para restaurar arquivos .zip imediatamente e botão para pular caso não possua backup.\n\n" +
                                "4. 🏷️ Categorias e Conclusão Inteligentes:\n" +
                                "Opções oficiais de Bloquear, Ignorar (exclui do sorteio na roleta) e Concluir, gravando e exibindo a data e ano de conclusão na biblioteca.\n\n" +
                                "5. 🔍 Busca com Verificação de Duplicatas:\n" +
                                "Ao pesquisar títulos, o app verifica o banco de dados e sinaliza se a obra já existe na biblioteca.\n\n" +
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
