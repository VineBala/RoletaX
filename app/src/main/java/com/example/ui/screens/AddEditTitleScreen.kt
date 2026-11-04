package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.model.Category
import com.example.data.model.TitleEntity
import com.example.data.model.WatchStatus
import com.example.data.remote.TitleSearchResult
import com.example.ui.components.StatusBadge
import com.example.ui.theme.GraphiteBorder
import com.example.ui.theme.GraphiteSurface
import com.example.ui.theme.GraphiteSurfaceVariant
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusRed
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.SeraAkisosViewModel

@Composable
fun AddEditTitleScreen(
    viewModel: SeraAkisosViewModel,
    modifier: Modifier = Modifier
) {
    val formState by viewModel.formState.collectAsStateWithLifecycle()
    val allTitles by viewModel.allTitles.collectAsStateWithLifecycle()
    val searchQuery by viewModel.onlineSearchQuery.collectAsStateWithLifecycle()
    val searchCategory by viewModel.onlineSearchCategory.collectAsStateWithLifecycle()
    val isSearching by viewModel.isSearchingOnline.collectAsStateWithLifecycle()
    val searchResults by viewModel.onlineSearchResults.collectAsStateWithLifecycle()
    val searchError by viewModel.onlineSearchError.collectAsStateWithLifecycle()

    var selectedTab by remember(formState.isEditing) {
        mutableIntStateOf(if (formState.isEditing) 1 else 0)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Screen Title
        Text(
            text = if (formState.isEditing) "EDITAR TÍTULO" else "NOVO TÍTULO",
            color = TextPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Tabs: Buscar Título vs Cadastro Manual
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = GraphiteSurface,
            contentColor = NeonCyan,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = NeonCyan,
                    height = 3.dp
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, GraphiteBorder, RoundedCornerShape(12.dp))
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = if (selectedTab == 0) NeonCyan else TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "BUSCAR TÍTULO",
                            fontSize = 12.sp,
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 0) NeonCyan else TextSecondary
                        )
                    }
                }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = if (selectedTab == 1) NeonCyan else TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (formState.isEditing) "EDITAR DADOS" else "CADASTRO MANUAL",
                            fontSize = 12.sp,
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 1) NeonCyan else TextSecondary
                        )
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (selectedTab == 0) {
            // Tab 0: BUSCAR TÍTULO (Online Search)
            OnlineSearchContent(
                query = searchQuery,
                onQueryChange = { viewModel.setOnlineSearchQuery(it) },
                category = searchCategory,
                onCategoryChange = { viewModel.setOnlineSearchCategory(it) },
                onSearch = { viewModel.performOnlineSearch() },
                isSearching = isSearching,
                results = searchResults,
                allTitles = allTitles,
                error = searchError,
                onSelectResult = { result ->
                    viewModel.prefillFromSearchResult(result)
                    selectedTab = 1
                },
                onAlreadyAdded = { existing ->
                    viewModel.showMessage("O título '${existing.name}' já está cadastrado na sua biblioteca!")
                },
                onSwitchToManual = { selectedTab = 1 }
            )
        } else {
            // Tab 1: CADASTRO MANUAL / REVIEW & CONFIRM
            ManualFormContent(
                formState = formState,
                onUpdateField = { updater -> viewModel.updateFormField(updater) },
                onSave = { viewModel.saveCurrentForm() }
            )
        }
    }
}

@Composable
private fun OnlineSearchContent(
    query: String,
    onQueryChange: (String) -> Unit,
    category: Category?,
    onCategoryChange: (Category?) -> Unit,
    onSearch: () -> Unit,
    isSearching: Boolean,
    results: List<TitleSearchResult>,
    allTitles: List<TitleEntity>,
    error: String?,
    onSelectResult: (TitleSearchResult) -> Unit,
    onAlreadyAdded: (TitleEntity) -> Unit,
    onSwitchToManual: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Busque dados oficiais de filmes, séries ou animes automaticamente:",
                    color = TextSecondary,
                    fontSize = 12.sp
                )

                // Search field + Search button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = onQueryChange,
                        placeholder = { Text("Nome da obra (ex: One Piece, Arcane, Interestelar)", fontSize = 12.sp, color = TextSecondary) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("online_search_query_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = GraphiteSurface,
                            unfocusedContainerColor = GraphiteSurface,
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = GraphiteBorder
                        )
                    )

                    Button(
                        onClick = onSearch,
                        enabled = query.isNotBlank() && !isSearching,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                        modifier = Modifier
                            .height(54.dp)
                            .testTag("execute_online_search_button")
                    ) {
                        if (isSearching) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Buscar",
                                tint = Color.White
                            )
                        }
                    }
                }

                // Filter by category
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = category == null,
                        onClick = { onCategoryChange(null) },
                        label = { Text("Todos", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NeonCyan,
                            selectedLabelColor = Color.Black,
                            containerColor = GraphiteSurface,
                            labelColor = TextSecondary
                        )
                    )
                    Category.entries.forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { onCategoryChange(if (category == cat) null else cat) },
                            label = { Text(cat.pluralName, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NeonPurple,
                                selectedLabelColor = Color.White,
                                containerColor = GraphiteSurface,
                                labelColor = TextSecondary
                            )
                        )
                    }
                }
            }
        }

        if (error != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = GraphiteSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = error,
                            color = TextSecondary,
                            fontSize = 12.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = onSwitchToManual,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Preencher Manualmente", color = NeonCyan, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        if (results.isNotEmpty()) {
            item {
                Text(
                    text = "RESULTADOS ENCONTRADOS (${results.size}) - Toque para revisar e salvar:",
                    color = NeonCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }

            items(results) { item ->
                SearchResultCard(
                    item = item,
                    allTitles = allTitles,
                    onSelect = { onSelectResult(item) },
                    onAlreadyAdded = onAlreadyAdded
                )
            }
        }
    }
}

@Composable
private fun SearchResultCard(
    item: TitleSearchResult,
    allTitles: List<TitleEntity>,
    onSelect: () -> Unit,
    onAlreadyAdded: (TitleEntity) -> Unit
) {
    val existingTitle = remember(item, allTitles) {
        allTitles.firstOrNull { existing ->
            val cleanExisting = existing.name.trim().lowercase().replace(Regex("[^a-z0-9]"), "")
            val cleanSearch = item.title.trim().lowercase().replace(Regex("[^a-z0-9]"), "")
            (cleanExisting.isNotEmpty() && cleanExisting == cleanSearch) ||
            existing.name.trim().equals(item.title.trim(), ignoreCase = true)
        }
    }
    val isAlreadyAdded = existingTitle != null

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = {
                if (isAlreadyAdded && existingTitle != null) {
                    onAlreadyAdded(existingTitle)
                } else {
                    onSelect()
                }
            }),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = GraphiteSurface),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isAlreadyAdded) StatusGreen.copy(alpha = 0.7f) else GraphiteBorder
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(width = 64.dp, height = 90.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(GraphiteSurfaceVariant)
            ) {
                if (item.coverUrl.isNotBlank()) {
                    AsyncImage(
                        model = item.coverUrl,
                        contentDescription = item.title,
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(NeonPurple.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = item.category.displayName,
                                color = NeonPurple,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (isAlreadyAdded) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(StatusGreen.copy(alpha = 0.2f))
                                    .border(1.dp, StatusGreen.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = StatusGreen,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "ADICIONADO",
                                        color = StatusGreen,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }
                    }

                    Text(
                        text = item.source,
                        color = TextSecondary,
                        fontSize = 10.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = item.title,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "${item.releaseYear} • ${item.genre}",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (item.category == Category.FILME) {
                    if (item.duration.isNotBlank()) {
                        Spacer(modifier = Modifier.height(3.dp))
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(NeonCyan.copy(alpha = 0.12f))
                                .border(0.5.dp, NeonCyan.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Duração: ${item.duration}",
                                color = NeonCyan,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.height(3.dp))
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(NeonCyan.copy(alpha = 0.12f))
                            .border(0.5.dp, NeonCyan.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (item.seasonBreakdown.isNotBlank())
                                item.seasonBreakdown
                            else
                                "${item.totalSeasons} temporada(s) • ${item.totalEpisodes} episódios",
                            color = NeonCyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (item.synopsis.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = item.synopsis,
                        color = TextSecondary,
                        fontSize = 11.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    if (isAlreadyAdded) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(StatusGreen.copy(alpha = 0.18f))
                                .border(1.dp, StatusGreen.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = StatusGreen,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Já Adicionado",
                                color = StatusGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(NeonCyan.copy(alpha = 0.15f))
                                .border(1.dp, NeonCyan.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudDownload,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Importar & Confirmar",
                                color = NeonCyan,
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

@Composable
private fun ManualFormContent(
    formState: com.example.viewmodel.TitleFormState,
    onUpdateField: ((com.example.viewmodel.TitleFormState) -> com.example.viewmodel.TitleFormState) -> Unit,
    onSave: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Nome
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = "Nome do Título *", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = formState.name,
                    onValueChange = { value -> onUpdateField { it.copy(name = value) } },
                    placeholder = { Text("Ex: One Piece, Interestelar...", color = TextSecondary) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("form_title_name"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = GraphiteSurface,
                        unfocusedContainerColor = GraphiteSurface,
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = GraphiteBorder
                    )
                )
            }
        }

        // Categoria (Filme, Série, Anime)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(text = "Categoria *", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Category.entries.forEach { cat ->
                        val isSelected = formState.category == cat
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    onUpdateField {
                                        if (cat == Category.FILME) {
                                            it.copy(
                                                category = cat,
                                                totalSeasons = 0,
                                                episodesPerSeason = 0,
                                                currentSeason = 0,
                                                currentEpisode = 0,
                                                totalEpisodes = 0
                                            )
                                        } else {
                                            it.copy(
                                                category = cat,
                                                totalSeasons = if (it.totalSeasons > 0) it.totalSeasons else 1,
                                                episodesPerSeason = if (it.episodesPerSeason > 0) it.episodesPerSeason else 12,
                                                currentSeason = if (it.currentSeason > 0) it.currentSeason else 1,
                                                currentEpisode = if (it.currentEpisode > 0) it.currentEpisode else 0,
                                                totalEpisodes = if (it.totalEpisodes > 0) it.totalEpisodes else 12
                                            )
                                        }
                                    }
                                },
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
                                    .padding(vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = cat.icon,
                                    contentDescription = null,
                                    tint = if (isSelected) NeonCyan else TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = cat.displayName,
                                    color = if (isSelected) Color.White else TextSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
        }

        // Capa (URL com visualização)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(text = "URL da Capa (Imagem)", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = formState.coverUrl,
                        onValueChange = { value -> onUpdateField { it.copy(coverUrl = value) } },
                        placeholder = { Text("https://...", color = TextSecondary, fontSize = 12.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("form_cover_url"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = GraphiteSurface,
                            unfocusedContainerColor = GraphiteSurface,
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = GraphiteBorder
                        )
                    )

                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(GraphiteSurface)
                            .border(1.dp, GraphiteBorder, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (formState.coverUrl.isNotBlank()) {
                            AsyncImage(
                                model = formState.coverUrl,
                                contentDescription = "Preview",
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }

        // Ano e Gênero
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(text = "Ano", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = formState.releaseYear.toString(),
                        onValueChange = { value ->
                            val parsed = value.toIntOrNull() ?: formState.releaseYear
                            onUpdateField { it.copy(releaseYear = parsed) }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = GraphiteSurface,
                            unfocusedContainerColor = GraphiteSurface,
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = GraphiteBorder
                        )
                    )
                }

                Column(modifier = Modifier.weight(2f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(text = "Gênero(s)", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = formState.genre,
                        onValueChange = { value -> onUpdateField { it.copy(genre = value) } },
                        placeholder = { Text("Ex: Ação, Aventura", color = TextSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = GraphiteSurface,
                            unfocusedContainerColor = GraphiteSurface,
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = GraphiteBorder
                        )
                    )
                }
            }
        }

        // Filmes: Duração em horas e minutos (Sem temporadas e sem episódios)
        if (formState.category == Category.FILME) {
            item {
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
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "DURAÇÃO DO FILME",
                                color = NeonCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }

                        Text(
                            text = "Duração em horas e minutos (ex: 1h 45m, 2h 10m):",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )

                        OutlinedTextField(
                            value = formState.duration,
                            onValueChange = { value -> onUpdateField { it.copy(duration = value) } },
                            placeholder = { Text("Ex: 1h 45m (deixe vazio se não souber)", color = TextSecondary, fontSize = 12.sp) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("form_duration_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = GraphiteSurfaceVariant,
                                unfocusedContainerColor = GraphiteSurfaceVariant,
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = GraphiteBorder
                            )
                        )
                    }
                }
            }
        }

        // Séries / Animes: Temporadas e Episódios
        if (formState.category != Category.FILME) {
            item {
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
                            text = "CONTROLE DE EPISÓDIOS E TEMPORADAS",
                            color = NeonCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Temporada Atual
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(text = "Temp. Atual", color = TextSecondary, fontSize = 11.sp)
                                OutlinedTextField(
                                    value = formState.currentSeason.toString(),
                                    onValueChange = { v ->
                                        val p = v.toIntOrNull() ?: formState.currentSeason
                                        onUpdateField { it.copy(currentSeason = p) }
                                    },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }

                            // Total de Temporadas
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(text = "Total Temps.", color = TextSecondary, fontSize = 11.sp)
                                OutlinedTextField(
                                    value = formState.totalSeasons.toString(),
                                    onValueChange = { v ->
                                        val p = v.toIntOrNull() ?: formState.totalSeasons
                                        onUpdateField { it.copy(totalSeasons = p) }
                                    },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Episódio Atual
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(text = "Episódio Atual", color = TextSecondary, fontSize = 11.sp)
                                OutlinedTextField(
                                    value = formState.currentEpisode.toString(),
                                    onValueChange = { v ->
                                        val p = v.toIntOrNull() ?: formState.currentEpisode
                                        onUpdateField { it.copy(currentEpisode = p) }
                                    },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }

                            // Total de Episódios
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(text = "Total Episódios", color = TextSecondary, fontSize = 11.sp)
                                OutlinedTextField(
                                    value = formState.totalEpisodes.toString(),
                                    onValueChange = { v ->
                                        val p = v.toIntOrNull() ?: formState.totalEpisodes
                                        onUpdateField { it.copy(totalEpisodes = p) }
                                    },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Status do Título (Verde, Vermelho, Completo, Pausado, Pendente)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(text = "Status *", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    WatchStatus.activeCatalogStatuses.forEach { status ->
                        val isSelected = formState.status == status
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
                                .clickable { onUpdateField { it.copy(status = status) } }
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
        }

        // Nota Pessoal (0 a 10)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Nota Pessoal", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = String.format("%.1f / 10", formState.personalRating),
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Slider(
                    value = formState.personalRating,
                    onValueChange = { v -> onUpdateField { it.copy(personalRating = v) } },
                    valueRange = 0f..10f,
                    steps = 19,
                    colors = SliderDefaults.colors(
                        thumbColor = NeonCyan,
                        activeTrackColor = NeonPurple,
                        inactiveTrackColor = GraphiteBorder
                    )
                )
            }
        }

        // Sinopse
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = "Sinopse", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = formState.synopsis,
                    onValueChange = { value -> onUpdateField { it.copy(synopsis = value) } },
                    placeholder = { Text("Breve resumo da trama...", color = TextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = GraphiteSurface,
                        unfocusedContainerColor = GraphiteSurface,
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = GraphiteBorder
                    )
                )
            }
        }

        // Observações
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = "Observações Pessoais", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = formState.notes,
                    onValueChange = { value -> onUpdateField { it.copy(notes = value) } },
                    placeholder = { Text("Ex: Gostei muito da temporada 1, aguardando dublagem...", color = TextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = GraphiteSurface,
                        unfocusedContainerColor = GraphiteSurface,
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = GraphiteBorder
                    )
                )
            }
        }

        // Botão Salvar Título
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Button(
                onClick = onSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_title_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NeonPurple)
            ) {
                Text(
                    text = if (formState.isEditing) "SALVAR ALTERAÇÕES" else "SALVAR NA BIBLIOTECA",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}
