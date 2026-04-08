package cl.untec.springedumanager.service;

import cl.untec.springedumanager.model.Curso;
import cl.untec.springedumanager.model.Evaluacion;
import cl.untec.springedumanager.model.Inscripcion;
import cl.untec.springedumanager.model.NotaFinal;
import cl.untec.springedumanager.model.PonderacionCurso;
import cl.untec.springedumanager.model.PracticaCalificacion;
import cl.untec.springedumanager.repository.EvaluacionRepository;
import cl.untec.springedumanager.repository.InscripcionRepository;
import cl.untec.springedumanager.repository.NotaFinalRepository;
import cl.untec.springedumanager.repository.PracticaCalificacionRepository;
import cl.untec.springedumanager.web.dto.ResumenNotasDto;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotaFinalService {

    private final NotaFinalRepository notaFinalRepository;
    private final PracticaCalificacionRepository practicaCalificacionRepository;
    private final EvaluacionRepository evaluacionRepository;
    private final InscripcionRepository inscripcionRepository;
    private final PonderacionCursoService ponderacionCursoService;

    public NotaFinalService(
        NotaFinalRepository notaFinalRepository,
        PracticaCalificacionRepository practicaCalificacionRepository,
        EvaluacionRepository evaluacionRepository,
        InscripcionRepository inscripcionRepository,
        PonderacionCursoService ponderacionCursoService
    ) {
        this.notaFinalRepository = notaFinalRepository;
        this.practicaCalificacionRepository = practicaCalificacionRepository;
        this.evaluacionRepository = evaluacionRepository;
        this.inscripcionRepository = inscripcionRepository;
        this.ponderacionCursoService = ponderacionCursoService;
    }

    @Transactional(readOnly = true)
    public List<NotaFinal> listarTodas() {
        return notaFinalRepository.findAllWithCursoYEstudiante();
    }

    @Transactional(readOnly = true)
    public List<NotaFinal> listarPorCurso(Integer cursoId) {
        return notaFinalRepository.findByCurso_IdOrderByIdDesc(cursoId);
    }

    @Transactional(readOnly = true)
    public List<NotaFinal> listarPorUsername(String username) {
        return notaFinalRepository.findByUsernameWithCurso(username);
    }

    @Transactional(readOnly = true)
    public ResumenNotasDto calcularResumen(Integer inscripcionId) {
        Inscripcion inscripcion = inscripcionRepository.findById(inscripcionId)
            .orElseThrow(() -> new IllegalArgumentException("Inscripción no válida."));

        BigDecimal promedioPractica = calcularPromedioPracticas(inscripcionId);
        BigDecimal promedioEvaluacion = calcularPromedioEvaluacionesSinExamen(inscripcionId);
        BigDecimal notaExamenFinal = obtenerExamenFinal(inscripcionId);
        BigDecimal notaFinalSemestre = calcularNotaFinal(promedioPractica, promedioEvaluacion, notaExamenFinal);

        return new ResumenNotasDto(
            inscripcion.getId(),
            inscripcion.getEstudiante().getId(),
            inscripcion.getCurso().getId(),
            promedioPractica,
            promedioEvaluacion,
            notaExamenFinal,
            notaFinalSemestre
        );
    }

    @Transactional
    public NotaFinal cerrarSemestre(Integer inscripcionId) {
        Inscripcion inscripcion = inscripcionRepository.findById(inscripcionId)
            .orElseThrow(() -> new IllegalArgumentException("Inscripción no válida."));
        ResumenNotasDto resumen = calcularResumen(inscripcionId);
        Curso curso = inscripcion.getCurso();
        Integer academicYear = curso.getFechaInicio() == null ? LocalDate.now().getYear() : curso.getFechaInicio().getYear();
        Integer termNumber = resolverNumeroPeriodo(curso);

        NotaFinal notaFinal = notaFinalRepository
            .findByEstudiante_IdAndCurso_IdAndAcademicYearAndTermNumber(
                inscripcion.getEstudiante().getId(),
                curso.getId(),
                academicYear,
                termNumber
            )
            .orElseGet(NotaFinal::new);

        notaFinal.setEstudiante(inscripcion.getEstudiante());
        notaFinal.setCurso(curso);
        notaFinal.setAcademicYear(academicYear);
        notaFinal.setTermNumber(termNumber);
        notaFinal.setPromedioPractica(resumen.promedioPractica());
        notaFinal.setPromedioEvaluacion(resumen.promedioEvaluacion());
        notaFinal.setNotaExamenFinal(resumen.notaExamenFinal());
        notaFinal.setNotaFinalSemestre(resumen.notaFinalSemestre());
        notaFinal.setFechaCierre(LocalDate.now());
        return notaFinalRepository.save(notaFinal);
    }

    private BigDecimal calcularPromedioPracticas(Integer inscripcionId) {
        List<PracticaCalificacion> practicas = practicaCalificacionRepository.findByInscripcionId(inscripcionId);
        if (practicas.isEmpty()) {
            throw new IllegalArgumentException("No hay prácticas registradas para esta inscripción.");
        }
        BigDecimal suma = BigDecimal.ZERO;
        int count = 0;
        for (PracticaCalificacion practica : practicas) {
            if (practica.getNota() == null) {
                continue;
            }
            suma = suma.add(practica.getNota());
            count++;
        }
        if (count == 0) {
            throw new IllegalArgumentException("Aún no hay notas de prácticas suficientes para calcular el promedio.");
        }
        return suma.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal calcularPromedioEvaluacionesSinExamen(Integer inscripcionId) {
        List<Evaluacion> evaluaciones = evaluacionRepository.findByInscripcion_Id(inscripcionId);
        BigDecimal suma = BigDecimal.ZERO;
        int count = 0;
        for (Evaluacion evaluacion : evaluaciones) {
            if ("EXAMEN_FINAL".equals(evaluacion.getTipo())) {
                continue;
            }
            suma = suma.add(evaluacion.getNota());
            count++;
        }
        if (count == 0) {
            throw new IllegalArgumentException("No hay evaluaciones parciales registradas para esta inscripción.");
        }
        return suma.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal obtenerExamenFinal(Integer inscripcionId) {
        return evaluacionRepository.findFirstByInscripcion_IdAndTipoOrderByFechaEvaluacionDesc(inscripcionId, "EXAMEN_FINAL")
            .map(Evaluacion::getNota)
            .orElseThrow(() -> new IllegalArgumentException("Debes registrar el examen final antes de cerrar el semestre."));
    }

    private BigDecimal calcularNotaFinal(
        BigDecimal promedioPractica,
        BigDecimal promedioEvaluacion,
        BigDecimal notaExamenFinal
    ) {
        PonderacionCurso ponderacion = ponderacionCursoService.obtenerActual();
        BigDecimal practicaBloque = promedioPractica.multiply(ponderacion.getPorcentajePracticas())
            .divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
        BigDecimal evaluacionBase = promedioEvaluacion.multiply(ponderacion.getPorcentajeEvaluacionesParciales())
            .divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
        BigDecimal examenBase = notaExamenFinal.multiply(ponderacion.getPorcentajeExamenFinal())
            .divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
        BigDecimal evaluacionBloque = evaluacionBase.add(examenBase)
            .multiply(ponderacion.getPorcentajeEvaluaciones())
            .divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
        return practicaBloque.add(evaluacionBloque).setScale(2, RoundingMode.HALF_UP);
    }

    private Integer resolverNumeroPeriodo(Curso curso) {
        if (curso.getPeriodoAcademico() == null || curso.getPeriodoAcademico().isBlank() || "ANUAL".equalsIgnoreCase(curso.getPeriodoAcademico())) {
            return 1;
        }
        if (curso.getPeriodoAcademico().toLowerCase().contains("primer")) {
            return 1;
        }
        if (curso.getPeriodoAcademico().toLowerCase().contains("segundo")) {
            return 2;
        }
        if (curso.getPeriodoAcademico().toLowerCase().contains("tercer")) {
            return 3;
        }
        if (curso.getPeriodoAcademico().toLowerCase().contains("cuarto")) {
            return 4;
        }
        return 1;
    }
}
