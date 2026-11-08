package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.model.Category
import com.example.data.model.TitleEntity
import com.example.data.model.WatchStatus
import com.example.ui.components.StatusBadge
import com.example.ui.theme.GraphiteBlack
import com.example.ui.theme.GraphiteBorder
import com.example.ui.theme.GraphiteDark
import com.example.ui.theme.GraphiteSurface
import com.example.ui.theme.GraphiteSurfaceVariant
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.RouletteMode
import com.example.viewmodel.SeraAkisosViewModel

@Composable
fun RouletteScreen(
    viewModel: SeraAkisosViewModel,
    modifier: Modifier = Modifier
) {
    val currentMode by viewModel.rouletteMode.collectAsStateWithLifecycle()
    val rouletteCategory by viewModel.rouletteCategory.collectAsStateWithLifecycle()
    val excludeCompleted by viewModel.rouletteExcludeCompleted.collectAsStateWithLifecycle()
    val onlyPending by viewModel.rouletteOnlyPending.collectAsStateWithLifecycle()
    val onlyInProgress by viewModel.rouletteOnlyInProgress.collectAsStateWithLifecycle()
    val avoidLastResult by viewModel.rouletteAvoidLastResult.collectAsStateWithLifecycle()
    val customStatus by viewModel.rouletteCustomStatus.collectAsStateWithLifecycle()
    val isSpinning by viewModel.isSpinning.collectAsStateWithLifecycle()
    val winner by viewModel.rouletteWinner.collectAsStateWithLifecycle()

    val activeRouletteTitle by viewModel.activeRouletteTitle.collectAsStateWithLifecycle()
    val showRouletteResult by viewModel.showRouletteResult.collectAsStateWithLifecycle()
    val isManualSelection by viewModel.isManualSelection.collectAsStateWithLifecycle()
    val cycleDrawnIds by viewModel.rouletteDrawnCycleIds.collectAsStateWithLifecycle()
    val allTitles by viewModel.allTitles.collectAsStateWithLifecycle()

    val eligibleTitles = viewModel.getEligibleTitles()

    var showManualSelectionDialog by remember { mutableStateOf(false) }

    if (showManualSelectionDialog) {
        ManualSelectionDialog(
            titles = allTitles.filter { !it.isIgnored },
            activeTitleId = activeRouletteTitle?.id,
            cycleDrawnIds = cycleDrawnIds,
            onSelect = { selected ->
                viewModel.selectManualTitle(selected, autoShowFullResult = false)
                showManualSelectionDialog = false
            },
            onDismiss = { showManualSelectionDialog = false }
        )
    }

    // NOVA INTERFACE DE RESULTADO DA ROLETA
    if (showRouletteResult && activeRouletteTitle != null) {
        RouletteResultView(
            title = activeRouletteTitle!!,
            onBack = { viewModel.setShowRouletteResult(false) },
            onSpinAgain = {
                viewModel.drawRouletteTitle()
            },
            onAdvanceEpisode = { delta ->
                viewModel.advanceEpisode(activeRouletteTitle!!, delta)
            },
            onToggleMovieWatched = {
                viewModel.toggleMovieWatched(activeRouletteTitle!!)
            },
            onSaveUpdatedTitle = { updated ->
                viewModel.updateTitleDirectly(updated)
            },
            onAddNewTitle = { newTitle ->
                viewModel.startNewTitleForm(newTitle.categoryEnum)
                viewModel.updateFormField {
                    it.copy(
                        name = newTitle.name,
                        category = newTitle.categoryEnum,
                        releaseYear = newTitle.releaseYear,
                        genre = newTitle.genre,
                        totalSeasons = newTitle.totalSeasons,
                        totalEpisodes = newTitle.totalEpisodes,
                        duration = newTitle.duration,
                        status = newTitle.statusEnum
                    )
                }
                viewModel.saveCurrentForm()
            },
            modifier = modifier
        )
        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Screen Header
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Casino,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ROLETA INTELIGENTE",
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Sorteie seu próximo filme, série ou anime sem indecisão",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Mode Selector Tabs (3 Modes: Geral, Categoria, Personalizada)
        item {
            TabRow(
                selectedTabIndex = currentMode.ordinal,
                containerColor = GraphiteSurface,
                contentColor = NeonCyan,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[currentMode.ordinal]),
                        color = NeonCyan,
                        height = 3.dp
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, GraphiteBorder, RoundedCornerShape(12.dp))
            ) {
                RouletteMode.entries.forEach { mode ->
                    Tab(
                        selected = currentMode == mode,
                        onClick = { viewModel.setRouletteMode(mode) },
                        text = {
                            Text(
                                text = mode.title,
                                fontSize = 12.sp,
                                fontWeight = if (currentMode == mode) FontWeight.Bold else FontWeight.Normal,
                                color = if (currentMode == mode) NeonCyan else TextSecondary
                            )
                        }
                    )
                }
            }
        }

        // Mode Specific Controls
        item {
            when (currentMode) {
                RouletteMode.GERAL -> {
                    Text(
                        text = "Sorteio amplo entre todos os títulos elegíveis da sua biblioteca.",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }

                RouletteMode.CATEGORIA -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Escolha a categoria a ser sorteada:",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Category.entries.forEach { cat ->
                                val isSelected = rouletteCategory == cat
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { viewModel.setRouletteCategory(cat) },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) NeonPurple.copy(alpha = 0.25f) else GraphiteSurface
                                    ),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isSelected) NeonPurple else GraphiteBorder
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 10.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            imageVector = cat.icon,
                                            contentDescription = cat.pluralName,
                                            tint = if (isSelected) NeonCyan else TextSecondary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = cat.displayName,
                                            color = if (isSelected) Color.White else TextSecondary,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                RouletteMode.PERSONALIZADA -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(GraphiteSurface)
                            .border(1.dp, GraphiteBorder, RoundedCornerShape(14.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Filtro de Status personalizado:",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(WatchStatus.activeCatalogStatuses) { status ->
                                val isChecked = customStatus.contains(status)
                                FilterChip(
                                    selected = isChecked,
                                    onClick = { viewModel.toggleCustomStatusFilter(status) },
                                    label = { Text(status.label, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = status.color,
                                        selectedLabelColor = Color.White,
                                        containerColor = GraphiteSurfaceVariant,
                                        labelColor = TextSecondary
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // Card de Título Sorteado em Destaque (Spotlight Card)
        item {
            RouletteSpotlightCard(
                title = activeRouletteTitle,
                isManualSelection = isManualSelection,
                eligibleCount = eligibleTitles.size,
                onOpenDetails = { viewModel.setShowRouletteResult(true) },
                onQuickAction = {
                    activeRouletteTitle?.let { title ->
                        if (title.isMovie) {
                            viewModel.toggleMovieWatched(title)
                        } else {
                            viewModel.advanceEpisode(title, 1)
                        }
                    }
                }
            )
        }

        // Indicador de títulos elegíveis e ciclo
        item {
            val drawnInCycleCount = eligibleTitles.count { cycleDrawnIds.contains(it.id) }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "${eligibleTitles.size} títulos elegíveis no sorteio atual",
                    color = if (eligibleTitles.isNotEmpty()) NeonCyan else Color(0xFFEF4444),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                if (eligibleTitles.isNotEmpty()) {
                    Text(
                        text = "Ciclo de repetição: $drawnInCycleCount/${eligibleTitles.size} títulos já participaram",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Primary Actions: "SORTEAR TÍTULO" and "SELEÇÃO MANUAL" Buttons
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        viewModel.drawRouletteTitle()
                    },
                    enabled = eligibleTitles.isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("spin_roulette_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonPurple,
                        disabledContainerColor = GraphiteSurfaceVariant
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Casino,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (activeRouletteTitle != null) "SORTEAR OUTRO TÍTULO" else "SORTEAR TÍTULO",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }
                }

                // Botão de Seleção Manual
                OutlinedButton(
                    onClick = { showManualSelectionDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("manual_selection_button"),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, NeonCyan),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = NeonCyan.copy(alpha = 0.08f)
                    )
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "SELEÇÃO MANUAL",
                            color = NeonCyan,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }

        // Advanced Options & Toggles Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
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
                        text = "OPÇÕES DO SORTEIO",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    // Excluir concluídos
                    RouletteToggleItem(
                        label = "Excluir títulos já concluídos",
                        checked = excludeCompleted,
                        onCheckedChange = { viewModel.toggleRouletteExcludeCompleted() }
                    )


                    // Evitar repetir último
                    RouletteToggleItem(
                        label = "Evitar repetir o último sorteado",
                        checked = avoidLastResult,
                        onCheckedChange = { viewModel.toggleRouletteAvoidLastResult() }
                    )
                }
            }
        }
    }
}

@Composable
private fun RouletteToggleItem(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = TextPrimary,
            fontSize = 12.sp,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = NeonCyan,
                checkedTrackColor = NeonPurpleDark,
                uncheckedThumbColor = TextSecondary,
                uncheckedTrackColor = GraphiteSurfaceVariant
            )
        )
    }
}

@Composable
fun WinnerCelebrationDialog(
    title: TitleEntity,
    onDismiss: () -> Unit,
    onOpenDetails: () -> Unit,
    onSpinAgain: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .border(
                        2.dp,
                        Brush.linearGradient(listOf(NeonPurple, NeonCyan)),
                        RoundedCornerShape(24.dp)
                    ),
                colors = CardDefaults.cardColors(containerColor = GraphiteDark)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Close button
                    Box(modifier = Modifier.fillMaxWidth()) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.align(Alignment.TopEnd)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Fechar",
                                tint = TextSecondary
                            )
                        }
                    }

                    // Celebration Header
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "O DESTINO ESCOLHEU!",
                            color = NeonCyan,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Title Poster
                    Box(
                        modifier = Modifier
                            .size(width = 160.dp, height = 220.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .border(2.dp, NeonPurple, RoundedCornerShape(16.dp))
                            .background(GraphiteSurface)
                    ) {
                        if (title.coverUrl.isNotBlank()) {
                            AsyncImage(
                                model = title.coverUrl,
                                contentDescription = title.name,
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = title.name,
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "${title.categoryEnum.displayName} • ${title.releaseYear} • ${title.genre}",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StatusBadge(status = title.statusEnum)

                        if (title.personalRating > 0f) {
                            Spacer(modifier = Modifier.width(10.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color(0xFFFFD700),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = String.format("%.1f", title.personalRating),
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    if (title.synopsis.isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = title.synopsis,
                            color = TextSecondary,
                            fontSize = 11.sp,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onSpinAgain,
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GraphiteSurfaceVariant)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Girar de Novo", color = NeonCyan, fontSize = 12.sp)
                        }

                        Button(
                            onClick = onOpenDetails,
                            modifier = Modifier
                                .weight(1.2f)
                                .height(46.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NeonPurple)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Ver Detalhes", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

private val NeonPurpleDark = Color(0xFF7A14B8)

@Composable
private fun RouletteSpotlightCard(
    title: TitleEntity?,
    isManualSelection: Boolean,
    eligibleCount: Int,
    onOpenDetails: () -> Unit,
    onQuickAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("roulette_spotlight_card")
            .clip(RoundedCornerShape(20.dp))
            .border(
                1.5.dp,
                if (title != null) {
                    Brush.linearGradient(listOf(NeonPurple, NeonCyan.copy(alpha = 0.8f)))
                } else {
                    Brush.linearGradient(listOf(GraphiteBorder, NeonPurple.copy(alpha = 0.3f)))
                },
                RoundedCornerShape(20.dp)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = GraphiteSurface)
    ) {
        if (title != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Header badge row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isManualSelection) NeonCyan.copy(alpha = 0.18f) else NeonPurple.copy(alpha = 0.2f)
                            )
                            .border(
                                1.dp,
                                if (isManualSelection) NeonCyan.copy(alpha = 0.7f) else NeonPurple.copy(alpha = 0.6f),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isManualSelection) Icons.Default.FilterList else Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = if (isManualSelection) NeonCyan else NeonCyan,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = if (isManualSelection) "ESCOLHIDO MANUALMENTE" else "TÍTULO SORTEADO",
                            color = NeonCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.8.sp
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(GraphiteDark)
                                .padding(horizontal = 7.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = title.categoryEnum.displayName.uppercase(),
                                color = NeonPurple,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (title.personalRating > 0f) {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(GraphiteDark)
                                    .padding(horizontal = 6.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color(0xFFFFD700),
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = String.format("%.1f", title.personalRating),
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Poster + Details row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    // Large Poster
                    Box(
                        modifier = Modifier
                            .size(width = 115.dp, height = 165.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF141822))
                            .border(1.dp, NeonPurple.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
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
                                    tint = NeonPurple.copy(alpha = 0.6f),
                                    modifier = Modifier.size(44.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    // Title info column
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = title.name,
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(
                            text = "${title.releaseYear} • ${title.genre}",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (title.isMovie) {
                            if (title.duration.isNotBlank()) {
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
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                            StatusBadge(status = title.statusEnum, useShortLabel = true)
                        } else {
                            // Series / Anime info
                            Text(
                                text = "Temporada ${title.currentSeason} de ${title.totalSeasons}",
                                color = NeonCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Episódio ${title.currentEpisode} de ${title.totalEpisodes} (${(title.progressPercentage * 100).toInt()}%)",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                            LinearProgressIndicator(
                                progress = { title.progressPercentage },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = NeonCyan,
                                trackColor = GraphiteDark
                            )
                            StatusBadge(status = title.statusEnum, useShortLabel = true)
                        }
                    }
                }

                if (title.synopsis.isNotBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = title.synopsis,
                        color = TextSecondary,
                        fontSize = 11.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Card actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onOpenDetails,
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonPurple)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Ver Detalhes",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedButton(
                        onClick = onQuickAction,
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.7f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan)
                    ) {
                        Text(
                            text = if (title.isMovie) {
                                if (title.statusEnum == WatchStatus.COMPLETO) "Desmarcar" else "Concluir"
                            } else {
                                "+1 Episódio"
                            },
                            color = NeonCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        } else {
            // Placeholder empty state
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(NeonPurple.copy(alpha = 0.25f), NeonCyan.copy(alpha = 0.15f))
                            )
                        )
                        .border(1.dp, NeonCyan.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Casino,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(34.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Pronto para o Sorteio?",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Configure seus filtros acima e toque no botão abaixo para sortear o próximo título.",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(14.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(GraphiteDark)
                        .border(1.dp, GraphiteBorder, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "$eligibleCount títulos elegíveis",
                        color = NeonCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun ManualSelectionDialog(
    titles: List<TitleEntity>,
    activeTitleId: Long?,
    cycleDrawnIds: Set<Long>,
    onSelect: (TitleEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf<Category?>(null) }

    val filteredTitles = remember(titles, searchQuery, selectedCategoryFilter) {
        titles.filter { title ->
            val matchesQuery = searchQuery.isBlank() || title.name.contains(searchQuery, ignoreCase = true) || title.genre.contains(searchQuery, ignoreCase = true)
            val matchesCat = selectedCategoryFilter == null || title.categoryEnum == selectedCategoryFilter
            matchesQuery && matchesCat
        }
    }

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
                    .height(600.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.5.dp, NeonCyan, RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = GraphiteDark)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "SELEÇÃO MANUAL",
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                        }

                        IconButton(onClick = onDismiss) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Fechar", tint = TextSecondary)
                        }
                    }

                    Text(
                        text = "Escolha diretamente uma obra para a roleta. O título selecionado entrará no ciclo de repetição universal e não será repetido no sorteio automático subsequente.",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Search input
                    androidx.compose.material3.OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Buscar por nome ou gênero...", fontSize = 12.sp, color = TextSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = GraphiteBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            cursorColor = NeonCyan
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Category chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            FilterChip(
                                selected = selectedCategoryFilter == null,
                                onClick = { selectedCategoryFilter = null },
                                label = { Text("Todos", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = NeonPurple,
                                    selectedLabelColor = Color.White,
                                    containerColor = GraphiteSurfaceVariant,
                                    labelColor = TextSecondary
                                )
                            )
                        }
                        items(Category.entries) { cat ->
                            val isSelected = selectedCategoryFilter == cat
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedCategoryFilter = if (isSelected) null else cat },
                                label = { Text(cat.displayName, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = NeonCyan,
                                    selectedLabelColor = Color.Black,
                                    containerColor = GraphiteSurfaceVariant,
                                    labelColor = TextSecondary
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Title list
                    if (filteredTitles.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Nenhum título encontrado.",
                                color = TextSecondary,
                                fontSize = 13.sp
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(filteredTitles, key = { it.id }) { itemTitle ->
                                val isCurrentActive = itemTitle.id == activeTitleId
                                val hasBeenDrawnInCycle = cycleDrawnIds.contains(itemTitle.id)

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { onSelect(itemTitle) }
                                        .border(
                                            1.dp,
                                            if (isCurrentActive) NeonCyan else GraphiteBorder,
                                            RoundedCornerShape(12.dp)
                                        ),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isCurrentActive) NeonCyan.copy(alpha = 0.12f) else GraphiteSurface
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Mini poster
                                        Box(
                                            modifier = Modifier
                                                .size(width = 42.dp, height = 56.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(GraphiteDark)
                                        ) {
                                            if (itemTitle.coverUrl.isNotBlank()) {
                                                AsyncImage(
                                                    model = itemTitle.coverUrl,
                                                    contentDescription = itemTitle.name,
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = itemTitle.name,
                                                color = TextPrimary,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "${itemTitle.categoryEnum.displayName} • ${itemTitle.releaseYear} • ${itemTitle.genre}",
                                                color = TextSecondary,
                                                fontSize = 10.sp
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                if (isCurrentActive) {
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(4.dp))
                                                            .background(NeonCyan.copy(alpha = 0.2f))
                                                            .padding(horizontal = 5.dp, vertical = 2.dp)
                                                    ) {
                                                        Text("EM DESTAQUE NA ROLETA", color = NeonCyan, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                                if (hasBeenDrawnInCycle) {
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(4.dp))
                                                            .background(NeonPurple.copy(alpha = 0.2f))
                                                            .padding(horizontal = 5.dp, vertical = 2.dp)
                                                    ) {
                                                        Text("JÁ NO CICLO", color = NeonPurple, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }
                                        }

                                        Button(
                                            onClick = { onSelect(itemTitle) },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (isCurrentActive) NeonCyan else NeonPurple
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = if (isCurrentActive) "Ativo" else "Escolher",
                                                color = if (isCurrentActive) Color.Black else Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

