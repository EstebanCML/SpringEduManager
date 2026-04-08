package cl.untec.springedumanager.service;

import cl.untec.springedumanager.model.Inscripcion;
import cl.untec.springedumanager.model.Practica;
import cl.untec.springedumanager.model.PracticaCalificacion;
import cl.untec.springedumanager.repository.CursoRepository;
import cl.untec.springedumanager.repository.InscripcionRepository;
import cl.untec.springedumanager.repository.PracticaCalificacionRepository;
import cl.untec.springedumanager.repository.PracticaRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PracticaService {

    private static final Set<String> ESTADOS_PRACTICA = Set.of("PENDIENTE", "ACTIVA", "CERRADA");
    private static final Set<String> ESTADOS_CALIFICACION = Set.of("PENDIENTE", "ENTREGADA", "REVISADA");

    private final PracticaRepository practicaRepository;
    private final PracticaCalificacionRepository practicaCalificacionRepository;
    private final CursoRepository cursoRepository;
    private final InscripcionRepository inscripcionRepository;

    public PracticaService(
        PracticaRepository practicaRepository,
        PracticaCalificacionRepository practicaCalificacionRepository,
        CursoRepository cursoRepository,
        InscripcionRepository inscripcionRepository
    ) {
        this.practicaRepository = practicaRepository;
        this.practicaCalificacionRepository = practicaCalificacionRepository;
        this.cursoRepository = cursoRepository;
        this.inscripcionRepository = inscripcionRepository;
    }

    @Transactional(readOnly = true)
    public List<Practica> listarTodas() {
        return practicaRepository.findAllWithCurso();
    }

    @Transactional(readOnly = true)
    public List<Practica> listarPorCurso(Integer cursoId) {
        return practicaRepository.findByCursoIdWithCurso(cursoId);
    }

    @Transactional(readOnly = true)
    public List<Practica> listarPorProfesorUsername(String username) {
        return practicaRepository.findByProfesorUsernameWithCurso(username);
    }

    @Transactional(readOnly = true)
    public Practica buscarPorId(Integer id) {
        return practicaRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Práctica no válida."));
    }

    @Transactional(readOnly = true)
    public List<PracticaCalificacion> listarCalificaciones(Integer practicaId) {
        return practicaCalificacionRepository.findByPracticaIdWithInscripcion(practicaId);
    }

    @Transactional(readOnly = true)
    public boolean perteneceAProfesor(Integer practicaId, String profesorUsername) {
        return practicaRepository.existsByIdAndCurso_Profesor_Usuario_Username(practicaId, profesorUsername);
    }

    @Transactional(readOnly = true)
    public List<PracticaCalificacion> listarCalificacionesPorUsername(String username) {
        return practicaCalificacionRepository.findByUsername(username);
    }

    @Transactional
    public Practica crear(
        Integer cursoId,
        String nombre,
        String descripcion,
        LocalDate fechaInicio,
        LocalDate fechaEntrega,
        String estado
    ) {
        validarFechas(fechaInicio, fechaEntrega);
        Practica practica = new Practica();
        practica.setCurso(cursoRepository.findById(cursoId)
            .orElseThrow(() -> new IllegalArgumentException("Curso no válido.")));
        practica.setNombre(nombre.trim());
        practica.setDescripcion(blankToNull(descripcion));
        practica.setFechaInicio(fechaInicio);
        practica.setFechaEntrega(fechaEntrega);
        practica.setEstado(normalizarEstadoPractica(estado));
        practica.setPorcentajeNota(BigDecimal.ZERO);
        Practica saved = practicaRepository.save(practica);
        crearCalificacionesIniciales(saved);
        return saved;
    }

    @Transactional
    public Practica actualizar(
        Integer practicaId,
        String nombre,
        String descripcion,
        LocalDate fechaInicio,
        LocalDate fechaEntrega,
        String estado
    ) {
        validarFechas(fechaInicio, fechaEntrega);
        Practica practica = buscarPorId(practicaId);
        practica.setNombre(nombre.trim());
        practica.setDescripcion(blankToNull(descripcion));
        practica.setFechaInicio(fechaInicio);
        practica.setFechaEntrega(fechaEntrega);
        practica.setEstado(normalizarEstadoPractica(estado));
        practica.setPorcentajeNota(BigDecimal.ZERO);
        return practicaRepository.save(practica);
    }

    @Transactional
    public PracticaCalificacion calificar(Integer practicaId, Integer inscripcionId, BigDecimal nota, String estado, String comentario) {
        validarNota(nota, false);
        PracticaCalificacion calificacion = practicaCalificacionRepository
            .findByPractica_IdAndInscripcion_Id(practicaId, inscripcionId)
            .orElseGet(() -> {
                PracticaCalificacion nueva = new PracticaCalificacion();
                nueva.setPractica(buscarPorId(practicaId));
                nueva.setInscripcion(inscripcionRepository.findById(inscripcionId)
                    .orElseThrow(() -> new IllegalArgumentException("Inscripción no válida.")));
                return nueva;
            });
        calificacion.setNota(nota);
        calificacion.setEstado(normalizarEstadoCalificacion(estado));
        calificacion.setComentario(blankToNull(comentario));
        return practicaCalificacionRepository.save(calificacion);
    }

    @Transactional
    public void eliminar(Integer practicaId) {
        practicaRepository.delete(buscarPorId(practicaId));
    }

    public List<String> listarEstadosPractica() {
        return List.of("PENDIENTE", "ACTIVA", "CERRADA");
    }

    public List<String> listarEstadosCalificacion() {
        return List.of("PENDIENTE", "ENTREGADA", "REVISADA");
    }

    private void crearCalificacionesIniciales(Practica practica) {
        for (Inscripcion inscripcion : inscripcionRepository.findByCursoIdWithCursoYEstudiante(practica.getCurso().getId())) {
            if ("RETIRADA".equals(inscripcion.getEstado())) {
                continue;
            }
            PracticaCalificacion calificacion = new PracticaCalificacion();
            calificacion.setPractica(practica);
            calificacion.setInscripcion(inscripcion);
            practicaCalificacionRepository.save(calificacion);
        }
    }

    private static void validarFechas(LocalDate inicio, LocalDate entrega) {
        if (inicio == null || entrega == null || inicio.isAfter(entrega)) {
            throw new IllegalArgumentException("La fecha de inicio no puede ser posterior a la fecha de entrega.");
        }
    }

    private static void validarNota(BigDecimal nota, boolean required) {
        if (nota == null) {
            if (required) {
                throw new IllegalArgumentException("La nota es obligatoria.");
            }
            return;
        }
        if (nota.compareTo(BigDecimal.ZERO) < 0 || nota.compareTo(new BigDecimal("7.00")) > 0) {
            throw new IllegalArgumentException("La nota debe estar entre 0.00 y 7.00.");
        }
    }

    private static String normalizarEstadoPractica(String estado) {
        String value = estado == null ? "PENDIENTE" : estado.trim().toUpperCase();
        if (!ESTADOS_PRACTICA.contains(value)) {
            throw new IllegalArgumentException("Estado de práctica no válido.");
        }
        return value;
    }

    private static String normalizarEstadoCalificacion(String estado) {
        String value = estado == null ? "REVISADA" : estado.trim().toUpperCase();
        if (!ESTADOS_CALIFICACION.contains(value)) {
            throw new IllegalArgumentException("Estado de calificación no válido.");
        }
        return value;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
