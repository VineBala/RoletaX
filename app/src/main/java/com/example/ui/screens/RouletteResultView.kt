package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Category
import com.example.data.model.TitleEntity
import com.example.data.model.WatchStatus
import com.example.ui.components.StatusBadge
import com.example.ui.theme.GraphiteBorder
import com.example.ui.theme.GraphiteDark
import com.example.ui.theme.GraphiteSurface
import com.example.ui.theme.GraphiteSurfaceVariant
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun RouletteResultView(
    title: TitleEntity,
    onBack: () -> Unit,
    onSpinAgain: () -> Unit,
    onAdvanceEpisode: (Int) -> Unit,
    onToggleMovieWatched: () -> Unit,
    onSaveUpdatedTitle: (TitleEntity) -> Unit,
    onAddNewTitle: (TitleEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    // Intercept back button to return to wheel without losing winner
    BackHandler {
        onBack()
    }

    var isEditing by remember { mutableStateOf(false) }
    var showQuickAddDialog by remember { mutableStateOf(false) }

    // Editable state synced with title
    var editCurrentSeason by remember(title.id) { mutableIntStateOf(title.currentSeason) }
    var editTotalSeasons by remember(title.id) { mutableIntStateOf(title.totalSeasons) }
    var editCurrentEpisode by remember(title.id) { mutableIntStateOf(title.currentEpisode) }
    var editTotalEpisodes by remember(title.id) { mutableIntStateOf(title.totalEpisodes) }
    var editDuration by remember(title.id) { mutableStateOf(title.duration) }
    var editStatus by remember(title.id) { mutableStateOf(title.statusEnum) }
    var editIsIgnored by remember(title.id) { mutableStateOf(title.isIgnored) }

    // When title updates externally, re-sync edit fields if not actively editing
    LaunchedEffect(title) {
        if (!isEditing) {
            editCurrentSeason = title.currentSeason
            editTotalSeasons = title.totalSeasons
            editCurrentEpisode = title.currentEpisode
            editTotalEpisodes = title.totalEpisodes
            editDuration = title.duration
            editStatus = title.statusEnum
            editIsIgnored = title.isIgnored
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(GraphiteDark)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("roulette_result_screen")
    ) {
        // Navigation Bar: Voltar, Girar de Novo, Novo Título
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(containerColor = GraphiteSurfaceVariant),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("roulette_result_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    tint = NeonCyan,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("VOLTAR", color = NeonCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Button to manually add a new title to the library
                OutlinedButton(
                    onClick = { showQuickAddDialog = true },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.6f)),
                    modifier = Modifier.testTag("roulette_add_new_title_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.PostAdd,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("+ Novo Título", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = onSpinAgain,
                    colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("roulette_result_spin_again_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Girar de Novo", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Headline Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(NeonPurple.copy(alpha = 0.25f), NeonCyan.copy(alpha = 0.15f))
                    )
                )
                .border(1.dp, NeonCyan.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                .padding(vertical = 10.dp, horizontal = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = NeonCyan,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "TÍTULO SORTEADO PELA ROLETA",
                    color = NeonCyan,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = NeonCyan,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Hero Card with Poster & Full Details
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = GraphiteSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, GraphiteBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    // Poster
                    Box(
                        modifier = Modifier
                            .size(width = 120.dp, height = 175.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .border(2.dp, NeonPurple, RoundedCornerShape(14.dp))
                            .background(Color(0xFF10131B))
                    ) {
                        if (title.coverUrl.isNotBlank()) {
                            AsyncImage(
                                model = title.coverUrl,
                                contentDescription = title.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = title.categoryEnum.icon,
                                    contentDescription = null,
                                    tint = NeonPurple.copy(alpha = 0.5f),
                                    modifier = Modifier.size(48.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Title Info
                    Column(modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(NeonCyan.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = title.categoryEnum.displayName.uppercase(),
                                color = NeonCyan,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = title.name,
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            lineHeight = 22.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "${title.releaseYear} • ${title.genre}",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        StatusBadge(status = title.statusEnum)

                        if (title.personalRating > 0f) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color(0xFFFFD700),
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = String.format("%.1f / 10", title.personalRating),
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // DEDICATED CATEGORY DISPLAY (FILMES vs SÉRIES/ANIMES)
                if (title.isMovie) {
                    // FILMES: Mostra apenas duração e status. NUNCA temporadas ou episódios.
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = GraphiteSurfaceVariant),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GraphiteBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "INFORMAÇÕES DO FILME",
                                color = NeonCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AccessTime,
                                        contentDescription = null,
                                        tint = NeonCyan,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (title.duration.isNotBlank()) "Duração: ${title.duration}" else "Duração não informada",
                                        color = TextPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Button(
                                    onClick = onToggleMovieWatched,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (title.statusEnum == WatchStatus.COMPLETO) NeonPurple else GraphiteDark
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (title.statusEnum == WatchStatus.COMPLETO) {
                                            if (title.completedDateDisplay != null) "Concluído em ${title.completedDateDisplay}" else "Concluído"
                                        } else "Concluir",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // SÉRIES E ANIMES: Temporada atual, total temporadas, episódio atual, total episódios, progresso %, status
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = GraphiteSurfaceVariant),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GraphiteBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "PROGRESSO DE EPISÓDIOS",
                                    color = NeonCyan,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "${(title.progressPercentage * 100).toInt()}% Concluído",
                                    color = NeonPurple,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            LinearProgressIndicator(
                                progress = { title.progressPercentage },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = NeonPurple,
                                trackColor = Color(0xFF202636)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Grid of Details: Seasons & Episodes
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(text = "Temporada", color = TextSecondary, fontSize = 11.sp)
                                    Text(
                                        text = "${title.currentSeason} / ${title.totalSeasons}",
                                        color = TextPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Column {
                                    Text(text = "Episódio Atual", color = TextSecondary, fontSize = 11.sp)
                                    Text(
                                        text = "${title.currentEpisode} / ${title.totalEpisodes}",
                                        color = TextPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Column {
                                    Text(text = "Faltam", color = TextSecondary, fontSize = 11.sp)
                                    val remaining = (title.totalEpisodes - title.currentEpisode).coerceAtLeast(0)
                                    Text(
                                        text = "$remaining eps",
                                        color = if (remaining == 0) NeonCyan else TextPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Stepper Rápido: Adicionar Episódio Manualmente
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Adicionar Episódio:",
                                    color = TextSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // -1 Ep
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(GraphiteDark)
                                            .border(1.dp, GraphiteBorder, CircleShape)
                                            .clickable(enabled = title.currentEpisode > 0) {
                                                onAdvanceEpisode(-1)
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Remove,
                                            contentDescription = "Voltar 1 episódio",
                                            tint = if (title.currentEpisode > 0) NeonCyan else TextSecondary.copy(alpha = 0.4f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    // Ep display
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(GraphiteDark)
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "Ep. ${title.currentEpisode}",
                                            color = TextPrimary,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    // +1 Ep
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
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
                                            contentDescription = "Avançar 1 episódio",
                                            tint = NeonCyan,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
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
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // SECTION: EDITAR TÍTULO DIRETAMENTE NA ROLETA
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = GraphiteSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, if (isEditing) NeonCyan else GraphiteBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "EDITAR CADASTRO NA ROLETA",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Button(
                        onClick = { isEditing = !isEditing },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isEditing) GraphiteSurfaceVariant else NeonCyan.copy(alpha = 0.2f)
                        ),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("roulette_toggle_edit_button")
                    ) {
                        Text(
                            text = if (isEditing) "Cancelar Edição" else "EDITAR",
                            color = if (isEditing) TextSecondary else NeonCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (isEditing) {
                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "As alterações feitas aqui serão salvas permanentemente no cadastro original deste título na biblioteca.",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (title.isMovie) {
                        // Edição Filme: apenas duração e status
                        OutlinedTextField(
                            value = editDuration,
                            onValueChange = { editDuration = it },
                            label = { Text("Duração (ex: 2h 15m)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("roulette_edit_duration_input"),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = GraphiteBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )
                    } else {
                        // Edição Série / Anime: Temporada atual, total temporadas, episódio atual, total episódios
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = editCurrentSeason.toString(),
                                onValueChange = { editCurrentSeason = it.toIntOrNull() ?: 1 },
                                label = { Text("Temp. Atual") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("roulette_edit_current_season"),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NeonCyan,
                                    unfocusedBorderColor = GraphiteBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                )
                            )

                            OutlinedTextField(
                                value = editTotalSeasons.toString(),
                                onValueChange = { editTotalSeasons = it.toIntOrNull() ?: 1 },
                                label = { Text("Total Temps") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("roulette_edit_total_seasons"),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NeonCyan,
                                    unfocusedBorderColor = GraphiteBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = editCurrentEpisode.toString(),
                                onValueChange = { editCurrentEpisode = it.toIntOrNull() ?: 0 },
                                label = { Text("Episódio Atual") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("roulette_edit_current_episode"),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NeonCyan,
                                    unfocusedBorderColor = GraphiteBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                )
                            )

                            OutlinedTextField(
                                value = editTotalEpisodes.toString(),
                                onValueChange = { editTotalEpisodes = it.toIntOrNull() ?: 1 },
                                label = { Text("Total Episódios") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("roulette_edit_total_episodes"),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NeonCyan,
                                    unfocusedBorderColor = GraphiteBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Novo Catálogo: Bloquear, Concluir, Ignorar
                    Text(
                        text = "NOVO CATÁLOGO",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Bloquear
                        val isBlocked = editStatus == WatchStatus.BLOQUEADO
                        Button(
                            onClick = { editStatus = WatchStatus.BLOQUEADO },
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
                                text = "Bloquear",
                                color = if (isBlocked) Color.White else TextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Concluir
                        val isCompleted = editStatus == WatchStatus.COMPLETO
                        Button(
                            onClick = { editStatus = WatchStatus.COMPLETO },
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
                            Text(
                                text = "Concluir",
                                color = if (isCompleted) Color.White else TextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Ignorar (impede o sorteio na roleta)
                        Button(
                            onClick = { editIsIgnored = !editIsIgnored },
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (editIsIgnored) Color(0xFFEF4444) else GraphiteSurfaceVariant
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (editIsIgnored) Color(0xFFEF4444) else GraphiteBorder
                            ),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Block,
                                    contentDescription = null,
                                    tint = if (editIsIgnored) Color.White else Color(0xFFEF4444),
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = if (editIsIgnored) "Ignorado" else "Ignorar",
                                    color = if (editIsIgnored) Color.White else Color(0xFFEF4444),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // SALVAR ALTERAÇÕES BUTTON
                    Button(
                        onClick = {
                            val updated = if (title.isMovie) {
                                title.copy(
                                    duration = editDuration.trim(),
                                    status = editStatus.name,
                                    isIgnored = editIsIgnored,
                                    totalSeasons = 0,
                                    episodesPerSeason = 0,
                                    currentSeason = 0,
                                    currentEpisode = 0,
                                    totalEpisodes = 1,
                                    updatedAt = System.currentTimeMillis()
                                )
                            } else {
                                val cSeason = editCurrentSeason.coerceAtLeast(1)
                                val tSeasons = editTotalSeasons.coerceAtLeast(cSeason)
                                val tEpisodes = editTotalEpisodes.coerceAtLeast(1)
                                val cEpisode = editCurrentEpisode.coerceIn(0, tEpisodes)
                                val autoStatus = if (cEpisode >= tEpisodes) WatchStatus.COMPLETO else editStatus

                                title.copy(
                                    currentSeason = cSeason,
                                    totalSeasons = tSeasons,
                                    currentEpisode = cEpisode,
                                    totalEpisodes = tEpisodes,
                                    duration = "",
                                    status = autoStatus.name,
                                    isIgnored = editIsIgnored,
                                    updatedAt = System.currentTimeMillis()
                                )
                            }
                            onSaveUpdatedTitle(updated)
                            isEditing = false
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("roulette_save_changes_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "SALVAR ALTERAÇÕES",
                            color = Color.Black,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }

    // Modal para Adicionar Novo Título Manualmente direto da roleta
    if (showQuickAddDialog) {
        QuickAddTitleDialog(
            onDismiss = { showQuickAddDialog = false },
            onConfirmAdd = { newTitle ->
                onAddNewTitle(newTitle)
                showQuickAddDialog = false
            }
        )
    }
}

@Composable
fun QuickAddTitleDialog(
    onDismiss: () -> Unit,
    onConfirmAdd: (TitleEntity) -> Unit
) {
    var titleName by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(Category.FILME) }
    var releaseYear by remember { mutableIntStateOf(2024) }
    var genre by remember { mutableStateOf("Ação") }
    var duration by remember { mutableStateOf("") }
    var totalSeasons by remember { mutableIntStateOf(1) }
    var totalEpisodes by remember { mutableIntStateOf(12) }
    var initialStatus by remember { mutableStateOf(WatchStatus.PENDENTE) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Cadastrar Novo Título Manualmente",
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "O título será adicionado à biblioteca e ficará imediatamente disponível para os próximos sorteios.",
                    color = TextSecondary,
                    fontSize = 12.sp
                )

                OutlinedTextField(
                    value = titleName,
                    onValueChange = { titleName = it },
                    label = { Text("Nome do Título *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = GraphiteBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                // Category selector
                Text("Categoria:", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Category.entries.forEach { cat ->
                        val isSel = selectedCategory == cat
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) NeonCyan.copy(alpha = 0.2f) else GraphiteSurface)
                                .border(1.dp, if (isSel) NeonCyan else GraphiteBorder, RoundedCornerShape(8.dp))
                                .clickable { selectedCategory = cat }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = cat.displayName,
                                color = if (isSel) NeonCyan else TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                if (selectedCategory == Category.FILME) {
                    OutlinedTextField(
                        value = duration,
                        onValueChange = { duration = it },
                        label = { Text("Duração (ex: 1h 45m)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = GraphiteBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = totalSeasons.toString(),
                            onValueChange = { totalSeasons = it.toIntOrNull() ?: 1 },
                            label = { Text("Temporadas") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = GraphiteBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )
                        OutlinedTextField(
                            value = totalEpisodes.toString(),
                            onValueChange = { totalEpisodes = it.toIntOrNull() ?: 1 },
                            label = { Text("Episódios") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = GraphiteBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )
                    }
                }

                OutlinedTextField(
                    value = genre,
                    onValueChange = { genre = it },
                    label = { Text("Gênero") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = GraphiteBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (titleName.isBlank()) return@Button
                    val isMovie = selectedCategory == Category.FILME
                    val newEntity = TitleEntity(
                        name = titleName.trim(),
                        category = selectedCategory.name,
                        releaseYear = releaseYear,
                        genre = genre.trim().ifBlank { "Geral" },
                        totalSeasons = if (isMovie) 0 else totalSeasons.coerceAtLeast(1),
                        episodesPerSeason = if (isMovie) 0 else (totalEpisodes / totalSeasons.coerceAtLeast(1)).coerceAtLeast(1),
                        currentSeason = if (isMovie) 0 else 1,
                        currentEpisode = 0,
                        totalEpisodes = if (isMovie) 0 else totalEpisodes.coerceAtLeast(1),
                        duration = if (isMovie) duration.trim() else "",
                        status = initialStatus.name,
                        updatedAt = System.currentTimeMillis()
                    )
                    onConfirmAdd(newEntity)
                },
                enabled = titleName.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
            ) {
                Text("Adicionar Título", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancelar", color = TextSecondary)
            }
        },
        containerColor = GraphiteDark
    )
}
