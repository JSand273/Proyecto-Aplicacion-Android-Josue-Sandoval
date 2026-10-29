package com.example.agendatrabajos

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Movimiento(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tipoId: Long,
    val fecha: String,
    val precioAplicado: Double
)