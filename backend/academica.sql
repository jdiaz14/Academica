DROP DATABASE IF EXISTS academica_db;
CREATE DATABASE academica_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE academica_db;

-- Tabla de Roles
CREATE TABLE roles (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO roles (id, nombre) VALUES
(1, 'Administrador'),
(2, 'Docente'),
(3, 'Estudiante');

-- Tabla de Usuarios (Contraseñas encriptadas con BCrypt)
CREATE TABLE usuarios (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre_completo VARCHAR(150) NOT NULL,
    usuario VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    rol_id INT NOT NULL,
    FOREIGN KEY (rol_id) REFERENCES roles(id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO usuarios (id, nombre_completo, usuario, password, rol_id) VALUES
(1, 'Administrador del Sistema', 'admin', '$2y$10$TcczVQjFILq7xyLtWIQLuOxiqSdwR9hvE9lVzvfwGl1kmuQfJUdE6', 1),
(2, 'Prof. Carlos Gómez', 'cgomez', '$2y$10$Qc7QyddcDWzxQVF2E96DPOEnAR8f.5ofI8v5t/KL9wBIDiFlnWNQy', 2),
(3, 'Julián Andrés Díaz Guevara', 'jdiaz', '$2y$10$MB4q8e7tTVNQsrnHsmz2zOLw0GU.Gk8XKLEhjElkRuGWol/fFwygS', 3);

-- Tabla de Programas
CREATE TABLE programas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    descripcion TEXT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO programas (id, nombre, descripcion) VALUES
(1, 'Ingeniería de Sistemas', 'Programa enfocado en software, bases de datos y arquitectura de sistemas.'),
(2, 'Ingeniería Electrónica', 'Programa enfocado en hardware, automatización y telecomunicaciones.');

-- Tabla de Asignaturas
CREATE TABLE asignaturas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    codigo VARCHAR(20) NOT NULL UNIQUE,
    creditos INT NOT NULL DEFAULT 3,
    estado VARCHAR(20) NOT NULL DEFAULT 'Activa',
    programa_id INT,
    FOREIGN KEY (programa_id) REFERENCES programas(id) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO asignaturas (id, nombre, codigo, creditos, estado, programa_id) VALUES
(1, 'Desarrollo de Apps Móviles', 'INF-301', 3, 'Activa', 1),
(2, 'Ingeniería de Software III', 'INF-302', 3, 'Activa', 1),
(3, 'Bases de Datos Avanzadas', 'INF-303', 4, 'Activa', 1);

-- Tabla de Proyectos
CREATE TABLE proyectos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    descripcion TEXT,
    fecha_registro DATE DEFAULT (CURRENT_DATE),
    asignatura_id INT,
    FOREIGN KEY (asignatura_id) REFERENCES asignaturas(id) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO proyectos (id, nombre, descripcion, fecha_registro, asignatura_id) VALUES
(1, 'Académica KMP', 'Aplicación móvil multiplataforma para la gestión académica y proyectos integradores.', '2026-09-05', 1),
(2, 'Sistema de Asistencia IoT', 'Prototipo de control de acceso mediante hardware y software.', '2026-09-10', 2);

-- Tabla de Equipos
CREATE TABLE equipos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    proyecto_id INT,
    FOREIGN KEY (proyecto_id) REFERENCES proyectos(id) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO equipos (id, nombre, proyecto_id) VALUES
(1, 'Equipo Alpha KMP', 1),
(2, 'Equipo IoT San Buenaventura', 2);

-- Tabla de Integrantes de Equipo
CREATE TABLE integrantes_equipo (
    id INT AUTO_INCREMENT PRIMARY KEY,
    equipo_id INT NOT NULL,
    usuario_id INT NOT NULL,
    FOREIGN KEY (equipo_id) REFERENCES equipos(id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO integrantes_equipo (equipo_id, usuario_id) VALUES
(1, 3);

-- Tabla de Asignación de Docentes Tutores
CREATE TABLE docentes_tutores (
    id INT AUTO_INCREMENT PRIMARY KEY,
    docente_id INT NOT NULL,
    proyecto_id INT NOT NULL,
    fecha_asignacion DATE DEFAULT (CURRENT_DATE),
    FOREIGN KEY (docente_id) REFERENCES usuarios(id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (proyecto_id) REFERENCES proyectos(id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO docentes_tutores (docente_id, proyecto_id, fecha_asignacion) VALUES
(2, 1, '2026-09-05');
