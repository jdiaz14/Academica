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

enum class ProgramasState { LIST, REGISTER, EDIT, CONSULT }

@Composable
fun ProgramasModule() {
    var currentState by remember { mutableStateOf(ProgramasState.LIST) }
    var selectedProg by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }

    AnimatedContent(targetState = currentState) { state ->
        when (state) {
            ProgramasState.LIST -> ProgramasList(
                searchQuery = searchQuery,
                onSearchChange = { searchQuery = it },
                onRegister = { currentState = ProgramasState.REGISTER },
                onEdit = { prog -> 
                    selectedProg = prog
                    currentState = ProgramasState.EDIT 
                },
                onConsult = { prog ->
                    selectedProg = prog
                    currentState = ProgramasState.CONSULT
                }
            )
            ProgramasState.REGISTER -> ProgramaForm(
                title = "Registrar Programa Académico",
                onBack = { currentState = ProgramasState.LIST }
            )
            ProgramasState.EDIT -> ProgramaForm(
                title = "Editar Programa: $selectedProg",
                initialNombre = selectedProg,
                onBack = { currentState = ProgramasState.LIST }
            )
            ProgramasState.CONSULT -> ProgramaConsulta(
                prog = selectedProg,
                onBack = { currentState = ProgramasState.LIST }
            )
        }
    }
}

@Composable
fun ProgramasList(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onRegister: () -> Unit,
    onEdit: (String) -> Unit,
    onConsult: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Gestión de Programas",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Buscar programa académico...") },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = onRegister, modifier = Modifier.fillMaxWidth()) {
            Text("Registrar Nuevo Programa")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Oferta Académica",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(listOf(
                "Ingeniería de Sistemas",
                "Psicología",
                "Derecho",
                "Administración de Empresas"
            ).filter { it.contains(searchQuery, ignoreCase = true) }) { prog ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = prog,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { onConsult(prog) }) {
                                Text("Consultar")
                            }
                            TextButton(onClick = { onEdit(prog) }) {
                                Text("Editar")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProgramaForm(title: String, initialNombre: String = "", onBack: () -> Unit) {
    var nombre by remember { mutableStateOf(initialNombre) }
    var mensaje by remember { mutableStateOf("") }
    var esError by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = title,
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
            label = { Text("Nombre del Programa") },
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
                Text("Cerrar")
            }
            Spacer(modifier = Modifier.width(12.dp))
            Button(
                onClick = { 
                    if (nombre.isBlank()) {
                        mensaje = "Error: Nombre requerido"
                        esError = true
                    } else {
                        mensaje = "Cambios guardados exitosamente"
                        esError = false
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Guardar")
            }
        }
    }
}

@Composable
fun ProgramaConsulta(prog: String, onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Consulta de Programa",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(24.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Nombre del Programa:", style = MaterialTheme.typography.labelLarge)
                Text(prog, style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Facultad:", style = MaterialTheme.typography.labelLarge)
                Text("Facultad de Ingeniería y Ciencias Básicas", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Modalidad:", style = MaterialTheme.typography.labelLarge)
                Text("Presencial")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Volver")
        }
    }
}
