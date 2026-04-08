package cl.untec.springedumanager.controller;

import cl.untec.springedumanager.model.Evaluacion;
import cl.untec.springedumanager.service.EvaluacionService;
import cl.untec.springedumanager.service.InscripcionService;
import cl.untec.springedumanager.web.dto.ApiEvaluacionResponse;
import cl.untec.springedumanager.web.dto.EvaluacionForm;
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
@RequestMapping("/api/evaluaciones")
public class ApiEvaluacionController {

    private final EvaluacionService evaluacionService;
    private final InscripcionService inscripcionService;

    public ApiEvaluacionController(EvaluacionService evaluacionService, InscripcionService inscripcionService) {
        this.evaluacionService = evaluacionService;
        this.inscripcionService = inscripcionService;
    }

    @GetMapping
    public List<ApiEvaluacionResponse> listar(Authentication authentication) {
        if (tieneRol(authentication, "ROLE_PROFESOR") && !tieneRol(authentication, "ROLE_ADMIN")) {
            return evaluacionService.listarPorProfesorUsername(authentication.getName()).stream().map(this::toResponse).toList();
        }
        return evaluacionService.listarTodas().stream().map(this::toResponse).toList();
    }

    @GetMapping("/{id}")
    public ApiEvaluacionResponse buscar(@PathVariable Integer id, Authentication authentication) {
        validarAccesoProfesor(evaluacionService.perteneceAProfesor(id, authentication.getName()), authentication);
        return toResponse(evaluacionService.buscarPorId(id));
    }

    @GetMapping("/mis")
    public List<ApiEvaluacionResponse> misEvaluaciones(Authentication authentication) {
        return evaluacionService.listarPorUsername(authentication.getName()).stream().map(this::toResponse).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiEvaluacionResponse crear(Authentication authentication, @Valid @RequestBody EvaluacionForm form) {
        validarAccesoProfesor(inscripcionService.perteneceAProfesor(form.getInscripcionId(), authentication.getName()), authentication);
        return toResponse(evaluacionService.crear(
            form.getInscripcionId(),
            form.getTipo(),
            form.getNombre(),
            form.getDescripcion(),
            form.getFechaEvaluacion(),
            form.getNota(),
            form.getComentario()
        ));
    }

    @PutMapping("/{id}")
    public ApiEvaluacionResponse actualizar(
        @PathVariable Integer id,
        Authentication authentication,
        @Valid @RequestBody EvaluacionForm form
    ) {
        validarAccesoProfesor(evaluacionService.perteneceAProfesor(id, authentication.getName()), authentication);
        return toResponse(evaluacionService.actualizar(
            id,
            form.getTipo(),
            form.getNombre(),
            form.getDescripcion(),
            form.getFechaEvaluacion(),
            form.getNota(),
            form.getComentario()
        ));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Integer id, Authentication authentication) {
        validarAccesoProfesor(evaluacionService.perteneceAProfesor(id, authentication.getName()), authentication);
        evaluacionService.eliminar(id);
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

    private ApiEvaluacionResponse toResponse(Evaluacion evaluacion) {
        return new ApiEvaluacionResponse(
            evaluacion.getId(),
            evaluacion.getInscripcion().getId(),
            evaluacion.getCurso().getId(),
            nombreCompleto(evaluacion),
            evaluacion.getCurso().getNombre(),
            evaluacion.getTipo(),
            evaluacion.getNombre(),
            evaluacion.getDescripcion(),
            evaluacion.getFechaEvaluacion(),
            evaluacion.getNota(),
            evaluacion.getComentario()
        );
    }

    private String nombreCompleto(Evaluacion e) {
        return (n(e.getInscripcion().getEstudiante().getUsuario().getNombre()) + " "
            + n(e.getInscripcion().getEstudiante().getUsuario().getPrimerApellido()) + " "
            + n(e.getInscripcion().getEstudiante().getUsuario().getSegundoApellido())).trim();
    }

    private String n(String value) {
        return value == null ? "" : value.trim();
    }
}
