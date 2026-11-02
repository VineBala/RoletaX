package com.example.data.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.StatusBlue
import com.example.ui.theme.StatusGray
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusRed
import com.example.ui.theme.StatusYellow

enum class WatchStatus(
    val label: String,
    val shortLabel: String,
    val color: Color
) {
    EM_ANDAMENTO("Em Andamento", "Assistindo", StatusGreen),
    BLOQUEADO("Bloqueado / Abandonado", "Bloqueado", StatusRed),
    COMPLETO("Completo", "Concluído", StatusBlue),
    PAUSADO("Pausado", "Pausado", StatusYellow),
    PENDENTE("Pendente", "Na Fila", StatusGray);

    companion object {
        fun fromString(value: String): WatchStatus {
            return entries.firstOrNull { 
                it.name.equals(value, ignoreCase = true) || 
                it.label.equals(value, ignoreCase = true) ||
                it.shortLabel.equals(value, ignoreCase = true)
            } ?: PENDENTE
        }
    }
}
