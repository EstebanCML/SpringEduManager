package cl.untec.springedumanager.controller;

import cl.untec.springedumanager.model.Usuario;
import cl.untec.springedumanager.web.dto.AdminUsuarioCreateForm;
import cl.untec.springedumanager.service.UsuarioService;
import cl.untec.springedumanager.web.dto.AdminUsuarioUpdateForm;
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
@RequestMapping("/usuarios")
public class AdminUsuarioController {

    private final UsuarioService usuarioService;

    public AdminUsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("usuarios", usuarioService.listarTodos());
        model.addAttribute("rolesPrincipales", usuarioService.obtenerRolPrincipalPorUsuario());
        return "usuarios/lista-usuario";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        if (!model.containsAttribute("usuarioCreateForm")) {
            model.addAttribute("usuarioCreateForm", new AdminUsuarioCreateForm());
        }
        model.addAttribute("rolesDisponibles", usuarioService.listarRolesAsignables());
        return "usuarios/formulario-crear-usuario";
    }

    @PostMapping
    public String crear(
        @Valid @ModelAttribute("usuarioCreateForm") AdminUsuarioCreateForm form,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        model.addAttribute("rolesDisponibles", usuarioService.listarRolesAsignables());
        if (bindingResult.hasErrors()) {
            return "usuarios/formulario-crear-usuario";
        }
        try {
            usuarioService.crearComoAdmin(form);
        } catch (IllegalArgumentException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return "usuarios/formulario-crear-usuario";
        }
        redirectAttributes.addFlashAttribute("successMessage", "Usuario creado correctamente.");
        return "redirect:/usuarios";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Integer id, Model model) {
        Usuario usuario = usuarioService.buscarPorId(id);
        if (!model.containsAttribute("usuarioForm")) {
            AdminUsuarioUpdateForm form = new AdminUsuarioUpdateForm();
            form.setNombre(usuario.getNombre());
            form.setSegundoNombre(usuario.getSegundoNombre());
            form.setPrimerApellido(usuario.getPrimerApellido());
            form.setSegundoApellido(usuario.getSegundoApellido());
            form.setEmailPersonal(usuario.getEmailPersonal());
            form.setActivo(Boolean.TRUE.equals(usuario.getActivo()));
            model.addAttribute("usuarioForm", form);
        }
        model.addAttribute("usuario", usuario);
        return "usuarios/formulario-usuario";
    }

    @PostMapping("/{id}")
    public String actualizar(
        @PathVariable Integer id,
        @Valid @ModelAttribute("usuarioForm") AdminUsuarioUpdateForm form,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        Usuario usuario = usuarioService.buscarPorId(id);
        if (bindingResult.hasErrors()) {
            model.addAttribute("usuario", usuario);
            return "usuarios/formulario-usuario";
        }
        try {
            usuarioService.actualizarComoAdmin(usuario, form);
        } catch (IllegalArgumentException ex) {
            model.addAttribute("usuario", usuario);
            model.addAttribute("errorMessage", ex.getMessage());
            return "usuarios/formulario-usuario";
        }
        redirectAttributes.addFlashAttribute("successMessage", "Usuario actualizado correctamente.");
        return "redirect:/usuarios";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            usuarioService.eliminar(id);
            redirectAttributes.addFlashAttribute("successMessage", "Usuario eliminado correctamente.");
        } catch (DataIntegrityViolationException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "No se puede eliminar el usuario: tiene datos relacionados.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/usuarios";
    }
}

