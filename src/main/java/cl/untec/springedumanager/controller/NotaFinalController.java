package cl.untec.springedumanager.controller;

import cl.untec.springedumanager.service.EvaluacionService;
import cl.untec.springedumanager.service.InscripcionService;
import cl.untec.springedumanager.service.NotaFinalService;
import cl.untec.springedumanager.service.PracticaService;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class NotaFinalController {

    private final NotaFinalService notaFinalService;
    private final InscripcionService inscripcionService;
    private final EvaluacionService evaluacionService;
    private final PracticaService practicaService;

    public NotaFinalController(
        NotaFinalService notaFinalService,
        InscripcionService inscripcionService,
        EvaluacionService evaluacionService,
        PracticaService practicaService
    ) {
        this.notaFinalService = notaFinalService;
        this.inscripcionService = inscripcionService;
        this.evaluacionService = evaluacionService;
        this.practicaService = practicaService;
    }

    @GetMapping("/notas-finales")
    public String listar(Model model) {
        model.addAttribute("notasFinales", notaFinalService.listarTodas());
        return "notas/lista-nota-final";
    }

    @GetMapping("/notas-finales/inscripcion/{id}")
    public String resumen(@PathVariable Integer id, Model model) {
        model.addAttribute("inscripcion", inscripcionService.buscarPorId(id));
        model.addAttribute("resumen", notaFinalService.calcularResumen(id));
        return "notas/resumen-nota-final";
    }

    @PostMapping("/notas-finales/inscripcion/{id}/cerrar")
    public String cerrar(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        notaFinalService.cerrarSemestre(id);
        redirectAttributes.addFlashAttribute("successMessage", "Nota final cerrada correctamente.");
        return "redirect:/notas-finales";
    }

    @GetMapping("/mis-notas")
    public String misNotas(Authentication authentication, Model model) {
        String username = authentication.getName();
        var evaluaciones = evaluacionService.listarPorUsername(username);
        var practicas = practicaService.listarCalificacionesPorUsername(username);
        var notasFinales = notaFinalService.listarPorUsername(username);
        var inscripciones = inscripcionService.listarPorUsername(username);
        model.addAttribute("evaluaciones", evaluaciones);
        model.addAttribute("practicas", practicas);
        model.addAttribute("notasFinales", notasFinales);
        model.addAttribute("inscripciones", inscripciones);
        model.addAttribute("evaluacionesPorCurso", agruparPorCursoEvaluaciones(evaluaciones));
        model.addAttribute("practicasPorCurso", agruparPorCursoPracticas(practicas));
        model.addAttribute("notasFinalesPorCurso", agruparPorCursoNotasFinales(notasFinales));
        model.addAttribute("inscripcionesPorCurso", agruparPorCursoInscripciones(inscripciones));
        return "notas/mis-notas";
    }

    @GetMapping("/mis-eventos-notas")
    public String misEventosNotas(Authentication authentication, Model model) {
        String username = authentication.getName();
        model.addAttribute("evaluaciones", evaluacionService.listarPorUsername(username));
        model.addAttribute("practicas", practicaService.listarCalificacionesPorUsername(username));
        return "notas/mis-eventos-notas";
    }

    private Map<String, List<cl.untec.springedumanager.model.Evaluacion>> agruparPorCursoEvaluaciones(
        List<cl.untec.springedumanager.model.Evaluacion> evaluaciones
    ) {
        return evaluaciones.stream()
            .collect(Collectors.groupingBy(e -> nombreCurso(e.getCurso().getNombre()), LinkedHashMap::new, Collectors.toList()));
    }

    private Map<String, List<cl.untec.springedumanager.model.PracticaCalificacion>> agruparPorCursoPracticas(
        List<cl.untec.springedumanager.model.PracticaCalificacion> practicas
    ) {
        return practicas.stream()
            .collect(Collectors.groupingBy(
                p -> nombreCurso(p.getPractica().getCurso().getNombre()),
                LinkedHashMap::new,
                Collectors.toList()
            ));
    }

    private Map<String, List<cl.untec.springedumanager.model.NotaFinal>> agruparPorCursoNotasFinales(
        List<cl.untec.springedumanager.model.NotaFinal> notasFinales
    ) {
        return notasFinales.stream()
            .collect(Collectors.groupingBy(n -> nombreCurso(n.getCurso().getNombre()), LinkedHashMap::new, Collectors.toList()));
    }

    private Map<String, List<cl.untec.springedumanager.model.Inscripcion>> agruparPorCursoInscripciones(
        List<cl.untec.springedumanager.model.Inscripcion> inscripciones
    ) {
        return inscripciones.stream()
            .collect(Collectors.groupingBy(i -> nombreCurso(i.getCurso().getNombre()), LinkedHashMap::new, Collectors.toList()));
    }

    private String nombreCurso(String nombre) {
        return nombre == null || nombre.isBlank() ? "Curso sin nombre" : nombre.trim();
    }
}
