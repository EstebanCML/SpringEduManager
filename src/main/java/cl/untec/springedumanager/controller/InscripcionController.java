package cl.untec.springedumanager.controller;

import cl.untec.springedumanager.model.Inscripcion;
import cl.untec.springedumanager.service.CursoService;
import cl.untec.springedumanager.service.EstudianteService;
import cl.untec.springedumanager.service.InscripcionService;
import cl.untec.springedumanager.web.dto.InscripcionForm;
import jakarta.validation.Valid;
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
@RequestMapping("/inscripciones")
public class InscripcionController {

    private final InscripcionService inscripcionService;
    private final EstudianteService estudianteService;
    private final CursoService cursoService;

    public InscripcionController(
        InscripcionService inscripcionService,
        EstudianteService estudianteService,
        CursoService cursoService
    ) {
        this.inscripcionService = inscripcionService;
        this.estudianteService = estudianteService;
        this.cursoService = cursoService;
    }

    @GetMapping
    public String listar(Authentication authentication, Model model) {
        if (tieneRol(authentication, "ROLE_PROFESOR") && !tieneRol(authentication, "ROLE_ADMIN")) {
            model.addAttribute("inscripciones", inscripcionService.listarPorProfesorUsername(authentication.getName()));
        } else {
            model.addAttribute("inscripciones", inscripcionService.listarTodas());
        }
        return "inscripciones/lista-inscripcion";
    }

    @GetMapping("/nueva")
    public String nueva(Model model) {
        if (!model.containsAttribute("inscripcionForm")) {
            InscripcionForm form = new InscripcionForm();
            form.setEstado("ACTIVA");
            model.addAttribute("inscripcionForm", form);
        }
        cargarCatalogos(model);
        return "inscripciones/formulario-inscripcion";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Integer id, Model model) {
        Inscripcion inscripcion = inscripcionService.buscarPorId(id);
        if (!model.containsAttribute("inscripcionForm")) {
            InscripcionForm form = new InscripcionForm();
            form.setEstudianteId(inscripcion.getEstudiante().getId());
            form.setCursoId(inscripcion.getCurso().getId());
            form.setFechaInscripcion(inscripcion.getFechaInscripcion());
            form.setEstado(inscripcion.getEstado());
            model.addAttribute("inscripcionForm", form);
        }
        model.addAttribute("inscripcion", inscripcion);
        cargarCatalogos(model);
        return "inscripciones/formulario-inscripcion";
    }

    @PostMapping
    public String crear(
        @Valid @ModelAttribute("inscripcionForm") InscripcionForm form,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            cargarCatalogos(model);
            return "inscripciones/formulario-inscripcion";
        }
        try {
            inscripcionService.crear(form.getEstudianteId(), form.getCursoId(), form.getFechaInscripcion(), form.getEstado());
        } catch (IllegalArgumentException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            cargarCatalogos(model);
            return "inscripciones/formulario-inscripcion";
        }
        redirectAttributes.addFlashAttribute("successMessage", "Inscripción creada correctamente.");
        return "redirect:/inscripciones";
    }

    @PostMapping("/{id}")
    public String actualizar(
        @PathVariable Integer id,
        @Valid @ModelAttribute("inscripcionForm") InscripcionForm form,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        Inscripcion inscripcion = inscripcionService.buscarPorId(id);
        if (bindingResult.hasErrors()) {
            model.addAttribute("inscripcion", inscripcion);
            cargarCatalogos(model);
            return "inscripciones/formulario-inscripcion";
        }
        try {
            inscripcionService.actualizar(id, form.getFechaInscripcion(), form.getEstado());
        } catch (IllegalArgumentException ex) {
            model.addAttribute("inscripcion", inscripcion);
            model.addAttribute("errorMessage", ex.getMessage());
            cargarCatalogos(model);
            return "inscripciones/formulario-inscripcion";
        }
        redirectAttributes.addFlashAttribute("successMessage", "Inscripción actualizada correctamente.");
        return "redirect:/inscripciones";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        inscripcionService.eliminar(id);
        redirectAttributes.addFlashAttribute("successMessage", "Inscripción eliminada correctamente.");
        return "redirect:/inscripciones";
    }

    private void cargarCatalogos(Model model) {
        model.addAttribute("estudiantes", estudianteService.listarTodos());
        model.addAttribute("cursos", cursoService.listarTodos());
        model.addAttribute("estadosInscripcion", inscripcionService.listarEstadosDisponibles());
    }

    private static boolean tieneRol(Authentication authentication, String rol) {
        return authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .anyMatch(rol::equals);
    }
}
