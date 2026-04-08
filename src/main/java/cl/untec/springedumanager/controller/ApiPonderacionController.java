package cl.untec.springedumanager.controller;

import cl.untec.springedumanager.model.PonderacionCurso;
import cl.untec.springedumanager.service.PonderacionCursoService;
import cl.untec.springedumanager.web.dto.ApiPonderacionCursoResponse;
import cl.untec.springedumanager.web.dto.PonderacionCursoForm;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ponderaciones")
public class ApiPonderacionController {

    private final PonderacionCursoService ponderacionCursoService;

    public ApiPonderacionController(PonderacionCursoService ponderacionCursoService) {
        this.ponderacionCursoService = ponderacionCursoService;
    }

    @GetMapping
    public ApiPonderacionCursoResponse obtener() {
        return toResponse(ponderacionCursoService.obtenerActual());
    }

    @PutMapping
    public ApiPonderacionCursoResponse actualizar(@Valid @RequestBody PonderacionCursoForm form) {
        return toResponse(ponderacionCursoService.actualizar(
            form.getPorcentajePracticas(),
            form.getPorcentajeEvaluaciones(),
            form.getPorcentajeEvaluacionesParciales(),
            form.getPorcentajeExamenFinal()
        ));
    }

    private ApiPonderacionCursoResponse toResponse(PonderacionCurso p) {
        return new ApiPonderacionCursoResponse(
            p.getId(),
            p.getPorcentajePracticas(),
            p.getPorcentajeEvaluaciones(),
            p.getPorcentajeEvaluacionesParciales(),
            p.getPorcentajeExamenFinal()
        );
    }
}
