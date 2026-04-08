# SpringEduManager

Proyecto Spring Boot (`MVC + JPA + Thymeleaf + Security`) para gestion academica, con modulo de notas y API REST protegida por roles.

## Requisitos
- Java 25
- MySQL
- Maven Wrapper (`mvnw`)

## Ejecucion rapida
1. Configura conexion MySQL en `src/main/resources/application.properties`.
2. Asegura que la BD tenga el esquema cargado (el proyecto usa `spring.sql.init.mode=never`).
3. Ejecuta:
   - `./mvnw spring-boot:run` (Linux/macOS)
   - `mvnw.cmd spring-boot:run` (Windows)

## Carga de `schema.sql` y persistencia de datos
- El proyecto esta configurado con `spring.sql.init.mode=never`.
- Esto significa que **Spring Boot no ejecuta `schema.sql` automaticamente** al iniciar.
- Debes cargar `src/main/resources/schema.sql` manualmente (por ejemplo, desde MySQL Workbench) la primera vez.
- Ventaja: al apagar y volver a ejecutar el `jar`, **no se borran** los cambios realizados en la aplicacion.
- Si quieres reiniciar datos desde cero, debes ejecutar manualmente `schema.sql` nuevamente.

## Usuarios de prueba (seed SQL)
Contrasena de prueba para usuarios seed: `1234` (guardada como hash bcrypt en BD).

- `admin` (rol ADMIN)
- `analopezm` (roles PROFESOR y AYUDANTE)
- `pedsotoq` (rol PROFESOR)
- `laurojasd` (rol PROFESOR)
- `juaperezg` (rol ESTUDIANTE)
- `margarcial` (rol ESTUDIANTE)
- `carlopezr` (rol ESTUDIANTE)
- `dievegas` (rol ESTUDIANTE)
- `camnavarrot` (rol ESTUDIANTE)

## Reglas por rol (resumen funcional)
- `ADMIN`: acceso total web/API del modulo academico; unico rol que crea/edita/elimina inscripciones; gestiona ponderaciones y cierre de semestre.
- `PROFESOR`: puede gestionar practicas/evaluaciones (incluye calificar en la web por curso/prueba), solo sobre sus cursos y estudiantes asociados.
- `AYUDANTE`: solo lectura de listados academicos y endpoints GET academicos.
- `ESTUDIANTE`: consulta vistas personales (`/mis-notas`, `/mis-eventos-notas`) y endpoints personales (`/api/practicas/mis`, `/api/evaluaciones/mis`, `/api/notas-finales/mis`).

## Matriz de permisos (web + API)
- Publico: `/`, `/login`, `/error`, `/acceso-denegado`, `/css/**`, `/js/**`.
- Autenticado: `/perfil`, `GET /api/me`.
- Estudiantes: `GET /estudiantes` (`ADMIN`,`PROFESOR`,`AYUDANTE`), altas/edicion API solo `ADMIN`.
- Usuarios: `/usuarios/**` solo `ADMIN`.
- Cursos: lectura amplia (`ADMIN`,`PROFESOR`,`AYUDANTE`,`ESTUDIANTE`), escritura segun ruta (`ADMIN`/`PROFESOR`), edicion/eliminacion final solo `ADMIN`.
- Modulo academico web: listados (`ADMIN`,`PROFESOR`,`AYUDANTE`), inscripciones solo `ADMIN`, practicas/evaluaciones escritura (`ADMIN`,`PROFESOR`), ponderaciones solo `ADMIN`.
- Calificacion en web (solo `ADMIN`,`PROFESOR`): practicas `GET /practicas/{id}/calificaciones` y `POST /practicas/{id}/calificaciones/{inscripcionId}`; evaluaciones `GET /evaluaciones/{id}/calificar` y `POST /evaluaciones/{id}/calificaciones/{inscripcionId}`.
- Modulo academico API: `inscripciones` escritura solo `ADMIN`; `practicas/evaluaciones` escritura `ADMIN`/`PROFESOR`; `ponderaciones` PUT solo `ADMIN`; endpoints `mis/*` autenticado.

## Modulo academico de notas
- Tablas integradas: `inscripciones`, `practicas`, `practica_calificaciones`, `evaluaciones`, `ponderaciones_curso`, `notas_finales`.
- Flujo: inscripcion -> practicas/evaluaciones -> examen final -> resumen -> cierre y persistencia de nota final.
- **Calificacion en la interfaz web**
  - **Practicas**: en el listado, **Calificar** abre la vista por estudiante (nota, estado de la calificacion, comentario); cada envio guarda la fila de `practica_calificaciones` para esa inscripcion.
  - **Evaluaciones**: **Calificar** agrupa por la misma prueba que la fila desde la que entras (mismo curso, nombre, fecha y tipo). Se listan los estudiantes del curso con inscripcion activa (no `RETIRADA`); si aun no existe `evaluaciones` para ese estudiante y esa prueba, se crea; si existe, se actualiza nota y comentario (nota entre 0.00 y 7.00).

## Formula de notas implementada
- `promedioPractica`: promedio simple de practicas con nota (`suma notas / cantidad`).
- `promedioEvaluacion`: promedio de evaluaciones parciales (excluye `EXAMEN_FINAL`).
- `bloqueEvaluacion`: `(promedioEvaluacion * %parciales) + (examenFinal * %examenFinal)`.
- `notaFinalSemestre`: `(promedioPractica * %practicas) + (bloqueEvaluacion * %evaluaciones)`.

## Endpoints REST implementados (L5)
- Perfil: `GET /api/me`.
- Cursos: `GET/POST/PUT/DELETE /api/cursos`.
- Estudiantes: `GET/POST/PUT/DELETE /api/estudiantes`.
- Modulo academico:
  - `GET/POST/PUT/DELETE /api/inscripciones`
  - `GET/POST/PUT/DELETE /api/practicas`
  - `GET /api/practicas/{id}/calificaciones`
  - `PUT /api/practicas/{id}/calificaciones/{inscripcionId}`
  - `GET/POST/PUT/DELETE /api/evaluaciones` (calificar fila a fila con `POST` o `PUT`; no hay ruta REST aparte para la vista masiva de `/evaluaciones/{id}/calificar`)
  - `GET/PUT /api/ponderaciones`
  - `GET /api/notas-finales`
  - `GET /api/notas-finales/inscripcion/{id}/resumen`
  - `POST /api/notas-finales/inscripcion/{id}/cerrar`
  - personales: `GET /api/practicas/mis`, `GET /api/evaluaciones/mis`, `GET /api/notas-finales/mis`

## Validacion de consumo
- Postman: iniciar sesion web y consumir endpoints con `Content-Type: application/json`.
- Cliente externo: ejemplo en `docs/resttemplate-consumo-ejemplo.md`.

## Configuracion editable (`application.properties`)
Ademas de lo habitual (conexion MySQL `spring.datasource.*`, `server.port`, JPA, Thymeleaf, Security), puedes ajustar:

| Propiedad | Uso |
|-----------|-----|
| `app.estudiantes.email-domain` | Dominio del correo institucional al crear estudiantes: se forma `username@<dominio>` (p. ej. `edu.cl`). |
| `app.periodos.sistema` | Tipo de calendario institucional (`SEMESTRAL`, `TRIMESTRAL`, `CUATRIMESTRAL`, …). Debe coincidir con un valor existente en la tabla `semestres` (`term_system`); si no, el alta de curso puede fallar con error de periodo. |
| `app.periodos.etiquetas` | Lista separada por **comas**; cada parte se recorta con `trim`. Son las opciones del desplegable de periodo academico al crear/editar curso. Debe haber **tantas etiquetas como periodos** use tu sistema (p. ej. dos si es semestral: `Primer semestre, Segundo semestre`). |

Valores por defecto en codigo si omites alguna: dominio `edu.cl`; sistema `SEMESTRAL`; etiquetas `Primer semestre,Segundo semestre`.

## Alta de estudiante (reglas)
- Crea automaticamente: `usuarios` + `usuario_roles` (`ESTUDIANTE`) + `estudiantes`.
- Genera `username`, correo institucional y `codigo_estudiante`.
