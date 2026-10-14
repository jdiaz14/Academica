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
data class DocenteTutor(
    val id: String,
    val docente: String,
    val proyecto: String,
    val fecha_asignacion: String = ""
)

@Serializable
data class DocentesListResponse(val success: Boolean, val data: List<DocenteTutor> = emptyList())

@Serializable
data class CreateAsignacionRequest(val docente: String, val proyecto: String)

@Serializable
data class UpdateAsignacionRequest(val id: Int, val docente: String, val proyecto: String)

enum class DocentesState { LIST, ASSIGN, EDIT, CONSULT }

@Composable
fun DocentesModule() {
    var currentState by remember { mutableStateOf(DocentesState.LIST) }
    var selectedAsign by remember { mutableStateOf<DocenteTutor?>(null) }

    val client = remember {
        HttpClient {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
    }

    AnimatedContent(targetState = currentState, label = "Docentes Anim") { state ->
        when (state) {
            DocentesState.LIST -> DocentesList(
                client = client,
                onAssign = { currentState = DocentesState.ASSIGN },
                onEdit = { asign ->
                    selectedAsign = asign
                    currentState = DocentesState.EDIT
                },
                onConsult = { asign ->
                    selectedAsign = asign
                    currentState = DocentesState.CONSULT
                }
            )
            DocentesState.ASSIGN -> AsignacionForm(
                client = client,
                title = "Nueva Asignación de Tutor",
                isEditing = false,
                initialAsign = null,
                onBack = { currentState = DocentesState.LIST }
            )
            DocentesState.EDIT -> AsignacionForm(
                client = client,
                title = "Editar Asignación de Tutor",
                isEditing = true,
                initialAsign = selectedAsign,
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
    client: HttpClient,
    onAssign: () -> Unit,
    onEdit: (DocenteTutor) -> Unit,
    onConsult: (DocenteTutor) -> Unit
) {
    val scope = rememberCoroutineScope()
    var searchQuery by remember { mutableStateOf("") }
    var docentesList by remember { mutableStateOf<List<DocenteTutor>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var mensajeError by remember { mutableStateOf("") }

    fun loadDocentes() {
        isLoading = true
        scope.launch {
            try {
                val response: DocentesListResponse = client.get("http://10.0.2.2/academica_api/docentes.php").body()
                if (response.success) {
                    docentesList = response.data
                }
            } catch (e: Exception) {
                mensajeError = "Error al conectar: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun deleteAsignacion(id: String) {
        scope.launch {
            try {
                val response: BaseResponse = client.delete("http://10.0.2.2/academica_api/docentes.php?id=$id").body()
                if (response.success) {
                    loadDocentes()
                } else {
                    mensajeError = response.mensaje
                }
            } catch (e: Exception) {
                mensajeError = "Error al eliminar: ${e.message}"
            }
        }
    }

    LaunchedEffect(Unit) { loadDocentes() }

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
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Buscar docente o proyecto...") },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = onAssign, modifier = Modifier.fillMaxWidth()) {
            Text("Nueva Asignación de Tutor")
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
                val filteredList = docentesList.filter {
                    it.docente.contains(searchQuery, ignoreCase = true) ||
                    it.proyecto.contains(searchQuery, ignoreCase = true)
                }

                items(filteredList) { asign ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "${asign.docente} -> ${asign.proyecto}",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Asignado el: ${asign.fecha_asignacion}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                IconButton(onClick = { onConsult(asign) }) {
                                    Icon(Icons.Default.Visibility, contentDescription = "Consultar Detalles", tint = MaterialTheme.colorScheme.primary)
                                }
                                IconButton(onClick = { onEdit(asign) }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Editar Asignación", tint = MaterialTheme.colorScheme.secondary)
                                }
                                IconButton(onClick = { deleteAsignacion(asign.id) }) {
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
fun AsignacionForm(
    client: HttpClient,
    title: String,
    isEditing: Boolean,
    initialAsign: DocenteTutor?,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var docente by remember { mutableStateOf(initialAsign?.docente ?: "") }
    var proyecto by remember { mutableStateOf(initialAsign?.proyecto ?: "") }
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
            value = docente,
            onValueChange = { docente = it; mensaje = "" },
            label = { Text("Nombre del Docente Tutor") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = proyecto,
            onValueChange = { proyecto = it; mensaje = "" },
            label = { Text("Nombre del Proyecto Asignado") },
            modifier = Modifier.fillMaxWidth()
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
                    if (docente.isBlank() || proyecto.isBlank()) {
                        mensaje = "Error: Complete docente y proyecto."
                        esError = true
                    } else {
                        isSaving = true
                        scope.launch {
                            try {
                                val response: BaseResponse = if (isEditing) {
                                    client.put("http://10.0.2.2/academica_api/docentes.php") {
                                        contentType(ContentType.Application.Json)
                                        setBody(UpdateAsignacionRequest(initialAsign!!.id.toInt(), docente, proyecto))
                                    }.body()
                                } else {
                                    client.post("http://10.0.2.2/academica_api/docentes.php") {
                                        contentType(ContentType.Application.Json)
                                        setBody(CreateAsignacionRequest(docente, proyecto))
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
fun AsignacionConsulta(asign: DocenteTutor?, onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Detalles de Asignación", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))

        if (asign != null) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("ID de Asignación:", style = MaterialTheme.typography.labelLarge)
                    Text(asign.id, style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Docente Tutor:", style = MaterialTheme.typography.labelLarge)
                    Text(asign.docente, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Proyecto Asignado:", style = MaterialTheme.typography.labelLarge)
                    Text(asign.proyecto, style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Fecha de Asignación:", style = MaterialTheme.typography.labelLarge)
                    Text(asign.fecha_asignacion, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Volver al Listado")
        }
    }
}
