package cl.untec.springedumanager.service;

import cl.untec.springedumanager.model.PonderacionCurso;
import cl.untec.springedumanager.repository.PonderacionCursoRepository;
import java.math.BigDecimal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PonderacionCursoService {

    private final PonderacionCursoRepository ponderacionCursoRepository;

    public PonderacionCursoService(PonderacionCursoRepository ponderacionCursoRepository) {
        this.ponderacionCursoRepository = ponderacionCursoRepository;
    }

    @Transactional(readOnly = true)
    public PonderacionCurso obtenerActual() {
        return ponderacionCursoRepository.findFirstByOrderByIdAsc()
            .orElseGet(this::crearDefault);
    }

    @Transactional
    public PonderacionCurso actualizar(
        BigDecimal porcentajePracticas,
        BigDecimal porcentajeEvaluaciones,
        BigDecimal porcentajeEvaluacionesParciales,
        BigDecimal porcentajeExamenFinal
    ) {
        validarSuma(porcentajePracticas, porcentajeEvaluaciones, "Las ponderaciones globales deben sumar 100.");
        validarSuma(
            porcentajeEvaluacionesParciales,
            porcentajeExamenFinal,
            "Las ponderaciones internas de evaluación deben sumar 100."
        );
        PonderacionCurso ponderacion = obtenerActual();
        ponderacion.setPorcentajePracticas(porcentajePracticas);
        ponderacion.setPorcentajeEvaluaciones(porcentajeEvaluaciones);
        ponderacion.setPorcentajeEvaluacionesParciales(porcentajeEvaluacionesParciales);
        ponderacion.setPorcentajeExamenFinal(porcentajeExamenFinal);
        return ponderacionCursoRepository.save(ponderacion);
    }

    private PonderacionCurso crearDefault() {
        PonderacionCurso ponderacion = new PonderacionCurso();
        return ponderacionCursoRepository.save(ponderacion);
    }

    private static void validarSuma(BigDecimal a, BigDecimal b, String message) {
        if (a == null || b == null || a.add(b).compareTo(new BigDecimal("100.00")) != 0) {
            throw new IllegalArgumentException(message);
        }
    }
}
