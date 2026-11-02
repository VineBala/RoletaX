package com.example.data.remote

import com.example.data.model.Category

data class TitleSearchResult(
    val title: String,
    val coverUrl: String,
    val synopsis: String,
    val category: Category,
    val releaseYear: Int,
    val genre: String,
    val totalSeasons: Int = 1,
    val episodesPerSeason: Int = 1,
    val totalEpisodes: Int = 1,
    val source: String
)
