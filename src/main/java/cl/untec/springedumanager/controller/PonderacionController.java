package cl.untec.springedumanager.controller;

import cl.untec.springedumanager.model.PonderacionCurso;
import cl.untec.springedumanager.service.PonderacionCursoService;
import cl.untec.springedumanager.web.dto.PonderacionCursoForm;
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
@RequestMapping("/ponderaciones")
public class PonderacionController {

    private final PonderacionCursoService ponderacionCursoService;

    public PonderacionController(PonderacionCursoService ponderacionCursoService) {
        this.ponderacionCursoService = ponderacionCursoService;
    }

    @GetMapping
    public String ver(Model model) {
        PonderacionCurso ponderacion = ponderacionCursoService.obtenerActual();
        if (!model.containsAttribute("ponderacionForm")) {
            PonderacionCursoForm form = new PonderacionCursoForm();
            form.setPorcentajePracticas(ponderacion.getPorcentajePracticas());
            form.setPorcentajeEvaluaciones(ponderacion.getPorcentajeEvaluaciones());
            form.setPorcentajeEvaluacionesParciales(ponderacion.getPorcentajeEvaluacionesParciales());
            form.setPorcentajeExamenFinal(ponderacion.getPorcentajeExamenFinal());
            model.addAttribute("ponderacionForm", form);
        }
        return "ponderaciones/formulario-ponderacion";
    }

    @PostMapping
    public String actualizar(
        @Valid @ModelAttribute("ponderacionForm") PonderacionCursoForm form,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            return "ponderaciones/formulario-ponderacion";
        }
        try {
            ponderacionCursoService.actualizar(
                form.getPorcentajePracticas(),
                form.getPorcentajeEvaluaciones(),
                form.getPorcentajeEvaluacionesParciales(),
                form.getPorcentajeExamenFinal()
            );
        } catch (IllegalArgumentException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return "ponderaciones/formulario-ponderacion";
        }
        redirectAttributes.addFlashAttribute("successMessage", "Ponderaciones actualizadas correctamente.");
        return "redirect:/ponderaciones";
    }
}
