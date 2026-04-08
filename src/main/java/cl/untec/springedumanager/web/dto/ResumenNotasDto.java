package cl.untec.springedumanager.web.dto;

import java.math.BigDecimal;

public record ResumenNotasDto(
    Integer inscripcionId,
    Integer estudianteId,
    Integer cursoId,
    BigDecimal promedioPractica,
    BigDecimal promedioEvaluacion,
    BigDecimal notaExamenFinal,
    BigDecimal notaFinalSemestre
) {}
