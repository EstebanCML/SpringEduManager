package cl.untec.springedumanager.controller;

import cl.untec.springedumanager.service.CursoService;
import cl.untec.springedumanager.web.dto.NuevoCursoForm;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
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
@RequestMapping("/cursos")
public class CursoController {

    private final CursoService cursoService;

    public CursoController(CursoService cursoService) {
        this.cursoService = cursoService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("cursos", cursoService.listarTodos());
        return "cursos/lista-curso";
    }

    @GetMapping("/nuevo")
    public String mostrarFormulario(Model model) {
        if (!model.containsAttribute("cursoForm")) {
            model.addAttribute("cursoForm", new NuevoCursoForm());
        }
        model.addAttribute("profesores", cursoService.listarProfesoresParaFormulario());
        model.addAttribute("periodos", cursoService.listarPeriodosParaFormulario());
        return "cursos/formulario-curso";
    }

    @GetMapping("/{id}/editar")
    public String mostrarFormularioEdicion(@PathVariable Integer id, Model model) {
        if (!model.containsAttribute("cursoForm")) {
            model.addAttribute("cursoForm", cursoService.crearFormEdicion(cursoService.buscarPorId(id)));
        }
        model.addAttribute("cursoId", id);
        model.addAttribute("modoEdicion", true);
        model.addAttribute("profesores", cursoService.listarProfesoresParaFormulario());
        model.addAttribute("periodos", cursoService.listarPeriodosParaFormulario());
        return "cursos/formulario-editar-curso";
    }

    @PostMapping
    public String registrar(
        @Valid @ModelAttribute("cursoForm") NuevoCursoForm form,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        model.addAttribute("profesores", cursoService.listarProfesoresParaFormulario());
        model.addAttribute("periodos", cursoService.listarPeriodosParaFormulario());
        if (bindingResult.hasErrors()) {
            return "cursos/formulario-curso";
        }
        try {
            cursoService.registrar(form);
        } catch (IllegalArgumentException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return "cursos/formulario-curso";
        }
        redirectAttributes.addFlashAttribute("successMessage", "Curso registrado correctamente.");
        return "redirect:/cursos";
    }

    @PostMapping("/{id}")
    public String actualizar(
        @PathVariable Integer id,
        @Valid @ModelAttribute("cursoForm") NuevoCursoForm form,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        model.addAttribute("cursoId", id);
        model.addAttribute("modoEdicion", true);
        model.addAttribute("profesores", cursoService.listarProfesoresParaFormulario());
        model.addAttribute("periodos", cursoService.listarPeriodosParaFormulario());
        if (bindingResult.hasErrors()) {
            return "cursos/formulario-editar-curso";
        }
        try {
            cursoService.actualizar(id, form);
        } catch (IllegalArgumentException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return "cursos/formulario-editar-curso";
        }
        redirectAttributes.addFlashAttribute("successMessage", "Curso actualizado correctamente.");
        return "redirect:/cursos";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            cursoService.eliminar(id);
            redirectAttributes.addFlashAttribute("successMessage", "Curso eliminado correctamente.");
        } catch (DataIntegrityViolationException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "No se puede eliminar el curso: tiene datos relacionados.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/cursos";
    }
}
