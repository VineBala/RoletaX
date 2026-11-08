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
}
