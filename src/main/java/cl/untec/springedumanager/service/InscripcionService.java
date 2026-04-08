package cl.untec.springedumanager.service;

import cl.untec.springedumanager.model.Inscripcion;
import cl.untec.springedumanager.model.Practica;
import cl.untec.springedumanager.model.PracticaCalificacion;
import cl.untec.springedumanager.repository.CursoRepository;
import cl.untec.springedumanager.repository.EstudianteRepository;
import cl.untec.springedumanager.repository.InscripcionRepository;
import cl.untec.springedumanager.repository.PracticaCalificacionRepository;
import cl.untec.springedumanager.repository.PracticaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InscripcionService {

    private static final Set<String> ESTADOS_VALIDOS = Set.of("PENDIENTE", "ACTIVA", "RETIRADA", "APROBADA", "REPROBADA");

    private final InscripcionRepository inscripcionRepository;
    private final EstudianteRepository estudianteRepository;
    private final CursoRepository cursoRepository;
    private final PracticaRepository practicaRepository;
    private final PracticaCalificacionRepository practicaCalificacionRepository;

    public InscripcionService(
        InscripcionRepository inscripcionRepository,
        EstudianteRepository estudianteRepository,
        CursoRepository cursoRepository,
        PracticaRepository practicaRepository,
        PracticaCalificacionRepository practicaCalificacionRepository
    ) {
        this.inscripcionRepository = inscripcionRepository;
        this.estudianteRepository = estudianteRepository;
        this.cursoRepository = cursoRepository;
        this.practicaRepository = practicaRepository;
        this.practicaCalificacionRepository = practicaCalificacionRepository;
    }

    @Transactional(readOnly = true)
    public List<Inscripcion> listarTodas() {
        return inscripcionRepository.findAllWithCursoYEstudiante();
    }

    @Transactional(readOnly = true)
    public List<Inscripcion> listarPorCurso(Integer cursoId) {
        return inscripcionRepository.findByCursoIdWithCursoYEstudiante(cursoId);
    }

    @Transactional(readOnly = true)
    public List<Inscripcion> listarPorUsername(String username) {
        return inscripcionRepository.findByUsernameWithCurso(username);
    }

    @Transactional(readOnly = true)
    public List<Inscripcion> listarPorProfesorUsername(String username) {
        return inscripcionRepository.findByProfesorUsernameWithCursoYEstudiante(username);
    }

    @Transactional(readOnly = true)
    public boolean perteneceAProfesor(Integer inscripcionId, String profesorUsername) {
        return inscripcionRepository.existsByIdAndCurso_Profesor_Usuario_Username(inscripcionId, profesorUsername);
    }

    @Transactional(readOnly = true)
    public Inscripcion buscarPorId(Integer id) {
        return inscripcionRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Inscripción no válida."));
    }

    @Transactional
    public Inscripcion crear(Integer estudianteId, Integer cursoId, LocalDate fechaInscripcion, String estado) {
        if (inscripcionRepository.existsByEstudiante_IdAndCurso_Id(estudianteId, cursoId)) {
            throw new IllegalArgumentException("El estudiante ya está inscrito en ese curso.");
        }
        String estadoNormalizado = normalizarEstado(estado);
        Inscripcion inscripcion = new Inscripcion();
        inscripcion.setEstudiante(estudianteRepository.findById(estudianteId)
            .orElseThrow(() -> new IllegalArgumentException("Estudiante no válido.")));
        inscripcion.setCurso(cursoRepository.findById(cursoId)
            .orElseThrow(() -> new IllegalArgumentException("Curso no válido.")));
        inscripcion.setFechaInscripcion(fechaInscripcion == null ? LocalDate.now() : fechaInscripcion);
        inscripcion.setEstado(estadoNormalizado);
        Inscripcion saved = inscripcionRepository.save(inscripcion);
        sincronizarPracticasExistentes(saved);
        return saved;
    }

    @Transactional
    public Inscripcion actualizar(Integer id, LocalDate fechaInscripcion, String estado) {
        Inscripcion inscripcion = buscarPorId(id);
        inscripcion.setFechaInscripcion(fechaInscripcion == null ? inscripcion.getFechaInscripcion() : fechaInscripcion);
        inscripcion.setEstado(normalizarEstado(estado));
        return inscripcionRepository.save(inscripcion);
    }

    @Transactional
    public void eliminar(Integer id) {
        inscripcionRepository.delete(buscarPorId(id));
    }

    public List<String> listarEstadosDisponibles() {
        return List.of("PENDIENTE", "ACTIVA", "RETIRADA", "APROBADA", "REPROBADA");
    }

    private String normalizarEstado(String estado) {
        String value = estado == null ? "PENDIENTE" : estado.trim().toUpperCase();
        if (!ESTADOS_VALIDOS.contains(value)) {
            throw new IllegalArgumentException("Estado de inscripción no válido.");
        }
        return value;
    }

    private void sincronizarPracticasExistentes(Inscripcion inscripcion) {
        if ("RETIRADA".equals(inscripcion.getEstado())) {
            return;
        }
        List<Practica> practicas = practicaRepository.findByCursoIdWithCurso(inscripcion.getCurso().getId());
        for (Practica practica : practicas) {
            if (practicaCalificacionRepository.findByPractica_IdAndInscripcion_Id(practica.getId(), inscripcion.getId()).isEmpty()) {
                PracticaCalificacion calificacion = new PracticaCalificacion();
                calificacion.setPractica(practica);
                calificacion.setInscripcion(inscripcion);
                practicaCalificacionRepository.save(calificacion);
            }
        }
    }
}
