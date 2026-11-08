package com.example.data.repository

import com.example.data.local.TitleDao
import com.example.data.model.Category
import com.example.data.model.TitleEntity
import com.example.data.model.WatchStatus
import com.example.data.remote.TitleSearchResult
import com.example.data.remote.TitleSearchService
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
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

        val epsPerSeason = current.effectiveEpisodesPerSeason
        var newSeason = current.currentSeason
        var newEp = current.currentEpisode + delta

        if (delta > 0 && newEp > epsPerSeason && newSeason < current.totalSeasons) {
            newSeason += 1
            newEp = 1
        } else if (delta < 0 && newEp < 0 && newSeason > 1) {
            newSeason -= 1
            newEp = epsPerSeason
        } else {
            val maxAllowed = if (current.totalSeasons <= 1) current.totalEpisodes.coerceAtLeast(1) else epsPerSeason
            newEp = newEp.coerceIn(0, maxAllowed)
        }

        val totalWatched = ((newSeason - 1).coerceAtLeast(0) * epsPerSeason + newEp)
        val newStatus = if (current.totalEpisodes > 0 && totalWatched >= current.totalEpisodes) {
            WatchStatus.COMPLETO.name
        } else if (totalWatched > 0 && current.status == WatchStatus.PENDENTE.name) {
            WatchStatus.EM_ANDAMENTO.name
        } else {
            current.status
        }

        val newCompletedAt = if (newStatus == WatchStatus.COMPLETO.name) {
            current.completedAt ?: System.currentTimeMillis()
        } else {
            null
        }

        val updated = current.copy(
            currentSeason = newSeason,
            currentEpisode = newEp,
            status = newStatus,
            completedAt = newCompletedAt,
            lastWatchedAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        update(updated)
    }

    suspend fun toggleMovieWatched(id: Long) = withContext(Dispatchers.IO) {
        toggleTitleCompleted(id)
    }

    suspend fun toggleTitleCompleted(id: Long) = withContext(Dispatchers.IO) {
        val current = titleDao.getTitleById(id).first() ?: return@withContext
        val isNowCompleted = current.statusEnum != WatchStatus.COMPLETO
        val newStatus = if (isNowCompleted) WatchStatus.COMPLETO.name else WatchStatus.PENDENTE.name
        val newCompletedAt = if (isNowCompleted) (current.completedAt ?: System.currentTimeMillis()) else null

        val updated = if (current.isMovie) {
            current.copy(
                status = newStatus,
                completedAt = newCompletedAt,
                lastWatchedAt = if (isNowCompleted) System.currentTimeMillis() else current.lastWatchedAt,
                updatedAt = System.currentTimeMillis()
            )
        } else {
            val newSeason = if (isNowCompleted) current.totalSeasons.coerceAtLeast(1) else current.currentSeason
            val newEpisode = if (isNowCompleted) {
                if (current.totalSeasons <= 1) current.totalEpisodes.coerceAtLeast(1) else current.effectiveEpisodesPerSeason
            } else {
                0
            }
            current.copy(
                status = newStatus,
                completedAt = newCompletedAt,
                currentSeason = newSeason,
                currentEpisode = newEpisode,
                lastWatchedAt = if (isNowCompleted) System.currentTimeMillis() else current.lastWatchedAt,
                updatedAt = System.currentTimeMillis()
            )
        }
        titleDao.update(updated)
    }

    suspend fun toggleIgnored(id: Long): Boolean = withContext(Dispatchers.IO) {
        val current = titleDao.getTitleById(id).first() ?: return@withContext false
        val newIgnored = !current.isIgnored
        titleDao.updateIgnored(id, newIgnored)
        newIgnored
    }

    suspend fun setIgnored(id: Long, ignored: Boolean) = withContext(Dispatchers.IO) {
        titleDao.updateIgnored(id, ignored)
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
                totalSeasons = 0,
                episodesPerSeason = 0,
                currentSeason = 0,
                currentEpisode = 0,
                totalEpisodes = 0,
                duration = "2h 49m",
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
                totalSeasons = 0,
                episodesPerSeason = 0,
                currentSeason = 0,
                currentEpisode = 0,
                totalEpisodes = 0,
                duration = "2h 46m",
                status = WatchStatus.PENDENTE.name,
                personalRating = 0.0f,
                notes = "Adicionado à fila para assistir no fim de semana.",
                lastWatchedAt = null
            )
        )
        titleDao.insertAll(sampleTitles)
    }

    private fun applyStatusAutomation(title: TitleEntity): TitleEntity {
        // Preserva conclusão explícita e garante o registro da data/ano no banco
        if (title.statusEnum == WatchStatus.COMPLETO) {
            val completedAt = title.completedAt ?: System.currentTimeMillis()
            return title.copy(status = WatchStatus.COMPLETO.name, completedAt = completedAt)
        }

        // Preserva bloqueio explícito
        if (title.statusEnum == WatchStatus.BLOQUEADO) {
            return title.copy(completedAt = null)
        }

        if (title.isMovie) {
            return title.copy(completedAt = null)
        }

        var newStatus = title.status
        var newCompletedAt = title.completedAt
        val totalWatched = title.totalWatchedEpisodes
        // Auto complete if total watched episodes reaches total
        if (title.totalEpisodes > 0 && totalWatched >= title.totalEpisodes) {
            newStatus = WatchStatus.COMPLETO.name
            if (newCompletedAt == null) newCompletedAt = System.currentTimeMillis()
        } else if (totalWatched > 0 && title.status == WatchStatus.PENDENTE.name) {
            newStatus = WatchStatus.EM_ANDAMENTO.name
        }

        return title.copy(status = newStatus, completedAt = newCompletedAt)
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
            obj.put("duration", item.duration)
            obj.put("isIgnored", item.isIgnored)
            if (item.completedAt != null) {
                obj.put("completedAt", item.completedAt)
            }
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

    fun exportToZip(titles: List<TitleEntity>, outputStream: OutputStream) {
        val jsonStr = exportToJson(titles)
        ZipOutputStream(BufferedOutputStream(outputStream)).use { zipOut ->
            val entry = ZipEntry("vortex_backup.json")
            zipOut.putNextEntry(entry)
            zipOut.write(jsonStr.toByteArray(Charsets.UTF_8))
            zipOut.closeEntry()
            zipOut.flush()
        }
    }

    fun readBackupStream(inputStream: InputStream): String {
        val buffered = BufferedInputStream(inputStream)
        buffered.mark(1024)
        val header = ByteArray(4)
        val bytesRead = buffered.read(header, 0, 4)
        buffered.reset()

        val isZip = bytesRead == 4 &&
                header[0] == 0x50.toByte() &&
                header[1] == 0x4B.toByte() &&
                header[2] == 0x03.toByte() &&
                header[3] == 0x04.toByte()

        return if (isZip) {
            ZipInputStream(buffered).use { zipIn ->
                var entry = zipIn.nextEntry
                while (entry != null) {
                    if (!entry.isDirectory && (entry.name.endsWith(".json", ignoreCase = true) || entry.name.contains("vortex", ignoreCase = true))) {
                        return@use zipIn.bufferedReader(Charsets.UTF_8).readText()
                    }
                    entry = zipIn.nextEntry
                }
                throw IllegalArgumentException("Arquivo ZIP válido, mas nenhum arquivo JSON foi encontrado.")
            }
        } else {
            buffered.bufferedReader(Charsets.UTF_8).readText()
        }
    }

    suspend fun importFromJson(jsonStr: String): Int = withContext(Dispatchers.IO) {
        val root = JSONObject(jsonStr)
        val array = root.optJSONArray("titles") ?: JSONArray(jsonStr)
        val imported = mutableListOf<TitleEntity>()

        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val categoryStr = obj.optString("category", Category.FILME.name)
            val isMovie = categoryStr.equals(Category.FILME.name, ignoreCase = true) || categoryStr.equals("Filme", ignoreCase = true)
            val statusStr = obj.optString("status", WatchStatus.PENDENTE.name)
            val parsedCompletedAt = if (obj.has("completedAt") && !obj.isNull("completedAt")) {
                obj.optLong("completedAt")
            } else if (statusStr.equals(WatchStatus.COMPLETO.name, ignoreCase = true) || statusStr.equals("Concluído", ignoreCase = true)) {
                System.currentTimeMillis()
            } else {
                null
            }

            val entity = TitleEntity(
                name = obj.optString("name", "Sem Nome"),
                coverUrl = obj.optString("coverUrl", ""),
                synopsis = obj.optString("synopsis", ""),
                category = categoryStr,
                releaseYear = obj.optInt("releaseYear", 2024),
                genre = obj.optString("genre", "Geral"),
                totalSeasons = if (isMovie) 0 else obj.optInt("totalSeasons", 1),
                episodesPerSeason = if (isMovie) 0 else obj.optInt("episodesPerSeason", 1),
                currentSeason = if (isMovie) 0 else obj.optInt("currentSeason", 1),
                currentEpisode = if (isMovie) 0 else obj.optInt("currentEpisode", 0),
                totalEpisodes = if (isMovie) 0 else obj.optInt("totalEpisodes", 1),
                duration = obj.optString("duration", ""),
                isIgnored = obj.optBoolean("isIgnored", false),
                completedAt = parsedCompletedAt,
                status = statusStr,
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

