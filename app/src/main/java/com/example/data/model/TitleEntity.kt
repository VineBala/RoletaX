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
    val lastWatchedAt: Long? = null,
    val duration: String = "",
    val isIgnored: Boolean = false,
    val completedAt: Long? = null
) {
    val categoryEnum: Category
        get() = Category.fromString(category)

    val statusEnum: WatchStatus
        get() = WatchStatus.fromString(status)

    val isMovie: Boolean
        get() = categoryEnum == Category.FILME

    val effectiveEpisodesPerSeason: Int
        get() = if (episodesPerSeason > 0) {
            episodesPerSeason
        } else if (totalSeasons > 0) {
            (totalEpisodes / totalSeasons).coerceAtLeast(1)
        } else {
            1
        }

    val totalWatchedEpisodes: Int
        get() {
            if (isMovie) return if (statusEnum == WatchStatus.COMPLETO) 1 else 0
            if (statusEnum == WatchStatus.COMPLETO) return totalEpisodes.coerceAtLeast(1)
            val prevEpisodes = (currentSeason - 1).coerceAtLeast(0) * effectiveEpisodesPerSeason
            return (prevEpisodes + currentEpisode).coerceIn(0, totalEpisodes.coerceAtLeast(1))
        }

    val progressPercentage: Float
        get() {
            return if (isMovie) {
                if (statusEnum == WatchStatus.COMPLETO) 1.0f else 0.0f
            } else {
                if (statusEnum == WatchStatus.COMPLETO) return 1.0f
                if (totalEpisodes <= 0) 0f
                else (totalWatchedEpisodes.toFloat() / totalEpisodes.coerceAtLeast(1)).coerceIn(0f, 1f)
            }
        }

    val completedDateDisplay: String?
        get() {
            if (statusEnum != WatchStatus.COMPLETO && completedAt == null) return null
            val ts = completedAt ?: lastWatchedAt ?: updatedAt ?: createdAt
            val validTs = if (ts > 0L) ts else System.currentTimeMillis()
            val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
            return sdf.format(java.util.Date(validTs))
        }

    val progressDisplay: String
        get() {
            return if (isMovie) {
                val durText = if (duration.isNotBlank()) " • $duration" else ""
                val compText = if (statusEnum == WatchStatus.COMPLETO) {
                    if (completedDateDisplay != null) "Concluído em $completedDateDisplay" else "Concluído"
                } else {
                    "Filme"
                }
                "$compText$durText"
            } else {
                val compText = if (statusEnum == WatchStatus.COMPLETO && completedDateDisplay != null) " • Concluído em $completedDateDisplay" else ""
                "T$currentSeason • Ep $currentEpisode/$totalEpisodes (${(progressPercentage * 100).toInt()}%)$compText"
            }
        }
}
