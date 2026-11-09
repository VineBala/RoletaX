package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.Category
import com.example.data.model.TitleEntity
import com.example.data.model.WatchStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read app name string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Vortex", appName)
    }

    @Test
    fun `test title progress calculation for series and anime`() {
        val anime = TitleEntity(
            name = "One Piece",
            category = Category.ANIME.name,
            currentSeason = 1,
            currentEpisode = 115,
            totalEpisodes = 1150
        )
        assertEquals(0.10f, anime.progressPercentage, 0.01f)
    }

    @Test
    fun `test movie watched status progress`() {
        val watchedMovie = TitleEntity(
            name = "Interestelar",
            category = Category.FILME.name,
            status = WatchStatus.COMPLETO.name
        )
        assertEquals(1.0f, watchedMovie.progressPercentage, 0.01f)

        val unwatchedMovie = TitleEntity(
            name = "Duna 2",
            category = Category.FILME.name,
            duration = "2h 46m",
            status = WatchStatus.PENDENTE.name
        )
        assertEquals(0.0f, unwatchedMovie.progressPercentage, 0.01f)
        assertEquals("Filme • 2h 46m", unwatchedMovie.progressDisplay)
    }

    @Test
    fun `test movie duration formatting without inventing data`() {
        val movieWithDuration = TitleEntity(
            name = "Interestelar",
            category = Category.FILME.name,
            duration = "2h 49m",
            status = WatchStatus.COMPLETO.name
        )
        val formattedDate = movieWithDuration.completedDateDisplay
        assertEquals("Concluído em $formattedDate • 2h 49m", movieWithDuration.progressDisplay)

        val movieWithoutDuration = TitleEntity(
            name = "Filme Desconhecido",
            category = Category.FILME.name,
            duration = "",
            status = WatchStatus.PENDENTE.name
        )
        assertEquals("Filme", movieWithoutDuration.progressDisplay)
    }

    @Test
    fun `test quick toggle title completed records timestamp and marks series fully watched`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = com.example.data.local.AppDatabase.getDatabase(context)
        val repository = com.example.data.repository.TitleRepository(db.titleDao())

        // Série não concluída (temporada 1 ep 3 de 24)
        val series = TitleEntity(
            id = 999L,
            name = "Attack on Titan",
            category = Category.ANIME.name,
            totalSeasons = 2,
            episodesPerSeason = 12,
            currentSeason = 1,
            currentEpisode = 3,
            totalEpisodes = 24,
            status = WatchStatus.EM_ANDAMENTO.name
        )
        db.titleDao().insert(series)

        // Concluir via toggleTitleCompleted (ação rápida de fora ou de dentro)
        repository.toggleTitleCompleted(999L)

        val updated = db.titleDao().getTitleById(999L).first()
        org.junit.Assert.assertNotNull(updated)
        assertEquals(WatchStatus.COMPLETO.name, updated!!.status)
        org.junit.Assert.assertNotNull(updated.completedAt)
        org.junit.Assert.assertTrue((updated.completedAt ?: 0L) > 0L)
        org.junit.Assert.assertNotNull(updated.completedDateDisplay)
        assertEquals(2, updated.currentSeason)
        assertEquals(12, updated.currentEpisode)
        assertEquals(1.0f, updated.progressPercentage, 0.001f)

        // Simula update geral: garantir que applyStatusAutomation NÃO rebaixa COMPLETO para EM_ANDAMENTO
        repository.update(updated.copy(notes = "Atualizado"))
        val afterUpdate = db.titleDao().getTitleById(999L).first()
        assertEquals(WatchStatus.COMPLETO.name, afterUpdate!!.status)
        org.junit.Assert.assertNotNull(afterUpdate.completedAt)
    }

    @Test
    fun `test title isIgnored flag defaults to false and can be toggled`() {
        val title = TitleEntity(
            id = 10L,
            name = "A Viagem de Chihiro",
            category = Category.ANIME.name,
            isIgnored = false
        )
        assertEquals(false, title.isIgnored)

        val ignored = title.copy(isIgnored = true)
        assertEquals(true, ignored.isIgnored)
        assertEquals(10L, ignored.id)
    }

    @Test
    fun `test title update preserves original ID and does not duplicate`() {
        val original = TitleEntity(
            id = 42L,
            name = "One Piece",
            category = Category.ANIME.name,
            currentSeason = 1,
            totalSeasons = 1,
            currentEpisode = 14,
            totalEpisodes = 1150
        )

        // Simula edição feita diretamente na roleta: avança para ep 15
        val updated = original.copy(
            currentEpisode = 15,
            updatedAt = System.currentTimeMillis()
        )

        assertEquals(original.id, updated.id)
        assertEquals(15, updated.currentEpisode)
        assertEquals(1150, updated.totalEpisodes)
    }

    @Test
    fun `test season progress calculation correctly sums previous seasons and current season`() {
        // Exemplo: Série com 4 temporadas, 12 eps por temporada, total 48 eps.
        // Usuário está na Temporada 2, Episódio 6.
        // Temporada 1 concluída (12 eps) + Ep 6 da T2 = 18 eps assistidos no total.
        // 18 / 48 = 0.375 (37.5% -> 38%)
        val series = TitleEntity(
            name = "Stranger Things",
            category = Category.SERIE.name,
            totalSeasons = 4,
            episodesPerSeason = 12,
            currentSeason = 2,
            currentEpisode = 6,
            totalEpisodes = 48
        )

        assertEquals(12, series.effectiveEpisodesPerSeason)
        assertEquals(18, series.totalWatchedEpisodes)
        assertEquals(0.375f, series.progressPercentage, 0.001f)
    }

    @Test
    fun `test completed title captures completion timestamp and date display`() {
        val completedTitle = TitleEntity(
            name = "Naruto Shippuden",
            category = Category.ANIME.name,
            status = WatchStatus.COMPLETO.name,
            completedAt = 1762214400000L // Data fixa para teste
        )

        assertEquals(WatchStatus.COMPLETO, completedTitle.statusEnum)
        org.junit.Assert.assertNotNull(completedTitle.completedDateDisplay)
        assertEquals(1.0f, completedTitle.progressPercentage, 0.001f)
    }

    @Test
    fun `test zip backup export and extraction`() {
        val sampleList = listOf(
            TitleEntity(
                name = "Arcane",
                category = Category.SERIE.name,
                totalSeasons = 2,
                totalEpisodes = 18,
                isIgnored = true
            )
        )

        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = com.example.data.local.AppDatabase.getDatabase(context)
        val repository = com.example.data.repository.TitleRepository(db.titleDao())

        // Export to ZIP byte stream
        val outStream = java.io.ByteArrayOutputStream()
        repository.exportToZip(sampleList, outStream)
        val zipBytes = outStream.toByteArray()

        org.junit.Assert.assertTrue(zipBytes.isNotEmpty())

        // Read back from stream and extract JSON
        val inStream = java.io.ByteArrayInputStream(zipBytes)
        val extractedJson = repository.readBackupStream(inStream)

        org.junit.Assert.assertTrue(extractedJson.contains("Arcane"))
        org.junit.Assert.assertTrue(extractedJson.contains("\"isIgnored\": true"))
    }

    @Test
    fun `test drawRouletteTitle picks eligible title and respects isIgnored filter`() {
        val ignoredTitle = TitleEntity(
            id = 1L,
            name = "Título Ignorado",
            category = Category.FILME.name,
            isIgnored = true
        )
        val eligibleTitle = TitleEntity(
            id = 2L,
            name = "Interestelar",
            category = Category.FILME.name,
            isIgnored = false
        )
        val sampleList = listOf(ignoredTitle, eligibleTitle)

        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        val viewModel = com.example.viewmodel.SeraAkisosViewModel(app)

        val eligibleList = viewModel.getEligibleTitles(sampleList)
        org.junit.Assert.assertEquals(1, eligibleList.size)
        org.junit.Assert.assertEquals(2L, eligibleList[0].id)
        org.junit.Assert.assertEquals("Interestelar", eligibleList[0].name)
        org.junit.Assert.assertFalse(eligibleList.any { it.isIgnored })

        // Executa o sorteio direto para o Card de Destaque
        val winner = viewModel.drawRouletteTitle(candidatesSource = sampleList)
        org.junit.Assert.assertNotNull(winner)
        assertEquals(2L, winner!!.id)
        assertEquals("Interestelar", winner.name)
    }

    @Test
    fun `test exportBackupDirectlyToDownloads creates zip in downloads folder`() {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        val db = com.example.data.local.AppDatabase.getDatabase(app)
        kotlinx.coroutines.runBlocking {
            db.titleDao().insert(
                TitleEntity(
                    name = "Cyberpunk: Edgerunners",
                    category = Category.ANIME.name,
                    totalEpisodes = 10
                )
            )
        }

        val viewModel = com.example.viewmodel.SeraAkisosViewModel(app)
        var callbackSuccess = false
        var generatedFileName: String? = null
        val latch = java.util.concurrent.CountDownLatch(1)

        viewModel.exportBackupDirectlyToDownloads(app) { success, fileName ->
            callbackSuccess = success
            generatedFileName = fileName
            latch.countDown()
        }

        var loops = 0
        while (latch.count > 0 && loops < 50) {
            Thread.sleep(100)
            org.robolectric.shadows.ShadowLooper.idleMainLooper()
            loops++
        }

        org.junit.Assert.assertTrue(callbackSuccess)
        org.junit.Assert.assertNotNull(generatedFileName)
        org.junit.Assert.assertTrue(generatedFileName!!.startsWith("vortex_backup_"))
        org.junit.Assert.assertTrue(generatedFileName!!.endsWith(".zip"))
    }

    @Test
    fun `test manual selection in roulette and universal cycle non-repetition`() {
        val titleA = TitleEntity(id = 101L, name = "Filme Alpha", category = Category.FILME.name)
        val titleB = TitleEntity(id = 102L, name = "Filme Beta", category = Category.FILME.name)
        val titleC = TitleEntity(id = 103L, name = "Filme Gama", category = Category.FILME.name)
        val pool = listOf(titleA, titleB, titleC)

        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        val viewModel = com.example.viewmodel.SeraAkisosViewModel(app)
        viewModel.resetRouletteCycle()

        // 1. Seleciona manualmente o Filme Alpha
        viewModel.selectManualTitle(titleA)
        org.junit.Assert.assertTrue(viewModel.isManualSelection.value)
        assertEquals(101L, viewModel.activeWinnerId.value)
        org.junit.Assert.assertTrue(viewModel.rouletteDrawnCycleIds.value.contains(101L))

        // 2. Agora sorteia automaticamente a partir do mesmo pool
        // Por regra universal, Filme Alpha NÃO pode ser sorteado porque já está no ciclo
        val secondPick = viewModel.drawRouletteTitle(candidatesSource = pool)
        org.junit.Assert.assertNotNull(secondPick)
        org.junit.Assert.assertFalse(viewModel.isManualSelection.value)
        org.junit.Assert.assertNotEquals(101L, secondPick!!.id)
        org.junit.Assert.assertTrue(secondPick.id == 102L || secondPick.id == 103L)

        // 3. Sorteia o terceiro título do ciclo
        val thirdPick = viewModel.drawRouletteTitle(candidatesSource = pool)
        org.junit.Assert.assertNotNull(thirdPick)
        org.junit.Assert.assertNotEquals(101L, thirdPick!!.id)
        org.junit.Assert.assertNotEquals(secondPick.id, thirdPick.id)

        // Todos os 3 títulos agora participaram do ciclo
        assertEquals(3, viewModel.rouletteDrawnCycleIds.value.size)

        // 4. No 4º sorteio, o ciclo se reinicia automaticamente
        val fourthPick = viewModel.drawRouletteTitle(candidatesSource = pool)
        org.junit.Assert.assertNotNull(fourthPick)
        assertEquals(1, viewModel.rouletteDrawnCycleIds.value.size)
    }

    @Test
    fun `test toggleBlocked sets status and skips blocked titles in update checking`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = com.example.data.local.AppDatabase.getDatabase(context)
        val repository = com.example.data.repository.TitleRepository(db.titleDao())

        val series = TitleEntity(
            id = 555L,
            name = "Série Congelada",
            category = Category.SERIE.name,
            status = WatchStatus.PENDENTE.name,
            totalEpisodes = 10
        )
        db.titleDao().insert(series)

        // 1. Bloquear
        val isNowBlocked = repository.toggleBlocked(555L)
        org.junit.Assert.assertTrue(isNowBlocked)
        val blockedItem = db.titleDao().getTitleById(555L).first()
        assertEquals(WatchStatus.BLOQUEADO.name, blockedItem?.status)

        // 2. Verificar que checkSingleTitleUpdate ignora título bloqueado
        val updateResult = repository.checkSingleTitleUpdate(555L)
        org.junit.Assert.assertNull(updateResult)

        // 3. Desbloquear
        val isUnblocked = repository.toggleBlocked(555L)
        org.junit.Assert.assertFalse(isUnblocked)
        val unblockedItem = db.titleDao().getTitleById(555L).first()
        assertEquals(WatchStatus.PENDENTE.name, unblockedItem?.status)
    }

    @Test
    fun `test revert completed status preserves progress and returns to in progress`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = com.example.data.local.AppDatabase.getDatabase(context)
        val repository = com.example.data.repository.TitleRepository(db.titleDao())

        val title = TitleEntity(
            id = 777L,
            name = "Solo Leveling",
            category = Category.ANIME.name,
            totalSeasons = 1,
            episodesPerSeason = 12,
            currentSeason = 1,
            currentEpisode = 8,
            totalEpisodes = 12,
            status = WatchStatus.EM_ANDAMENTO.name
        )
        db.titleDao().insert(title)

        // 1. Marca como concluído
        repository.toggleTitleCompleted(777L)
        val completed = db.titleDao().getTitleById(777L).first()
        org.junit.Assert.assertNotNull(completed)
        assertEquals(WatchStatus.COMPLETO.name, completed!!.status)
        org.junit.Assert.assertNotNull(completed.completedAt)

        // 2. Reverte o status de concluído
        repository.toggleTitleCompleted(777L)
        val reverted = db.titleDao().getTitleById(777L).first()
        org.junit.Assert.assertNotNull(reverted)
        assertEquals(WatchStatus.EM_ANDAMENTO.name, reverted!!.status)
        org.junit.Assert.assertNull(reverted.completedAt)
        // O progresso não é zerado para 0
        assertEquals(1, reverted.currentSeason)
        org.junit.Assert.assertTrue((reverted.currentEpisode) > 0)
    }

    @Test
    fun `test zip backup export and import preserves exact episode and season progress`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = com.example.data.local.AppDatabase.getDatabase(context)
        val repository = com.example.data.repository.TitleRepository(db.titleDao())

        val original = TitleEntity(
            name = "Frieren",
            category = Category.ANIME.name,
            totalSeasons = 1,
            episodesPerSeason = 28,
            currentSeason = 1,
            currentEpisode = 17,
            totalEpisodes = 28,
            status = WatchStatus.EM_ANDAMENTO.name
        )

        val outStream = java.io.ByteArrayOutputStream()
        repository.exportToZip(listOf(original), outStream)
        val zipBytes = outStream.toByteArray()

        val jsonStr = repository.readBackupStream(java.io.ByteArrayInputStream(zipBytes))
        db.titleDao().deleteAll()

        val count = repository.importFromJson(jsonStr)
        assertEquals(1, count)

        val imported = db.titleDao().getAllTitles().first().first()
        assertEquals("Frieren", imported.name)
        assertEquals(1, imported.currentSeason)
        assertEquals(17, imported.currentEpisode)
        assertEquals(28, imported.totalEpisodes)
        assertEquals(WatchStatus.EM_ANDAMENTO.name, imported.status)
    }

    @Test
    fun `test library search matches portuguese terms and synonyms`() {
        // Exemplo direto do usuário: Buscar por "Chaves" localiza "El Chavo del Ocho"
        val chavesMatch = com.example.util.TitleSearchHelper.matchesQuery(
            query = "Chaves",
            targetTitle = "El Chavo del Ocho"
        )
        org.junit.Assert.assertTrue("Busca por 'Chaves' deve localizar 'El Chavo del Ocho'", chavesMatch)

        // Busca por "Ataque dos Titãs" localiza "Attack on Titan" / "Shingeki no Kyojin"
        val aotMatch = com.example.util.TitleSearchHelper.matchesQuery(
            query = "Ataque dos Titãs",
            targetTitle = "Attack on Titan"
        )
        org.junit.Assert.assertTrue("Busca por 'Ataque dos Titãs' deve localizar 'Attack on Titan'", aotMatch)

        // Busca sem acento localiza com acento
        val interestelarMatch = com.example.util.TitleSearchHelper.matchesQuery(
            query = "interstellar",
            targetTitle = "Interestelar"
        )
        org.junit.Assert.assertTrue(interestelarMatch)
    }

    @Test
    fun `test version name is dynamic 2 dot 1`() {
        assertEquals("2.1", BuildConfig.VERSION_NAME)
    }
}
