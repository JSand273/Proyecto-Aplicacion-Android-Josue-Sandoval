package com.example.agendatrabajos

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun EncabezadoPerfil(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("perfil", Context.MODE_PRIVATE) }

    var nombre by remember { mutableStateOf(prefs.getString("nombre", "") ?: "") }
    var unidad by remember { mutableStateOf(prefs.getString("unidad", "") ?: "") }
    var editando by remember { mutableStateOf(nombre.isBlank()) }
    var nombreTemp by remember { mutableStateOf(nombre) }
    var unidadTemp by remember { mutableStateOf(unidad) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                nombreTemp = nombre
                unidadTemp = unidad
                editando = true
            }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            if (nombre.isBlank()) "Toca para configurar" else nombre,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        if (unidad.isNotBlank()) {
            Text("Unidad No. $unidad")
        }
    }

    if (editando) {
        AlertDialog(
            onDismissRequest = { if (nombre.isNotBlank()) editando = false },
            title = { Text("Datos del usuario") },
            text = {
                Column {
                    OutlinedTextField(
                        value = nombreTemp,
                        onValueChange = { nombreTemp = it },
                        label = { Text("Nombre") }
                    )
                    OutlinedTextField(
                        value = unidadTemp,
                        onValueChange = { unidadTemp = it },
                        label = { Text("Unidad No.") }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (nombreTemp.isNotBlank()) {
                        nombre = nombreTemp.trim()
                        unidad = unidadTemp.trim()
                        prefs.edit()
                            .putString("nombre", nombre)
                            .putString("unidad", unidad)
                            .apply()
                        editando = false
                    }
                }) { Text("Guardar") }
            }
        )
    }
}