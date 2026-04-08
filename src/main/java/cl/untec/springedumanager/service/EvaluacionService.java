package cl.untec.springedumanager.service;

import cl.untec.springedumanager.model.Evaluacion;
import cl.untec.springedumanager.model.Inscripcion;
import cl.untec.springedumanager.repository.EvaluacionRepository;
import cl.untec.springedumanager.repository.InscripcionRepository;
import cl.untec.springedumanager.web.dto.EvaluacionCalificacionFila;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EvaluacionService {

    private static final Set<String> TIPOS = Set.of("EVALUACION", "TEORICA", "PROYECTO_ABP", "CUESTIONARIO", "EXAMEN_FINAL");

    private final EvaluacionRepository evaluacionRepository;
    private final InscripcionRepository inscripcionRepository;

    public EvaluacionService(EvaluacionRepository evaluacionRepository, InscripcionRepository inscripcionRepository) {
        this.evaluacionRepository = evaluacionRepository;
        this.inscripcionRepository = inscripcionRepository;
    }

    @Transactional(readOnly = true)
    public List<Evaluacion> listarTodas() {
        return evaluacionRepository.findAllWithInscripcion();
    }

    @Transactional(readOnly = true)
    public List<Evaluacion> listarPorCurso(Integer cursoId) {
        return evaluacionRepository.findByCursoIdWithInscripcion(cursoId);
    }

    @Transactional(readOnly = true)
    public List<Evaluacion> listarPorProfesorUsername(String username) {
        return evaluacionRepository.findByProfesorUsernameWithInscripcion(username);
    }

    @Transactional(readOnly = true)
    public List<Evaluacion> listarPorUsername(String username) {
        return evaluacionRepository.findByUsernameWithCurso(username);
    }

    @Transactional(readOnly = true)
    public Evaluacion buscarPorId(Integer id) {
        return evaluacionRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Evaluación no válida."));
    }

    @Transactional(readOnly = true)
    public boolean perteneceAProfesor(Integer evaluacionId, String profesorUsername) {
        return evaluacionRepository.existsByIdAndCurso_Profesor_Usuario_Username(evaluacionId, profesorUsername);
    }

    /**
     * Todas las inscripciones activas del curso, con la nota de esta evaluación (mismo nombre, fecha y tipo) si existe.
     */
    @Transactional(readOnly = true)
    public List<EvaluacionCalificacionFila> listarFilasCalificar(Integer evaluacionReferenciaId) {
        Evaluacion ref = buscarPorId(evaluacionReferenciaId);
        Integer cursoId = ref.getCurso().getId();
        List<Inscripcion> inscripciones = inscripcionRepository.findByCursoIdWithCursoYEstudiante(cursoId);
        Map<Integer, Evaluacion> porInscripcion = evaluacionRepository
            .findGrupoPorCursoNombreFechaTipo(cursoId, ref.getNombre(), ref.getFechaEvaluacion(), ref.getTipo())
            .stream()
            .collect(Collectors.toMap(e -> e.getInscripcion().getId(), e -> e, (a, b) -> a));
        return inscripciones.stream()
            .filter(i -> !"RETIRADA".equals(i.getEstado()))
            .map(i -> new EvaluacionCalificacionFila(i, porInscripcion.get(i.getId())))
            .toList();
    }

    @Transactional
    public void guardarCalificacion(Integer evaluacionReferenciaId, Integer inscripcionId, BigDecimal nota, String comentario) {
        validarNota(nota);
        Evaluacion ref = buscarPorId(evaluacionReferenciaId);
        Inscripcion inscripcion = inscripcionRepository.findById(inscripcionId)
            .orElseThrow(() -> new IllegalArgumentException("Inscripción no válida."));
        if (!inscripcion.getCurso().getId().equals(ref.getCurso().getId())) {
            throw new IllegalArgumentException("La inscripción no pertenece al curso de la evaluación.");
        }
        Optional<Evaluacion> opt = evaluacionRepository.findByInscripcion_IdAndNombreAndFechaEvaluacionAndTipo(
            inscripcionId,
            ref.getNombre(),
            ref.getFechaEvaluacion(),
            ref.getTipo()
        );
        if (opt.isPresent()) {
            Evaluacion e = opt.get();
            e.setNota(nota);
            e.setComentario(blankToNull(comentario));
            evaluacionRepository.save(e);
        } else {
            crear(
                inscripcionId,
                ref.getTipo(),
                ref.getNombre(),
                ref.getDescripcion(),
                ref.getFechaEvaluacion(),
                nota,
                comentario
            );
        }
    }

    @Transactional
    public Evaluacion crear(
        Integer inscripcionId,
        String tipo,
        String nombre,
        String descripcion,
        LocalDate fechaEvaluacion,
        BigDecimal nota,
        String comentario
    ) {
        validarNota(nota);
        Inscripcion inscripcion = inscripcionRepository.findById(inscripcionId)
            .orElseThrow(() -> new IllegalArgumentException("Inscripción no válida."));
        Evaluacion evaluacion = new Evaluacion();
        evaluacion.setInscripcion(inscripcion);
        evaluacion.setCurso(inscripcion.getCurso());
        evaluacion.setTipo(normalizarTipo(tipo));
        evaluacion.setNombre(nombre.trim());
        evaluacion.setDescripcion(blankToNull(descripcion));
        evaluacion.setFechaEvaluacion(fechaEvaluacion == null ? LocalDate.now() : fechaEvaluacion);
        evaluacion.setNota(nota);
        evaluacion.setComentario(blankToNull(comentario));
        return evaluacionRepository.save(evaluacion);
    }

    @Transactional
    public Evaluacion actualizar(
        Integer id,
        String tipo,
        String nombre,
        String descripcion,
        LocalDate fechaEvaluacion,
        BigDecimal nota,
        String comentario
    ) {
        validarNota(nota);
        Evaluacion evaluacion = buscarPorId(id);
        evaluacion.setTipo(normalizarTipo(tipo));
        evaluacion.setNombre(nombre.trim());
        evaluacion.setDescripcion(blankToNull(descripcion));
        evaluacion.setFechaEvaluacion(fechaEvaluacion == null ? evaluacion.getFechaEvaluacion() : fechaEvaluacion);
        evaluacion.setNota(nota);
        evaluacion.setComentario(blankToNull(comentario));
        return evaluacionRepository.save(evaluacion);
    }

    @Transactional
    public void eliminar(Integer id) {
        evaluacionRepository.delete(buscarPorId(id));
    }

    public List<String> listarTiposDisponibles() {
        return List.of("EVALUACION", "TEORICA", "PROYECTO_ABP", "CUESTIONARIO", "EXAMEN_FINAL");
    }

    private static void validarNota(BigDecimal nota) {
        if (nota == null || nota.compareTo(BigDecimal.ZERO) < 0 || nota.compareTo(new BigDecimal("7.00")) > 0) {
            throw new IllegalArgumentException("La nota debe estar entre 0.00 y 7.00.");
        }
    }

    private static String normalizarTipo(String tipo) {
        String value = tipo == null ? "EVALUACION" : tipo.trim().toUpperCase();
        if (!TIPOS.contains(value)) {
            throw new IllegalArgumentException("Tipo de evaluación no válido.");
        }
        return value;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
