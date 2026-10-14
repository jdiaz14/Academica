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
data class Equipo(
    val id: String,
    val nombre: String,
    val integrantes: String = "Sin integrantes"
)

@Serializable
data class EquiposListResponse(val success: Boolean, val data: List<Equipo> = emptyList())

@Serializable
data class CreateEquipoRequest(val nombre: String, val integrantes_texto: String = "")

@Serializable
data class UpdateEquipoRequest(val id: Int, val nombre: String, val integrantes_texto: String = "")

enum class EquiposState { LIST, CREATE, EDIT, CONSULT }

@Composable
fun EquiposModule() {
    var currentState by remember { mutableStateOf(EquiposState.LIST) }
    var selectedEquipo by remember { mutableStateOf<Equipo?>(null) }

    val client = remember {
        HttpClient {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
    }

    AnimatedContent(targetState = currentState, label = "Equipos Anim") { state ->
        when (state) {
            EquiposState.LIST -> EquiposList(
                client = client,
                onCreate = { currentState = EquiposState.CREATE },
                onEdit = { equipo ->
                    selectedEquipo = equipo
                    currentState = EquiposState.EDIT
                },
                onConsult = { equipo ->
                    selectedEquipo = equipo
                    currentState = EquiposState.CONSULT
                }
            )
            EquiposState.CREATE -> EquipoForm(
                client = client,
                title = "Creación de Equipo Académico",
                isEditing = false,
                initialEquipo = null,
                onBack = { currentState = EquiposState.LIST }
            )
            EquiposState.EDIT -> EquipoForm(
                client = client,
                title = "Editar Equipo Académico",
                isEditing = true,
                initialEquipo = selectedEquipo,
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
    client: HttpClient,
    onCreate: () -> Unit,
    onEdit: (Equipo) -> Unit,
    onConsult: (Equipo) -> Unit
) {
    val scope = rememberCoroutineScope()
    var searchQuery by remember { mutableStateOf("") }
    var equiposList by remember { mutableStateOf<List<Equipo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var mensajeError by remember { mutableStateOf("") }

    fun loadEquipos() {
        isLoading = true
        scope.launch {
            try {
                val response: EquiposListResponse = client.get("http://10.0.2.2/academica_api/equipos.php").body()
                if (response.success) {
                    equiposList = response.data
                }
            } catch (e: Exception) {
                mensajeError = "Error al conectar: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun deleteEquipo(id: String) {
        scope.launch {
            try {
                val response: BaseResponse = client.delete("http://10.0.2.2/academica_api/equipos.php?id=$id").body()
                if (response.success) {
                    loadEquipos()
                } else {
                    mensajeError = response.mensaje
                }
            } catch (e: Exception) {
                mensajeError = "Error al eliminar: ${e.message}"
            }
        }
    }

    LaunchedEffect(Unit) { loadEquipos() }

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
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Buscar integrante o equipo...") },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = onCreate, modifier = Modifier.fillMaxWidth()) {
            Text("Crear Nuevo Equipo de Trabajo")
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
                val filteredList = equiposList.filter {
                    it.nombre.contains(searchQuery, ignoreCase = true) ||
                    it.integrantes.contains(searchQuery, ignoreCase = true)
                }

                items(filteredList) { equipo ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = equipo.nombre,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Integrantes: ${equipo.integrantes}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                IconButton(onClick = { onConsult(equipo) }) {
                                    Icon(Icons.Default.Visibility, contentDescription = "Consultar Integrantes", tint = MaterialTheme.colorScheme.primary)
                                }
                                IconButton(onClick = { onEdit(equipo) }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Editar Equipo", tint = MaterialTheme.colorScheme.secondary)
                                }
                                IconButton(onClick = { deleteEquipo(equipo.id) }) {
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
fun EquipoForm(
    client: HttpClient,
    title: String,
    isEditing: Boolean,
    initialEquipo: Equipo?,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var nombre by remember { mutableStateOf(initialEquipo?.nombre ?: "") }
    var integrantes by remember { mutableStateOf(if (initialEquipo?.integrantes != "Sin integrantes") initialEquipo?.integrantes ?: "" else "") }
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
            label = { Text("Nombre del Equipo") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = integrantes,
            onValueChange = { integrantes = it; mensaje = "" },
            label = { Text("Nombres de Integrantes (separados por coma)") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2
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
            ) {
                Text("Cancelar")
            }
            Spacer(modifier = Modifier.width(12.dp))
            Button(
                onClick = {
                    if (nombre.isBlank()) {
                        mensaje = "Error: El nombre del equipo es obligatorio."
                        esError = true
                    } else {
                        isSaving = true
                        scope.launch {
                            try {
                                val response: BaseResponse = if (isEditing) {
                                    client.put("http://10.0.2.2/academica_api/equipos.php") {
                                        contentType(ContentType.Application.Json)
                                        setBody(UpdateEquipoRequest(initialEquipo!!.id.toInt(), nombre, integrantes))
                                    }.body()
                                } else {
                                    client.post("http://10.0.2.2/academica_api/equipos.php") {
                                        contentType(ContentType.Application.Json)
                                        setBody(CreateEquipoRequest(nombre, integrantes))
                                    }.body()
                                }

                                mensaje = response.mensaje
                                esError = !response.success
                                if (response.success) {
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
fun EquipoConsulta(equipo: Equipo?, onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Detalles del Equipo", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))

        if (equipo != null) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("ID del Sistema:", style = MaterialTheme.typography.labelLarge)
                    Text(equipo.id, style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Equipo:", style = MaterialTheme.typography.labelLarge)
                    Text(equipo.nombre, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Integrantes:", style = MaterialTheme.typography.labelLarge)
                    Text(equipo.integrantes, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Volver al Listado")
        }
    }
}
