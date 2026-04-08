package cl.untec.springedumanager.web.dto;

import java.math.BigDecimal;

public record ApiPonderacionCursoResponse(
    Integer id,
    BigDecimal porcentajePracticas,
    BigDecimal porcentajeEvaluaciones,
    BigDecimal porcentajeEvaluacionesParciales,
    BigDecimal porcentajeExamenFinal
) {}
