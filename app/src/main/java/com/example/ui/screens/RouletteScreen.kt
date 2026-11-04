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
import com.example.ui.components.RouletteWheel
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

    val eligibleTitles = viewModel.getEligibleTitles()
    var targetWinnerIndex by remember { mutableIntStateOf(0) }
    var selectedWinnerTitle by remember { mutableStateOf<TitleEntity?>(null) }

    // NOVA INTERFACE DE RESULTADO DA ROLETA
    if (showRouletteResult && activeRouletteTitle != null) {
        RouletteResultView(
            title = activeRouletteTitle!!,
            onBack = { viewModel.setShowRouletteResult(false) },
            onSpinAgain = {
                viewModel.setShowRouletteResult(false)
                viewModel.spinRoulette { chosenWinner, chosenIndex ->
                    selectedWinnerTitle = chosenWinner
                    targetWinnerIndex = chosenIndex
                }
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

        // Card do Último Resultado Sorteado (permanece salvo e acessível a qualquer momento)
        activeRouletteTitle?.let { activeTitle ->
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("roulette_active_result_card")
                        .clickable { viewModel.setShowRouletteResult(true) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = GraphiteSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonPurple.copy(alpha = 0.8f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(width = 44.dp, height = 62.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(GraphiteDark)
                                    .border(1.dp, NeonCyan.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            ) {
                                if (activeTitle.coverUrl.isNotBlank()) {
                                    AsyncImage(
                                        model = activeTitle.coverUrl,
                                        contentDescription = activeTitle.name,
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = activeTitle.categoryEnum.icon,
                                            contentDescription = null,
                                            tint = NeonPurple,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = NeonCyan,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "ÚLTIMO SORTEIO ATIVO",
                                        color = NeonCyan,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                                Text(
                                    text = activeTitle.name,
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = if (activeTitle.isMovie) {
                                        if (activeTitle.duration.isNotBlank()) "Filme • ${activeTitle.duration}" else "Filme"
                                    } else {
                                        "Ep ${activeTitle.currentEpisode}/${activeTitle.totalEpisodes} • Temp ${activeTitle.currentSeason}"
                                    },
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = { viewModel.setShowRouletteResult(true) },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("open_roulette_result_button")
                        ) {
                            Text("Ver Resultado", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
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

        // Roulette Interactive Visual Wheel
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                RouletteWheel(
                    items = eligibleTitles,
                    isSpinning = isSpinning,
                    targetWinnerIndex = targetWinnerIndex,
                    onSpinComplete = {
                        selectedWinnerTitle?.let { winnerTitle ->
                            viewModel.finishSpin(winnerTitle)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Eligible count indicator
                Text(
                    text = "${eligibleTitles.size} títulos elegíveis para o sorteio",
                    color = if (eligibleTitles.isNotEmpty()) NeonCyan else Color(0xFFEF4444),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Primary Action: "GIRAR ROLETA" Button
        item {
            Button(
                onClick = {
                    viewModel.spinRoulette { chosenWinner, chosenIndex ->
                        selectedWinnerTitle = chosenWinner
                        targetWinnerIndex = chosenIndex
                    }
                },
                enabled = !isSpinning && eligibleTitles.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
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
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (isSpinning) "SORTEANDO..." else "GIRAR ROLETA",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
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
