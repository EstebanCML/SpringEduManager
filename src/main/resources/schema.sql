-- ============================================================================
-- SCHEMA: spring_edu_manager
-- Descripcion: Base de datos para la aplicacion SpringEduManager
-- Creada: 7 de abril 2026
-- ============================================================================
-- Estructura del script:
--   1) Limpieza (DROP) y creacion de TODAS las tablas (DDL)
--   2) Datos iniciales (DML) en orden respetando dependencias entre FK
-- ============================================================================

CREATE DATABASE IF NOT EXISTS spring_edu_manager
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE spring_edu_manager;

-- ============================================================================
-- Limpieza para permitir re-ejecutar el script durante desarrollo
-- ============================================================================
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS notas_finales;
DROP TABLE IF EXISTS evaluaciones;
DROP TABLE IF EXISTS practicas;
DROP TABLE IF EXISTS inscripciones;
DROP TABLE IF EXISTS cursos;
DROP TABLE IF EXISTS ponderaciones_curso;
DROP TABLE IF EXISTS semestres;
DROP TABLE IF EXISTS estudiantes;
DROP TABLE IF EXISTS profesores;
DROP TABLE IF EXISTS usuario_roles;
DROP TABLE IF EXISTS usuarios;
DROP TABLE IF EXISTS roles;
SET FOREIGN_KEY_CHECKS = 1;

-- ############################################################################
-- # DDL: DEFINICION DE TABLAS (orden compatible con claves foraneas)
-- ############################################################################

-- ============================================================================
-- TABLA: roles
-- ============================================================================
CREATE TABLE IF NOT EXISTS roles (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT 'Identificador unico del rol',
    nombre VARCHAR(40) NOT NULL UNIQUE COMMENT 'Nombre del rol (ADMIN, PROFESOR, AYUDANTE, ESTUDIANTE)',
    nivel TINYINT UNSIGNED NOT NULL COMMENT 'Nivel de permisos (mayor numero, mas privilegios)',
    descripcion VARCHAR(150) NULL COMMENT 'Descripcion del rol',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Catalogo de roles del sistema';

-- ============================================================================
-- TABLA: usuarios
-- ============================================================================
CREATE TABLE IF NOT EXISTS usuarios (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT 'Identificador unico del usuario',
    nombre VARCHAR(60) NOT NULL COMMENT 'Primer nombre',
    segundo_nombre VARCHAR(60) NULL COMMENT 'Segundo nombre (opcional)',
    primer_apellido VARCHAR(60) NOT NULL COMMENT 'Primer apellido',
    segundo_apellido VARCHAR(60) NOT NULL COMMENT 'Segundo apellido',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT 'Usuario institucional unico',
    email VARCHAR(120) NOT NULL UNIQUE COMMENT 'Correo institucional unico',
    email_personal VARCHAR(120) NULL UNIQUE COMMENT 'Correo personal opcional',
    password_hash VARCHAR(255) NOT NULL COMMENT 'Hash bcrypt (nunca la contraseña en claro)',
    activo BOOLEAN NOT NULL DEFAULT 1 COMMENT 'Estado del usuario',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_usuarios_activo (activo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Cuenta de acceso transversal para personas del sistema';

-- ============================================================================
-- TABLA: usuario_roles
-- ============================================================================
CREATE TABLE IF NOT EXISTS usuario_roles (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT 'Identificador unico del registro usuario-rol',
    usuario_id INT NOT NULL COMMENT 'Usuario asignado al rol',
    rol_id INT NOT NULL COMMENT 'Rol asignado al usuario',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_usuario_roles_usuario FOREIGN KEY (usuario_id)
        REFERENCES usuarios(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT fk_usuario_roles_rol FOREIGN KEY (rol_id)
        REFERENCES roles(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    UNIQUE KEY uk_usuario_roles_usuario_rol (usuario_id, rol_id),
    INDEX idx_usuario_roles_rol_id (rol_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Relacion N:M entre usuarios y roles';

-- ============================================================================
-- TABLA: profesores
-- ============================================================================
CREATE TABLE IF NOT EXISTS profesores (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT 'Identificador unico del profesor',
    usuario_id INT NOT NULL UNIQUE COMMENT 'Relacion 1:1 con usuario',
    codigo_profesor VARCHAR(20) NOT NULL UNIQUE COMMENT 'Codigo institucional del profesor',
    especialidad VARCHAR(120) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_profesores_usuario FOREIGN KEY (usuario_id)
        REFERENCES usuarios(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Perfil academico de profesores';

-- ============================================================================
-- TABLA: estudiantes
-- ============================================================================
CREATE TABLE IF NOT EXISTS estudiantes (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT 'Identificador unico del estudiante',
    usuario_id INT NOT NULL UNIQUE COMMENT 'Relacion 1:1 con usuario',
    codigo_estudiante VARCHAR(20) NOT NULL UNIQUE COMMENT 'Codigo institucional del estudiante',
    sede VARCHAR(80) NOT NULL COMMENT 'Sede/campus del estudiante',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_estudiantes_usuario FOREIGN KEY (usuario_id)
        REFERENCES usuarios(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    INDEX idx_estudiantes_codigo (codigo_estudiante),
    INDEX idx_estudiantes_sede (sede)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Perfil academico de estudiantes';

-- ============================================================================
-- TABLA: semestres
-- Catalogo del tipo de sistema academico de la institucion.
-- Las fechas y el academic_year viven en cursos, no aqui.
-- ============================================================================
CREATE TABLE IF NOT EXISTS semestres (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT 'Identificador unico del tipo de periodo',
    term_system VARCHAR(20) NOT NULL UNIQUE COMMENT 'Tipo de sistema: SEMESTRAL, TRIMESTRAL, CUATRIMESTRAL, ANUAL',
    descripcion VARCHAR(150) NULL COMMENT 'Descripcion del sistema academico',
    activo BOOLEAN NOT NULL DEFAULT 1 COMMENT 'Indica si el sistema esta en uso',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT chk_semestres_term_system CHECK (term_system IN ('SEMESTRAL', 'TRIMESTRAL', 'CUATRIMESTRAL', 'ANUAL'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Catalogo de sistemas de periodo academico';

-- ============================================================================
-- TABLA: cursos
-- ============================================================================
CREATE TABLE IF NOT EXISTS cursos (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT 'Identificador unico del curso',
    codigo VARCHAR(20) NOT NULL UNIQUE COMMENT 'Codigo del curso (ej: M6-001)',
    nombre VARCHAR(100) NOT NULL,
    descripcion TEXT NULL,
    profesor_id INT NOT NULL COMMENT 'Profesor a cargo del curso',
    semestre_id INT NOT NULL COMMENT 'Tipo de sistema academico del curso (FK a semestres.id)',
    anual BOOLEAN NOT NULL DEFAULT 0 COMMENT '1: curso anual, 0: requiere periodo academico especifico',
    periodo_academico VARCHAR(40) NULL COMMENT 'Primer semestre, Segundo semestre, etc. Si anual: ANUAL',
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_cursos_profesor FOREIGN KEY (profesor_id)
        REFERENCES profesores(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT fk_cursos_semestre FOREIGN KEY (semestre_id)
        REFERENCES semestres(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT chk_cursos_fechas CHECK (fecha_inicio <= fecha_fin),
    INDEX idx_cursos_nombre (nombre),
    INDEX idx_cursos_profesor_id (profesor_id),
    INDEX idx_cursos_semestre_id (semestre_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Cursos ofertados con profesor y periodo';

-- ============================================================================
-- TABLA: inscripciones
-- ============================================================================
CREATE TABLE IF NOT EXISTS inscripciones (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT 'Identificador unico de la inscripcion',
    estudiante_id INT NOT NULL,
    curso_id INT NOT NULL,
    fecha_inscripcion DATE NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE' COMMENT 'PENDIENTE: solicitud registrada sin confirmar | ACTIVA: matricula confirmada | RETIRADA/APROBADA/REPROBADA: cierre del curso',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_inscripciones_estudiante FOREIGN KEY (estudiante_id)
        REFERENCES estudiantes(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT fk_inscripciones_curso FOREIGN KEY (curso_id)
        REFERENCES cursos(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,

    CONSTRAINT chk_inscripciones_estado CHECK (estado IN ('PENDIENTE', 'ACTIVA', 'RETIRADA', 'APROBADA', 'REPROBADA')),
    UNIQUE KEY uk_inscripciones_estudiante_curso (estudiante_id, curso_id),
    INDEX idx_inscripciones_curso_id (curso_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Matrícula de estudiantes en cursos';

-- ============================================================================
-- TABLA: practicas
-- ============================================================================
CREATE TABLE IF NOT EXISTS practicas (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT 'Identificador unico de la practica',
    curso_id INT NOT NULL,
    nombre VARCHAR(120) NOT NULL COMMENT 'Nombre descriptivo de la practica',
    descripcion TEXT NULL COMMENT 'Consigna o instrucciones detalladas',
    fecha_inicio DATE NOT NULL COMMENT 'Fecha en que se habilita la practica',
    fecha_entrega DATE NOT NULL COMMENT 'Fecha limite de entrega',
    estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE' COMMENT 'PENDIENTE/ACTIVA/CERRADA',
    porcentaje_nota DECIMAL(5,2) NOT NULL DEFAULT 0.00 COMMENT 'Ponderacion en porcentaje',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_practicas_curso FOREIGN KEY (curso_id)
        REFERENCES cursos(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT chk_practicas_porcentaje CHECK (porcentaje_nota >= 0.00 AND porcentaje_nota <= 100.00),
    INDEX idx_practicas_fecha_entrega (fecha_entrega)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Actividades practicas evaluables';

-- ============================================================================
-- TABLA: practica_calificaciones
-- Nota por practica y estudiante inscrito
-- ============================================================================
CREATE TABLE IF NOT EXISTS practica_calificaciones (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT 'Identificador unico de la calificacion de practica',
    practica_id INT NOT NULL,
    inscripcion_id INT NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE' COMMENT 'PENDIENTE/ENTREGADA/REVISADA',
    nota DECIMAL(3,2) NULL COMMENT 'Escala chilena 0.00 a 7.00',
    comentario TEXT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_practica_calificaciones_practica FOREIGN KEY (practica_id)
        REFERENCES practicas(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_practica_calificaciones_inscripcion FOREIGN KEY (inscripcion_id)
        REFERENCES inscripciones(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT chk_practica_calificaciones_estado CHECK (estado IN ('PENDIENTE', 'ENTREGADA', 'REVISADA')),
    CONSTRAINT chk_practica_calificaciones_nota CHECK (nota IS NULL OR (nota >= 0.00 AND nota <= 7.00)),
    UNIQUE KEY uk_practica_calificaciones_unica (practica_id, inscripcion_id),
    INDEX idx_practica_calificaciones_inscripcion_id (inscripcion_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Notas de practicas por estudiante inscrito';

-- ============================================================================
-- TABLA: evaluaciones
-- ============================================================================
CREATE TABLE IF NOT EXISTS evaluaciones (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT 'Identificador unico de la evaluacion',
    inscripcion_id INT NOT NULL COMMENT 'Garantiza que solo alumnos inscritos sean evaluados',
    curso_id INT NOT NULL COMMENT 'Curso al que pertenece la evaluacion',
    tipo VARCHAR(20) NOT NULL DEFAULT 'EVALUACION' COMMENT 'TEORICA, PROYECTO_ABP, CUESTIONARIO, EXAMEN_FINAL, etc.',
    nombre VARCHAR(120) NOT NULL COMMENT 'Nombre de la evaluacion',
    descripcion TEXT NULL COMMENT 'Consigna, feedback o comentarios',
    fecha_evaluacion DATE NOT NULL COMMENT 'Fecha de evaluacion',
    nota DECIMAL(3,2) NOT NULL COMMENT 'Escala chilena 0.00 a 7.00',
    comentario TEXT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_evaluaciones_inscripcion FOREIGN KEY (inscripcion_id)
        REFERENCES inscripciones(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_evaluaciones_curso FOREIGN KEY (curso_id)
        REFERENCES cursos(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT chk_evaluaciones_nota CHECK (nota >= 0.00 AND nota <= 7.00),
    UNIQUE KEY uk_evaluaciones_unica (inscripcion_id, nombre, fecha_evaluacion),
    INDEX idx_evaluaciones_fecha (fecha_evaluacion),
    INDEX idx_evaluaciones_nota (nota)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Evaluaciones por inscripcion y curso';

-- ============================================================================
-- TABLA: ponderaciones_curso
-- Guarda el % de prácticas y evaluaciones para todo el instituto. Solo una fila.
-- ============================================================================
CREATE TABLE IF NOT EXISTS ponderaciones_curso (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    porcentaje_practicas DECIMAL(5,2) NOT NULL DEFAULT 60.00,
    porcentaje_evaluaciones DECIMAL(5,2) NOT NULL DEFAULT 40.00,
    porcentaje_evaluaciones_parciales DECIMAL(5,2) NOT NULL DEFAULT 60.00,
    porcentaje_examen_final DECIMAL(5,2) NOT NULL DEFAULT 40.00,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT chk_ponderaciones_suma CHECK (porcentaje_practicas + porcentaje_evaluaciones = 100.00),
    CONSTRAINT chk_ponderaciones_evaluacion CHECK (porcentaje_evaluaciones_parciales + porcentaje_examen_final = 100.00)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Ponderaciones de nota final globales';

-- ============================================================================
-- TABLA: notas_finales
-- Guarda el histórico de la nota final de cada estudiante por curso, año y periodo.
-- ============================================================================
CREATE TABLE IF NOT EXISTS notas_finales (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    estudiante_id INT NOT NULL,
    curso_id INT NOT NULL,
    academic_year SMALLINT NOT NULL,
    term_number TINYINT NOT NULL,
    promedio_practica DECIMAL(3,2) NOT NULL,
    promedio_evaluacion DECIMAL(3,2) NOT NULL,
    nota_examen_final DECIMAL(3,2) NOT NULL,
    nota_final_semestre DECIMAL(3,2) NOT NULL,
    fecha_cierre DATE NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_notas_finales_estudiante FOREIGN KEY (estudiante_id)
        REFERENCES estudiantes(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_notas_finales_curso FOREIGN KEY (curso_id)
        REFERENCES cursos(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    UNIQUE KEY uk_notas_finales (estudiante_id, curso_id, academic_year, term_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Histórico de notas finales por estudiante, curso, año y periodo';

-- ############################################################################
-- # DML: DATOS INICIALES (orden obligatorio por dependencias FK)
-- #
-- #   1. roles
-- #   2. usuarios
-- #   3. usuario_roles        (requiere usuarios, roles)
-- #   4. profesores           (requiere usuarios)
-- #   5. estudiantes          (requiere usuarios)
-- #   6. semestres            (catalogo sin FK)
-- #   7. cursos               (requiere profesores, semestres)
-- #   8. inscripciones        (requiere estudiantes, cursos)
-- #   9. practicas            (requiere cursos)
-- #  10. practica_calificaciones (requiere practicas, inscripciones)
-- #  11. evaluaciones        (requiere inscripciones, cursos; curso_id coherente con la inscripcion)
-- #  12. ponderaciones_curso  (sin FK)
-- #  13. notas_finales        (requiere estudiantes, cursos)
-- ############################################################################

-- (1) roles
INSERT IGNORE INTO roles (id, nombre, nivel, descripcion) VALUES
(1, 'ADMIN', 100, 'Administrador del sistema'),
(2, 'PROFESOR', 80, 'Docente a cargo de cursos'),
(3, 'AYUDANTE', 60, 'Asistente academico'),
(4, 'ESTUDIANTE', 20, 'Alumno del bootcamp');

-- (2) usuarios  |  Username sugerido: 3 letras nombre + primer apellido + 1 letra segundo apellido
--     password_hash: bcrypt de la contraseña literal "1234" (BCryptPasswordEncoder, cost 10)
INSERT IGNORE INTO usuarios (id, nombre, segundo_nombre, primer_apellido, segundo_apellido, username, email, email_personal, password_hash, activo) VALUES
(1, 'Admin', NULL, 'Sistema', 'Principal', 'admin', 'admin@springedumanager.test', NULL, '$2a$10$1SYWDCoHz1ygmjPNUl/xmO0b7bmq2.ZCjC4OdYp5yP2FKb7uxnQFe', 1),
(2, 'Ana', 'Maria', 'Lopez', 'Mora', 'analopezm', 'analopezm@springedumanager.test', 'ana@gmail.com', '$2a$10$1SYWDCoHz1ygmjPNUl/xmO0b7bmq2.ZCjC4OdYp5yP2FKb7uxnQFe', 1),
(3, 'Juan', 'Eduardo', 'Perez', 'Garcia', 'juaperezg', 'juaperezg@springedumanager.test', NULL, '$2a$10$1SYWDCoHz1ygmjPNUl/xmO0b7bmq2.ZCjC4OdYp5yP2FKb7uxnQFe', 1),
(4, 'Maria', 'Alejandra', 'Garcia', 'Lopez', 'margarcial', 'margarcial@springedumanager.test', 'maria@gmail.com', '$2a$10$1SYWDCoHz1ygmjPNUl/xmO0b7bmq2.ZCjC4OdYp5yP2FKb7uxnQFe', 1),
(5, 'Carlos', 'Andres', 'Lopez', 'Ruiz', 'carlopezr', 'carlopezr@springedumanager.test', NULL, '$2a$10$1SYWDCoHz1ygmjPNUl/xmO0b7bmq2.ZCjC4OdYp5yP2FKb7uxnQFe', 1),
(6, 'Pedro', 'Luis', 'Soto', 'Quispe', 'pedsotoq', 'pedsotoq@springedumanager.test', 'pedro@gmail.com', '$2a$10$1SYWDCoHz1ygmjPNUl/xmO0b7bmq2.ZCjC4OdYp5yP2FKb7uxnQFe', 1),
(7, 'Laura', 'Ines', 'Rojas', 'Diaz', 'laurojasd', 'laurojasd@springedumanager.test', 'laura@gmail.com', '$2a$10$1SYWDCoHz1ygmjPNUl/xmO0b7bmq2.ZCjC4OdYp5yP2FKb7uxnQFe', 1),
(8, 'Diego', 'Martin', 'Vega', 'Silva', 'dievegas', 'dievegas@springedumanager.test', NULL, '$2a$10$1SYWDCoHz1ygmjPNUl/xmO0b7bmq2.ZCjC4OdYp5yP2FKb7uxnQFe', 1),
(9, 'Camila', 'Paz', 'Navarro', 'Torres', 'camnavarrot', 'camnavarrot@springedumanager.test', 'camila@gmail.com', '$2a$10$1SYWDCoHz1ygmjPNUl/xmO0b7bmq2.ZCjC4OdYp5yP2FKb7uxnQFe', 1);

-- (3) usuario_roles
INSERT IGNORE INTO usuario_roles (usuario_id, rol_id) VALUES
(1, 1),
(2, 2),
(3, 4),
(4, 4),
(5, 4),
(2, 3),
(6, 2),
(7, 2),
(8, 4),
(9, 4);

-- (4) profesores
INSERT IGNORE INTO profesores (id, usuario_id, codigo_profesor, especialidad) VALUES
(1, 2, 'PRF-0001', 'Spring Framework'),
(2, 6, 'PRF-0002', 'Arquitectura Backend'),
(3, 7, 'PRF-0003', 'Integración y APIs');

-- (5) estudiantes
INSERT IGNORE INTO estudiantes (id, usuario_id, codigo_estudiante, sede) VALUES
(1, 3, 'STD-0001', 'Santiago Centro'),
(2, 4, 'STD-0002', 'Santiago Norte'),
(3, 5, 'STD-0003', 'Valparaiso'),
(4, 8, 'STD-0004', 'Santiago Sur'),
(5, 9, 'STD-0005', 'Concepcion');

-- (6) semestres
INSERT IGNORE INTO semestres (id, term_system, descripcion, activo) VALUES
(1, 'SEMESTRAL',      'Sistema de dos periodos por academic year',   1),
(2, 'TRIMESTRAL',     'Sistema de tres periodos por academic year',  1),
(3, 'CUATRIMESTRAL',  'Sistema de cuatro periodos por academic year',1),
(4, 'ANUAL',          'Un solo periodo por academic year',           1);

-- (7) cursos
INSERT IGNORE INTO cursos (id, codigo, nombre, descripcion, profesor_id, semestre_id, anual, periodo_academico, fecha_inicio, fecha_fin, activo) VALUES
(1, 'M6-001', 'Desarrollo de Aplicaciones JEE', 'Modulo 6 con Spring Boot, MVC, JPA, Security y REST', 1, 1, 0, 'Primer semestre', '2026-03-10', '2026-07-15', 1),
(2, 'M6-002', 'Spring Boot Avanzado', 'Temas avanzados de Spring Boot y arquitectura', 1, 1, 1, 'ANUAL', '2026-03-12', '2026-07-20', 1),
(3, 'M6-003', 'Microservicios con Spring', 'Diseño de microservicios y comunicación entre servicios', 2, 1, 0, 'Primer semestre', '2026-03-15', '2026-07-18', 1),
(4, 'M6-004', 'Integracion de Sistemas', 'REST, eventos y prácticas de interoperabilidad', 3, 1, 0, 'Primer semestre', '2026-03-18', '2026-07-22', 1),
(5, 'M6-005', 'Taller de APIs REST', 'Curso de pruebas REST para validación de cálculos', 1, 1, 0, 'Primer semestre', '2026-03-20', '2026-07-25', 1),
(6, 'M6-006', 'Persistencia y Consultas', 'Curso para prácticas y evaluaciones de persistencia', 2, 1, 0, 'Primer semestre', '2026-03-22', '2026-07-26', 1),
(7, 'M6-007', 'Eventos y Mensajeria', 'Curso para simulación de eventos académicos', 3, 1, 0, 'Primer semestre', '2026-03-24', '2026-07-27', 1);

-- (8) inscripciones
INSERT IGNORE INTO inscripciones (id, estudiante_id, curso_id, fecha_inscripcion, estado) VALUES
(1, 1, 1, '2026-03-05', 'ACTIVA'),
(2, 2, 1, '2026-03-05', 'ACTIVA'),
(3, 3, 2, '2026-03-06', 'ACTIVA'),
(4, 4, 3, '2026-03-07', 'ACTIVA'),
(5, 5, 4, '2026-03-08', 'ACTIVA'),
(6, 1, 3, '2026-03-09', 'ACTIVA'),
(7, 2, 4, '2026-03-10', 'ACTIVA'),
(8, 1, 5, '2026-03-11', 'ACTIVA'),
(9, 1, 6, '2026-03-12', 'ACTIVA'),
(10, 1, 7, '2026-03-13', 'ACTIVA');

-- (9) practicas
INSERT IGNORE INTO practicas (id, curso_id, nombre, descripcion, fecha_inicio, fecha_entrega, estado, porcentaje_nota) VALUES
(1, 1, 'Practica 1 MVC', 'Formularios y validaciones en Spring MVC', '2026-04-18', '2026-05-05', 'ACTIVA', 20.00),
(2, 1, 'Practica 2 JPA', 'Persistencia y consultas con JPA', '2026-05-20', '2026-06-05', 'ACTIVA', 25.00),
(3, 2, 'Practica 1 Security', 'Autenticacion y autorizacion con Spring Security', '2026-04-25', '2026-05-05', 'ACTIVA', 30.00),
(4, 3, 'Practica 1 Microservicios', 'Construcción de un servicio base', '2026-04-20', '2026-05-10', 'ACTIVA', 20.00),
(5, 4, 'Practica 1 Integracion', 'Consumo de API externa y mapeo', '2026-04-21', '2026-05-12', 'ACTIVA', 20.00),
(6, 5, 'Practica 1 REST', 'Endpoints y validaciones', '2026-04-10', '2026-04-20', 'CERRADA', 50.00),
(7, 5, 'Practica 2 REST', 'Versionado y manejo de errores', '2026-04-25', '2026-05-05', 'CERRADA', 50.00),
(8, 6, 'Practica 1 JPA', 'Entidades y relaciones', '2026-04-12', '2026-04-22', 'CERRADA', 50.00),
(9, 6, 'Practica 2 JPA', 'Consultas y rendimiento', '2026-04-28', '2026-05-08', 'CERRADA', 50.00),
(10, 7, 'Practica 1 Eventos', 'Publicación de eventos', '2026-04-14', '2026-04-24', 'CERRADA', 50.00),
(11, 7, 'Practica 2 Eventos', 'Consumo de eventos', '2026-04-30', '2026-05-10', 'CERRADA', 50.00);

-- (10) practica_calificaciones
INSERT IGNORE INTO practica_calificaciones (id, practica_id, inscripcion_id, estado, nota, comentario) VALUES
(1, 1, 1, 'REVISADA', 5.60, 'Entrega completa y correcta'),
(2, 1, 2, 'REVISADA', 5.20, 'Buen trabajo con detalles por mejorar'),
(3, 3, 3, 'REVISADA', 4.80, 'Debe reforzar conceptos de seguridad'),
(4, 4, 4, 'REVISADA', 5.90, 'Buen dominio de arquitectura'),
(5, 5, 5, 'REVISADA', 6.10, 'Excelente integración'),
(6, 4, 6, 'REVISADA', 5.40, 'Cumple con lo esperado'),
(7, 5, 7, 'REVISADA', 5.70, 'Buena resolución de incidencias'),
(8, 6, 8, 'REVISADA', 4.10, 'Prueba controlada para cálculo'),
(9, 7, 8, 'REVISADA', 4.10, 'Prueba controlada para cálculo'),
(10, 8, 9, 'REVISADA', 5.60, 'Prueba controlada para cálculo'),
(11, 9, 9, 'REVISADA', 5.60, 'Prueba controlada para cálculo'),
(12, 10, 10, 'REVISADA', 6.80, 'Prueba controlada para cálculo'),
(13, 11, 10, 'REVISADA', 6.80, 'Prueba controlada para cálculo');

-- (11) evaluaciones  |  curso_id debe coincidir con el curso de la inscripcion
INSERT IGNORE INTO evaluaciones (id, inscripcion_id, curso_id, tipo, nombre, descripcion, fecha_evaluacion, nota, comentario) VALUES
(1, 1, 1, 'EVALUACION', 'Evaluacion Parcial 1', 'Controlador, vistas y formularios', '2026-04-22', 5.80, 'Buen manejo de controlador y vistas'),
(2, 2, 1, 'EVALUACION', 'Evaluacion Parcial 2', 'Persistencia y consultas JPA', '2026-05-25', 6.20, 'Muy buen manejo de JPA'),
(3, 3, 2, 'EVALUACION', 'Evaluacion Security', 'Autenticacion y autorizacion', '2026-04-30', 5.50, 'Debe mejorar JWT'),
(4, 4, 3, 'EVALUACION', 'Evaluacion Microservicios', 'Diseño y comunicación entre servicios', '2026-05-02', 5.90, 'Buen diseño'),
(5, 5, 4, 'EVALUACION', 'Evaluacion Integracion', 'Consumo de APIs y trazabilidad', '2026-05-04', 6.00, 'Muy buen desempeño'),
(6, 6, 3, 'EVALUACION', 'Evaluacion API Gateway', 'Ruteo y seguridad', '2026-05-06', 5.30, 'Le faltó validación de errores'),
(7, 7, 4, 'EVALUACION', 'Evaluacion Eventos', 'Eventos asincrónicos', '2026-05-08', 5.70, 'Correcto uso de colas'),
(8, 8, 5, 'EVALUACION', 'Parcial REST 1', 'Consumo y diseño de endpoints', '2026-05-12', 4.10, 'Prueba controlada para cálculo'),
(9, 8, 5, 'EXAMEN_FINAL', 'Examen Final REST', 'Cierre del curso REST', '2026-07-20', 4.10, 'Prueba controlada para cálculo'),
(10, 9, 6, 'EVALUACION', 'Parcial JPA 1', 'Mapeos y persistencia', '2026-05-13', 5.60, 'Prueba controlada para cálculo'),
(11, 9, 6, 'EXAMEN_FINAL', 'Examen Final JPA', 'Cierre del curso JPA', '2026-07-21', 5.60, 'Prueba controlada para cálculo'),
(12, 10, 7, 'EVALUACION', 'Parcial Eventos 1', 'Patrones de publicación y consumo', '2026-05-14', 6.80, 'Prueba controlada para cálculo'),
(13, 10, 7, 'EXAMEN_FINAL', 'Examen Final Eventos', 'Cierre del curso de eventos', '2026-07-22', 6.80, 'Prueba controlada para cálculo');

-- (12) ponderaciones_curso
INSERT IGNORE INTO ponderaciones_curso (
    id, porcentaje_practicas, porcentaje_evaluaciones, porcentaje_evaluaciones_parciales, porcentaje_examen_final
) VALUES
(1, 60.00, 40.00, 60.00, 40.00);

-- (13) notas_finales
INSERT IGNORE INTO notas_finales (
    id, estudiante_id, curso_id, academic_year, term_number,
    promedio_practica, promedio_evaluacion, nota_examen_final, nota_final_semestre, fecha_cierre
) VALUES
(1, 1, 1, 2026, 1, 5.50, 5.80, 6.00, 5.75, '2026-07-15'),
(2, 2, 1, 2026, 1, 5.20, 6.20, 6.00, 5.80, '2026-07-15'),
(3, 3, 2, 2026, 1, 4.80, 5.50, 6.00, 5.40, '2026-07-20'),
(4, 1, 5, 2026, 1, 4.10, 4.10, 4.10, 4.10, '2026-07-25'),
(5, 1, 6, 2026, 1, 5.60, 5.60, 5.60, 5.60, '2026-07-26'),
(6, 1, 7, 2026, 1, 6.80, 6.80, 6.80, 6.80, '2026-07-27');

-- ============================================================================
-- VERIFICACION: Consultas utiles
-- ============================================================================
/*
SHOW TABLES;

SELECT u.username, r.nombre AS rol
FROM usuarios u
JOIN usuario_roles ur ON ur.usuario_id = u.id
JOIN roles r ON r.id = ur.rol_id;

SELECT c.nombre AS curso, CONCAT(u.nombre, ' ', u.primer_apellido) AS profesor_a_cargo
FROM cursos c
JOIN profesores p ON p.id = c.profesor_id
JOIN usuarios u ON u.id = p.usuario_id;

SELECT e.id, ev.nombre, ev.fecha_evaluacion, ev.nota
FROM evaluaciones ev
JOIN inscripciones i ON i.id = ev.inscripcion_id
JOIN estudiantes e ON e.id = i.estudiante_id;
*/
