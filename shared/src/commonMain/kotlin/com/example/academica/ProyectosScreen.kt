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
data class Proyecto(
    val id: String,
    val nombre: String,
    val descripcion: String = "",
    val fecha_registro: String = "",
    val asignatura: String = "General"
)

@Serializable
data class ProyectosListResponse(val success: Boolean, val data: List<Proyecto> = emptyList())

@Serializable
data class CreateProyectoRequest(val nombre: String, val descripcion: String)

@Serializable
data class UpdateProyectoRequest(val id: Int, val nombre: String, val descripcion: String)

enum class ProyectosState { LIST, REGISTER, EDIT, CONSULT }

@Composable
fun ProyectosModule() {
    var currentState by remember { mutableStateOf(ProyectosState.LIST) }
    var selectedProy by remember { mutableStateOf<Proyecto?>(null) }

    val client = remember {
        HttpClient {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
    }

    AnimatedContent(targetState = currentState, label = "Proyectos Anim") { state ->
        when (state) {
            ProyectosState.LIST -> ProyectosList(
                client = client,
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
                client = client,
                title = "Registrar Nuevo Proyecto Académico",
                isEditing = false,
                initialProy = null,
                onBack = { currentState = ProyectosState.LIST }
            )
            ProyectosState.EDIT -> ProyectoForm(
                client = client,
                title = "Editar Proyecto",
                isEditing = true,
                initialProy = selectedProy,
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
    client: HttpClient,
    onRegister: () -> Unit,
    onEdit: (Proyecto) -> Unit,
    onConsult: (Proyecto) -> Unit
) {
    val scope = rememberCoroutineScope()
    var searchQuery by remember { mutableStateOf("") }
    var proyectosList by remember { mutableStateOf<List<Proyecto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var mensajeError by remember { mutableStateOf("") }

    fun loadProyectos() {
        isLoading = true
        scope.launch {
            try {
                val response: ProyectosListResponse = client.get("http://10.0.2.2/academica_api/proyectos.php").body()
                if (response.success) {
                    proyectosList = response.data
                }
            } catch (e: Exception) {
                mensajeError = "Error al conectar: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun deleteProyecto(id: String) {
        scope.launch {
            try {
                val response: BaseResponse = client.delete("http://10.0.2.2/academica_api/proyectos.php?id=$id").body()
                if (response.success) {
                    loadProyectos()
                } else {
                    mensajeError = response.mensaje
                }
            } catch (e: Exception) {
                mensajeError = "Error al eliminar: ${e.message}"
            }
        }
    }

    LaunchedEffect(Unit) { loadProyectos() }

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
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Buscar proyecto...") },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = onRegister, modifier = Modifier.fillMaxWidth()) {
            Text("Registrar Nuevo Proyecto")
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
                val filteredList = proyectosList.filter {
                    it.nombre.contains(searchQuery, ignoreCase = true) ||
                    it.descripcion.contains(searchQuery, ignoreCase = true)
                }

                items(filteredList) { proy ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = proy.nombre,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                            if (proy.descripcion.isNotEmpty()) {
                                Text(
                                    text = proy.descripcion,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.secondary,
                                    maxLines = 2
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                IconButton(onClick = { onConsult(proy) }) {
                                    Icon(Icons.Default.Visibility, contentDescription = "Consultar", tint = MaterialTheme.colorScheme.primary)
                                }
                                IconButton(onClick = { onEdit(proy) }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.secondary)
                                }
                                IconButton(onClick = { deleteProyecto(proy.id) }) {
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
fun ProyectoForm(
    client: HttpClient,
    title: String,
    isEditing: Boolean,
    initialProy: Proyecto?,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var nombre by remember { mutableStateOf(initialProy?.nombre ?: "") }
    var descripcion by remember { mutableStateOf(initialProy?.descripcion ?: "") }
    var mensaje by remember { mutableStateOf("") }
    var esError by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it; mensaje = "" },
            label = { Text("Nombre del Proyecto") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = descripcion,
            onValueChange = { descripcion = it; mensaje = "" },
            label = { Text("Descripción Detallada") },
            modifier = Modifier.fillMaxWidth().height(120.dp),
            maxLines = 4
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (mensaje.isNotEmpty()) {
            Text(mensaje, color = if (esError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(16.dp))
        }

        Row(modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = onBack,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
            ) { Text("Cancelar") }
            Spacer(modifier = Modifier.width(12.dp))
            Button(
                onClick = {
                    if (nombre.isBlank()) {
                        mensaje = "Error: El nombre es obligatorio."; esError = true
                    } else {
                        isSaving = true
                        scope.launch {
                            try {
                                val response: BaseResponse = if (isEditing) {
                                    client.put("http://10.0.2.2/academica_api/proyectos.php") {
                                        contentType(ContentType.Application.Json)
                                        setBody(UpdateProyectoRequest(initialProy!!.id.toInt(), nombre, descripcion))
                                    }.body()
                                } else {
                                    client.post("http://10.0.2.2/academica_api/proyectos.php") {
                                        contentType(ContentType.Application.Json)
                                        setBody(CreateProyectoRequest(nombre, descripcion))
                                    }.body()
                                }

                                mensaje = response.mensaje; esError = !response.success
                                if (response.success) { kotlinx.coroutines.delay(1000); onBack() }
                            } catch (e: Exception) {
                                mensaje = "Error de red: ${e.message}"; esError = true
                            } finally {
                                isSaving = false
                            }
                        }
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = !isSaving
            ) { Text(if (isSaving) "Guardando..." else "Guardar") }
        }
    }
}

@Composable
fun ProyectoConsulta(proy: Proyecto?, onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Detalles del Proyecto", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))

        if (proy != null) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("ID del Sistema:", style = MaterialTheme.typography.labelLarge)
                    Text(proy.id, style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Nombre del Proyecto:", style = MaterialTheme.typography.labelLarge)
                    Text(proy.nombre, style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Descripción:", style = MaterialTheme.typography.labelLarge)
                    Text(if (proy.descripcion.isNotEmpty()) proy.descripcion else "Sin descripción", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Fecha de Registro:", style = MaterialTheme.typography.labelLarge)
                    Text(proy.fecha_registro, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Volver al Listado") }
    }
}
