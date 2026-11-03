package com.example.data.repository

import com.example.data.local.TitleDao
import com.example.data.model.Category
import com.example.data.model.TitleEntity
import com.example.data.model.WatchStatus
import com.example.data.remote.TitleSearchResult
import com.example.data.remote.TitleSearchService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class TitleRepository(
    private val titleDao: TitleDao,
    private val searchService: TitleSearchService = TitleSearchService()
) {
    val allTitles: Flow<List<TitleEntity>> = titleDao.getAllTitles()
    val inProgressTitles: Flow<List<TitleEntity>> = titleDao.getInProgressTitles()
    val recentTitles: Flow<List<TitleEntity>> = titleDao.getRecentlyAddedTitles()

    suspend fun insert(title: TitleEntity): Long = withContext(Dispatchers.IO) {
        val sanitized = applyStatusAutomation(title)
        titleDao.insert(sanitized)
    }

    suspend fun update(title: TitleEntity) = withContext(Dispatchers.IO) {
        val sanitized = applyStatusAutomation(title)
        titleDao.update(sanitized.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun delete(title: TitleEntity) = withContext(Dispatchers.IO) {
        titleDao.delete(title)
    }

    suspend fun deleteById(id: Long) = withContext(Dispatchers.IO) {
        titleDao.deleteById(id)
    }

    suspend fun deleteAll() = withContext(Dispatchers.IO) {
        titleDao.deleteAll()
    }

    suspend fun advanceEpisode(id: Long, delta: Int) = withContext(Dispatchers.IO) {
        val current = titleDao.getTitleById(id).first() ?: return@withContext
        if (current.isMovie) return@withContext

        val newEp = (current.currentEpisode + delta).coerceIn(0, current.totalEpisodes.coerceAtLeast(1))
        val updated = current.copy(
            currentEpisode = newEp,
            lastWatchedAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        update(updated)
    }

    suspend fun toggleMovieWatched(id: Long) = withContext(Dispatchers.IO) {
        val current = titleDao.getTitleById(id).first() ?: return@withContext
        if (!current.isMovie) return@withContext

        val newStatus = if (current.statusEnum == WatchStatus.COMPLETO) {
            WatchStatus.PENDENTE.name
        } else {
            WatchStatus.COMPLETO.name
        }
        val updated = current.copy(
            status = newStatus,
            lastWatchedAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        titleDao.update(updated)
    }

    suspend fun searchOnline(query: String, category: Category?): List<TitleSearchResult> {
        return searchService.searchTitles(query, category)
    }

    suspend fun seedSampleDataIfEmpty() = withContext(Dispatchers.IO) {
        val currentList = titleDao.getAllTitles().first()
        if (currentList.isEmpty()) {
            seedSampleData()
        }
    }

    suspend fun seedSampleData() = withContext(Dispatchers.IO) {
        val sampleTitles = listOf(
            TitleEntity(
                name = "One Piece",
                coverUrl = "https://cdn.myanimelist.net/images/anime/6/73245l.jpg",
                synopsis = "Monkey D. Luffy se recusa a deixar alguém ou algo ficar em seu caminho para se tornar o Rei dos Piratas. Ele navega pelas perigosas águas da Grand Line em busca do maior tesouro de todos: o One Piece.",
                category = Category.ANIME.name,
                releaseYear = 1999,
                genre = "Aventura, Ação, Fantasia",
                totalSeasons = 1,
                episodesPerSeason = 1150,
                currentSeason = 1,
                currentEpisode = 14,
                totalEpisodes = 1150,
                status = WatchStatus.EM_ANDAMENTO.name,
                personalRating = 9.8f,
                notes = "Arco do Baratie em andamento. Obra-prima absoluta!",
                lastWatchedAt = System.currentTimeMillis() - 3600000
            ),
            TitleEntity(
                name = "Interestelar",
                coverUrl = "https://is1-ssl.mzstatic.com/image/thumb/Video115/v4/bf/20/0f/bf200f68-7c87-84e1-255e-26a9712a45a6/pr_source.lsr/600x600bb.jpg",
                synopsis = "As reservas naturais da Terra estão chegando ao fim e um grupo de astronautas é enviado em uma missão perigosa através de um buraco de minhoca para encontrar um novo lar para a humanidade.",
                category = Category.FILME.name,
                releaseYear = 2014,
                genre = "Ficção Científica, Drama, Aventura",
                totalSeasons = 1,
                episodesPerSeason = 1,
                currentSeason = 1,
                currentEpisode = 1,
                totalEpisodes = 1,
                status = WatchStatus.COMPLETO.name,
                personalRating = 10.0f,
                notes = "Trilha sonora fantástica de Hans Zimmer. Filme favorito da vida.",
                lastWatchedAt = System.currentTimeMillis() - 86400000
            ),
            TitleEntity(
                name = "Breaking Bad",
                coverUrl = "https://static.tvmaze.com/uploads/images/original_untouched/501/1253519.jpg",
                synopsis = "Um professor de química deprimido diagnosticado com câncer terminal de pulmão une forças com um ex-aluno para cozinhar e vender metanfetamina e garantir o futuro financeiro de sua família.",
                category = Category.SERIE.name,
                releaseYear = 2008,
                genre = "Crime, Drama, Suspense",
                totalSeasons = 5,
                episodesPerSeason = 13,
                currentSeason = 3,
                currentEpisode = 28,
                totalEpisodes = 62,
                status = WatchStatus.EM_ANDAMENTO.name,
                personalRating = 9.9f,
                notes = "Começando a temporada 3, episódio 'Fly'. Gus Fring é implacável.",
                lastWatchedAt = System.currentTimeMillis() - 7200000
            ),
            TitleEntity(
                name = "Arcane",
                coverUrl = "https://static.tvmaze.com/uploads/images/original_untouched/536/1341857.jpg",
                synopsis = "Em meio ao conflito entre as cidades-gêmeas de Piltover e Zaun, duas irmãs lutam em lados opostos de uma guerra entre tecnologias mágicas e convicções incompatíveis.",
                category = Category.SERIE.name,
                releaseYear = 2021,
                genre = "Animação, Ficção Científica, Ação",
                totalSeasons = 2,
                episodesPerSeason = 9,
                currentSeason = 1,
                currentEpisode = 9,
                totalEpisodes = 18,
                status = WatchStatus.PAUSADO.name,
                personalRating = 9.7f,
                notes = "Temporada 1 concluída, aguardando momento livre para devorar a T2.",
                lastWatchedAt = System.currentTimeMillis() - 172800000
            ),
            TitleEntity(
                name = "Attack on Titan",
                coverUrl = "https://cdn.myanimelist.net/images/anime/10/47347l.jpg",
                synopsis = "Após sua cidade natal ser destruída e sua mãe ser morta, o jovem Eren Jaeger jura limpar a terra de todos os Titãs gigantes que levaram a humanidade à beira da extinção.",
                category = Category.ANIME.name,
                releaseYear = 2013,
                genre = "Ação, Fantasia Sombria, Mistério",
                totalSeasons = 4,
                episodesPerSeason = 22,
                currentSeason = 4,
                currentEpisode = 87,
                totalEpisodes = 87,
                status = WatchStatus.COMPLETO.name,
                personalRating = 9.8f,
                notes = "História incrível com plot twists inacreditáveis até o final.",
                lastWatchedAt = System.currentTimeMillis() - 259200000
            ),
            TitleEntity(
                name = "Duna: Parte 2",
                coverUrl = "https://is1-ssl.mzstatic.com/image/thumb/Video211/v4/0c/3e/ee/0c3eee32-c6cb-1d6f-23a9-95a947fb7e60/pr_source.lsr/600x600bb.jpg",
                synopsis = "Paul Atreides se une a Chani e aos Fremen enquanto busca vingança contra os conspiradores que destruíram sua família. Diante de uma escolha entre o amor e o destino do universo.",
                category = Category.FILME.name,
                releaseYear = 2024,
                genre = "Ficção Científica, Aventura",
                totalSeasons = 1,
                episodesPerSeason = 1,
                currentSeason = 1,
                currentEpisode = 0,
                totalEpisodes = 1,
                status = WatchStatus.PENDENTE.name,
                personalRating = 0.0f,
                notes = "Adicionado à fila para assistir no fim de semana.",
                lastWatchedAt = null
            )
        )
        titleDao.insertAll(sampleTitles)
    }

    private fun applyStatusAutomation(title: TitleEntity): TitleEntity {
        if (title.isMovie) {
            return title
        }

        var newStatus = title.status
        // Auto complete if current episode reaches total
        if (title.totalEpisodes > 0 && title.currentEpisode >= title.totalEpisodes) {
            newStatus = WatchStatus.COMPLETO.name
        } else if (title.currentEpisode > 0 && title.status == WatchStatus.PENDENTE.name) {
            newStatus = WatchStatus.EM_ANDAMENTO.name
        } else if (title.currentEpisode < title.totalEpisodes && title.status == WatchStatus.COMPLETO.name) {
            newStatus = WatchStatus.EM_ANDAMENTO.name
        }

        return title.copy(status = newStatus)
    }

    fun exportToJson(titles: List<TitleEntity>): String {
        val array = JSONArray()
        for (item in titles) {
            val obj = JSONObject()
            obj.put("id", item.id)
            obj.put("name", item.name)
            obj.put("coverUrl", item.coverUrl)
            obj.put("synopsis", item.synopsis)
            obj.put("category", item.category)
            obj.put("releaseYear", item.releaseYear)
            obj.put("genre", item.genre)
            obj.put("totalSeasons", item.totalSeasons)
            obj.put("episodesPerSeason", item.episodesPerSeason)
            obj.put("currentSeason", item.currentSeason)
            obj.put("currentEpisode", item.currentEpisode)
            obj.put("totalEpisodes", item.totalEpisodes)
            obj.put("status", item.status)
            obj.put("personalRating", item.personalRating.toDouble())
            obj.put("notes", item.notes)
            obj.put("createdAt", item.createdAt)
            obj.put("updatedAt", item.updatedAt)
            array.put(obj)
        }
        val root = JSONObject()
        root.put("app", "Vortex")
        root.put("version", "2.0")
        root.put("exportedAt", System.currentTimeMillis())
        root.put("titles", array)
        return root.toString(2)
    }

    suspend fun importFromJson(jsonStr: String): Int = withContext(Dispatchers.IO) {
        val root = JSONObject(jsonStr)
        val array = root.optJSONArray("titles") ?: JSONArray(jsonStr)
        val imported = mutableListOf<TitleEntity>()

        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val entity = TitleEntity(
                name = obj.optString("name", "Sem Nome"),
                coverUrl = obj.optString("coverUrl", ""),
                synopsis = obj.optString("synopsis", ""),
                category = obj.optString("category", Category.FILME.name),
                releaseYear = obj.optInt("releaseYear", 2024),
                genre = obj.optString("genre", "Geral"),
                totalSeasons = obj.optInt("totalSeasons", 1),
                episodesPerSeason = obj.optInt("episodesPerSeason", 1),
                currentSeason = obj.optInt("currentSeason", 1),
                currentEpisode = obj.optInt("currentEpisode", 0),
                totalEpisodes = obj.optInt("totalEpisodes", 1),
                status = obj.optString("status", WatchStatus.PENDENTE.name),
                personalRating = obj.optDouble("personalRating", 0.0).toFloat(),
                notes = obj.optString("notes", ""),
                createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                updatedAt = System.currentTimeMillis()
            )
            imported.add(applyStatusAutomation(entity))
        }

        if (imported.isNotEmpty()) {
            titleDao.insertAll(imported)
        }
        imported.size
    }

    suspend fun checkForUpdates(
        onProgress: (current: Int, total: Int, titleName: String) -> Unit
    ): List<com.example.data.model.UpdateResult> = withContext(Dispatchers.IO) {
        val titles = titleDao.getAllTitles().first()
        val updateResults = mutableListOf<com.example.data.model.UpdateResult>()

        val eligibleForUpdate = titles.filter { !it.isMovie }
        val totalCount = eligibleForUpdate.size

        eligibleForUpdate.forEachIndexed { index, title ->
            onProgress(index + 1, totalCount, title.name)
            val info = searchService.checkTitleForUpdates(title)
            if (info.hasUpdates) {
                val updated = title.copy(
                    totalEpisodes = info.newTotalEpisodes,
                    totalSeasons = info.newTotalSeasons,
                    updatedAt = System.currentTimeMillis()
                )
                titleDao.update(applyStatusAutomation(updated))
                updateResults.add(
                    com.example.data.model.UpdateResult(
                        titleId = title.id,
                        titleName = title.name,
                        category = title.categoryEnum,
                        oldEpisodes = title.totalEpisodes,
                        newEpisodes = info.newTotalEpisodes,
                        oldSeasons = title.totalSeasons,
                        newSeasons = info.newTotalSeasons,
                        details = info.description
                    )
                )
            }
        }

        updateResults
    }

    suspend fun checkSingleTitleUpdate(titleId: Long): com.example.data.model.UpdateResult? = withContext(Dispatchers.IO) {
        val title = titleDao.getTitleById(titleId).first() ?: return@withContext null
        val info = searchService.checkTitleForUpdates(title)
        if (info.hasUpdates) {
            val updated = title.copy(
                totalEpisodes = info.newTotalEpisodes,
                totalSeasons = info.newTotalSeasons,
                updatedAt = System.currentTimeMillis()
            )
            titleDao.update(applyStatusAutomation(updated))
            return@withContext com.example.data.model.UpdateResult(
                titleId = title.id,
                titleName = title.name,
                category = title.categoryEnum,
                oldEpisodes = title.totalEpisodes,
                newEpisodes = info.newTotalEpisodes,
                oldSeasons = title.totalSeasons,
                newSeasons = info.newTotalSeasons,
                details = info.description
            )
        }
        null
    }

    suspend fun simulateTitleUpdate(titleId: Long, addEpisodes: Int = 2, addSeasons: Int = 0): com.example.data.model.UpdateResult? = withContext(Dispatchers.IO) {
        val title = titleDao.getTitleById(titleId).first() ?: return@withContext null
        val newEpisodes = title.totalEpisodes + addEpisodes
        val newSeasons = title.totalSeasons + addSeasons
        val updated = title.copy(
            totalEpisodes = newEpisodes,
            totalSeasons = newSeasons,
            updatedAt = System.currentTimeMillis()
        )
        titleDao.update(applyStatusAutomation(updated))
        com.example.data.model.UpdateResult(
            titleId = title.id,
            titleName = title.name,
            category = title.categoryEnum,
            oldEpisodes = title.totalEpisodes,
            newEpisodes = newEpisodes,
            oldSeasons = title.totalSeasons,
            newSeasons = newSeasons,
            details = "Simulação: Novos episódios/temporadas adicionados!"
        )
    }
}

