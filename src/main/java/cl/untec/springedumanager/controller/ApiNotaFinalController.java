package cl.untec.springedumanager.controller;

import cl.untec.springedumanager.model.NotaFinal;
import cl.untec.springedumanager.service.NotaFinalService;
import cl.untec.springedumanager.web.dto.ApiNotaFinalResponse;
import cl.untec.springedumanager.web.dto.ResumenNotasDto;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notas-finales")
public class ApiNotaFinalController {

    private final NotaFinalService notaFinalService;

    public ApiNotaFinalController(NotaFinalService notaFinalService) {
        this.notaFinalService = notaFinalService;
    }

    @GetMapping
    public List<ApiNotaFinalResponse> listar() {
        return notaFinalService.listarTodas().stream().map(this::toResponse).toList();
    }

    @GetMapping("/mis")
    public List<ApiNotaFinalResponse> misNotas(Authentication authentication) {
        return notaFinalService.listarPorUsername(authentication.getName()).stream().map(this::toResponse).toList();
    }

    @GetMapping("/inscripcion/{id}/resumen")
    public ResumenNotasDto resumen(@PathVariable Integer id) {
        return notaFinalService.calcularResumen(id);
    }

    @PostMapping("/inscripcion/{id}/cerrar")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiNotaFinalResponse cerrar(@PathVariable Integer id) {
        return toResponse(notaFinalService.cerrarSemestre(id));
    }

    private ApiNotaFinalResponse toResponse(NotaFinal nf) {
        return new ApiNotaFinalResponse(
            nf.getId(),
            nf.getEstudiante().getId(),
            nombreCompleto(nf),
            nf.getCurso().getId(),
            nf.getCurso().getNombre(),
            nf.getAcademicYear(),
            nf.getTermNumber(),
            nf.getPromedioPractica(),
            nf.getPromedioEvaluacion(),
            nf.getNotaExamenFinal(),
            nf.getNotaFinalSemestre(),
            nf.getFechaCierre()
        );
    }

    private String nombreCompleto(NotaFinal nf) {
        return (n(nf.getEstudiante().getUsuario().getNombre()) + " "
            + n(nf.getEstudiante().getUsuario().getPrimerApellido()) + " "
            + n(nf.getEstudiante().getUsuario().getSegundoApellido())).trim();
    }

    private String n(String value) {
        return value == null ? "" : value.trim();
    }
}
