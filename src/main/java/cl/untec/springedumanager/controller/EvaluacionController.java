package cl.untec.springedumanager.controller;

import cl.untec.springedumanager.model.Evaluacion;
import cl.untec.springedumanager.service.EvaluacionService;
import cl.untec.springedumanager.service.InscripcionService;
import cl.untec.springedumanager.web.dto.EvaluacionCalificacionForm;
import cl.untec.springedumanager.web.dto.EvaluacionForm;
import jakarta.validation.Valid;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/evaluaciones")
public class EvaluacionController {

    private final EvaluacionService evaluacionService;
    private final InscripcionService inscripcionService;

    public EvaluacionController(EvaluacionService evaluacionService, InscripcionService inscripcionService) {
        this.evaluacionService = evaluacionService;
        this.inscripcionService = inscripcionService;
    }

    @GetMapping
    public String listar(Authentication authentication, Model model) {
        if (tieneRol(authentication, "ROLE_PROFESOR") && !tieneRol(authentication, "ROLE_ADMIN")) {
            model.addAttribute("evaluaciones", evaluacionService.listarPorProfesorUsername(authentication.getName()));
        } else {
            model.addAttribute("evaluaciones", evaluacionService.listarTodas());
        }
        return "evaluaciones/lista-evaluacion";
    }

    @GetMapping("/nueva")
    public String nueva(Authentication authentication, Model model) {
        if (!model.containsAttribute("evaluacionForm")) {
            model.addAttribute("evaluacionForm", new EvaluacionForm());
        }
        cargarCatalogos(authentication, model);
        return "evaluaciones/formulario-evaluacion";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Integer id, Authentication authentication, Model model) {
        validarAccesoProfesor(evaluacionService.perteneceAProfesor(id, authentication.getName()), authentication);
        Evaluacion evaluacion = evaluacionService.buscarPorId(id);
        if (!model.containsAttribute("evaluacionForm")) {
            EvaluacionForm form = new EvaluacionForm();
            form.setInscripcionId(evaluacion.getInscripcion().getId());
            form.setTipo(evaluacion.getTipo());
            form.setNombre(evaluacion.getNombre());
            form.setDescripcion(evaluacion.getDescripcion());
            form.setFechaEvaluacion(evaluacion.getFechaEvaluacion());
            form.setNota(evaluacion.getNota());
            form.setComentario(evaluacion.getComentario());
            model.addAttribute("evaluacionForm", form);
        }
        model.addAttribute("evaluacion", evaluacion);
        cargarCatalogos(authentication, model);
        return "evaluaciones/formulario-evaluacion";
    }

    @PostMapping
    public String crear(
        Authentication authentication,
        @Valid @ModelAttribute("evaluacionForm") EvaluacionForm form,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            cargarCatalogos(authentication, model);
            return "evaluaciones/formulario-evaluacion";
        }
        validarAccesoProfesor(inscripcionService.perteneceAProfesor(form.getInscripcionId(), authentication.getName()), authentication);
        try {
            evaluacionService.crear(
                form.getInscripcionId(),
                form.getTipo(),
                form.getNombre(),
                form.getDescripcion(),
                form.getFechaEvaluacion(),
                form.getNota(),
                form.getComentario()
            );
        } catch (IllegalArgumentException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            cargarCatalogos(authentication, model);
            return "evaluaciones/formulario-evaluacion";
        }
        redirectAttributes.addFlashAttribute("successMessage", "Evaluación creada correctamente.");
        return "redirect:/evaluaciones";
    }

    @PostMapping("/{id}")
    public String actualizar(
        @PathVariable Integer id,
        Authentication authentication,
        @Valid @ModelAttribute("evaluacionForm") EvaluacionForm form,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        validarAccesoProfesor(evaluacionService.perteneceAProfesor(id, authentication.getName()), authentication);
        Evaluacion evaluacion = evaluacionService.buscarPorId(id);
        if (bindingResult.hasErrors()) {
            model.addAttribute("evaluacion", evaluacion);
            cargarCatalogos(authentication, model);
            return "evaluaciones/formulario-evaluacion";
        }
        try {
            evaluacionService.actualizar(
                id,
                form.getTipo(),
                form.getNombre(),
                form.getDescripcion(),
                form.getFechaEvaluacion(),
                form.getNota(),
                form.getComentario()
            );
        } catch (IllegalArgumentException ex) {
            model.addAttribute("evaluacion", evaluacion);
            model.addAttribute("errorMessage", ex.getMessage());
            cargarCatalogos(authentication, model);
            return "evaluaciones/formulario-evaluacion";
        }
        redirectAttributes.addFlashAttribute("successMessage", "Evaluación actualizada correctamente.");
        return "redirect:/evaluaciones";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Integer id, Authentication authentication, RedirectAttributes redirectAttributes) {
        validarAccesoProfesor(evaluacionService.perteneceAProfesor(id, authentication.getName()), authentication);
        evaluacionService.eliminar(id);
        redirectAttributes.addFlashAttribute("successMessage", "Evaluación eliminada correctamente.");
        return "redirect:/evaluaciones";
    }

    @GetMapping("/{id}/calificar")
    public String calificarVista(@PathVariable Integer id, Authentication authentication, Model model) {
        validarAccesoProfesor(evaluacionService.perteneceAProfesor(id, authentication.getName()), authentication);
        model.addAttribute("evaluacionRef", evaluacionService.buscarPorId(id));
        model.addAttribute("filas", evaluacionService.listarFilasCalificar(id));
        return "evaluaciones/calificaciones-evaluacion";
    }

    @PostMapping("/{id}/calificaciones/{inscripcionId}")
    public String guardarCalificacion(
        @PathVariable Integer id,
        @PathVariable Integer inscripcionId,
        Authentication authentication,
        @Valid @ModelAttribute EvaluacionCalificacionForm form,
        BindingResult bindingResult,
        RedirectAttributes redirectAttributes
    ) {
        validarAccesoProfesor(evaluacionService.perteneceAProfesor(id, authentication.getName()), authentication);
        validarAccesoProfesor(inscripcionService.perteneceAProfesor(inscripcionId, authentication.getName()), authentication);
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Datos de calificación inválidos.");
            return "redirect:/evaluaciones/" + id + "/calificar";
        }
        try {
            evaluacionService.guardarCalificacion(id, inscripcionId, form.getNota(), form.getComentario());
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/evaluaciones/" + id + "/calificar";
        }
        redirectAttributes.addFlashAttribute("successMessage", "Calificación guardada correctamente.");
        return "redirect:/evaluaciones/" + id + "/calificar";
    }

    private void cargarCatalogos(Authentication authentication, Model model) {
        if (tieneRol(authentication, "ROLE_PROFESOR") && !tieneRol(authentication, "ROLE_ADMIN")) {
            model.addAttribute("inscripciones", inscripcionService.listarPorProfesorUsername(authentication.getName()));
        } else {
            model.addAttribute("inscripciones", inscripcionService.listarTodas());
        }
        model.addAttribute("tiposEvaluacion", evaluacionService.listarTiposDisponibles());
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
}
