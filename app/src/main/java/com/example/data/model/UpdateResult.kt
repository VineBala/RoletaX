package com.example.data.model

data class UpdateResult(
    val titleId: Long,
    val titleName: String,
    val category: Category,
    val oldEpisodes: Int,
    val newEpisodes: Int,
    val oldSeasons: Int,
    val newSeasons: Int,
    val details: String
)
