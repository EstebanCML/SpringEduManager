package cl.untec.springedumanager.controller;

import cl.untec.springedumanager.model.Curso;
import cl.untec.springedumanager.service.CursoService;
import cl.untec.springedumanager.web.dto.ApiCursoResponse;
import cl.untec.springedumanager.web.dto.ApiCursoUpsertRequest;
import cl.untec.springedumanager.web.dto.NuevoCursoForm;
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
@RequestMapping("/api/cursos")
public class ApiCursoController {

    private final CursoService cursoService;

    public ApiCursoController(CursoService cursoService) {
        this.cursoService = cursoService;
    }

    @GetMapping
    public List<ApiCursoResponse> listar() {
        return cursoService.listarTodos().stream()
            .map(this::toResponse)
            .toList();
    }

    @GetMapping("/{id}")
    public ApiCursoResponse buscarPorId(@PathVariable Integer id) {
        return toResponse(cursoService.buscarPorId(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiCursoResponse crear(@Valid @RequestBody ApiCursoUpsertRequest request) {
        NuevoCursoForm form = toForm(request);
        cursoService.registrar(form);
        Curso creado = cursoService.buscarPorCodigo(form.getCodigo().trim());
        return toResponse(creado);
    }

    @PutMapping("/{id}")
    public ApiCursoResponse actualizar(@PathVariable Integer id, @Valid @RequestBody ApiCursoUpsertRequest request) {
        cursoService.actualizar(id, toForm(request));
        return toResponse(cursoService.buscarPorId(id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Integer id) {
        cursoService.eliminar(id);
    }

    private NuevoCursoForm toForm(ApiCursoUpsertRequest request) {
        NuevoCursoForm form = new NuevoCursoForm();
        form.setCodigo(request.getCodigo());
        form.setNombre(request.getNombre());
        form.setDescripcion(request.getDescripcion());
        form.setProfesorId(request.getProfesorId());
        form.setAnual(request.isAnual());
        form.setPeriodoIndice(request.getPeriodoIndice());
        form.setFechaInicio(request.getFechaInicio());
        form.setFechaFin(request.getFechaFin());
        form.setActivo(request.isActivo());
        return form;
    }

    private ApiCursoResponse toResponse(Curso curso) {
        return new ApiCursoResponse(
            curso.getId(),
            curso.getCodigo(),
            curso.getNombre(),
            curso.getDescripcion(),
            curso.getProfesor().getId(),
            curso.getSemestre().getId(),
            curso.getSemestre().getTermSystem(),
            curso.getAnual(),
            curso.getPeriodoAcademico(),
            curso.getFechaInicio(),
            curso.getFechaFin(),
            curso.getActivo()
        );
    }
}
