package com.example.agendatrabajos

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class TipoMovimiento(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val precio: Double,
    val activo: Boolean = true
)