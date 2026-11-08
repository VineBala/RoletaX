package com.example.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.ui.graphics.vector.ImageVector

enum class Category(val displayName: String, val pluralName: String) {
    FILME("Filme", "Filmes"),
    SERIE("Série", "Séries"),
    ANIME("Anime", "Animes");

    val icon: ImageVector
        get() = when (this) {
            FILME -> Icons.Default.Movie
            SERIE -> Icons.Default.Tv
            ANIME -> Icons.Default.VideoLibrary
        }

    companion object {
        fun fromString(value: String): Category {
            return entries.firstOrNull { 
                it.name.equals(value, ignoreCase = true) || 
                it.displayName.equals(value, ignoreCase = true) 
            } ?: FILME
        }
    }
}
