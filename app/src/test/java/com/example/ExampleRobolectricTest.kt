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
            status = WatchStatus.PENDENTE.name
        )
        assertEquals(0.0f, unwatchedMovie.progressPercentage, 0.01f)
    }
}
