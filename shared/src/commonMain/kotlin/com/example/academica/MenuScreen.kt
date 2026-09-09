package com.example.academica

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow

class MenuScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        var selectedItem by remember { mutableIntStateOf(0) }

        Scaffold(
            bottomBar = {
                NavigationBar(
                    tonalElevation = 8.dp
                ) {
                    val labelStyle = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp)

                    NavigationBarItem(
                        selected = selectedItem == 0,
                        onClick = { selectedItem = 0 },
                        label = { Text("Usuarios", style = labelStyle) },
                        icon = { Icon(Icons.Default.Person, contentDescription = "Usuarios") }
                    )
                    NavigationBarItem(
                        selected = selectedItem == 1,
                        onClick = { selectedItem = 1 },
                        label = { Text("Programas", style = labelStyle) },
                        icon = { Icon(Icons.Default.School, contentDescription = "Programas") }
                    )
                    NavigationBarItem(
                        selected = selectedItem == 2,
                        onClick = { selectedItem = 2 },
                        label = { Text("Asignaturas", style = labelStyle) },
                        icon = { Icon(Icons.Default.MenuBook, contentDescription = "Asignaturas") }
                    )
                    NavigationBarItem(
                        selected = selectedItem == 3,
                        onClick = { selectedItem = 3 },
                        label = { Text("Proyectos", style = labelStyle) },
                        icon = { Icon(Icons.Default.Folder, contentDescription = "Proyectos") }
                    )
                    NavigationBarItem(
                        selected = selectedItem == 4,
                        onClick = { selectedItem = 4 },
                        label = { Text("Equipos", style = labelStyle) },
                        icon = { Icon(Icons.Default.Groups, contentDescription = "Equipos") }
                    )
                    NavigationBarItem(
                        selected = selectedItem == 5,
                        onClick = { selectedItem = 5 },
                        label = { Text("Docentes", style = labelStyle) },
                        icon = { Icon(Icons.Default.Badge, contentDescription = "Docentes") }
                    )
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                // Cabecera con Identificación de Usuario (Guía)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Julian Andres Diaz Guevara",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Sesión Activa - Académica",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    Button(
                        onClick = {
                            // Cierre de sesión (Guía)
                            navigator.popUntilRoot()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    ) {
                        Text("Salir")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))

                // Contenido del Módulo con Animación Sencilla (Guía)
                AnimatedContent(
                    targetState = selectedItem,
                    transitionSpec = {
                        fadeIn().togetherWith(fadeOut())
                    },
                    modifier = Modifier.weight(1f)
                ) { index ->
                    when (index) {
                        0 -> UsuariosModule()
                        1 -> ProgramasModule()
                        2 -> AsignaturasModule()
                        3 -> ProyectosModule()
                        4 -> EquiposModule()
                        5 -> DocentesModule()
                    }
                }
            }
        }
    }
}
