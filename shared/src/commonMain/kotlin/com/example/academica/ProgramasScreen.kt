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
data class Programa(
    val id: String, 
    val nombre: String, 
    val descripcion: String
)

@Serializable
data class ProgramasListResponse(val success: Boolean, val data: List<Programa> = emptyList())

@Serializable
data class CreateProgramaRequest(val nombre: String, val descripcion: String)

@Serializable
data class UpdateProgramaRequest(val id: Int, val nombre: String, val descripcion: String)

enum class ProgramasState { LIST, REGISTER, EDIT, CONSULT }

@Composable
fun ProgramasModule() {
    var currentState by remember { mutableStateOf(ProgramasState.LIST) }
    var selectedPrograma by remember { mutableStateOf<Programa?>(null) }
    
    val client = remember {
        HttpClient {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
    }

    AnimatedContent(targetState = currentState, label = "Programas Anim") { state ->
        when (state) {
            ProgramasState.LIST -> ProgramasList(
                client = client,
                onRegister = { currentState = ProgramasState.REGISTER },
                onEdit = { programa -> 
                    selectedPrograma = programa
                    currentState = ProgramasState.EDIT 
                },
                onConsult = { programa ->
                    selectedPrograma = programa
                    currentState = ProgramasState.CONSULT
                }
            )
            ProgramasState.REGISTER -> ProgramaForm(
                client = client,
                title = "Registrar Nuevo Programa",
                isEditing = false,
                initialPrograma = null,
                onBack = { currentState = ProgramasState.LIST }
            )
            ProgramasState.EDIT -> ProgramaForm(
                client = client,
                title = "Editar Programa",
                isEditing = true,
                initialPrograma = selectedPrograma,
                onBack = { currentState = ProgramasState.LIST }
            )
            ProgramasState.CONSULT -> ProgramaConsulta(
                programa = selectedPrograma,
                onBack = { currentState = ProgramasState.LIST }
            )
        }
    }
}

@Composable
fun ProgramasList(
    client: HttpClient,
    onRegister: () -> Unit,
    onEdit: (Programa) -> Unit,
    onConsult: (Programa) -> Unit
) {
    val scope = rememberCoroutineScope()
    var searchQuery by remember { mutableStateOf("") }
    var programasList by remember { mutableStateOf<List<Programa>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var mensajeError by remember { mutableStateOf("") }

    fun loadProgramas() {
        isLoading = true
        scope.launch {
            try {
                val response: ProgramasListResponse = client.get("http://10.0.2.2/academica_api/programas.php").body()
                if (response.success) {
                    programasList = response.data
                }
            } catch (e: Exception) {
                mensajeError = "Error al conectar: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun deletePrograma(id: String) {
        scope.launch {
            try {
                val response: BaseResponse = client.delete("http://10.0.2.2/academica_api/programas.php?id=$id").body()
                if (response.success) {
                    loadProgramas()
                } else {
                    mensajeError = response.mensaje
                }
            } catch (e: Exception) {
                mensajeError = "Error al eliminar: ${e.message}"
            }
        }
    }

    LaunchedEffect(Unit) { loadProgramas() }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Administración de Programas",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Buscar programa...") },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = onRegister, modifier = Modifier.fillMaxWidth()) {
            Text("Registrar Nuevo Programa")
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
                val filteredList = programasList.filter { 
                    it.nombre.contains(searchQuery, ignoreCase = true) 
                }
                
                items(filteredList) { programa ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = programa.nombre,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                IconButton(onClick = { onConsult(programa) }) {
                                    Icon(Icons.Default.Visibility, contentDescription = "Consultar", tint = MaterialTheme.colorScheme.primary)
                                }
                                IconButton(onClick = { onEdit(programa) }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.secondary)
                                }
                                IconButton(onClick = { deletePrograma(programa.id) }) {
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
fun ProgramaForm(
    client: HttpClient,
    title: String, 
    isEditing: Boolean,
    initialPrograma: Programa?,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var nombre by remember { mutableStateOf(initialPrograma?.nombre ?: "") }
    var descripcion by remember { mutableStateOf(initialPrograma?.descripcion ?: "") }
    var mensaje by remember { mutableStateOf("") }
    var esError by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it; mensaje = "" },
            label = { Text("Nombre del Programa") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = descripcion,
            onValueChange = { descripcion = it; mensaje = "" },
            label = { Text("Descripción") },
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
                                    client.put("http://10.0.2.2/academica_api/programas.php") {
                                        contentType(ContentType.Application.Json)
                                        setBody(UpdateProgramaRequest(initialPrograma!!.id.toInt(), nombre, descripcion))
                                    }.body()
                                } else {
                                    client.post("http://10.0.2.2/academica_api/programas.php") {
                                        contentType(ContentType.Application.Json)
                                        setBody(CreateProgramaRequest(nombre, descripcion))
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
fun ProgramaConsulta(programa: Programa?, onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Consulta de Programa", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))

        if (programa != null) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("ID del Sistema:", style = MaterialTheme.typography.labelLarge)
                    Text(programa.id, style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Nombre:", style = MaterialTheme.typography.labelLarge)
                    Text(programa.nombre, style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Descripción:", style = MaterialTheme.typography.labelLarge)
                    Text(programa.descripcion, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Volver al Listado") }
    }
}
