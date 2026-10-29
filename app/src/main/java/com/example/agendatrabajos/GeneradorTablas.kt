package com.example.agendatrabajos

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.TemporalAdjusters

data class FilaResumen(
    val etiqueta: String,
    val cantidad: Int,
    val total: Double,
    val movimientos: List<Movimiento>
)
object GeneradorTablas {

    private fun resumir(
        movs: List<Movimiento>,
        clave: (LocalDate) -> String,
        etiqueta: (String) -> String
    ): List<FilaResumen> {
        return movs
            .mapNotNull { m ->
                runCatching { LocalDate.parse(m.fecha) }.getOrNull()?.let { it to m }
            }
            .groupBy({ clave(it.first) }, { it.second })
            .map { (k, lista) ->
                k to FilaResumen(etiqueta(k), lista.size, lista.sumOf { it.precioAplicado }, lista)
            }
            .sortedByDescending { it.first }
            .map { it.second }
    }

    fun diaria(movs: List<Movimiento>) =
        resumir(movs, { it.toString() }, { it })

    fun semanal(movs: List<Movimiento>) =
        resumir(
            movs,
            { it.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).toString() },
            { "Semana del $it" }
        )

    fun mensual(movs: List<Movimiento>) =
        resumir(movs, { YearMonth.from(it).toString() }, { it })
}