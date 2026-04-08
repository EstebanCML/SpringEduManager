package cl.untec.springedumanager.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ApiPracticaResponse(
    Integer id,
    Integer cursoId,
    String cursoNombre,
    String nombre,
    String descripcion,
    LocalDate fechaInicio,
    LocalDate fechaEntrega,
    String estado,
    BigDecimal porcentajeNota
) {}
