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

enum class AsignaturasState { LIST, REGISTER, UPDATE, CONSULT }

@Composable
fun AsignaturasModule() {
    var currentState by remember { mutableStateOf(AsignaturasState.LIST) }
    var selectedAsig by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }

    AnimatedContent(targetState = currentState) { state ->
        when (state) {
            AsignaturasState.LIST -> AsignaturasList(
                searchQuery = searchQuery,
                onSearchChange = { searchQuery = it },
                onRegister = { currentState = AsignaturasState.REGISTER },
                onUpdate = { asig ->
                    selectedAsig = asig
                    currentState = AsignaturasState.UPDATE
                },
                onConsult = { asig ->
                    selectedAsig = asig
                    currentState = AsignaturasState.CONSULT
                }
            )
            AsignaturasState.REGISTER -> AsignaturaForm(
                title = "Registrar Nueva Asignatura",
                confirmText = "Registrar",
                successMessage = "Asignatura registrada con éxito",
                onBack = { currentState = AsignaturasState.LIST }
            )
            AsignaturasState.UPDATE -> AsignaturaForm(
                title = "Actualizar Asignatura: $selectedAsig",
                initialNombre = selectedAsig,
                confirmText = "Actualizar",
                successMessage = "Asignatura actualizada con éxito",
                onBack = { currentState = AsignaturasState.LIST }
            )
            AsignaturasState.CONSULT -> AsignaturaConsulta(
                asig = selectedAsig,
                onBack = { currentState = AsignaturasState.LIST }
            )
        }
    }
}

@Composable
fun AsignaturasList(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onRegister: () -> Unit,
    onUpdate: (String) -> Unit,
    onConsult: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Administración de Asignaturas",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Buscar asignatura activa...") },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = onRegister, modifier = Modifier.fillMaxWidth()) {
            Text("Registrar Nueva Asignatura")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Asignaturas Activas",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(listOf(
                "Cálculo Diferencial",
                "Programación I",
                "Física Mecánica",
                "Ingeniería de Software"
            ).filter { it.contains(searchQuery, ignoreCase = true) }) { asig ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = asig,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { onConsult(asig) }) {
                                Text("Consultar")
                            }
                            TextButton(onClick = { onUpdate(asig) }) {
                                Text("Actualizar")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AsignaturaForm(
    title: String,
    initialNombre: String = "",
    confirmText: String,
    successMessage: String,
    onBack: () -> Unit
) {
    var nombre by remember { mutableStateOf(initialNombre) }
    var codigo by remember { mutableStateOf("") }
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
            label = { Text("Nombre de la Asignatura") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        OutlinedTextField(
            value = codigo,
            onValueChange = { 
                codigo = it
                mensaje = ""
            },
            label = { Text("Código de Asignatura") },
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
                    if (nombre.isBlank() || codigo.isBlank()) {
                        mensaje = "Error: Complete los datos"
                        esError = true
                    } else {
                        mensaje = successMessage
                        esError = false
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(confirmText)
            }
        }
    }
}

@Composable
fun AsignaturaConsulta(asig: String, onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Consulta de Asignatura",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(24.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Nombre de la Asignatura:", style = MaterialTheme.typography.labelLarge)
                Text(asig, style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Créditos:", style = MaterialTheme.typography.labelLarge)
                Text("3 Créditos", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Estado:", style = MaterialTheme.typography.labelLarge)
                Text("Activa", color = MaterialTheme.colorScheme.primary)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Volver")
        }
    }
}
