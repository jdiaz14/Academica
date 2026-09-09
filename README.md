# Académica 🎓

**Académica** es una aplicación multiplataforma (Android e iOS) diseñada para la gestión de información académica. Desarrollada con **Kotlin Multiplatform (KMP)** y **Compose Multiplatform**, permite compartir la lógica de negocio y la interfaz de usuario entre ambas plataformas.

## ✨ Características

La aplicación incluye módulos de gestión para:
*   **Autenticación:** Pantalla de Login segura.
*   **Usuarios:** Control de acceso y perfiles.
*   **Docentes:** Información detallada del personal académico.
*   **Asignaturas:** Gestión del currículo y materias.
*   **Programas:** Estructura de programas académicos.
*   **Proyectos y Equipos:** Organización de trabajos colaborativos.

## 🚀 Tecnologías Utilizadas

*   **Kotlin Multiplatform (KMP):** Compartición de código entre Android e iOS.
*   **Compose Multiplatform:** UI declarativa compartida.
*   **Voyager:** Gestión de navegación fluida entre pantallas.
*   **Material 3:** Interfaz de usuario moderna y adaptativa.

## 📁 Estructura del Proyecto

*   **androidApp**: Contenedor específico para la aplicación Android.
*   **iosApp**: Proyecto Xcode necesario para la aplicación iOS.
*   **shared**: El corazón del proyecto. Contiene toda la lógica y la UI compartida.
    *   `commonMain`: Aquí se encuentran todas las pantallas (`Screens`) y componentes comunes.

## 🛠️ Requisitos e Instalación

1.  Clonar el repositorio:
    ```bash
    git clone https://github.com/jdiaz14/Academica.git
    ```
2.  Abrir con **Android Studio**.
3.  Sincronizar el proyecto con Gradle.

## 📱 Ejecución

*   **Android:** Selecciona el módulo `androidApp` en la parte superior y presiona el botón **Run** (triángulo verde).
*   **iOS:** Abre la carpeta `iosApp` en Xcode o utiliza la configuración de ejecución de iOS en Android Studio (requiere macOS).

---
Desarrollado con ❤️ usando Kotlin Multiplatform.
