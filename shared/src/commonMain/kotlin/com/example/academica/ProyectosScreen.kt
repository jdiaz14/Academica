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

enum class ProyectosState { LIST, REGISTER, EDIT, CONSULT }

@Composable
fun ProyectosModule() {
    var currentState by remember { mutableStateOf(ProyectosState.LIST) }
    var selectedProy by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }

    AnimatedContent(targetState = currentState) { state ->
        when (state) {
            ProyectosState.LIST -> ProyectosList(
                searchQuery = searchQuery,
                onSearchChange = { searchQuery = it },
                onRegister = { currentState = ProyectosState.REGISTER },
                onEdit = { proy ->
                    selectedProy = proy
                    currentState = ProyectosState.EDIT
                },
                onConsult = { proy ->
                    selectedProy = proy
                    currentState = ProyectosState.CONSULT
                }
            )
            ProyectosState.REGISTER -> ProyectoForm(
                title = "Registrar Proyecto Académico",
                onBack = { currentState = ProyectosState.LIST }
            )
            ProyectosState.EDIT -> ProyectoForm(
                title = "Editar Proyecto: $selectedProy",
                initialNombre = selectedProy,
                onBack = { currentState = ProyectosState.LIST }
            )
            ProyectosState.CONSULT -> ProyectoConsulta(
                proy = selectedProy,
                onBack = { currentState = ProyectosState.LIST }
            )
        }
    }
}

@Composable
fun ProyectosList(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onRegister: () -> Unit,
    onEdit: (String) -> Unit,
    onConsult: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Administración de Proyectos",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Buscar proyecto...") },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = onRegister, modifier = Modifier.fillMaxWidth()) {
            Text("Registrar Nuevo Proyecto")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Proyectos Registrados",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(listOf(
                "Proyecto IA v1 - Finalizado",
                "Desarrollo Web Académico - En Proceso",
                "Redes Neuronales Aplicadas - Planeación",
                "Gestión Académica KMP - En Desarrollo"
            ).filter { it.contains(searchQuery, ignoreCase = true) }) { proy ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = proy,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { onConsult(proy) }) {
                                Text("Consultar")
                            }
                            TextButton(onClick = { onEdit(proy) }) {
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
fun ProyectoForm(title: String, initialNombre: String = "", onBack: () -> Unit) {
    var nombre by remember { mutableStateOf(initialNombre) }
    var descripcion by remember { mutableStateOf("") }
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
            label = { Text("Nombre del Proyecto") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        OutlinedTextField(
            value = descripcion,
            onValueChange = { 
                descripcion = it
                mensaje = ""
            },
            label = { Text("Descripción Detallada") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
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
                    if (nombre.isBlank() || descripcion.isBlank()) {
                        mensaje = "Error: Faltan datos obligatorios"
                        esError = true
                    } else {
                        mensaje = "Proyecto guardado exitosamente"
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
fun ProyectoConsulta(proy: String, onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Detalles del Proyecto",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(24.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Nombre del Proyecto:", style = MaterialTheme.typography.labelLarge)
                Text(proy, style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Responsable:", style = MaterialTheme.typography.labelLarge)
                Text("Julian Diaz", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Fecha de Registro:", style = MaterialTheme.typography.labelLarge)
                Text("2026-09-05")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Volver")
        }
    }
}
