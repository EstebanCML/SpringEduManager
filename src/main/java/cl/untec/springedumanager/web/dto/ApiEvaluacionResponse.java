package cl.untec.springedumanager.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ApiEvaluacionResponse(
    Integer id,
    Integer inscripcionId,
    Integer cursoId,
    String estudianteNombre,
    String cursoNombre,
    String tipo,
    String nombre,
    String descripcion,
    LocalDate fechaEvaluacion,
    BigDecimal nota,
    String comentario
) {}
