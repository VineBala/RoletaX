package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Sync
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.model.TitleEntity
import com.example.data.model.WatchStatus
import com.example.ui.components.StatusBadge
import com.example.ui.theme.GraphiteBlack
import com.example.ui.theme.GraphiteBorder
import com.example.ui.theme.GraphiteDark
import com.example.ui.theme.GraphiteSurface
import com.example.ui.theme.GraphiteSurfaceVariant
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun TitleDetailDialog(
    title: TitleEntity,
    onDismiss: () -> Unit,
    onAdvanceEpisode: (Int) -> Unit = {},
    onToggleMovieWatched: () -> Unit,
    onUpdateStatus: (WatchStatus) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleIgnore: () -> Unit = {},
    onCheckUpdates: () -> Unit = {},
    onSaveManualProgress: (TitleEntity) -> Unit = {}
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .border(1.dp, GraphiteBorder, RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = GraphiteDark)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp)
                ) {
                    // Top Bar: Close, Edit, Delete
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Fechar",
                                tint = TextSecondary
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = onToggleIgnore) {
                                Icon(
                                    imageVector = Icons.Default.Block,
                                    contentDescription = if (title.isIgnored) "Desbloquear da Roleta" else "Ignorar na Roleta",
                                    tint = if (title.isIgnored) Color(0xFFEF4444) else TextSecondary
                                )
                            }
                            if (!title.isMovie) {
                                IconButton(onClick = onCheckUpdates) {
                                    Icon(
                                        imageVector = Icons.Default.Sync,
                                        contentDescription = "Buscar Atualizações Online",
                                        tint = NeonCyan
                                    )
                                }
                            }
                            IconButton(onClick = onEdit) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Editar",
                                    tint = NeonCyan
                                )
                            }
                            IconButton(onClick = { showDeleteConfirm = true }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Excluir",
                                    tint = Color(0xFFEF4444)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Title Header with Poster
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .size(width = 110.dp, height = 160.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(GraphiteSurface)
                                .border(1.dp, NeonPurple.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        ) {
                            if (title.coverUrl.isNotBlank()) {
                                AsyncImage(
                                    model = title.coverUrl,
                                    contentDescription = title.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(NeonPurple.copy(alpha = 0.2f))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = title.categoryEnum.displayName,
                                    color = NeonPurple,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = title.name,
                                color = TextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "${title.releaseYear} • ${title.genre}",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )

                            if (title.isMovie && title.duration.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AccessTime,
                                        contentDescription = null,
                                        tint = NeonCyan,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Duração: ${title.duration}",
                                        color = NeonCyan,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            } else if (!title.isMovie) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${title.totalSeasons} temporada(s) • ${title.totalEpisodes} episódios",
                                    color = NeonCyan,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            if (title.personalRating > 0f) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = Color(0xFFFFD700),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = String.format("%.1f / 10", title.personalRating),
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            StatusBadge(status = title.statusEnum)

                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (title.isIgnored) Color(0xFFEF4444).copy(alpha = 0.2f) else GraphiteSurfaceVariant)
                                    .border(1.dp, if (title.isIgnored) Color(0xFFEF4444) else GraphiteBorder, RoundedCornerShape(8.dp))
                                    .clickable { onToggleIgnore() }
                                    .padding(horizontal = 8.dp, vertical = 5.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Block,
                                        contentDescription = null,
                                        tint = if (title.isIgnored) Color(0xFFEF4444) else NeonCyan,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = if (title.isIgnored) "BLOQUEADO NA ROLETA (Toque p/ desbloquear)" else "Ativo na Roleta (Toque p/ bloquear)",
                                        color = if (title.isIgnored) Color(0xFFEF4444) else TextSecondary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Progress & Episode Tracker Section
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = GraphiteSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GraphiteBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "CONTROLE DE PROGRESSO",
                                color = NeonCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )

                            LinearProgressIndicator(
                                progress = { title.progressPercentage },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = if (title.statusEnum == WatchStatus.COMPLETO) NeonPurple else NeonCyan,
                                trackColor = GraphiteBorder
                            )

                            Text(
                                text = if (title.isMovie) {
                                    val compDate = if (title.completedDateDisplay != null) " em ${title.completedDateDisplay}" else ""
                                    if (title.duration.isNotBlank()) {
                                        if (title.statusEnum == WatchStatus.COMPLETO)
                                            "Filme concluído$compDate • Duração: ${title.duration}"
                                        else
                                            "Filme • Duração: ${title.duration}"
                                    } else {
                                        if (title.statusEnum == WatchStatus.COMPLETO)
                                            "Filme concluído$compDate"
                                        else
                                            "Filme"
                                    }
                                } else {
                                    title.progressDisplay
                                },
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )

                            if (title.isMovie) {
                                Button(
                                    onClick = onToggleMovieWatched,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (title.statusEnum == WatchStatus.COMPLETO)
                                            GraphiteSurfaceVariant
                                        else
                                            NeonPurple
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (title.statusEnum == WatchStatus.COMPLETO)
                                            "Desmarcar Concluído"
                                        else
                                            "Concluir Filme",
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            } else {
                                // Controle manual direto de progresso para Séries e Animes
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(GraphiteSurfaceVariant)
                                        .border(1.dp, GraphiteBorder, RoundedCornerShape(12.dp))
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        text = "EDIÇÃO MANUAL DE PROGRESSO",
                                        color = NeonCyan,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.8.sp
                                    )

                                    var inputCurrentSeason by remember(title.id, title.currentSeason) {
                                        mutableStateOf(title.currentSeason.toString())
                                    }
                                    var inputCurrentEpisode by remember(title.id, title.currentEpisode) {
                                        mutableStateOf(title.currentEpisode.toString())
                                    }
                                    var inputTotalSeasons by remember(title.id, title.totalSeasons) {
                                        mutableStateOf(title.totalSeasons.toString())
                                    }
                                    var inputTotalEpisodes by remember(title.id, title.totalEpisodes) {
                                        mutableStateOf(title.totalEpisodes.toString())
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = inputCurrentSeason,
                                            onValueChange = { newVal ->
                                                inputCurrentSeason = newVal
                                                val parsed = newVal.toIntOrNull()
                                                if (parsed != null && parsed >= 1) {
                                                    onSaveManualProgress(
                                                        title.copy(currentSeason = parsed)
                                                    )
                                                }
                                            },
                                            label = { Text("Temp. Atual", fontSize = 10.sp) },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            singleLine = true,
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(8.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = NeonCyan,
                                                unfocusedBorderColor = GraphiteBorder,
                                                focusedTextColor = TextPrimary,
                                                unfocusedTextColor = TextPrimary
                                            )
                                        )

                                        OutlinedTextField(
                                            value = inputCurrentEpisode,
                                            onValueChange = { newVal ->
                                                inputCurrentEpisode = newVal
                                                val parsed = newVal.toIntOrNull()
                                                if (parsed != null && parsed >= 0) {
                                                    onSaveManualProgress(
                                                        title.copy(currentEpisode = parsed)
                                                    )
                                                }
                                            },
                                            label = { Text("Ep. Atual", fontSize = 10.sp) },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            singleLine = true,
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(8.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = NeonCyan,
                                                unfocusedBorderColor = GraphiteBorder,
                                                focusedTextColor = TextPrimary,
                                                unfocusedTextColor = TextPrimary
                                            )
                                        )
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = inputTotalSeasons,
                                            onValueChange = { newVal ->
                                                inputTotalSeasons = newVal
                                                val parsed = newVal.toIntOrNull()
                                                if (parsed != null && parsed >= 1) {
                                                    onSaveManualProgress(
                                                        title.copy(totalSeasons = parsed)
                                                    )
                                                }
                                            },
                                            label = { Text("Temp. Total", fontSize = 10.sp) },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            singleLine = true,
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(8.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = NeonPurple,
                                                unfocusedBorderColor = GraphiteBorder,
                                                focusedTextColor = TextPrimary,
                                                unfocusedTextColor = TextPrimary
                                            )
                                        )

                                        OutlinedTextField(
                                            value = inputTotalEpisodes,
                                            onValueChange = { newVal ->
                                                inputTotalEpisodes = newVal
                                                val parsed = newVal.toIntOrNull()
                                                if (parsed != null && parsed >= 1) {
                                                    onSaveManualProgress(
                                                        title.copy(totalEpisodes = parsed)
                                                    )
                                                }
                                            },
                                            label = { Text("Ep. Total", fontSize = 10.sp) },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            singleLine = true,
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(8.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = NeonPurple,
                                                unfocusedBorderColor = GraphiteBorder,
                                                focusedTextColor = TextPrimary,
                                                unfocusedTextColor = TextPrimary
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Novo Catálogo: Bloquear, Concluir, Ignorar (assistir e na fila removidos)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "NOVO CATÁLOGO",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // 1. Bloquear / Desbloquear
                            val isBlocked = title.statusEnum == WatchStatus.BLOQUEADO
                            Button(
                                onClick = {
                                    val nextStatus = if (isBlocked) WatchStatus.PENDENTE else WatchStatus.BLOQUEADO
                                    onUpdateStatus(nextStatus)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isBlocked) WatchStatus.BLOQUEADO.color else GraphiteSurfaceVariant
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isBlocked) WatchStatus.BLOQUEADO.color else GraphiteBorder
                                ),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (isBlocked) "Desbloquear" else "Bloquear",
                                    color = if (isBlocked) Color.White else TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // 2. Concluir
                            val isCompleted = title.statusEnum == WatchStatus.COMPLETO
                            Button(
                                onClick = {
                                    if (isCompleted) {
                                        onUpdateStatus(WatchStatus.PENDENTE)
                                    } else {
                                        onUpdateStatus(WatchStatus.COMPLETO)
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isCompleted) WatchStatus.COMPLETO.color else GraphiteSurfaceVariant
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isCompleted) WatchStatus.COMPLETO.color else GraphiteBorder
                                ),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (isCompleted) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                    }
                                    Text(
                                        text = if (isCompleted) "Concluído" else "Concluir",
                                        color = if (isCompleted) Color.White else TextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // 3. Ignorar
                            val isIgnored = title.isIgnored
                            Button(
                                onClick = onToggleIgnore,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isIgnored) Color(0xFFEF4444) else GraphiteSurfaceVariant
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isIgnored) Color(0xFFEF4444) else GraphiteBorder
                                ),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Block,
                                        contentDescription = null,
                                        tint = if (isIgnored) Color.White else Color(0xFFEF4444),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isIgnored) "Ignorado" else "Ignorar",
                                        color = if (isIgnored) Color.White else Color(0xFFEF4444),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    if (title.synopsis.isNotBlank()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "SINOPSE",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = title.synopsis,
                            color = TextPrimary,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }

                    if (title.notes.isNotBlank()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "OBSERVAÇÕES",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = title.notes,
                            color = NeonCyan,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Excluir Título", color = TextPrimary) },
            text = { Text("Tem certeza que deseja remover '${title.name}' da biblioteca?", color = TextSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Excluir", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancelar", color = TextPrimary)
                }
            },
            containerColor = GraphiteDark
        )
    }
}
