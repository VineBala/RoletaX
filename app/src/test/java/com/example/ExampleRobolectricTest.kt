package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.Category
import com.example.data.model.TitleEntity
import com.example.data.model.WatchStatus
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
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
        assertEquals("Concluído • 2h 49m", movieWithDuration.progressDisplay)

        val movieWithoutDuration = TitleEntity(
            name = "Filme Desconhecido",
            category = Category.FILME.name,
            duration = "",
            status = WatchStatus.PENDENTE.name
        )
        assertEquals("Filme", movieWithoutDuration.progressDisplay)
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
}
