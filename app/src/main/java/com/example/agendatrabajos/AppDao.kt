package com.example.agendatrabajos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface AppDao {

    @Insert
    suspend fun insertarTipo(tipo: TipoMovimiento)

    @Query("SELECT * FROM TipoMovimiento WHERE activo = 1")
    suspend fun tiposActivos(): List<TipoMovimiento>

    @Insert
    suspend fun insertarMovimiento(mov: Movimiento)

    @Delete
    suspend fun borrarMovimiento(mov: Movimiento)

    @Query("SELECT * FROM Movimiento ORDER BY fecha DESC")
    suspend fun todosLosMovimientos(): List<Movimiento>

    @Query("UPDATE TipoMovimiento SET activo = 0 WHERE id = :id")
    suspend fun desactivarTipo(id: Long)

    @Query("SELECT * FROM TipoMovimiento")
    suspend fun todosLosTipos(): List<TipoMovimiento>

    @Query("UPDATE TipoMovimiento SET nombre = :nombre, precio = :precio WHERE id = :id")
    suspend fun actualizarTipo(id: Long, nombre: String, precio: Double)
}