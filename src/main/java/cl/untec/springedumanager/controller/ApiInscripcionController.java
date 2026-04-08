package cl.untec.springedumanager.controller;

import cl.untec.springedumanager.model.Inscripcion;
import cl.untec.springedumanager.service.InscripcionService;
import cl.untec.springedumanager.web.dto.ApiInscripcionResponse;
import cl.untec.springedumanager.web.dto.InscripcionForm;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/inscripciones")
public class ApiInscripcionController {

    private final InscripcionService inscripcionService;

    public ApiInscripcionController(InscripcionService inscripcionService) {
        this.inscripcionService = inscripcionService;
    }

    @GetMapping
    public List<ApiInscripcionResponse> listar(Authentication authentication) {
        if (tieneRol(authentication, "ROLE_PROFESOR") && !tieneRol(authentication, "ROLE_ADMIN")) {
            return inscripcionService.listarPorProfesorUsername(authentication.getName()).stream().map(this::toResponse).toList();
        }
        return inscripcionService.listarTodas().stream().map(this::toResponse).toList();
    }

    @GetMapping("/{id}")
    public ApiInscripcionResponse buscar(@PathVariable Integer id, Authentication authentication) {
        if (tieneRol(authentication, "ROLE_PROFESOR")
            && !tieneRol(authentication, "ROLE_ADMIN")
            && !inscripcionService.perteneceAProfesor(id, authentication.getName())) {
            throw new AccessDeniedException("No tienes permisos para ver esta inscripción.");
        }
        return toResponse(inscripcionService.buscarPorId(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiInscripcionResponse crear(@Valid @RequestBody InscripcionForm form) {
        return toResponse(inscripcionService.crear(
            form.getEstudianteId(),
            form.getCursoId(),
            form.getFechaInscripcion(),
            form.getEstado()
        ));
    }

    @PutMapping("/{id}")
    public ApiInscripcionResponse actualizar(@PathVariable Integer id, @Valid @RequestBody InscripcionForm form) {
        return toResponse(inscripcionService.actualizar(id, form.getFechaInscripcion(), form.getEstado()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Integer id) {
        inscripcionService.eliminar(id);
    }

    private ApiInscripcionResponse toResponse(Inscripcion i) {
        return new ApiInscripcionResponse(
            i.getId(),
            i.getEstudiante().getId(),
            i.getEstudiante().getCodigoEstudiante(),
            nombreCompleto(i),
            i.getCurso().getId(),
            i.getCurso().getCodigo(),
            i.getCurso().getNombre(),
            i.getFechaInscripcion(),
            i.getEstado()
        );
    }

    private String nombreCompleto(Inscripcion i) {
        return (n(i.getEstudiante().getUsuario().getNombre()) + " "
            + n(i.getEstudiante().getUsuario().getPrimerApellido()) + " "
            + n(i.getEstudiante().getUsuario().getSegundoApellido())).trim();
    }

    private String n(String value) {
        return value == null ? "" : value.trim();
    }

    private static boolean tieneRol(Authentication authentication, String rol) {
        return authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .anyMatch(rol::equals);
    }
}
