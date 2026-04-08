package cl.untec.springedumanager.controller;

import cl.untec.springedumanager.model.Usuario;
import cl.untec.springedumanager.service.UsuarioService;
import cl.untec.springedumanager.web.dto.CambioPasswordForm;
import cl.untec.springedumanager.web.dto.PerfilUpdateForm;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class PerfilController {

    private final UsuarioService usuarioService;

    public PerfilController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/perfil")
    public String perfil(Authentication authentication, Model model) {
        Usuario usuario = usuarioService.buscarPorUsername(authentication.getName());
        if (!model.containsAttribute("perfilForm")) {
            PerfilUpdateForm form = new PerfilUpdateForm();
            form.setNombre(usuario.getNombre());
            form.setSegundoNombre(usuario.getSegundoNombre());
            form.setPrimerApellido(usuario.getPrimerApellido());
            form.setSegundoApellido(usuario.getSegundoApellido());
            form.setEmailPersonal(usuario.getEmailPersonal());
            model.addAttribute("perfilForm", form);
        }
        if (!model.containsAttribute("passwordForm")) {
            model.addAttribute("passwordForm", new CambioPasswordForm());
        }
        model.addAttribute("usuario", usuario);
        return "cuenta/perfil";
    }

    @PostMapping("/perfil")
    public String actualizarPerfil(
        Authentication authentication,
        @Valid @ModelAttribute("perfilForm") PerfilUpdateForm form,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        Usuario usuario = usuarioService.buscarPorUsername(authentication.getName());
        if (bindingResult.hasErrors()) {
            model.addAttribute("usuario", usuario);
            model.addAttribute("passwordForm", new CambioPasswordForm());
            return "cuenta/perfil";
        }
        try {
            usuarioService.actualizarPerfil(usuario, form);
        } catch (IllegalArgumentException ex) {
            model.addAttribute("usuario", usuario);
            model.addAttribute("passwordForm", new CambioPasswordForm());
            model.addAttribute("errorMessage", ex.getMessage());
            return "cuenta/perfil";
        }
        redirectAttributes.addFlashAttribute("successMessage", "Datos personales actualizados.");
        return "redirect:/perfil";
    }

    @PostMapping("/perfil/password")
    public String cambiarPassword(
        Authentication authentication,
        @Valid @ModelAttribute("passwordForm") CambioPasswordForm form,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        Usuario usuario = usuarioService.buscarPorUsername(authentication.getName());
        if (bindingResult.hasErrors()) {
            model.addAttribute("usuario", usuario);
            model.addAttribute("perfilForm", mapearPerfil(usuario));
            model.addAttribute("passwordError", "Revisa los campos de contraseña.");
            return "cuenta/perfil";
        }
        try {
            usuarioService.cambiarPassword(usuario, form);
        } catch (IllegalArgumentException ex) {
            model.addAttribute("usuario", usuario);
            model.addAttribute("perfilForm", mapearPerfil(usuario));
            model.addAttribute("passwordError", ex.getMessage());
            return "cuenta/perfil";
        }
        redirectAttributes.addFlashAttribute("successMessage", "Contraseña actualizada correctamente.");
        return "redirect:/perfil";
    }

    private static PerfilUpdateForm mapearPerfil(Usuario usuario) {
        PerfilUpdateForm form = new PerfilUpdateForm();
        form.setNombre(usuario.getNombre());
        form.setSegundoNombre(usuario.getSegundoNombre());
        form.setPrimerApellido(usuario.getPrimerApellido());
        form.setSegundoApellido(usuario.getSegundoApellido());
        form.setEmailPersonal(usuario.getEmailPersonal());
        return form;
    }
}

