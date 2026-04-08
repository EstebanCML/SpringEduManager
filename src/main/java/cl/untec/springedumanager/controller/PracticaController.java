package cl.untec.springedumanager.controller;

import cl.untec.springedumanager.model.Practica;
import cl.untec.springedumanager.service.CursoService;
import cl.untec.springedumanager.service.InscripcionService;
import cl.untec.springedumanager.service.PracticaService;
import cl.untec.springedumanager.web.dto.PracticaCalificacionForm;
import cl.untec.springedumanager.web.dto.PracticaForm;
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
@RequestMapping("/practicas")
public class PracticaController {

    private final PracticaService practicaService;
    private final CursoService cursoService;
    private final InscripcionService inscripcionService;

    public PracticaController(PracticaService practicaService, CursoService cursoService, InscripcionService inscripcionService) {
        this.practicaService = practicaService;
        this.cursoService = cursoService;
        this.inscripcionService = inscripcionService;
    }

    @GetMapping
    public String listar(Authentication authentication, Model model) {
        java.util.List<Practica> practicas;
        if (tieneRol(authentication, "ROLE_PROFESOR") && !tieneRol(authentication, "ROLE_ADMIN")) {
            practicas = practicaService.listarPorProfesorUsername(authentication.getName());
        } else {
            practicas = practicaService.listarTodas();
        }
        model.addAttribute("practicas", practicas);
        return "practicas/lista-practica";
    }

    @GetMapping("/nueva")
    public String nueva(Authentication authentication, Model model) {
        if (!model.containsAttribute("practicaForm")) {
            PracticaForm form = new PracticaForm();
            form.setEstado("ACTIVA");
            model.addAttribute("practicaForm", form);
        }
        cargarCatalogos(authentication, model);
        return "practicas/formulario-practica";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Integer id, Authentication authentication, Model model) {
        validarAccesoProfesor(practicaService.perteneceAProfesor(id, authentication.getName()), authentication);
        Practica practica = practicaService.buscarPorId(id);
        if (!model.containsAttribute("practicaForm")) {
            PracticaForm form = new PracticaForm();
            form.setCursoId(practica.getCurso().getId());
            form.setNombre(practica.getNombre());
            form.setDescripcion(practica.getDescripcion());
            form.setFechaInicio(practica.getFechaInicio());
            form.setFechaEntrega(practica.getFechaEntrega());
            form.setEstado(practica.getEstado());
            model.addAttribute("practicaForm", form);
        }
        model.addAttribute("practica", practica);
        cargarCatalogos(authentication, model);
        return "practicas/formulario-practica";
    }

    @PostMapping
    public String crear(
        Authentication authentication,
        @Valid @ModelAttribute("practicaForm") PracticaForm form,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            cargarCatalogos(authentication, model);
            return "practicas/formulario-practica";
        }
        if (tieneRol(authentication, "ROLE_PROFESOR") && !tieneRol(authentication, "ROLE_ADMIN")) {
            validarAccesoProfesor(
                cursoService.listarPorProfesorUsername(authentication.getName()).stream().anyMatch(c -> c.getId().equals(form.getCursoId())),
                authentication
            );
        }
        try {
            practicaService.crear(
                form.getCursoId(),
                form.getNombre(),
                form.getDescripcion(),
                form.getFechaInicio(),
                form.getFechaEntrega(),
                form.getEstado()
            );
        } catch (IllegalArgumentException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            cargarCatalogos(authentication, model);
            return "practicas/formulario-practica";
        }
        redirectAttributes.addFlashAttribute("successMessage", "Práctica creada correctamente.");
        return "redirect:/practicas";
    }

    @PostMapping("/{id}")
    public String actualizar(
        @PathVariable Integer id,
        Authentication authentication,
        @Valid @ModelAttribute("practicaForm") PracticaForm form,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        validarAccesoProfesor(practicaService.perteneceAProfesor(id, authentication.getName()), authentication);
        Practica practica = practicaService.buscarPorId(id);
        if (bindingResult.hasErrors()) {
            model.addAttribute("practica", practica);
            cargarCatalogos(authentication, model);
            return "practicas/formulario-practica";
        }
        try {
            practicaService.actualizar(
                id,
                form.getNombre(),
                form.getDescripcion(),
                form.getFechaInicio(),
                form.getFechaEntrega(),
                form.getEstado()
            );
        } catch (IllegalArgumentException ex) {
            model.addAttribute("practica", practica);
            model.addAttribute("errorMessage", ex.getMessage());
            cargarCatalogos(authentication, model);
            return "practicas/formulario-practica";
        }
        redirectAttributes.addFlashAttribute("successMessage", "Práctica actualizada correctamente.");
        return "redirect:/practicas";
    }

    @GetMapping("/{id}/calificaciones")
    public String calificaciones(@PathVariable Integer id, Authentication authentication, Model model) {
        validarAccesoProfesor(practicaService.perteneceAProfesor(id, authentication.getName()), authentication);
        model.addAttribute("practica", practicaService.buscarPorId(id));
        model.addAttribute("calificaciones", practicaService.listarCalificaciones(id));
        model.addAttribute("estadosCalificacion", practicaService.listarEstadosCalificacion());
        return "practicas/calificaciones-practica";
    }

    @PostMapping("/{id}/calificaciones/{inscripcionId}")
    public String calificar(
        @PathVariable Integer id,
        @PathVariable Integer inscripcionId,
        Authentication authentication,
        @Valid @ModelAttribute PracticaCalificacionForm form,
        BindingResult bindingResult,
        RedirectAttributes redirectAttributes
    ) {
        validarAccesoProfesor(practicaService.perteneceAProfesor(id, authentication.getName()), authentication);
        validarAccesoProfesor(inscripcionService.perteneceAProfesor(inscripcionId, authentication.getName()), authentication);
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Datos de calificación inválidos.");
            return "redirect:/practicas/" + id + "/calificaciones";
        }
        practicaService.calificar(id, inscripcionId, form.getNota(), form.getEstado(), form.getComentario());
        redirectAttributes.addFlashAttribute("successMessage", "Calificación guardada correctamente.");
        return "redirect:/practicas/" + id + "/calificaciones";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Integer id, Authentication authentication, RedirectAttributes redirectAttributes) {
        validarAccesoProfesor(practicaService.perteneceAProfesor(id, authentication.getName()), authentication);
        practicaService.eliminar(id);
        redirectAttributes.addFlashAttribute("successMessage", "Práctica eliminada correctamente.");
        return "redirect:/practicas";
    }

    private void cargarCatalogos(Authentication authentication, Model model) {
        if (tieneRol(authentication, "ROLE_PROFESOR") && !tieneRol(authentication, "ROLE_ADMIN")) {
            model.addAttribute("cursos", cursoService.listarPorProfesorUsername(authentication.getName()));
        } else {
            model.addAttribute("cursos", cursoService.listarTodos());
        }
        model.addAttribute("estadosPractica", practicaService.listarEstadosPractica());
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
