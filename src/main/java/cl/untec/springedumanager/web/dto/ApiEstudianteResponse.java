package cl.untec.springedumanager.web.dto;

public record ApiEstudianteResponse(
    Integer id,
    String codigoEstudiante,
    String sede,
    Integer usuarioId,
    String username,
    String nombreCompleto,
    String email,
    String emailPersonal,
    Boolean activo
) {}
