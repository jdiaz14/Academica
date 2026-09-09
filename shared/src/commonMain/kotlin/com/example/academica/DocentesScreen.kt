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

enum class DocentesState { LIST, ASSIGN, CONSULT }

@Composable
fun DocentesModule() {
    var currentState by remember { mutableStateOf(DocentesState.LIST) }
    var selectedAsign by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }

    AnimatedContent(targetState = currentState) { state ->
        when (state) {
            DocentesState.LIST -> DocentesList(
                searchQuery = searchQuery,
                onSearchChange = { searchQuery = it },
                onAssign = { currentState = DocentesState.ASSIGN },
                onConsult = { asign ->
                    selectedAsign = asign
                    currentState = DocentesState.CONSULT
                }
            )
            DocentesState.ASSIGN -> AsignacionForm(
                onBack = { currentState = DocentesState.LIST }
            )
            DocentesState.CONSULT -> AsignacionConsulta(
                asign = selectedAsign,
                onBack = { currentState = DocentesState.LIST }
            )
        }
    }
}

@Composable
fun DocentesList(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onAssign: () -> Unit,
    onConsult: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Asignación de Docentes",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Buscar docente o proyecto...") },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = onAssign, modifier = Modifier.fillMaxWidth()) {
            Text("Nueva Asignación de Tutor")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Tutorías Registradas",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(listOf(
                "Prof. Gomez -> Proyecto Inteligencia Artificial",
                "Prof. Sanchez -> Desarrollo Web Multiplataforma",
                "Prof. Ruiz -> Redes Neuronales y Seguridad",
                "Prof. Martinez -> Ingeniería de Software III"
            ).filter { it.contains(searchQuery, ignoreCase = true) }) { asign ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = asign,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { onConsult(asign) }) {
                                Text("Consultar Detalles")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AsignacionForm(onBack: () -> Unit) {
    var docente by remember { mutableStateOf("") }
    var proyecto by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf("") }
    var esError by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Nueva Asignación de Tutor",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = docente,
            onValueChange = { 
                docente = it
                mensaje = ""
            },
            label = { Text("Nombre del Docente Tutor") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        OutlinedTextField(
            value = proyecto,
            onValueChange = { 
                proyecto = it
                mensaje = ""
            },
            label = { Text("Proyecto y Estudiantes Asignados") },
            modifier = Modifier.fillMaxWidth()
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
                    if (docente.isBlank() || proyecto.isBlank()) {
                        mensaje = "Error: Complete la asignación"
                        esError = true
                    } else {
                        mensaje = "Asignación registrada exitosamente"
                        esError = false
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Asignar")
            }
        }
    }
}

@Composable
fun AsignacionConsulta(asign: String, onBack: () -> Unit) {
    val partes = asign.split(" -> ")
    val docente = partes.firstOrNull() ?: ""
    val proyecto = partes.lastOrNull() ?: ""

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Detalles de Asignación",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(24.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Docente Tutor:", style = MaterialTheme.typography.labelLarge)
                Text(docente, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Proyecto Asignado:", style = MaterialTheme.typography.labelLarge)
                Text(proyecto, style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Fecha de Asignación:", style = MaterialTheme.typography.labelLarge)
                Text("2026-09-05")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Volver")
        }
    }
}
