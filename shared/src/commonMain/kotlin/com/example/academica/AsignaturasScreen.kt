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
data class Asignatura(
    val id: String,
    val nombre: String,
    val codigo: String,
    val creditos: String = "3",
    val estado: String = "Activa",
    val programa: String = "General"
)

@Serializable
data class AsignaturasListResponse(val success: Boolean, val data: List<Asignatura> = emptyList())

@Serializable
data class CreateAsignaturaRequest(val nombre: String, val codigo: String, val creditos: Int = 3, val estado: String = "Activa")

@Serializable
data class UpdateAsignaturaRequest(val id: Int, val nombre: String, val codigo: String, val creditos: Int = 3, val estado: String = "Activa")

enum class AsignaturasState { LIST, REGISTER, EDIT, CONSULT }

@Composable
fun AsignaturasModule() {
    var currentState by remember { mutableStateOf(AsignaturasState.LIST) }
    var selectedAsig by remember { mutableStateOf<Asignatura?>(null) }

    val client = remember {
        HttpClient {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
    }

    AnimatedContent(targetState = currentState, label = "Asignaturas Anim") { state ->
        when (state) {
            AsignaturasState.LIST -> AsignaturasList(
                client = client,
                onRegister = { currentState = AsignaturasState.REGISTER },
                onEdit = { asig ->
                    selectedAsig = asig
                    currentState = AsignaturasState.EDIT
                },
                onConsult = { asig ->
                    selectedAsig = asig
                    currentState = AsignaturasState.CONSULT
                }
            )
            AsignaturasState.REGISTER -> AsignaturaForm(
                client = client,
                title = "Registrar Nueva Asignatura",
                isEditing = false,
                initialAsig = null,
                onBack = { currentState = AsignaturasState.LIST }
            )
            AsignaturasState.EDIT -> AsignaturaForm(
                client = client,
                title = "Editar Asignatura",
                isEditing = true,
                initialAsig = selectedAsig,
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
    client: HttpClient,
    onRegister: () -> Unit,
    onEdit: (Asignatura) -> Unit,
    onConsult: (Asignatura) -> Unit
) {
    val scope = rememberCoroutineScope()
    var searchQuery by remember { mutableStateOf("") }
    var asignaturasList by remember { mutableStateOf<List<Asignatura>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var mensajeError by remember { mutableStateOf("") }

    fun loadAsignaturas() {
        isLoading = true
        scope.launch {
            try {
                val response: AsignaturasListResponse = client.get("http://10.0.2.2/academica_api/asignaturas.php").body()
                if (response.success) {
                    asignaturasList = response.data
                }
            } catch (e: Exception) {
                mensajeError = "Error al conectar: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun deleteAsignatura(id: String) {
        scope.launch {
            try {
                val response: BaseResponse = client.delete("http://10.0.2.2/academica_api/asignaturas.php?id=$id").body()
                if (response.success) {
                    loadAsignaturas()
                } else {
                    mensajeError = response.mensaje
                }
            } catch (e: Exception) {
                mensajeError = "Error al eliminar: ${e.message}"
            }
        }
    }

    LaunchedEffect(Unit) { loadAsignaturas() }

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
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Buscar asignatura o código...") },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = onRegister, modifier = Modifier.fillMaxWidth()) {
            Text("Registrar Nueva Asignatura")
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
                val filteredList = asignaturasList.filter {
                    it.nombre.contains(searchQuery, ignoreCase = true) ||
                    it.codigo.contains(searchQuery, ignoreCase = true)
                }

                items(filteredList) { asig ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "${asig.nombre} (${asig.codigo})",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Créditos: ${asig.creditos} | Estado: ${asig.estado}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                IconButton(onClick = { onConsult(asig) }) {
                                    Icon(Icons.Default.Visibility, contentDescription = "Consultar", tint = MaterialTheme.colorScheme.primary)
                                }
                                IconButton(onClick = { onEdit(asig) }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.secondary)
                                }
                                IconButton(onClick = { deleteAsignatura(asig.id) }) {
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
fun AsignaturaForm(
    client: HttpClient,
    title: String,
    isEditing: Boolean,
    initialAsig: Asignatura?,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var nombre by remember { mutableStateOf(initialAsig?.nombre ?: "") }
    var codigo by remember { mutableStateOf(initialAsig?.codigo ?: "") }
    var creditos by remember { mutableStateOf(initialAsig?.creditos ?: "3") }
    var mensaje by remember { mutableStateOf("") }
    var esError by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it; mensaje = "" },
            label = { Text("Nombre de la Asignatura") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = codigo,
            onValueChange = { codigo = it; mensaje = "" },
            label = { Text("Código de Asignatura") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = creditos,
            onValueChange = { creditos = it; mensaje = "" },
            label = { Text("Créditos Académicos") },
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
            ) { Text("Cancelar") }
            Spacer(modifier = Modifier.width(12.dp))
            Button(
                onClick = {
                    if (nombre.isBlank() || codigo.isBlank()) {
                        mensaje = "Error: Nombre y Código son obligatorios."; esError = true
                    } else {
                        isSaving = true
                        scope.launch {
                            try {
                                val credsInt = creditos.toIntOrNull() ?: 3
                                val response: BaseResponse = if (isEditing) {
                                    client.put("http://10.0.2.2/academica_api/asignaturas.php") {
                                        contentType(ContentType.Application.Json)
                                        setBody(UpdateAsignaturaRequest(initialAsig!!.id.toInt(), nombre, codigo, credsInt))
                                    }.body()
                                } else {
                                    client.post("http://10.0.2.2/academica_api/asignaturas.php") {
                                        contentType(ContentType.Application.Json)
                                        setBody(CreateAsignaturaRequest(nombre, codigo, credsInt))
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
fun AsignaturaConsulta(asig: Asignatura?, onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Consulta de Asignatura", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))

        if (asig != null) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("ID del Sistema:", style = MaterialTheme.typography.labelLarge)
                    Text(asig.id, style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Nombre de la Asignatura:", style = MaterialTheme.typography.labelLarge)
                    Text(asig.nombre, style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Código Oficial:", style = MaterialTheme.typography.labelLarge)
                    Text(asig.codigo, style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Créditos:", style = MaterialTheme.typography.labelLarge)
                    Text("${asig.creditos} Créditos", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Estado:", style = MaterialTheme.typography.labelLarge)
                    Text(asig.estado, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Volver al Listado") }
    }
}
