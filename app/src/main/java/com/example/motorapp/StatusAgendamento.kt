package com.example.motorapp

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.widget.TextView

/**
 * Centraliza os status possíveis de um agendamento e a cor de cada um.
 * Os textos são EXATAMENTE os mesmos gravados no banco (coluna agendamento.status).
 *
 * Fluxo normal:  Pendente -> Confirmado -> Em Manutenção -> Concluído
 * A qualquer momento antes da manutenção o mecânico pode recusar/cancelar (Cancelado).
 */
object StatusAgendamento {
    const val PENDENTE = "Pendente"
    const val CONFIRMADO = "Confirmado"
    const val EM_MANUTENCAO = "Em Manutenção"
    const val CONCLUIDO = "Concluído"
    const val CANCELADO = "Cancelado"
    const val TODOS = "Todos"

    fun cor(status: String): Int = when (status) {
        PENDENTE -> Color.parseColor("#F9A825")
        CONFIRMADO -> Color.parseColor("#1976D2")
        EM_MANUTENCAO -> Color.parseColor("#EF6C00")
        CONCLUIDO -> Color.parseColor("#2E7D32")
        CANCELADO -> Color.parseColor("#D32F2F")
        else -> Color.parseColor("#757575")
    }

    /** Pinta um TextView como um "selo" arredondado com a cor do status. */
    fun aplicarSelo(textView: TextView, status: String) {
        textView.text = status
        textView.setTextColor(Color.WHITE)
        textView.background = GradientDrawable().apply {
            cornerRadius = 40f
            setColor(cor(status))
        }
    }

    /** Converte "2026-10-05" em "05/10/2026" (se vier em outro formato, devolve igual). */
    fun formatarData(data: String): String {
        val partes = data.split("-")
        return if (partes.size == 3 && data.length == 10) "${partes[2]}/${partes[1]}/${partes[0]}" else data
    }
}
