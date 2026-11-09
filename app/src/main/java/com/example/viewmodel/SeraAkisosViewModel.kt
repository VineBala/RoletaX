package com.example.viewmodel

import android.app.Application
import android.content.ContentValues
import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.Category
import com.example.data.model.TitleEntity
import com.example.data.model.UpdateResult
import com.example.data.model.WatchStatus
import com.example.data.remote.TitleSearchResult
import com.example.data.repository.TitleRepository
import com.example.util.NotificationHelper
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

enum class NavDestination(val title: String) {
    HOME("Início"),
    LIBRARY("Biblioteca"),
    ROULETTE("Roleta"),
    ADD("Adicionar"),
    SETTINGS("Configurações")
}

enum class RouletteMode(val title: String, val description: String) {
    GERAL("Roleta Geral", "Sorteia entre todo o catálogo elegível"),
    CATEGORIA("Por Categoria", "Escolha entre Filmes, Séries ou Animes"),
    PERSONALIZADA("Personalizada", "Filtros avançados de status e gênero")
}

enum class LibrarySort(val label: String) {
    RECENT_UPDATE("Mais Recentes"),
    TITLE_ASC("Nome (A-Z)"),
    TITLE_DESC("Nome (Z-A)"),
    RATING_DESC("Maior Nota"),
    PROGRESS_DESC("Maior Progresso"),
    YEAR_DESC("Ano de Lançamento")
}

data class TitleFormState(
    val id: Long? = null,
    val name: String = "",
    val coverUrl: String = "",
    val synopsis: String = "",
    val category: Category = Category.FILME,
    val releaseYear: Int = 2024,
    val genre: String = "Ação",
    val totalSeasons: Int = 1,
    val episodesPerSeason: Int = 1,
    val currentSeason: Int = 1,
    val currentEpisode: Int = 0,
    val totalEpisodes: Int = 1,
    val duration: String = "",
    val status: WatchStatus = WatchStatus.PENDENTE,
    val isIgnored: Boolean = false,
    val completedAt: Long? = null,
    val personalRating: Float = 0f,
    val notes: String = "",
    val isEditing: Boolean = false
)

class SeraAkisosViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TitleRepository
    private val roulettePrefs = application.getSharedPreferences("vortex_roulette_prefs", Context.MODE_PRIVATE)
    private val appPrefs = application.getSharedPreferences("vortex_user_prefs", Context.MODE_PRIVATE)
    private val _hasSeenWelcome = MutableStateFlow(appPrefs.getBoolean("has_seen_welcome_v2", false))
    val hasSeenWelcome: StateFlow<Boolean> = _hasSeenWelcome.asStateFlow()

    private val _activeWinnerId = MutableStateFlow<Long?>(null)
    private val _isManualSelection = MutableStateFlow<Boolean>(false)
    val isManualSelection: StateFlow<Boolean> = _isManualSelection.asStateFlow()

    private val _rouletteDrawnCycleIds = MutableStateFlow<Set<Long>>(emptySet())
    val rouletteDrawnCycleIds: StateFlow<Set<Long>> = _rouletteDrawnCycleIds.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = TitleRepository(database.titleDao())
        viewModelScope.launch {
            repository.seedSampleDataIfEmpty()
        }
        val savedWinnerId = roulettePrefs.getLong("active_roulette_winner_id", -1L)
        if (savedWinnerId > 0) {
            _activeWinnerId.value = savedWinnerId
        }
        _isManualSelection.value = roulettePrefs.getBoolean("active_roulette_is_manual", false)

        val savedCycleString = roulettePrefs.getString("roulette_cycle_ids", "") ?: ""
        if (savedCycleString.isNotBlank()) {
            val ids = savedCycleString.split(",")
                .mapNotNull { it.trim().toLongOrNull() }
                .toSet()
            _rouletteDrawnCycleIds.value = ids
        }
    }

    // Navigation
    private val _currentNav = MutableStateFlow(NavDestination.HOME)
    val currentNav: StateFlow<NavDestination> = _currentNav.asStateFlow()

    fun navigateTo(destination: NavDestination) {
        _currentNav.value = destination
    }

    // Dark Theme Toggle
    private val _isDarkTheme = MutableStateFlow(true)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    fun toggleDarkTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
    }

    fun setDarkTheme(dark: Boolean) {
        _isDarkTheme.value = dark
    }

    // UI Snackbars / Events
    private val _messageEvents = MutableSharedFlow<String>()
    val messageEvents = _messageEvents.asSharedFlow()

    fun showMessage(msg: String) {
        viewModelScope.launch {
            _messageEvents.emit(msg)
        }
    }

    // All Titles from DB
    val allTitles: StateFlow<List<TitleEntity>> = repository.allTitles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val inProgressTitles: StateFlow<List<TitleEntity>> = repository.inProgressTitles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentTitles: StateFlow<List<TitleEntity>> = repository.recentTitles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selected Title for Detail Dialog
    private val _selectedTitle = MutableStateFlow<TitleEntity?>(null)
    val selectedTitle: StateFlow<TitleEntity?> = _selectedTitle.asStateFlow()

    fun openTitleDetails(title: TitleEntity) {
        _selectedTitle.value = title
    }

    fun closeTitleDetails() {
        _selectedTitle.value = null
    }

    // Library Filtering & Sorting
    private val _librarySearchQuery = MutableStateFlow("")
    val librarySearchQuery = _librarySearchQuery.asStateFlow()

    private val _selectedCategoryFilter = MutableStateFlow<Category?>(null)
    val selectedCategoryFilter = _selectedCategoryFilter.asStateFlow()

    private val _selectedStatusFilter = MutableStateFlow<WatchStatus?>(null)
    val selectedStatusFilter = _selectedStatusFilter.asStateFlow()

    private val _selectedSort = MutableStateFlow(LibrarySort.RECENT_UPDATE)
    val selectedSort = _selectedSort.asStateFlow()

    private val _showOnlyIgnored = MutableStateFlow(false)
    val showOnlyIgnored = _showOnlyIgnored.asStateFlow()

    fun setLibrarySearchQuery(q: String) {
        _librarySearchQuery.value = q
    }

    fun setCategoryFilter(category: Category?) {
        _selectedCategoryFilter.value = category
    }

    fun setStatusFilter(status: WatchStatus?) {
        _selectedStatusFilter.value = status
    }

    fun setSort(sort: LibrarySort) {
        _selectedSort.value = sort
    }

    fun toggleShowOnlyIgnored() {
        _showOnlyIgnored.value = !_showOnlyIgnored.value
    }

    fun clearFilters() {
        _librarySearchQuery.value = ""
        _selectedCategoryFilter.value = null
        _selectedStatusFilter.value = null
        _showOnlyIgnored.value = false
    }

    private data class LibraryFilterCriteria(
        val query: String,
        val category: Category?,
        val status: WatchStatus?,
        val sort: LibrarySort,
        val onlyIgnored: Boolean
    )

    private val _libraryFilterCriteria = combine(
        _librarySearchQuery,
        _selectedCategoryFilter,
        _selectedStatusFilter,
        _selectedSort,
        _showOnlyIgnored
    ) { query, category, status, sort, onlyIgnored ->
        LibraryFilterCriteria(query, category, status, sort, onlyIgnored)
    }

    // Filtered Titles stream
    val filteredTitles: StateFlow<List<TitleEntity>> = combine(
        allTitles,
        _libraryFilterCriteria
    ) { titles, criteria ->
        var list = titles

        if (criteria.onlyIgnored) {
            list = list.filter { it.isIgnored }
        }

        if (criteria.query.isNotBlank()) {
            list = list.filter {
                com.example.util.TitleSearchHelper.matchesQuery(
                    query = criteria.query,
                    targetTitle = it.name,
                    extraSearchFields = listOf(it.genre, it.synopsis, it.releaseYear.toString(), it.notes)
                )
            }
        }

        if (criteria.category != null) {
            list = list.filter { it.categoryEnum == criteria.category }
        }

        if (criteria.status != null) {
            list = list.filter { it.statusEnum == criteria.status }
        }

        when (criteria.sort) {
            LibrarySort.RECENT_UPDATE -> list.sortedByDescending { it.updatedAt }
            LibrarySort.TITLE_ASC -> list.sortedBy { it.name.lowercase() }
            LibrarySort.TITLE_DESC -> list.sortedByDescending { it.name.lowercase() }
            LibrarySort.RATING_DESC -> list.sortedByDescending { it.personalRating }
            LibrarySort.PROGRESS_DESC -> list.sortedByDescending { it.progressPercentage }
            LibrarySort.YEAR_DESC -> list.sortedByDescending { it.releaseYear }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Title Form (Add / Edit / Review from Search)
    private val _formState = MutableStateFlow(TitleFormState())
    val formState = _formState.asStateFlow()

    fun startNewTitleForm(prefillCategory: Category = Category.FILME) {
        val isMovie = prefillCategory == Category.FILME
        _formState.value = TitleFormState(
            category = prefillCategory,
            totalSeasons = if (isMovie) 0 else 1,
            episodesPerSeason = if (isMovie) 0 else 1,
            currentSeason = if (isMovie) 0 else 1,
            currentEpisode = 0,
            totalEpisodes = if (isMovie) 0 else 1,
            duration = "",
            isEditing = false
        )
    }

    fun startEditTitleForm(title: TitleEntity) {
        _formState.value = TitleFormState(
            id = title.id,
            name = title.name,
            coverUrl = title.coverUrl,
            synopsis = title.synopsis,
            category = title.categoryEnum,
            releaseYear = title.releaseYear,
            genre = title.genre,
            totalSeasons = title.totalSeasons,
            episodesPerSeason = title.episodesPerSeason,
            currentSeason = title.currentSeason,
            currentEpisode = title.currentEpisode,
            totalEpisodes = title.totalEpisodes,
            duration = title.duration,
            status = title.statusEnum,
            isIgnored = title.isIgnored,
            completedAt = title.completedAt,
            personalRating = title.personalRating,
            notes = title.notes,
            isEditing = true
        )
        _currentNav.value = NavDestination.ADD
    }

    fun prefillFromSearchResult(result: TitleSearchResult) {
        val isMovie = result.category == Category.FILME
        _formState.value = TitleFormState(
            name = result.title,
            coverUrl = result.coverUrl,
            synopsis = result.synopsis,
            category = result.category,
            releaseYear = result.releaseYear,
            genre = result.genre,
            totalSeasons = if (isMovie) 0 else result.totalSeasons.coerceAtLeast(1),
            episodesPerSeason = if (isMovie) 0 else result.episodesPerSeason.coerceAtLeast(1),
            currentSeason = if (isMovie) 0 else 1,
            currentEpisode = 0,
            totalEpisodes = if (isMovie) 0 else result.totalEpisodes.coerceAtLeast(1),
            duration = if (isMovie) result.duration else "",
            status = WatchStatus.PENDENTE,
            isIgnored = false,
            completedAt = null,
            personalRating = 0f,
            notes = if (result.seasonBreakdown.isNotBlank()) "Origem: ${result.source} • ${result.seasonBreakdown}" else "Origem: ${result.source}",
            isEditing = false
        )
    }

    fun updateFormField(updater: (TitleFormState) -> TitleFormState) {
        _formState.value = updater(_formState.value)
    }

    fun saveCurrentForm() {
        val form = _formState.value
        if (form.name.isBlank()) {
            showMessage("O título não pode estar vazio!")
            return
        }

        viewModelScope.launch {
            val isMovie = form.category == Category.FILME
            val isCompleted = form.status == WatchStatus.COMPLETO
            val calcCompletedAt = if (isCompleted) {
                form.completedAt ?: System.currentTimeMillis()
            } else {
                null
            }
            val finalSeason = if (isMovie) 0 else {
                if (isCompleted && form.currentSeason <= 1 && form.totalSeasons > 1) form.totalSeasons.coerceAtLeast(1)
                else form.currentSeason.coerceAtLeast(1)
            }
            val finalEpisode = if (isMovie) 0 else {
                if (isCompleted && form.currentEpisode == 0) {
                    if (form.totalSeasons <= 1) form.totalEpisodes.coerceAtLeast(1) else form.episodesPerSeason.coerceAtLeast(1)
                } else form.currentEpisode.coerceAtLeast(0)
            }
            val entity = TitleEntity(
                id = form.id ?: 0L,
                name = form.name.trim(),
                coverUrl = form.coverUrl.trim(),
                synopsis = form.synopsis.trim(),
                category = form.category.name,
                releaseYear = form.releaseYear,
                genre = form.genre.trim(),
                totalSeasons = if (isMovie) 0 else form.totalSeasons.coerceAtLeast(1),
                episodesPerSeason = if (isMovie) 0 else form.episodesPerSeason.coerceAtLeast(1),
                currentSeason = finalSeason,
                currentEpisode = finalEpisode,
                totalEpisodes = if (isMovie) 0 else form.totalEpisodes.coerceAtLeast(1),
                duration = if (isMovie) form.duration.trim() else "",
                isIgnored = form.isIgnored,
                completedAt = calcCompletedAt,
                status = form.status.name,
                personalRating = form.personalRating.coerceIn(0f, 10f),
                notes = form.notes.trim(),
                updatedAt = System.currentTimeMillis()
            )

            if (form.isEditing && form.id != null) {
                repository.update(entity)
                showMessage("Título '${form.name}' atualizado com sucesso!")
                // Also update selected title if opened
                if (_selectedTitle.value?.id == form.id) {
                    _selectedTitle.value = entity
                }
            } else {
                repository.insert(entity)
                showMessage("Título '${form.name}' adicionado à biblioteca!")
            }

            _currentNav.value = NavDestination.LIBRARY
            _formState.value = TitleFormState()
        }
    }

    // Quick Title Actions
    fun advanceEpisode(title: TitleEntity, delta: Int) {
        viewModelScope.launch {
            repository.advanceEpisode(title.id, delta)
        }
    }

    fun toggleTitleCompleted(title: TitleEntity) {
        viewModelScope.launch {
            val isNowCompleted = title.statusEnum != WatchStatus.COMPLETO
            val newStatus = if (isNowCompleted) {
                WatchStatus.COMPLETO.name
            } else {
                if (title.isMovie) {
                    WatchStatus.PENDENTE.name
                } else {
                    if (title.currentEpisode > 0 || title.currentSeason > 1) WatchStatus.EM_ANDAMENTO.name else WatchStatus.PENDENTE.name
                }
            }
            val newCompletedAt = if (isNowCompleted) (title.completedAt ?: System.currentTimeMillis()) else null
            val newSeason = if (isNowCompleted && !title.isMovie) title.totalSeasons.coerceAtLeast(1) else title.currentSeason
            val newEpisode = if (isNowCompleted && !title.isMovie) {
                if (title.totalSeasons <= 1) title.totalEpisodes.coerceAtLeast(1) else title.effectiveEpisodesPerSeason
            } else {
                title.currentEpisode
            }

            val updated = title.copy(
                status = newStatus,
                completedAt = newCompletedAt,
                currentSeason = newSeason,
                currentEpisode = newEpisode,
                lastWatchedAt = if (isNowCompleted) System.currentTimeMillis() else title.lastWatchedAt,
                updatedAt = System.currentTimeMillis()
            )
            repository.update(updated)
            if (_selectedTitle.value?.id == title.id) {
                _selectedTitle.value = updated
            }
            val msg = if (isNowCompleted) "Título '${title.name}' marcado como Concluído!" else "Status de '${title.name}' revertido com sucesso!"
            showMessage(msg)
        }
    }

    fun toggleBlockTitle(title: TitleEntity) {
        viewModelScope.launch {
            val isNowBlocked = repository.toggleBlocked(title.id)
            val updated = title.copy(
                status = if (isNowBlocked) WatchStatus.BLOQUEADO.name else WatchStatus.PENDENTE.name,
                updatedAt = System.currentTimeMillis()
            )
            if (_selectedTitle.value?.id == title.id) {
                _selectedTitle.value = updated
            }
            if (isNowBlocked) {
                showMessage("Título '${title.name}' bloqueado! (Atualizações e notificações suspensas)")
            } else {
                showMessage("Título '${title.name}' desbloqueado com sucesso.")
            }
        }
    }

    fun toggleMovieWatched(title: TitleEntity) {
        toggleTitleCompleted(title)
    }

    fun updateStatus(title: TitleEntity, newStatus: WatchStatus) {
        viewModelScope.launch {
            val isNowCompleted = newStatus == WatchStatus.COMPLETO
            val newCompletedAt = if (isNowCompleted) {
                title.completedAt ?: System.currentTimeMillis()
            } else {
                null
            }
            val newSeason = if (isNowCompleted && !title.isMovie) title.totalSeasons.coerceAtLeast(1) else title.currentSeason
            val newEpisode = if (isNowCompleted && !title.isMovie) {
                if (title.totalSeasons <= 1) title.totalEpisodes.coerceAtLeast(1) else title.effectiveEpisodesPerSeason
            } else {
                title.currentEpisode
            }

            val updated = title.copy(
                status = newStatus.name,
                completedAt = newCompletedAt,
                currentSeason = newSeason,
                currentEpisode = newEpisode,
                lastWatchedAt = if (isNowCompleted) System.currentTimeMillis() else title.lastWatchedAt,
                updatedAt = System.currentTimeMillis()
            )
            repository.update(updated)
            if (_selectedTitle.value?.id == title.id) {
                _selectedTitle.value = updated
            }
            showMessage("Status alterado para ${newStatus.label}")
        }
    }

    fun updateRating(title: TitleEntity, rating: Float) {
        viewModelScope.launch {
            val updated = title.copy(personalRating = rating, updatedAt = System.currentTimeMillis())
            repository.update(updated)
            if (_selectedTitle.value?.id == title.id) {
                _selectedTitle.value = updated
            }
        }
    }

    fun deleteTitle(title: TitleEntity) {
        viewModelScope.launch {
            repository.delete(title)
            if (_selectedTitle.value?.id == title.id) {
                _selectedTitle.value = null
            }
            showMessage("Título '${title.name}' excluído.")
        }
    }

    // Online Search State
    private val _onlineSearchQuery = MutableStateFlow("")
    val onlineSearchQuery = _onlineSearchQuery.asStateFlow()

    private val _onlineSearchCategory = MutableStateFlow<Category?>(null)
    val onlineSearchCategory = _onlineSearchCategory.asStateFlow()

    private val _isSearchingOnline = MutableStateFlow(false)
    val isSearchingOnline = _isSearchingOnline.asStateFlow()

    private val _onlineSearchResults = MutableStateFlow<List<TitleSearchResult>>(emptyList())
    val onlineSearchResults = _onlineSearchResults.asStateFlow()

    private val _onlineSearchError = MutableStateFlow<String?>(null)
    val onlineSearchError = _onlineSearchError.asStateFlow()

    fun setOnlineSearchQuery(q: String) {
        _onlineSearchQuery.value = q
    }

    fun setOnlineSearchCategory(c: Category?) {
        _onlineSearchCategory.value = c
    }

    fun performOnlineSearch() {
        val q = _onlineSearchQuery.value.trim()
        if (q.isEmpty()) return

        viewModelScope.launch {
            _isSearchingOnline.value = true
            _onlineSearchError.value = null
            try {
                val results = repository.searchOnline(q, _onlineSearchCategory.value)
                _onlineSearchResults.value = results
                if (results.isEmpty()) {
                    _onlineSearchError.value = "Nenhum resultado encontrado para \"$q\". Você pode cadastrar manualmente."
                }
            } catch (e: Exception) {
                _onlineSearchError.value = "Falha ao buscar títulos online. Verifique sua conexão ou cadastre manualmente."
            } finally {
                _isSearchingOnline.value = false
            }
        }
    }

    fun clearOnlineSearch() {
        _onlineSearchQuery.value = ""
        _onlineSearchResults.value = emptyList()
        _onlineSearchError.value = null
    }

    // ==========================================
    // ROLETA INTELIGENTE (SMART ROULETTE)
    // ==========================================
    private val _rouletteMode = MutableStateFlow(RouletteMode.GERAL)
    val rouletteMode = _rouletteMode.asStateFlow()

    private val _rouletteCategory = MutableStateFlow(Category.FILME)
    val rouletteCategory = _rouletteCategory.asStateFlow()

    // Roulette Toggles
    private val _rouletteExcludeCompleted = MutableStateFlow(true)
    val rouletteExcludeCompleted = _rouletteExcludeCompleted.asStateFlow()

    private val _rouletteOnlyPending = MutableStateFlow(false)
    val rouletteOnlyPending = _rouletteOnlyPending.asStateFlow()

    private val _rouletteOnlyInProgress = MutableStateFlow(false)
    val rouletteOnlyInProgress = _rouletteOnlyInProgress.asStateFlow()

    private val _rouletteAvoidLastResult = MutableStateFlow(true)
    val rouletteAvoidLastResult = _rouletteAvoidLastResult.asStateFlow()

    private val _rouletteCustomStatus = MutableStateFlow(setOf(WatchStatus.PENDENTE, WatchStatus.EM_ANDAMENTO))
    val rouletteCustomStatus = _rouletteCustomStatus.asStateFlow()

    private val _lastWinnerId = MutableStateFlow<Long?>(null)
    val lastWinnerId = _lastWinnerId.asStateFlow()

    private val _rouletteWinner = MutableStateFlow<TitleEntity?>(null)
    val rouletteWinner = _rouletteWinner.asStateFlow()

    val activeWinnerId = _activeWinnerId.asStateFlow()

    val activeRouletteTitle: StateFlow<TitleEntity?> = combine(_activeWinnerId, allTitles) { id, titles ->
        if (id != null && id > 0) titles.find { it.id == id } else null
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _showRouletteResult = MutableStateFlow(false)
    val showRouletteResult = _showRouletteResult.asStateFlow()

    fun setShowRouletteResult(show: Boolean) {
        _showRouletteResult.value = show
    }

    private val _isSpinning = MutableStateFlow(false)
    val isSpinning = _isSpinning.asStateFlow()

    fun setRouletteMode(mode: RouletteMode) {
        _rouletteMode.value = mode
    }

    fun setRouletteCategory(cat: Category) {
        _rouletteCategory.value = cat
    }

    fun toggleRouletteExcludeCompleted() {
        _rouletteExcludeCompleted.value = !_rouletteExcludeCompleted.value
    }

    fun toggleRouletteOnlyPending() {
        val next = !_rouletteOnlyPending.value
        _rouletteOnlyPending.value = next
        if (next) _rouletteOnlyInProgress.value = false
    }

    fun toggleRouletteOnlyInProgress() {
        val next = !_rouletteOnlyInProgress.value
        _rouletteOnlyInProgress.value = next
        if (next) _rouletteOnlyPending.value = false
    }

    fun toggleRouletteAvoidLastResult() {
        _rouletteAvoidLastResult.value = !_rouletteAvoidLastResult.value
    }

    fun toggleCustomStatusFilter(status: WatchStatus) {
        val current = _rouletteCustomStatus.value.toMutableSet()
        if (current.contains(status)) {
            if (current.size > 1) current.remove(status)
        } else {
            current.add(status)
        }
        _rouletteCustomStatus.value = current
    }

    fun toggleIgnoreTitle(title: TitleEntity) {
        viewModelScope.launch {
            val newIgnored = repository.toggleIgnored(title.id)
            if (_selectedTitle.value?.id == title.id) {
                _selectedTitle.value = _selectedTitle.value?.copy(isIgnored = newIgnored)
            }
            if (newIgnored) {
                showMessage("'${title.name}' ignorado nos sorteios da roleta.")
            } else {
                showMessage("'${title.name}' desbloqueado para os sorteios da roleta.")
            }
        }
    }

    fun updateTitleDirectly(updated: TitleEntity) {
        viewModelScope.launch {
            val calcCompletedAt = if (updated.statusEnum == WatchStatus.COMPLETO) {
                updated.completedAt ?: System.currentTimeMillis()
            } else {
                null
            }
            val sanitized = updated.copy(
                completedAt = calcCompletedAt,
                updatedAt = System.currentTimeMillis()
            )
            repository.update(sanitized)
            if (_selectedTitle.value?.id == sanitized.id) {
                _selectedTitle.value = sanitized
            }
            showMessage("Alterações em '${sanitized.name}' salvas com sucesso!")
        }
    }

    fun closeWinnerDialog() {
        _rouletteWinner.value = null
    }

    fun clearRouletteResult() {
        _activeWinnerId.value = null
        _rouletteWinner.value = null
        _showRouletteResult.value = false
        _isManualSelection.value = false
        roulettePrefs.edit()
            .remove("active_roulette_winner_id")
            .putBoolean("active_roulette_is_manual", false)
            .apply()
    }

    private fun persistCycleIds(cycle: Set<Long>) {
        _rouletteDrawnCycleIds.value = cycle
        roulettePrefs.edit().putString("roulette_cycle_ids", cycle.joinToString(",")).apply()
    }

    fun resetRouletteCycle() {
        persistCycleIds(emptySet())
        showMessage("Ciclo da roleta reiniciado! Todos os títulos estão disponíveis novamente.")
    }

    fun selectManualTitle(title: TitleEntity, autoShowFullResult: Boolean = false) {
        val currentCycle = _rouletteDrawnCycleIds.value.toMutableSet()
        currentCycle.add(title.id)
        persistCycleIds(currentCycle)

        _lastWinnerId.value = title.id
        _rouletteWinner.value = title
        _activeWinnerId.value = title.id
        _isManualSelection.value = true

        roulettePrefs.edit()
            .putLong("active_roulette_winner_id", title.id)
            .putBoolean("active_roulette_is_manual", true)
            .apply()

        if (autoShowFullResult) {
            _showRouletteResult.value = true
        }
        showMessage("Título '${title.name}' selecionado manualmente para a roleta!")
    }

    // Calculates eligible titles for the roulette based on current mode and filters
    fun getEligibleTitles(titlesSource: List<TitleEntity>? = null): List<TitleEntity> {
        val all = titlesSource ?: allTitles.value
        if (all.isEmpty()) return emptyList()

        // Regra fundamental: Títulos ignorados NUNCA podem ser sorteados
        val notIgnored = all.filter { !it.isIgnored }
        if (notIgnored.isEmpty()) return emptyList()

        var eligible = when (_rouletteMode.value) {
            RouletteMode.GERAL -> notIgnored
            RouletteMode.CATEGORIA -> notIgnored.filter { it.categoryEnum == _rouletteCategory.value }
            RouletteMode.PERSONALIZADA -> {
                notIgnored.filter { _rouletteCustomStatus.value.contains(it.statusEnum) }
            }
        }

        if (_rouletteExcludeCompleted.value && _rouletteMode.value != RouletteMode.PERSONALIZADA) {
            eligible = eligible.filter { it.statusEnum != WatchStatus.COMPLETO }
        }

        if (_rouletteOnlyPending.value && _rouletteMode.value != RouletteMode.PERSONALIZADA) {
            eligible = eligible.filter { it.statusEnum == WatchStatus.PENDENTE }
        }

        if (_rouletteOnlyInProgress.value && _rouletteMode.value != RouletteMode.PERSONALIZADA) {
            eligible = eligible.filter { it.statusEnum == WatchStatus.EM_ANDAMENTO }
        }

        return eligible
    }

    fun drawRouletteTitle(autoShowFullResult: Boolean = false, candidatesSource: List<TitleEntity>? = null): TitleEntity? {
        val eligible = getEligibleTitles(candidatesSource)
        if (eligible.isEmpty()) {
            showMessage("Nenhum título elegível encontrado com os filtros atuais!")
            return null
        }

        val eligibleIds = eligible.map { it.id }.toSet()
        var currentCycle = _rouletteDrawnCycleIds.value.intersect(eligibleIds).toMutableSet()

        // Títulos elegíveis ainda não sorteados nem selecionados neste ciclo
        var notYetDrawnInCycle = eligible.filter { !currentCycle.contains(it.id) }

        // Se todos os títulos elegíveis já foram sorteados/selecionados, reinicia o ciclo
        if (notYetDrawnInCycle.isEmpty()) {
            currentCycle.clear()
            persistCycleIds(emptySet())
            notYetDrawnInCycle = eligible
        }

        var candidatePool = notYetDrawnInCycle
        if (_rouletteAvoidLastResult.value && candidatePool.size > 1 && _lastWinnerId.value != null) {
            val filtered = candidatePool.filter { it.id != _lastWinnerId.value }
            if (filtered.isNotEmpty()) {
                candidatePool = filtered
            }
        }

        val randomIndex = Random.nextInt(candidatePool.size)
        val winner = candidatePool[randomIndex]

        currentCycle.add(winner.id)
        persistCycleIds(currentCycle)

        _lastWinnerId.value = winner.id
        _rouletteWinner.value = winner
        _activeWinnerId.value = winner.id
        _isManualSelection.value = false

        roulettePrefs.edit()
            .putLong("active_roulette_winner_id", winner.id)
            .putBoolean("active_roulette_is_manual", false)
            .apply()

        _isSpinning.value = false
        if (autoShowFullResult) {
            _showRouletteResult.value = true
        }
        showMessage("Sorteado: ${winner.name}")
        return winner
    }

    fun spinRoulette(onResultSelected: ((TitleEntity, Int) -> Unit)? = null) {
        val winner = drawRouletteTitle(autoShowFullResult = false)
        if (winner != null && onResultSelected != null) {
            val eligible = getEligibleTitles()
            val winnerEligibleIndex = eligible.indexOfFirst { it.id == winner.id }.coerceAtLeast(0)
            onResultSelected(winner, winnerEligibleIndex)
        }
    }

    fun finishSpin(winner: TitleEntity) {
        _isSpinning.value = false
        _rouletteWinner.value = winner
        _activeWinnerId.value = winner.id
        roulettePrefs.edit().putLong("active_roulette_winner_id", winner.id).apply()
        _showRouletteResult.value = true
    }

    // Welcome Screen Flow
    fun dismissWelcome() {
        appPrefs.edit().putBoolean("has_seen_welcome_v2", true).apply()
        _hasSeenWelcome.value = true
    }

    fun showWelcomeScreen() {
        _hasSeenWelcome.value = false
    }

    // Backup & Settings (.ZIP)
    fun exportBackupDirectlyToDownloads(context: Context, onComplete: ((Boolean, String?) -> Unit)? = null): kotlinx.coroutines.Job =
        viewModelScope.launch {
            try {
                val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                val timestamp = sdf.format(Date())
                val fileName = "vortex_backup_$timestamp.zip"

                val listToExport = if (allTitles.value.isNotEmpty()) allTitles.value else repository.allTitles.first()
                val success = withContext(Dispatchers.IO) {
                    var saved = false
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        try {
                            val values = ContentValues().apply {
                                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                                put(MediaStore.MediaColumns.MIME_TYPE, "application/zip")
                                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                            }
                            val resolver = context.contentResolver
                            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                            if (uri != null) {
                                resolver.openOutputStream(uri)?.use { os ->
                                    repository.exportToZip(listToExport, os)
                                }
                                saved = true
                            }
                        } catch (ignored: Exception) {
                            saved = false
                        }
                    }

                    if (!saved) {
                        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                        if (!downloadsDir.exists()) {
                            downloadsDir.mkdirs()
                        }
                        val file = File(downloadsDir, fileName)
                        FileOutputStream(file).use { os ->
                            repository.exportToZip(listToExport, os)
                        }
                        try {
                            MediaScannerConnection.scanFile(context, arrayOf(file.absolutePath), arrayOf("application/zip"), null)
                        } catch (ignored: Throwable) {}
                        saved = true
                    }
                    saved
                }

                if (success) {
                    showMessage("Backup salvo em Downloads: $fileName")
                    onComplete?.invoke(true, fileName)
                } else {
                    showMessage("Falha ao salvar backup .zip.")
                    onComplete?.invoke(false, null)
                }
            } catch (e: Exception) {
                showMessage("Erro ao exportar backup: ${e.message ?: "falha desconhecida"}")
                onComplete?.invoke(false, null)
            }
        }

    fun exportBackupZip(outputStream: OutputStream) {
        try {
            repository.exportToZip(allTitles.value, outputStream)
            showMessage("Backup .zip exportado com sucesso!")
        } catch (e: Exception) {
            showMessage("Erro ao exportar backup .zip.")
        }
    }

    fun importBackupFromUri(context: Context, uri: Uri, onComplete: ((Boolean, Int) -> Unit)? = null) {
        viewModelScope.launch {
            try {
                val json = withContext(Dispatchers.IO) {
                    val stream = context.contentResolver.openInputStream(uri)
                        ?: throw java.io.IOException("Não foi possível abrir o arquivo selecionado.")
                    stream.use { s ->
                        repository.readBackupStream(s)
                    }
                }
                val count = repository.importFromJson(json)
                onComplete?.invoke(true, count)
                showMessage("$count títulos restaurados com sucesso do backup .zip!")
            } catch (e: Exception) {
                onComplete?.invoke(false, 0)
                showMessage("Erro ao ler backup: ${e.message ?: "arquivo inválido"}")
            }
        }
    }

    fun importBackupStream(inputStream: InputStream, onComplete: (Boolean, Int) -> Unit) {
        viewModelScope.launch {
            try {
                val json = withContext(Dispatchers.IO) {
                    repository.readBackupStream(inputStream)
                }
                val count = repository.importFromJson(json)
                onComplete(true, count)
                showMessage("$count títulos restaurados com sucesso do backup!")
            } catch (e: Exception) {
                onComplete(false, 0)
                showMessage("Erro ao ler backup: ${e.message ?: "arquivo inválido"}")
            }
        }
    }

    fun seedSampleData() {
        viewModelScope.launch {
            repository.seedSampleData()
            showMessage("Títulos de exemplo adicionados com sucesso!")
        }
    }

    fun clearAllTitles() {
        viewModelScope.launch {
            repository.deleteAll()
            showMessage("Todos os dados da biblioteca foram limpos.")
        }
    }

    // ==========================================
    // SYNC TITLES & NOTIFICATIONS
    // ==========================================
    private val _isSyncingTitles = MutableStateFlow(false)
    val isSyncingTitles = _isSyncingTitles.asStateFlow()

    private val _syncProgressText = MutableStateFlow("")
    val syncProgressText = _syncProgressText.asStateFlow()

    private val _syncResults = MutableStateFlow<List<UpdateResult>?>(null)
    val syncResults = _syncResults.asStateFlow()

    private val _notificationsEnabled = MutableStateFlow(true)
    val notificationsEnabled = _notificationsEnabled.asStateFlow()

    fun setNotificationsEnabled(enabled: Boolean) {
        _notificationsEnabled.value = enabled
    }

    fun toggleNotificationsEnabled() {
        _notificationsEnabled.value = !_notificationsEnabled.value
    }

    fun dismissSyncResults() {
        _syncResults.value = null
    }

    fun syncAllTitles() {
        if (_isSyncingTitles.value) return
        viewModelScope.launch {
            _isSyncingTitles.value = true
            _syncProgressText.value = "Conectando às fontes oficiais..."
            try {
                val results = repository.checkForUpdates { current, total, titleName ->
                    _syncProgressText.value = "Verificando $current/$total: $titleName..."
                }
                _syncResults.value = results

                if (results.isNotEmpty()) {
                    if (_notificationsEnabled.value) {
                        results.forEach { item ->
                            val msg = "Novos episódios disponíveis! Total atualizado: ${item.newEpisodes} episódios (${item.newSeasons} temporadas)."
                            NotificationHelper.sendTitleUpdateNotification(
                                context = getApplication(),
                                titleName = item.titleName,
                                message = msg,
                                notificationId = item.titleId.toInt()
                            )
                        }
                    }
                    showMessage("${results.size} título(s) receberam atualizações de episódios/temporadas!")
                } else {
                    showMessage("Todos os títulos já estão na versão mais recente!")
                }
            } catch (e: Exception) {
                showMessage("Erro ao buscar atualizações online. Verifique sua conexão.")
            } finally {
                _isSyncingTitles.value = false
                _syncProgressText.value = ""
            }
        }
    }

    fun syncSingleTitle(title: TitleEntity) {
        viewModelScope.launch {
            _isSyncingTitles.value = true
            _syncProgressText.value = "Verificando '${title.name}'..."
            try {
                val result = repository.checkSingleTitleUpdate(title.id)
                if (result != null) {
                    _syncResults.value = listOf(result)
                    if (_notificationsEnabled.value) {
                        NotificationHelper.sendTitleUpdateNotification(
                            context = getApplication(),
                            titleName = result.titleName,
                            message = "Novos episódios encontrados! Agora são ${result.newEpisodes} episódios.",
                            notificationId = result.titleId.toInt()
                        )
                    }
                    showMessage("Novos episódios encontrados para '${title.name}'!")
                } else {
                    showMessage("'${title.name}' já está na versão mais recente.")
                }
            } catch (e: Exception) {
                showMessage("Erro ao verificar atualização para '${title.name}'.")
            } finally {
                _isSyncingTitles.value = false
            }
        }
    }

    fun simulateUpdateForTesting() {
        viewModelScope.launch {
            val list = allTitles.value.filter { !it.isMovie && it.statusEnum != WatchStatus.BLOQUEADO }
            if (list.isEmpty()) {
                showMessage("Adicione ao menos uma série ou anime ativa para testar a notificação!")
                return@launch
            }
            val target = list.first()
            val result = repository.simulateTitleUpdate(target.id, addEpisodes = 1)
            if (result != null) {
                _syncResults.value = listOf(result)
                if (_notificationsEnabled.value) {
                    NotificationHelper.sendTitleUpdateNotification(
                        context = getApplication(),
                        titleName = result.titleName,
                        message = "NOVO EPISÓDIO DISPONÍVEL! Total: ${result.newEpisodes} episódios.",
                        notificationId = result.titleId.toInt()
                    )
                }
                showMessage("Atualização simulada e notificação enviada para '${result.titleName}'!")
            }
        }
    }

    fun sendTestNotification() {
        NotificationHelper.sendTestNotification(getApplication())
        showMessage("Notificação de teste disparada!")
    }
}
