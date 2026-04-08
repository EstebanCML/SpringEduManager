package cl.untec.springedumanager.web.dto;

import java.time.LocalDate;

public record ApiCursoResponse(
    Integer id,
    String codigo,
    String nombre,
    String descripcion,
    Integer profesorId,
    Integer semestreId,
    String termSystem,
    Boolean anual,
    String periodoAcademico,
    LocalDate fechaInicio,
    LocalDate fechaFin,
    Boolean activo
) {}
