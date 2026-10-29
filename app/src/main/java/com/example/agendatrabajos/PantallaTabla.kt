package com.example.agendatrabajos

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private fun construirResumen(
    nombreUsuario: String,
    unidad: String,
    fila: FilaResumen,
    tipos: List<TipoMovimiento>
): String {
    val sb = StringBuilder()
    sb.appendLine("Resumen: ${fila.etiqueta}")
    if (nombreUsuario.isNotBlank()) sb.appendLine("Nombre: $nombreUsuario")
    if (unidad.isNotBlank()) sb.appendLine("Unidad No. $unidad")
    sb.appendLine()
    fila.movimientos.groupBy { it.tipoId }.forEach { (tipoId, lista) ->
        val nombre = tipos.find { it.id == tipoId }?.nombre ?: "?"
        sb.appendLine("${lista.size} x $nombre = ${lista.sumOf { it.precioAplicado }}")
    }
    sb.appendLine()
    sb.append("Total: ${fila.total} (${fila.cantidad} movimientos)")
    return sb.toString()
}

@Composable
fun PantallaTabla(modo: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val dao = remember { AppDatabase.obtener(context).dao() }
    var filas by remember { mutableStateOf<List<FilaResumen>>(emptyList()) }
    var tipos by remember { mutableStateOf<List<TipoMovimiento>>(emptyList()) }
    var filaElegida by remember { mutableStateOf<FilaResumen?>(null) }

    LaunchedEffect(modo) {
        val movs = dao.todosLosMovimientos()
        tipos = dao.todosLosTipos()
        filas = when (modo) {
            "Diaria" -> GeneradorTablas.diaria(movs)
            "Semanal" -> GeneradorTablas.semanal(movs)
            else -> GeneradorTablas.mensual(movs)
        }
    }

    Column(modifier = modifier.padding(16.dp)) {
        Row {
            Text("Periodo", modifier = Modifier.weight(2f), fontWeight = FontWeight.Bold)
            Text("Cantidad", modifier = Modifier.weight(1.4f), fontWeight = FontWeight.Bold)
            Text("Total", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
        }
        LazyColumn {
            items(filas) { f ->
                Row(
                    modifier = Modifier
                        .clickable { filaElegida = f }
                        .padding(vertical = 8.dp)
                ) {
                    Text(f.etiqueta, modifier = Modifier.weight(2f))
                    Text("${f.cantidad}", modifier = Modifier.weight(1.4f))
                    Text("${f.total}", modifier = Modifier.weight(1f))
                }
            }
        }
    }

    val elegida = filaElegida
    if (elegida != null) {
        AlertDialog(
            onDismissRequest = { filaElegida = null },
            title = { Text(elegida.etiqueta) },
            text = {
                Column {
                    elegida.movimientos
                        .groupBy { it.tipoId }
                        .forEach { (tipoId, lista) ->
                            val nombre = tipos.find { it.id == tipoId }?.nombre ?: "?"
                            Row {
                                Text("${lista.size}", modifier = Modifier.weight(0.6f))
                                Text(nombre, modifier = Modifier.weight(2f))
                                Text("${lista.sumOf { it.precioAplicado }}", modifier = Modifier.weight(1f))
                            }
                        }
                    Text(
                        "Total: ${elegida.total}",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { filaElegida = null }) { Text("Cerrar") }
            },
            dismissButton = {
                TextButton(onClick = {
                    val prefs = context.getSharedPreferences("perfil", Context.MODE_PRIVATE)
                    val texto = construirResumen(
                        prefs.getString("nombre", "") ?: "",
                        prefs.getString("unidad", "") ?: "",
                        elegida,
                        tipos
                    )
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, texto)
                    }
                    context.startActivity(Intent.createChooser(intent, "Compartir resumen"))
                }) { Text("Compartir") }
            }
        )
    }
}