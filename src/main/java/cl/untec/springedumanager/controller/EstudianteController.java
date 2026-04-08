package cl.untec.springedumanager.controller;

import cl.untec.springedumanager.service.EstudianteService;
import cl.untec.springedumanager.web.dto.NuevoEstudianteForm;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/estudiantes")
public class EstudianteController {

    private final EstudianteService estudianteService;

    public EstudianteController(EstudianteService estudianteService) {
        this.estudianteService = estudianteService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("estudiantes", estudianteService.listarTodos());
        return "estudiantes/lista-estudiante";
    }

    @GetMapping("/nuevo")
    public String mostrarFormulario(Model model) {
        if (!model.containsAttribute("estudianteForm")) {
            model.addAttribute("estudianteForm", new NuevoEstudianteForm());
        }
        model.addAttribute("sedesDisponibles", estudianteService.listarSedesDisponibles());
        return "estudiantes/formulario-estudiante";
    }

    @PostMapping
    public String registrar(
        @Valid @ModelAttribute("estudianteForm") NuevoEstudianteForm form,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        model.addAttribute("sedesDisponibles", estudianteService.listarSedesDisponibles());
        if (bindingResult.hasErrors()) {
            return "estudiantes/formulario-estudiante";
        }
        try {
            estudianteService.registrar(form);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return "estudiantes/formulario-estudiante";
        }
        redirectAttributes.addFlashAttribute("successMessage", "Estudiante registrado correctamente.");
        return "redirect:/estudiantes";
    }
}
