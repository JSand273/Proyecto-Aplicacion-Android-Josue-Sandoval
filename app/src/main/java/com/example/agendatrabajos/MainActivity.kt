package com.example.agendatrabajos

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import com.example.agendatrabajos.ui.theme.AgendaTrabajosTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AgendaTrabajosTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    var pestana by remember { mutableIntStateOf(0) }
                    val titulos = listOf("Registro", "Diaria", "Semanal", "Mensual")
                    Column(modifier = Modifier.padding(innerPadding)) {
                        EncabezadoPerfil()
                        TabRow(selectedTabIndex = pestana) {
                            titulos.forEachIndexed { i, titulo ->
                                Tab(
                                    selected = pestana == i,
                                    onClick = { pestana = i },
                                    text = { Text(titulo) }
                                )
                            }
                        }
                        when (pestana) {
                            0 -> PantallaRegistro(modifier = Modifier.weight(1f))
                            else -> PantallaTabla(modo = titulos[pestana], modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    AgendaTrabajosTheme {
        Greeting("Android")
    }
}