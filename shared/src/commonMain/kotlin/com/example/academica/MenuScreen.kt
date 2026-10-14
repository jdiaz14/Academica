package com.example.academica

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow

enum class TabItem(val title: String, val icon: ImageVector) {
    USUARIOS("Usuarios", Icons.Default.Person),
    PROGRAMAS("Programas", Icons.Default.School),
    ASIGNATURAS("Asignaturas", Icons.AutoMirrored.Filled.MenuBook),
    PROYECTOS("Proyectos", Icons.Default.Folder),
    EQUIPOS("Equipos", Icons.Default.Groups),
    DOCENTES("Docentes", Icons.Default.Badge)
}

class MenuScreen(private val usuario: UsuarioData) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        val availableTabs = remember(usuario.rol) {
            when (usuario.rol) {
                "Administrador" -> listOf(
                    TabItem.USUARIOS, TabItem.PROGRAMAS, TabItem.ASIGNATURAS, 
                    TabItem.PROYECTOS, TabItem.EQUIPOS, TabItem.DOCENTES
                )
                "Docente" -> listOf(
                    TabItem.PROYECTOS, TabItem.EQUIPOS, TabItem.DOCENTES
                )
                "Estudiante" -> listOf(
                    TabItem.PROYECTOS, TabItem.EQUIPOS
                )
                else -> emptyList()
            }
        }

        var selectedTabIndex by remember { mutableIntStateOf(0) }
        val selectedTab = availableTabs.getOrNull(selectedTabIndex) ?: availableTabs.firstOrNull()

        val (roleColor, roleBg) = when (usuario.rol) {
            "Administrador" -> Pair(Color(0xFFE05A10), Color(0xFFFFECE5))
            "Docente" -> Pair(Color(0xFF005691), Color(0xFFE8F0FE))
            else -> Pair(Color(0xFF2E6B27), Color(0xFFE6F4EA))
        }

        Scaffold(
            bottomBar = {
                if (availableTabs.isNotEmpty()) {
                    NavigationBar(
                        tonalElevation = 6.dp,
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        val fontSize = if (availableTabs.size > 4) 9.sp else 11.sp
                        val labelStyle = MaterialTheme.typography.labelSmall.copy(
                            fontSize = fontSize,
                            fontWeight = FontWeight.SemiBold
                        )

                        availableTabs.forEachIndexed { index, tab ->
                            NavigationBarItem(
                                selected = selectedTabIndex == index,
                                onClick = { selectedTabIndex = index },
                                label = {
                                    Text(
                                        text = tab.title,
                                        style = labelStyle,
                                        maxLines = 1,
                                        softWrap = false,
                                        overflow = TextOverflow.Clip
                                    )
                                },
                                icon = { Icon(tab.icon, contentDescription = tab.title) }
                            )
                        }
                    }
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Bar de Encabezado Superior (Top Bar Unificada)
                Surface(
                    tonalElevation = 3.dp,
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Contenedor Circular para el Logo
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            LogoAcademica(size = 38.dp, showText = false)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Info del Usuario y Rol
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = usuario.nombre_completo,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = roleBg
                                ) {
                                    Text(
                                        text = usuario.rol,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = roleColor,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "USB BOGOTÁ",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = { navigator.popUntilRoot() },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f))
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                contentDescription = "Salir",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Contenedor de Contenido
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) {
                    if (selectedTab != null) {
                        AnimatedContent(
                            targetState = selectedTab,
                            transitionSpec = {
                                (fadeIn(animationSpec = tween(220)) + slideInHorizontally(animationSpec = tween(220)) { it / 3 }) togetherWith
                                (fadeOut(animationSpec = tween(180)) + slideOutHorizontally(animationSpec = tween(180)) { -it / 3 })
                            },
                            label = "Cambio de Modulo"
                        ) { tab ->
                            when (tab) {
                                TabItem.USUARIOS -> UsuariosModule()
                                TabItem.PROGRAMAS -> ProgramasModule()
                                TabItem.ASIGNATURAS -> AsignaturasModule()
                                TabItem.PROYECTOS -> ProyectosModule()
                                TabItem.EQUIPOS -> EquiposModule()
                                TabItem.DOCENTES -> DocentesModule()
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No tiene permisos para acceder a ningún módulo.")
                        }
                    }
                }
            }
        }
    }
}
