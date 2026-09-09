package com.example.academica

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

enum class EquiposState { LIST, CREATE, CONSULT }

@Composable
fun EquiposModule() {
    var currentState by remember { mutableStateOf(EquiposState.LIST) }
    var selectedEquipo by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }

    AnimatedContent(targetState = currentState) { state ->
        when (state) {
            EquiposState.LIST -> EquiposList(
                searchQuery = searchQuery,
                onSearchChange = { searchQuery = it },
                onCreate = { currentState = EquiposState.CREATE },
                onConsult = { equipo ->
                    selectedEquipo = equipo
                    currentState = EquiposState.CONSULT
                }
            )
            EquiposState.CREATE -> EquipoForm(
                onBack = { currentState = EquiposState.LIST }
            )
            EquiposState.CONSULT -> EquipoConsulta(
                equipo = selectedEquipo,
                onBack = { currentState = EquiposState.LIST }
            )
        }
    }
}

@Composable
fun EquiposList(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onCreate: () -> Unit,
    onConsult: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Gestión de Equipos",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Buscar integrante o equipo...") },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = onCreate, modifier = Modifier.fillMaxWidth()) {
            Text("Crear Nuevo Equipo de Trabajo")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Equipos Existentes",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(listOf(
                "Equipo Alpha - Julian, Carlos, Maria",
                "Equipo Beta - Jose, Ana, Luis",
                "Equipo Gamma - Elena, Pedro, Sofia",
                "Equipo Delta - Laura, Mateo"
            ).filter { it.contains(searchQuery, ignoreCase = true) }) { equipo ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = equipo.split(" - ").first(),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { onConsult(equipo) }) {
                                Text("Consultar Integrantes")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EquipoForm(onBack: () -> Unit) {
    var nombre by remember { mutableStateOf("") }
    var integrantes by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf("") }
    var esError by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Creación de Equipo Académico",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = nombre,
            onValueChange = { 
                nombre = it
                mensaje = ""
            },
            label = { Text("Nombre del Equipo") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        OutlinedTextField(
            value = integrantes,
            onValueChange = { 
                integrantes = it
                mensaje = ""
            },
            label = { Text("Nombres de Integrantes (Separados por coma)") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (mensaje.isNotEmpty()) {
            Text(
                text = mensaje,
                color = if (esError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        Row(modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = onBack,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text("Cancelar")
            }
            Spacer(modifier = Modifier.width(12.dp))
            Button(
                onClick = { 
                    if (nombre.isBlank() || integrantes.isBlank()) {
                        mensaje = "Error: Complete los campos de equipo"
                        esError = true
                    } else {
                        mensaje = "Equipo creado exitosamente"
                        esError = false
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Crear Equipo")
            }
        }
    }
}

@Composable
fun EquipoConsulta(equipo: String, onBack: () -> Unit) {
    val partes = equipo.split(" - ")
    val nombre = partes.first()
    val integrantes = partes.last().split(", ")

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Detalles del Equipo",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(24.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Equipo:", style = MaterialTheme.typography.labelLarge)
                Text(nombre, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Integrantes:", style = MaterialTheme.typography.labelLarge)
                integrantes.forEach { integrante ->
                    Text("• $integrante", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Volver")
        }
    }
}
