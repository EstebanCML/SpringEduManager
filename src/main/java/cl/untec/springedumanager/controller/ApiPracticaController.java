package cl.untec.springedumanager.controller;

import cl.untec.springedumanager.model.Practica;
import cl.untec.springedumanager.model.PracticaCalificacion;
import cl.untec.springedumanager.service.CursoService;
import cl.untec.springedumanager.service.InscripcionService;
import cl.untec.springedumanager.service.PracticaService;
import cl.untec.springedumanager.web.dto.ApiPracticaCalificacionResponse;
import cl.untec.springedumanager.web.dto.ApiPracticaResponse;
import cl.untec.springedumanager.web.dto.PracticaCalificacionForm;
import cl.untec.springedumanager.web.dto.PracticaForm;
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
@RequestMapping("/api/practicas")
public class ApiPracticaController {

    private final PracticaService practicaService;
    private final CursoService cursoService;
    private final InscripcionService inscripcionService;

    public ApiPracticaController(
        PracticaService practicaService,
        CursoService cursoService,
        InscripcionService inscripcionService
    ) {
        this.practicaService = practicaService;
        this.cursoService = cursoService;
        this.inscripcionService = inscripcionService;
    }

    @GetMapping
    public List<ApiPracticaResponse> listar(Authentication authentication) {
        if (tieneRol(authentication, "ROLE_PROFESOR") && !tieneRol(authentication, "ROLE_ADMIN")) {
            return practicaService.listarPorProfesorUsername(authentication.getName()).stream().map(this::toResponse).toList();
        }
        return practicaService.listarTodas().stream().map(this::toResponse).toList();
    }

    @GetMapping("/{id}")
    public ApiPracticaResponse buscar(@PathVariable Integer id, Authentication authentication) {
        validarAccesoProfesor(practicaService.perteneceAProfesor(id, authentication.getName()), authentication);
        return toResponse(practicaService.buscarPorId(id));
    }

    @GetMapping("/{id}/calificaciones")
    public List<ApiPracticaCalificacionResponse> listarCalificaciones(@PathVariable Integer id, Authentication authentication) {
        validarAccesoProfesor(practicaService.perteneceAProfesor(id, authentication.getName()), authentication);
        return practicaService.listarCalificaciones(id).stream().map(this::toCalificacionResponse).toList();
    }

    @GetMapping("/mis")
    public List<ApiPracticaCalificacionResponse> misPracticas(Authentication authentication) {
        return practicaService.listarCalificacionesPorUsername(authentication.getName()).stream()
            .map(this::toCalificacionResponse)
            .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiPracticaResponse crear(Authentication authentication, @Valid @RequestBody PracticaForm form) {
        if (tieneRol(authentication, "ROLE_PROFESOR") && !tieneRol(authentication, "ROLE_ADMIN")) {
            validarAccesoProfesor(
                cursoService.listarPorProfesorUsername(authentication.getName()).stream().anyMatch(c -> c.getId().equals(form.getCursoId())),
                authentication
            );
        }
        return toResponse(practicaService.crear(
            form.getCursoId(),
            form.getNombre(),
            form.getDescripcion(),
            form.getFechaInicio(),
            form.getFechaEntrega(),
            form.getEstado()
        ));
    }

    @PutMapping("/{id}")
    public ApiPracticaResponse actualizar(@PathVariable Integer id, Authentication authentication, @Valid @RequestBody PracticaForm form) {
        validarAccesoProfesor(practicaService.perteneceAProfesor(id, authentication.getName()), authentication);
        return toResponse(practicaService.actualizar(
            id,
            form.getNombre(),
            form.getDescripcion(),
            form.getFechaInicio(),
            form.getFechaEntrega(),
            form.getEstado()
        ));
    }

    @PutMapping("/{id}/calificaciones/{inscripcionId}")
    public ApiPracticaCalificacionResponse calificar(
        @PathVariable Integer id,
        @PathVariable Integer inscripcionId,
        Authentication authentication,
        @Valid @RequestBody PracticaCalificacionForm form
    ) {
        validarAccesoProfesor(practicaService.perteneceAProfesor(id, authentication.getName()), authentication);
        validarAccesoProfesor(inscripcionService.perteneceAProfesor(inscripcionId, authentication.getName()), authentication);
        return toCalificacionResponse(practicaService.calificar(id, inscripcionId, form.getNota(), form.getEstado(), form.getComentario()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Integer id, Authentication authentication) {
        validarAccesoProfesor(practicaService.perteneceAProfesor(id, authentication.getName()), authentication);
        practicaService.eliminar(id);
    }

    private static boolean tieneRol(Authentication authentication, String rol) {
        return authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .anyMatch(rol::equals);
    }

    private static void validarAccesoProfesor(boolean permitido, Authentication authentication) {
        if (!tieneRol(authentication, "ROLE_PROFESOR") || tieneRol(authentication, "ROLE_ADMIN")) {
            return;
        }
        if (!permitido) {
            throw new AccessDeniedException("No tienes permisos para operar sobre cursos no asignados.");
        }
    }

    private ApiPracticaResponse toResponse(Practica practica) {
        return new ApiPracticaResponse(
            practica.getId(),
            practica.getCurso().getId(),
            practica.getCurso().getNombre(),
            practica.getNombre(),
            practica.getDescripcion(),
            practica.getFechaInicio(),
            practica.getFechaEntrega(),
            practica.getEstado(),
            practica.getPorcentajeNota()
        );
    }

    private ApiPracticaCalificacionResponse toCalificacionResponse(PracticaCalificacion c) {
        return new ApiPracticaCalificacionResponse(
            c.getId(),
            c.getPractica().getId(),
            c.getInscripcion().getId(),
            nombreCompleto(c),
            c.getEstado(),
            c.getNota(),
            c.getComentario()
        );
    }

    private String nombreCompleto(PracticaCalificacion c) {
        return (n(c.getInscripcion().getEstudiante().getUsuario().getNombre()) + " "
            + n(c.getInscripcion().getEstudiante().getUsuario().getPrimerApellido()) + " "
            + n(c.getInscripcion().getEstudiante().getUsuario().getSegundoApellido())).trim();
    }

    private String n(String value) {
        return value == null ? "" : value.trim();
    }
}
