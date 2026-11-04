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
    BLOQUEADO("Bloqueado", "Bloquear", StatusRed),
    COMPLETO("Concluído", "Concluir", StatusBlue),
    // Estados mantidos internamente para compatibilidade de dados legados
    EM_ANDAMENTO("Em Andamento", "Em Andamento", StatusGreen),
    PAUSADO("Pausado", "Pausado", StatusYellow),
    PENDENTE("Pendente", "Pendente", StatusGray);

    companion object {
        // O catálogo exibe exclusivamente Bloquear e Concluir
        val activeCatalogStatuses = listOf(BLOQUEADO, COMPLETO)

        fun fromString(value: String): WatchStatus {
            return entries.firstOrNull { 
                it.name.equals(value, ignoreCase = true) || 
                it.label.equals(value, ignoreCase = true) ||
                it.shortLabel.equals(value, ignoreCase = true)
            } ?: PENDENTE
        }
    }
}
