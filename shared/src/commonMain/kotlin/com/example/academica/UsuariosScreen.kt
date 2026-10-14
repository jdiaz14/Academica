package com.example.academica

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

// --- MODELOS DE DATOS ---
@Serializable
data class Usuario(
    val id: String, 
    val nombre_completo: String, 
    val usuario: String, 
    val rol_id: String, 
    val rol: String
)

@Serializable
data class UsuariosListResponse(val success: Boolean, val data: List<Usuario> = emptyList())

@Serializable
data class BaseResponse(val success: Boolean, val mensaje: String)

@Serializable
data class CreateUsuarioRequest(val nombre_completo: String, val usuario: String, val password: String, val rol_id: Int)

@Serializable
data class UpdateUsuarioRequest(val id: Int, val nombre_completo: String, val usuario: String, val rol_id: Int, val password: String = "")


enum class UsuariosState { LIST, REGISTER, EDIT, CONSULT }

@Composable
fun UsuariosModule() {
    var currentState by remember { mutableStateOf(UsuariosState.LIST) }
    var selectedUser by remember { mutableStateOf<Usuario?>(null) }
    
    // Cliente HTTP para este módulo
    val client = remember {
        HttpClient {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
    }

    AnimatedContent(targetState = currentState, label = "Usuarios Anim") { state ->
        when (state) {
            UsuariosState.LIST -> UsuariosList(
                client = client,
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
                client = client,
                title = "Registrar Nuevo Usuario",
                isEditing = false,
                initialUser = null,
                onBack = { currentState = UsuariosState.LIST }
            )
            UsuariosState.EDIT -> UsuarioForm(
                client = client,
                title = "Editar Usuario",
                isEditing = true,
                initialUser = selectedUser,
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
    client: HttpClient,
    onRegister: () -> Unit,
    onEdit: (Usuario) -> Unit,
    onConsult: (Usuario) -> Unit
) {
    val scope = rememberCoroutineScope()
    var searchQuery by remember { mutableStateOf("") }
    var usuariosList by remember { mutableStateOf<List<Usuario>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var mensajeError by remember { mutableStateOf("") }

    // Función para cargar los usuarios desde XAMPP
    fun loadUsers() {
        isLoading = true
        scope.launch {
            try {
                val response: UsuariosListResponse = client.get("http://10.0.2.2/academica_api/usuarios.php").body()
                if (response.success) {
                    usuariosList = response.data
                }
            } catch (e: Exception) {
                mensajeError = "Error al conectar: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    // Función para eliminar un usuario
    fun deleteUser(id: String) {
        scope.launch {
            try {
                val response: BaseResponse = client.delete("http://10.0.2.2/academica_api/usuarios.php?id=$id").body()
                if (response.success) {
                    loadUsers() // Recargar la lista después de eliminar
                } else {
                    mensajeError = response.mensaje
                }
            } catch (e: Exception) {
                mensajeError = "Error al eliminar: ${e.message}"
            }
        }
    }

    // Cargar al iniciar la pantalla
    LaunchedEffect(Unit) {
        loadUsers()
    }

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
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Buscar por nombre o usuario...") },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onRegister,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Registrar Nuevo Usuario")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (mensajeError.isNotEmpty()) {
            Text(mensajeError, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                val filteredList = usuariosList.filter { 
                    it.nombre_completo.contains(searchQuery, ignoreCase = true) || 
                    it.usuario.contains(searchQuery, ignoreCase = true) 
                }
                
                items(filteredList) { user ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "${user.nombre_completo} (${user.usuario})",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Rol: ${user.rol}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                IconButton(onClick = { onConsult(user) }) {
                                    Icon(Icons.Default.Visibility, contentDescription = "Consultar", tint = MaterialTheme.colorScheme.primary)
                                }
                                IconButton(onClick = { onEdit(user) }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.secondary)
                                }
                                IconButton(onClick = { deleteUser(user.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                                }
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
    client: HttpClient,
    title: String, 
    isEditing: Boolean,
    initialUser: Usuario?,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    
    var nombre by remember { mutableStateOf(initialUser?.nombre_completo ?: "") }
    var usuario by remember { mutableStateOf(initialUser?.usuario ?: "") }
    var password by remember { mutableStateOf("") }
    var rolId by remember { mutableStateOf(initialUser?.rol_id ?: "1") } // 1: Admin, 2: Docente, 3: Estudiante
    
    var mensaje by remember { mutableStateOf("") }
    var esError by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it; mensaje = "" },
            label = { Text("Nombre Completo") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        OutlinedTextField(
            value = usuario,
            onValueChange = { usuario = it; mensaje = "" },
            label = { Text("Nombre de Usuario (Login)") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it; mensaje = "" },
            label = { Text(if (isEditing) "Nueva Contraseña (opcional)" else "Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Selector simple de Rol (Radio Buttons)
        Text("Rol del Usuario", style = MaterialTheme.typography.labelLarge)
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = rolId == "1", onClick = { rolId = "1" })
            Text("Administrador")
            Spacer(modifier = Modifier.width(8.dp))
            RadioButton(selected = rolId == "2", onClick = { rolId = "2" })
            Text("Docente")
            Spacer(modifier = Modifier.width(8.dp))
            RadioButton(selected = rolId == "3", onClick = { rolId = "3" })
            Text("Estudiante")
        }

        Spacer(modifier = Modifier.height(24.dp))

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
                    if (nombre.isBlank() || usuario.isBlank() || (!isEditing && password.isBlank())) {
                        mensaje = "Error: Complete todos los campos obligatorios."
                        esError = true
                    } else {
                        isSaving = true
                        scope.launch {
                            try {
                                val response: BaseResponse = if (isEditing) {
                                    client.put("http://10.0.2.2/academica_api/usuarios.php") {
                                        contentType(ContentType.Application.Json)
                                        setBody(UpdateUsuarioRequest(initialUser!!.id.toInt(), nombre, usuario, rolId.toInt(), password))
                                    }.body()
                                } else {
                                    client.post("http://10.0.2.2/academica_api/usuarios.php") {
                                        contentType(ContentType.Application.Json)
                                        setBody(CreateUsuarioRequest(nombre, usuario, password, rolId.toInt()))
                                    }.body()
                                }

                                mensaje = response.mensaje
                                esError = !response.success
                                if (response.success) {
                                    // Si guardó bien, volvemos a la lista después de un pequeño retraso
                                    kotlinx.coroutines.delay(1000)
                                    onBack()
                                }
                            } catch (e: Exception) {
                                mensaje = "Error de red: ${e.message}"
                                esError = true
                            } finally {
                                isSaving = false
                            }
                        }
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = !isSaving
            ) {
                Text(if (isSaving) "Guardando..." else "Guardar")
            }
        }
    }
}

@Composable
fun UsuarioConsulta(user: Usuario?, onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Consulta de Usuario",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(24.dp))

        if (user != null) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("ID del Sistema:", style = MaterialTheme.typography.labelLarge)
                    Text(user.id, style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text("Nombre Completo:", style = MaterialTheme.typography.labelLarge)
                    Text(user.nombre_completo, style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text("Nombre de Usuario (Login):", style = MaterialTheme.typography.labelLarge)
                    Text(user.usuario, style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text("Rol Asignado:", style = MaterialTheme.typography.labelLarge)
                    Text(user.rol, style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text("Estado:", style = MaterialTheme.typography.labelLarge)
                    Text("Activo y Registrado", color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Volver al Listado")
        }
    }
}
