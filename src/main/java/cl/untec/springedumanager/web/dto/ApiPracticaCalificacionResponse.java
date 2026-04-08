package cl.untec.springedumanager.web.dto;

import java.math.BigDecimal;

public record ApiPracticaCalificacionResponse(
    Integer id,
    Integer practicaId,
    Integer inscripcionId,
    String estudianteNombre,
    String estado,
    BigDecimal nota,
    String comentario
) {}
