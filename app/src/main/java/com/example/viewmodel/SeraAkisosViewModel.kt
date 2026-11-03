package com.example.viewmodel

import android.app.Application
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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
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
    val status: WatchStatus = WatchStatus.PENDENTE,
    val personalRating: Float = 0f,
    val notes: String = "",
    val isEditing: Boolean = false
)

class SeraAkisosViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TitleRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = TitleRepository(database.titleDao())
        viewModelScope.launch {
            repository.seedSampleDataIfEmpty()
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

    // Filtered Titles stream
    val filteredTitles: StateFlow<List<TitleEntity>> = combine(
        allTitles,
        _librarySearchQuery,
        _selectedCategoryFilter,
        _selectedStatusFilter,
        _selectedSort
    ) { titles, query, category, status, sort ->
        var list = titles

        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            list = list.filter {
                it.name.lowercase().contains(q) ||
                it.genre.lowercase().contains(q) ||
                it.synopsis.lowercase().contains(q) ||
                it.releaseYear.toString().contains(q)
            }
        }

        if (category != null) {
            list = list.filter { it.categoryEnum == category }
        }

        if (status != null) {
            list = list.filter { it.statusEnum == status }
        }

        when (sort) {
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
        _formState.value = TitleFormState(
            category = prefillCategory,
            currentEpisode = if (prefillCategory == Category.FILME) 0 else 1,
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
            status = title.statusEnum,
            personalRating = title.personalRating,
            notes = title.notes,
            isEditing = true
        )
        _currentNav.value = NavDestination.ADD
    }

    fun prefillFromSearchResult(result: TitleSearchResult) {
        _formState.value = TitleFormState(
            name = result.title,
            coverUrl = result.coverUrl,
            synopsis = result.synopsis,
            category = result.category,
            releaseYear = result.releaseYear,
            genre = result.genre,
            totalSeasons = result.totalSeasons.coerceAtLeast(1),
            episodesPerSeason = result.episodesPerSeason.coerceAtLeast(1),
            currentSeason = 1,
            currentEpisode = if (result.category == Category.FILME) 0 else 1,
            totalEpisodes = result.totalEpisodes.coerceAtLeast(1),
            status = WatchStatus.PENDENTE,
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
            val entity = TitleEntity(
                id = form.id ?: 0L,
                name = form.name.trim(),
                coverUrl = form.coverUrl.trim(),
                synopsis = form.synopsis.trim(),
                category = form.category.name,
                releaseYear = form.releaseYear,
                genre = form.genre.trim(),
                totalSeasons = form.totalSeasons.coerceAtLeast(1),
                episodesPerSeason = form.episodesPerSeason.coerceAtLeast(1),
                currentSeason = form.currentSeason.coerceAtLeast(1),
                currentEpisode = form.currentEpisode.coerceAtLeast(0),
                totalEpisodes = form.totalEpisodes.coerceAtLeast(1),
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
            // Update in-memory selected title if visible
            val newEp = (title.currentEpisode + delta).coerceIn(0, title.totalEpisodes.coerceAtLeast(1))
            val newStatus = if (title.totalEpisodes > 0 && newEp >= title.totalEpisodes) {
                WatchStatus.COMPLETO.name
            } else if (newEp > 0 && title.status == WatchStatus.PENDENTE.name) {
                WatchStatus.EM_ANDAMENTO.name
            } else {
                title.status
            }
            if (_selectedTitle.value?.id == title.id) {
                _selectedTitle.value = title.copy(currentEpisode = newEp, status = newStatus)
            }
        }
    }

    fun toggleMovieWatched(title: TitleEntity) {
        viewModelScope.launch {
            repository.toggleMovieWatched(title.id)
            if (_selectedTitle.value?.id == title.id) {
                val next = if (title.statusEnum == WatchStatus.COMPLETO) WatchStatus.PENDENTE else WatchStatus.COMPLETO
                _selectedTitle.value = title.copy(status = next.name)
            }
        }
    }

    fun updateStatus(title: TitleEntity, newStatus: WatchStatus) {
        viewModelScope.launch {
            val updated = title.copy(status = newStatus.name, updatedAt = System.currentTimeMillis())
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

    fun closeWinnerDialog() {
        _rouletteWinner.value = null
    }

    // Calculates eligible titles for the roulette based on current mode and filters
    fun getEligibleTitles(): List<TitleEntity> {
        val all = allTitles.value
        if (all.isEmpty()) return emptyList()

        var eligible = when (_rouletteMode.value) {
            RouletteMode.GERAL -> all
            RouletteMode.CATEGORIA -> all.filter { it.categoryEnum == _rouletteCategory.value }
            RouletteMode.PERSONALIZADA -> {
                all.filter { _rouletteCustomStatus.value.contains(it.statusEnum) }
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

    fun spinRoulette(onResultSelected: (TitleEntity, Int) -> Unit) {
        val eligible = getEligibleTitles()
        if (eligible.isEmpty()) {
            showMessage("Nenhum título elegível encontrado com os filtros atuais!")
            return
        }

        var candidatePool = eligible
        if (_rouletteAvoidLastResult.value && eligible.size > 1 && _lastWinnerId.value != null) {
            val filtered = eligible.filter { it.id != _lastWinnerId.value }
            if (filtered.isNotEmpty()) {
                candidatePool = filtered
            }
        }

        val randomIndex = Random.nextInt(candidatePool.size)
        val winner = candidatePool[randomIndex]
        _lastWinnerId.value = winner.id

        // Find winner index in eligible list
        val winnerEligibleIndex = eligible.indexOfFirst { it.id == winner.id }.coerceAtLeast(0)

        _isSpinning.value = true
        onResultSelected(winner, winnerEligibleIndex)
    }

    fun finishSpin(winner: TitleEntity) {
        _isSpinning.value = false
        _rouletteWinner.value = winner
    }

    // Backup & Settings
    fun exportBackupJson(): String {
        return repository.exportToJson(allTitles.value)
    }

    fun importBackupJson(json: String, onComplete: (Boolean, Int) -> Unit) {
        viewModelScope.launch {
            try {
                val count = repository.importFromJson(json)
                onComplete(true, count)
                showMessage("$count títulos importados com sucesso!")
            } catch (e: Exception) {
                onComplete(false, 0)
                showMessage("Erro ao importar dados: formato inválido.")
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
            val list = allTitles.value.filter { !it.isMovie }
            if (list.isEmpty()) {
                showMessage("Adicione ao menos uma série ou anime para testar a notificação!")
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
