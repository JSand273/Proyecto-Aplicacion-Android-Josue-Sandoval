package com.example.agendatrabajos

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDateTime
import kotlinx.coroutines.launch

private const val HORA_CORTE = 6

private fun fechaLaboral(): String =
    LocalDateTime.now().minusHours(HORA_CORTE.toLong()).toLocalDate().toString()

@Composable
fun PantallaRegistro(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val dao = remember { AppDatabase.obtener(context).dao() }
    val scope = rememberCoroutineScope()

    var tipos by remember { mutableStateOf<List<TipoMovimiento>>(emptyList()) }
    var todosTipos by remember { mutableStateOf<List<TipoMovimiento>>(emptyList()) }
    var movimientos by remember { mutableStateOf<List<Movimiento>>(emptyList()) }

    var dialogoMovimiento by remember { mutableStateOf(false) }
    var dialogoTipo by remember { mutableStateOf(false) }
    var menuAbierto by remember { mutableStateOf(false) }
    var tipoPorBorrar by remember { mutableStateOf<TipoMovimiento?>(null) }
    var tipoPorEditar by remember { mutableStateOf<TipoMovimiento?>(null) }
    var movPorQuitar by remember { mutableStateOf<Movimiento?>(null) }

    var tipoElegido by remember { mutableStateOf<TipoMovimiento?>(null) }
    var nombreNuevo by remember { mutableStateOf("") }
    var precioNuevo by remember { mutableStateOf("") }
    var nombreEdit by remember { mutableStateOf("") }
    var precioEdit by remember { mutableStateOf("") }

    val hoy = fechaLaboral()

    suspend fun recargar() {
        tipos = dao.tiposActivos()
        todosTipos = dao.todosLosTipos()
        movimientos = dao.todosLosMovimientos()
    }

    LaunchedEffect(Unit) { recargar() }

    val gruposHoy = movimientos
        .filter { it.fecha == hoy }
        .sortedBy { it.id }
        .groupBy { it.tipoId }
        .toList()

    Column(modifier = modifier.padding(16.dp)) {
        Text("Hoy: $hoy", fontSize = 18.sp, fontWeight = FontWeight.Bold)

        if (gruposHoy.isEmpty()) {
            Text(
                "Aún no hay movimientos hoy. Toca \"Agregar movimiento\".",
                modifier = Modifier.padding(top = 12.dp)
            )
        }

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(gruposHoy) { (tipoId, lista) ->
                val nombre = todosTipos.find { it.id == tipoId }?.nombre ?: "?"
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(nombre, fontSize = 18.sp, modifier = Modifier.weight(1f))
                        OutlinedButton(onClick = {
                            movPorQuitar = lista.maxBy { it.id }
                        }) { Text("−", fontSize = 22.sp) }
                        Text(
                            "${lista.size}",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Button(onClick = {
                            scope.launch {
                                val ultimo = lista.maxBy { it.id }
                                val precio = todosTipos.find { it.id == tipoId }?.precio
                                    ?: ultimo.precioAplicado
                                dao.insertarMovimiento(
                                    Movimiento(
                                        tipoId = tipoId,
                                        fecha = hoy,
                                        precioAplicado = precio
                                    )
                                )
                                recargar()
                            }
                        }) { Text("+", fontSize = 22.sp) }
                    }
                    HorizontalDivider()
                }
            }
        }

        Button(
            onClick = {
                tipoElegido = null
                dialogoMovimiento = true
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Agregar movimiento") }
    }

    if (dialogoMovimiento) {
        AlertDialog(
            onDismissRequest = { dialogoMovimiento = false },
            title = { Text("Nuevo movimiento") },
            text = {
                Column {
                    Box {
                        OutlinedButton(onClick = { menuAbierto = true }) {
                            Text(tipoElegido?.nombre ?: "Elegir opción")
                        }
                        DropdownMenu(
                            expanded = menuAbierto,
                            onDismissRequest = { menuAbierto = false }
                        ) {
                            tipos.forEach { t ->
                                DropdownMenuItem(
                                    text = { Text("${t.nombre} - ${t.precio}") },
                                    onClick = {
                                        tipoElegido = t
                                        menuAbierto = false
                                    },
                                    trailingIcon = {
                                        Row {
                                            TextButton(onClick = {
                                                menuAbierto = false
                                                nombreEdit = t.nombre
                                                precioEdit = t.precio.toString()
                                                tipoPorEditar = t
                                            }) { Text("Editar") }
                                            TextButton(onClick = {
                                                menuAbierto = false
                                                tipoPorBorrar = t
                                            }) { Text("Borrar") }
                                        }
                                    }
                                )
                            }
                            DropdownMenuItem(
                                text = { Text("+ Agregar opción nueva") },
                                onClick = {
                                    menuAbierto = false
                                    dialogoTipo = true
                                }
                            )
                        }
                    }
                    Text("Fecha: $hoy", modifier = Modifier.padding(top = 12.dp))
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val t = tipoElegido
                    if (t != null) {
                        scope.launch {
                            dao.insertarMovimiento(
                                Movimiento(
                                    tipoId = t.id,
                                    fecha = hoy,
                                    precioAplicado = t.precio
                                )
                            )
                            recargar()
                            dialogoMovimiento = false
                        }
                    }
                }) { Text("Guardar") }
            },
            dismissButton = {
                TextButton(onClick = { dialogoMovimiento = false }) { Text("Cancelar") }
            }
        )
    }

    if (dialogoTipo) {
        AlertDialog(
            onDismissRequest = { dialogoTipo = false },
            title = { Text("Nueva opción") },
            text = {
                Column {
                    OutlinedTextField(
                        value = nombreNuevo,
                        onValueChange = { nombreNuevo = it },
                        label = { Text("Nombre") }
                    )
                    OutlinedTextField(
                        value = precioNuevo,
                        onValueChange = { precioNuevo = it },
                        label = { Text("Precio") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val precio = precioNuevo.toDoubleOrNull()
                    if (nombreNuevo.isNotBlank() && precio != null) {
                        scope.launch {
                            dao.insertarTipo(
                                TipoMovimiento(nombre = nombreNuevo.trim(), precio = precio)
                            )
                            recargar()
                            tipoElegido = tipos.lastOrNull()
                            nombreNuevo = ""
                            precioNuevo = ""
                            dialogoTipo = false
                        }
                    }
                }) { Text("Guardar") }
            },
            dismissButton = {
                TextButton(onClick = { dialogoTipo = false }) { Text("Cancelar") }
            }
        )
    }

    val porEditar = tipoPorEditar
    if (porEditar != null) {
        AlertDialog(
            onDismissRequest = { tipoPorEditar = null },
            title = { Text("Editar opción") },
            text = {
                Column {
                    OutlinedTextField(
                        value = nombreEdit,
                        onValueChange = { nombreEdit = it },
                        label = { Text("Nombre") }
                    )
                    OutlinedTextField(
                        value = precioEdit,
                        onValueChange = { precioEdit = it },
                        label = { Text("Precio") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    Text(
                        "Los movimientos ya registrados conservan el precio con el que se anotaron.",
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val precio = precioEdit.toDoubleOrNull()
                    if (nombreEdit.isNotBlank() && precio != null) {
                        scope.launch {
                            dao.actualizarTipo(porEditar.id, nombreEdit.trim(), precio)
                            recargar()
                            if (tipoElegido?.id == porEditar.id) {
                                tipoElegido = tipos.find { it.id == porEditar.id }
                            }
                            tipoPorEditar = null
                        }
                    }
                }) { Text("Guardar") }
            },
            dismissButton = {
                TextButton(onClick = { tipoPorEditar = null }) { Text("Cancelar") }
            }
        )
    }

    val porBorrar = tipoPorBorrar
    if (porBorrar != null) {
        AlertDialog(
            onDismissRequest = { tipoPorBorrar = null },
            title = { Text("¿Borrar esta opción?") },
            text = {
                Text("${porBorrar.nombre} - ${porBorrar.precio}\n\nLos movimientos que ya registraste con ella no se pierden.")
            },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        dao.desactivarTipo(porBorrar.id)
                        if (tipoElegido?.id == porBorrar.id) tipoElegido = null
                        recargar()
                        tipoPorBorrar = null
                    }
                }) { Text("Sí, borrar") }
            },
            dismissButton = {
                TextButton(onClick = { tipoPorBorrar = null }) { Text("Cancelar") }
            }
        )
    }

    val porQuitar = movPorQuitar
    if (porQuitar != null) {
        val nombreQuitar = todosTipos.find { it.id == porQuitar.tipoId }?.nombre ?: "?"
        AlertDialog(
            onDismissRequest = { movPorQuitar = null },
            title = { Text("¿Quitar un movimiento?") },
            text = { Text("Se quitará 1 movimiento de $nombreQuitar.") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        dao.borrarMovimiento(porQuitar)
                        recargar()
                        movPorQuitar = null
                    }
                }) { Text("Sí, quitar") }
            },
            dismissButton = {
                TextButton(onClick = { movPorQuitar = null }) { Text("Cancelar") }
            }
        )
    }
}