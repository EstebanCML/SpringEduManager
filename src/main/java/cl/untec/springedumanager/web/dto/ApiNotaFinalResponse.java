package cl.untec.springedumanager.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ApiNotaFinalResponse(
    Integer id,
    Integer estudianteId,
    String estudianteNombre,
    Integer cursoId,
    String cursoNombre,
    Integer academicYear,
    Integer termNumber,
    BigDecimal promedioPractica,
    BigDecimal promedioEvaluacion,
    BigDecimal notaExamenFinal,
    BigDecimal notaFinalSemestre,
    LocalDate fechaCierre
) {}
