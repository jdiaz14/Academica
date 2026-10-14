# UNIVERSIDAD DE SAN BUENAVENTURA - SECCIONAL BOGOTÁ
## FACULTAD DE INGENIERÍA - INGENIERÍA DE SISTEMAS
### DOCUMENTO DE ENTREGA SEGUNDO PARCIAL - DESARROLLO DE APPS MÓVILES

* **Nombre del Proyecto:** Académica - Sistema de Gestión de Proyectos Integradores (SIGAPI)
* **Estudiante:** Julián Andrés Díaz Guevara
* **Docente:** Dr. Yamil Buenaño Palacios
* **Asignatura:** Desarrollo de Apps Móviles
* **Fecha de Entrega:** 09 de octubre de 2026
* **Ciudad:** Bogotá D.C., Colombia

---

## 1. ALCANCE Y DOCUMENTACIÓN TÉCNICA DEL PROYECTO

### 1.1 Descripción y Formulación del Problema

#### Contextualización
En las instituciones de educación superior, los proyectos integradores constituyen una estrategia pedagógica fundamental que fortalece el aprendizaje basado en proyectos, la interdisciplinariedad y la aplicación práctica de los conocimientos adquiridos por los estudiantes. Su desarrollo requiere la participación coordinada de estudiantes, docentes tutores y directivos académicos para planificar, realizar seguimiento, evaluar y documentar cada iniciativa.

#### Formulación del Problema
Actualmente, en muchas facultades la información sobre programas académicos, asignaturas, proyectos, equipos de trabajo y asignación de tutores se gestiona mediante herramientas dispersas o procedimientos manuales. Esta falta de centralización genera:
1. Duplicidad e inconsistencia de la información académica.
2. Dificultad para consultar la conformación real de equipos e integrantes.
3. Ausencia de trazabilidad en las asignaciones de docentes tutores a proyectos.
4. Alta carga administrativa para docentes y directivos.

#### Pregunta Problema
> **¿Cómo construir una aplicación móvil multiplataforma que permita gestionar y administrar de manera eficiente la información académica, proyectos integradores, equipos de trabajo y asignación de tutores en la USBBOG, garantizando persistencia en una base de datos externa y control de acceso basado en roles?**

---

### 1.2 Especificación de los Requerimientos del Sistema

#### 1.2.1 Requerimientos Funcionales (RF)

| Código | Requerimiento | Descripción Detallada |
| :---: | :--- | :--- |
| **RF01** | **Pantalla de Inicio (Splash)** | Iniciar con una pantalla de bienvenida que presente el nombre de la App (*Académica*), logo oficial, datos del desarrollador (*Julián Andrés Díaz Guevara*), asignatura (*Desarrollo de Apps Móviles*) y botón de navegación al Login. |
| **RF02** | **Control de Acceso (Login)** | Permitir el ingreso mediante usuario y contraseña, validando contra la base de datos MySQL en XAMPP. |
| **RF03** | **Visualización de Sesión Activa** | Mostrar en todo momento el **Nombre Completo** y el **Rol activo** del usuario autenticado en la barra superior del Dashboard. |
| **RF04** | **Cierre de Sesión** | Permitir al usuario cerrar la sesión de forma segura retornando al inicio. |
| **RF05** | **Gestión de Usuarios (CRUD)** | Permitir al Administrador Registrar, Listar/Buscar, Editar, Eliminar y Consultar usuarios del sistema con asignación de roles. |
| **RF06** | **Gestión de Programas (CRUD)** | Permitir Registrar, Listar/Buscar, Editar, Eliminar y Consultar programas académicos. |
| **RF07** | **Gestión de Asignaturas (CRUD)** | Permitir Registrar, Listar/Buscar, Editar, Eliminar y Consultar asignaturas, sus códigos oficiales y créditos. |
| **RF08** | **Gestión de Proyectos (CRUD)** | Permitir Registrar, Listar/Buscar, Editar, Eliminar y Consultar proyectos integradores. |
| **RF09** | **Gestión de Equipos (CRUD)** | Crear equipos de trabajo, asociar integrantes estudiantes y consultar los integrantes vinculados. |
| **RF10** | **Asignación de Docentes (CRUD)** | Registrar, consultar, editar y eliminar la asignación de docentes tutores a proyectos académicos. |
| **RF11** | **Control de Acceso por Roles (RBAC)** | Adaptar el menú de navegación dinámicamente según el perfil logueado (Administrador, Docente, Estudiante). |
| **RF12** | **Validación de Formularios** | Controlar campos obligatorios, validar tipos de datos e informar sobre errores o confirmaciones de guardado. |

---

#### 1.2.2 Requerimientos No Funcionales (RNF)

| Código | Tipo | Descripción |
| :---: | :--- | :--- |
| **RNF01** | **Usabilidad** | Interfaz limpia basada en **Material Design 3**, clara y adaptada a la paleta institucional de la Universidad de San Buenaventura. |
| **RNF02** | **Seguridad** | Encriptación de contraseñas mediante **BCrypt (`password_hash`)** en MySQL y autenticación PDO en PHP. |
| **RNF03** | **Multiplataforma** | Desarrollo en **Kotlin Multiplatform (KMP)** con UI declarativa compartida en **Compose Multiplatform** para Android e iOS. |
| **RNF04** | **Persistencia Externa** | Conexión cliente-servidor asíncrona usando **Ktor Client** consumiendo servicios RESTful PHP desplegados en **MySQL/XAMPP**. |
| **RNF05** | **Rendimiento** | Transiciones suaves entre pantallas (`AnimatedContent`), respuestas en milisegundos y carga diferida en listas (`LazyColumn`). |

---

### 1.3 Especificación de las Acciones del Alcance de la Aplicación

El alcance técnico del Segundo Parcial comprende:
1. **Pantalla de Inicio (Splash Screen):** Presentación institucional completa.
2. **Módulo de Autenticación:** Verificación de credenciales en tiempo real contra MySQL (`login.php`).
3. **Dashboard e Identificación:** Muestra permanente del usuario en sesión (`UsuarioData`).
4. **Cobertura 100% CRUD (Crear, Leer/Buscar, Actualizar, Eliminar, Consultar):**
   - Módulo de Usuarios
   - Módulo de Programas Académicos
   - Módulo de Asignaturas
   - Módulo de Proyectos Integradores
   - Módulo de Equipos de Trabajo e Integrantes
   - Módulo de Asignación de Docentes Tutores
5. **Servidor Backend y Base de Datos Relacional Externa:** Despliegue en XAMPP local (`http://localhost/academica_api/`).

---

### 1.4 Implementación del Modelo Relacional del Sistema

El sistema implementa una base de datos relacional normalizada (`academica_db`) desplegada en MySQL/XAMPP con 8 tablas e integridad referencial (claves foráneas con restricciones en cascada).

#### Diccionario de Datos

1. **`roles`**: Almacena los perfiles del sistema (`1: Administrador`, `2: Docente`, `3: Estudiante`).
2. **`usuarios`**: Almacena `id`, `nombre_completo`, `usuario`, `password` (encriptada en BCrypt) y `rol_id` (FK ➔ `roles.id`).
3. **`programas`**: Almacena `id`, `nombre` y `descripcion` de las carreras académicas.
4. **`asignaturas`**: Almacena `id`, `nombre`, `codigo` (único), `creditos`, `estado` y `programa_id` (FK ➔ `programas.id`).
5. **`proyectos`**: Almacena `id`, `nombre`, `descripcion`, `fecha_registro` y `asignatura_id` (FK ➔ `asignaturas.id`).
6. **`equipos`**: Almacena `id`, `nombre` del equipo y `proyecto_id` (FK ➔ `proyectos.id`).
7. **`integrantes_equipo`**: Tabla asociativa N:M que relaciona `equipo_id` (FK ➔ `equipos.id`) y `usuario_id` (FK ➔ `usuarios.id`).
8. **`docentes_tutores`**: Almacena la relación de tutoría entre `docente_id` (FK ➔ `usuarios.id`), `proyecto_id` (FK ➔ `proyectos.id`) y `fecha_asignacion`.

---

### 1.5 Clasificación de Funcionalidades por Actores (Matriz RBAC)

La aplicación aplica Control de Acceso Basado en Roles (RBAC), ajustando la interfaz y las pestañas visibles automáticamente:

| Módulo / Funcionalidad | Administrador | Docente Tutor | Estudiante |
| :--- | :---: | :---: | :---: |
| **Inicio & Login** | ✅ Acceso | ✅ Acceso | ✅ Acceso |
| **Módulo de Usuarios (CRUD)** | ✅ Total | ❌ Sin acceso | ❌ Sin acceso |
| **Módulo de Programas (CRUD)** | ✅ Total | ❌ Sin acceso | ❌ Sin acceso |
| **Módulo de Asignaturas (CRUD)** | ✅ Total | ❌ Sin acceso | ❌ Sin acceso |
| **Módulo de Proyectos (CRUD)** | ✅ Total | ✅ Consulta / Tutoría | ✅ Consulta |
| **Módulo de Equipos (CRUD)** | ✅ Total | ✅ Consulta Integrantes | ✅ Consulta Integrantes |
| **Módulo de Docentes Tutores (CRUD)** | ✅ Total | ✅ Consulta Tutorías | ❌ Sin acceso |

---

## 2. INSTRUCCIONES PARA COPIAR A MICROSOFT WORD

Este documento contiene la estructura formal exacta de los 5 puntos exigidos en la Sección 6.1 de la Guía de Evaluación (*Sección 1.1 a 1.5*). 

Para entregarlo en formato `.docx` (Word):
1. Abre Microsoft Word.
2. Selecciona y copia todo el texto de este archivo.
3. Pégalo en Microsoft Word (se mantendrán los títulos, tablas formateadas, textos en negrita y listas).
4. Guarda el archivo con el nombre: **`Segundo_Parcial_Apps_Moviles_Julian_Diaz.docx`**.
