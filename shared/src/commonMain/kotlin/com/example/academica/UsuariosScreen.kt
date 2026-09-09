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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

enum class UsuariosState { LIST, REGISTER, EDIT, CONSULT }

@Composable
fun UsuariosModule() {
    var currentState by remember { mutableStateOf(UsuariosState.LIST) }
    var selectedUser by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }

    AnimatedContent(targetState = currentState) { state ->
        when (state) {
            UsuariosState.LIST -> UsuariosList(
                searchQuery = searchQuery,
                onSearchChange = { searchQuery = it },
                onRegister = { currentState = UsuariosState.REGISTER },
                onEdit = { user -> 
                    selectedUser = user
                    currentState = UsuariosState.EDIT 
                },
                onConsult = { user ->
                    selectedUser = user
                    currentState = UsuariosState.CONSULT
                }
            )
            UsuariosState.REGISTER -> UsuarioForm(
                title = "Registrar Nuevo Usuario",
                onBack = { currentState = UsuariosState.LIST }
            )
            UsuariosState.EDIT -> UsuarioForm(
                title = "Editar Usuario: $selectedUser",
                initialNombre = selectedUser.split(" - ").firstOrNull() ?: "",
                initialRol = selectedUser.split(" - ").lastOrNull() ?: "",
                onBack = { currentState = UsuariosState.LIST }
            )
            UsuariosState.CONSULT -> UsuarioConsulta(
                user = selectedUser,
                onBack = { currentState = UsuariosState.LIST }
            )
        }
    }
}

@Composable
fun UsuariosList(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onRegister: () -> Unit,
    onEdit: (String) -> Unit,
    onConsult: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Administración de Usuarios",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Buscar por nombre o rol...") },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onRegister,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Registrar Nuevo Usuario")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Listado de Usuarios",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(listOf(
                "Julian Diaz - Administrador",
                "Carlos Pérez - Docente Tutor",
                "Maria Lopez - Estudiante",
                "Andrés García - Evaluador"
            ).filter { it.contains(searchQuery, ignoreCase = true) }) { user ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = user,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { onConsult(user) }) {
                                Text("Consultar")
                            }
                            TextButton(onClick = { onEdit(user) }) {
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
fun UsuarioForm(
    title: String, 
    initialNombre: String = "", 
    initialRol: String = "",
    onBack: () -> Unit
) {
    var nombre by remember { mutableStateOf(initialNombre) }
    var rol by remember { mutableStateOf(initialRol) }
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
            label = { Text("Nombre Completo") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        OutlinedTextField(
            value = rol,
            onValueChange = { 
                rol = it
                mensaje = ""
            },
            label = { Text("Rol (Administrador, Docente, Estudiante)") },
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
                    if (nombre.isBlank() || rol.isBlank()) {
                        mensaje = "Error: Complete todos los campos"
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
fun UsuarioConsulta(user: String, onBack: () -> Unit) {
    val partes = user.split(" - ")
    val nombre = partes.firstOrNull() ?: ""
    val rol = partes.lastOrNull() ?: ""

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Consulta de Usuario",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(24.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Nombre:", style = MaterialTheme.typography.labelLarge)
                Text(nombre, style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Rol Asignado:", style = MaterialTheme.typography.labelLarge)
                Text(rol, style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Estado:", style = MaterialTheme.typography.labelLarge)
                Text("Activo", color = MaterialTheme.colorScheme.primary)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Volver al Listado")
        }
    }
}
