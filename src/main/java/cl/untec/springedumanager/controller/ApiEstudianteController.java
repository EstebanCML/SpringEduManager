package cl.untec.springedumanager.controller;

import cl.untec.springedumanager.model.Estudiante;
import cl.untec.springedumanager.service.EstudianteService;
import cl.untec.springedumanager.web.dto.ApiEstudianteCreateRequest;
import cl.untec.springedumanager.web.dto.ApiEstudianteResponse;
import cl.untec.springedumanager.web.dto.ApiEstudianteUpdateRequest;
import cl.untec.springedumanager.web.dto.NuevoEstudianteForm;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
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
@RequestMapping("/api/estudiantes")
public class ApiEstudianteController {

    private final EstudianteService estudianteService;

    public ApiEstudianteController(EstudianteService estudianteService) {
        this.estudianteService = estudianteService;
    }

    @GetMapping
    public List<ApiEstudianteResponse> listar() {
        return estudianteService.listarTodos().stream().map(this::toResponse).toList();
    }

    @GetMapping("/{id}")
    public ApiEstudianteResponse buscarPorId(@PathVariable Integer id) {
        return toResponse(estudianteService.buscarPorId(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiEstudianteResponse crear(@Valid @RequestBody ApiEstudianteCreateRequest request) {
        NuevoEstudianteForm form = new NuevoEstudianteForm();
        form.setNombre(request.getNombre());
        form.setSegundoNombre(request.getSegundoNombre());
        form.setPrimerApellido(request.getPrimerApellido());
        form.setSegundoApellido(request.getSegundoApellido());
        form.setPassword(request.getPassword());
        form.setEmailPersonal(request.getEmailPersonal());
        form.setSede(request.getSede());
        Estudiante creado = estudianteService.registrar(form);
        return toResponse(creado);
    }

    @PutMapping("/{id}")
    public ApiEstudianteResponse actualizar(@PathVariable Integer id, @Valid @RequestBody ApiEstudianteUpdateRequest request) {
        estudianteService.actualizarDesdeApi(id, request);
        return toResponse(estudianteService.buscarPorId(id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Integer id) {
        estudianteService.eliminarDesdeApi(id);
    }

    private ApiEstudianteResponse toResponse(Estudiante e) {
        String nombreCompleto = (n(e.getUsuario().getNombre()) + " "
            + n(e.getUsuario().getSegundoNombre()) + " "
            + n(e.getUsuario().getPrimerApellido()) + " "
            + n(e.getUsuario().getSegundoApellido()))
            .trim()
            .replaceAll("\\s+", " ");
        return new ApiEstudianteResponse(
            e.getId(),
            e.getCodigoEstudiante(),
            e.getSede(),
            e.getUsuario().getId(),
            e.getUsuario().getUsername(),
            nombreCompleto,
            e.getUsuario().getEmail(),
            e.getUsuario().getEmailPersonal(),
            e.getUsuario().getActivo()
        );
    }

    private static String n(String value) {
        return value == null ? "" : value.trim();
    }
}
