package cl.untec.springedumanager.web.dto;

import java.util.List;

public record ApiMeResponse(
    Integer id,
    String username,
    String nombreCompleto,
    String email,
    String emailPersonal,
    Boolean activo,
    List<String> roles
) {}
