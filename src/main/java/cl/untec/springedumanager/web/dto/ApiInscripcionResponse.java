package cl.untec.springedumanager.web.dto;

import java.time.LocalDate;

public record ApiInscripcionResponse(
    Integer id,
    Integer estudianteId,
    String codigoEstudiante,
    String estudianteNombre,
    Integer cursoId,
    String cursoCodigo,
    String cursoNombre,
    LocalDate fechaInscripcion,
    String estado
) {}
