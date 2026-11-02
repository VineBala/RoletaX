package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "titles")
data class TitleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val coverUrl: String = "",
    val synopsis: String = "",
    val category: String = Category.FILME.name,
    val releaseYear: Int = 2024,
    val genre: String = "Geral",
    val totalSeasons: Int = 1,
    val episodesPerSeason: Int = 1,
    val currentSeason: Int = 1,
    val currentEpisode: Int = 0,
    val totalEpisodes: Int = 1,
    val status: String = WatchStatus.PENDENTE.name,
    val personalRating: Float = 0f,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val lastWatchedAt: Long? = null
) {
    val categoryEnum: Category
        get() = Category.fromString(category)

    val statusEnum: WatchStatus
        get() = WatchStatus.fromString(status)

    val isMovie: Boolean
        get() = categoryEnum == Category.FILME

    val progressPercentage: Float
        get() {
            return if (isMovie) {
                if (statusEnum == WatchStatus.COMPLETO) 1.0f else 0.0f
            } else {
                if (totalEpisodes <= 0) 0f
                else (currentEpisode.toFloat() / totalEpisodes).coerceIn(0f, 1f)
            }
        }

    val progressDisplay: String
        get() {
            return if (isMovie) {
                if (statusEnum == WatchStatus.COMPLETO) "Assistido" else "Não assistido"
            } else {
                "T$currentSeason • Ep $currentEpisode/$totalEpisodes (${(progressPercentage * 100).toInt()}%)"
            }
        }
}
