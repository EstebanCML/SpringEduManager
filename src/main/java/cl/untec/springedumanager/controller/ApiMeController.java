package cl.untec.springedumanager.controller;

import cl.untec.springedumanager.model.Usuario;
import cl.untec.springedumanager.service.UsuarioService;
import cl.untec.springedumanager.web.dto.ApiMeResponse;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ApiMeController {

    private final UsuarioService usuarioService;

    public ApiMeController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/me")
    public ApiMeResponse me(Authentication authentication) {
        Usuario usuario = usuarioService.buscarPorUsername(authentication.getName());
        List<String> roles = authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .map(authority -> authority.startsWith("ROLE_") ? authority.substring(5) : authority)
            .sorted()
            .toList();
        return new ApiMeResponse(
            usuario.getId(),
            usuario.getUsername(),
            construirNombreCompleto(usuario),
            usuario.getEmail(),
            usuario.getEmailPersonal(),
            usuario.getActivo(),
            roles
        );
    }

    private static String construirNombreCompleto(Usuario usuario) {
        return String.join(
            " ",
            valor(usuario.getNombre()),
            valor(usuario.getSegundoNombre()),
            valor(usuario.getPrimerApellido()),
            valor(usuario.getSegundoApellido())
        ).trim().replaceAll("\\s+", " ");
    }

    private static String valor(String texto) {
        return texto == null ? "" : texto.trim();
    }
}
