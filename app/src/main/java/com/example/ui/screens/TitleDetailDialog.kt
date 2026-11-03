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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
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
    onAdvanceEpisode: (Int) -> Unit,
    onToggleMovieWatched: () -> Unit,
    onUpdateStatus: (WatchStatus) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onCheckUpdates: () -> Unit = {}
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
                                text = title.progressDisplay,
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
                                            "Marcar como Não Assistido"
                                        else
                                            "Marcar como Assistido",
                                        color = Color.White,
                                        fontSize = 13.sp
                                    )
                                }
                            } else {
                                // Series & Anime Episode Controls (-1 and +1)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Episódio ${title.currentEpisode} de ${title.totalEpisodes}",
                                        color = TextSecondary,
                                        fontSize = 12.sp
                                    )

                                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        // -1 Ep
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(CircleShape)
                                                .background(GraphiteSurfaceVariant)
                                                .border(1.dp, GraphiteBorder, CircleShape)
                                                .clickable(enabled = title.currentEpisode > 0) {
                                                    onAdvanceEpisode(-1)
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Remove,
                                                contentDescription = "Retroceder episódio",
                                                tint = if (title.currentEpisode > 0) Color.White else TextSecondary.copy(alpha = 0.3f),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                        // +1 Ep
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(CircleShape)
                                                .background(NeonCyan.copy(alpha = 0.2f))
                                                .border(1.dp, NeonCyan, CircleShape)
                                                .clickable(enabled = title.currentEpisode < title.totalEpisodes) {
                                                    onAdvanceEpisode(1)
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = "Avançar episódio",
                                                tint = NeonCyan,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Change Status Selector
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "ALTERAR STATUS",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            WatchStatus.entries.forEach { status ->
                                val isSelected = title.statusEnum == status
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (isSelected) status.color.copy(alpha = 0.25f) else GraphiteSurface
                                        )
                                        .border(
                                            1.dp,
                                            if (isSelected) status.color else GraphiteBorder,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable { onUpdateStatus(status) }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = status.shortLabel,
                                        color = if (isSelected) status.color else TextSecondary,
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        maxLines = 1
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
