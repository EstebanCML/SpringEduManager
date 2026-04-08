package cl.untec.springedumanager.security;

import cl.untec.springedumanager.model.Usuario;
import cl.untec.springedumanager.model.UsuarioRol;
import cl.untec.springedumanager.repository.UsuarioRepository;
import cl.untec.springedumanager.repository.UsuarioRolRepository;
import java.util.Collections;
import java.util.List;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Carga {@link Usuario} y roles desde {@code usuario_roles} para el login por formulario (L4).
 */
@Service
public class UsuarioUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioRolRepository usuarioRolRepository;

    public UsuarioUserDetailsService(UsuarioRepository usuarioRepository, UsuarioRolRepository usuarioRolRepository) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioRolRepository = usuarioRolRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario u = usuarioRepository
            .findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));
        if (!Boolean.TRUE.equals(u.getActivo())) {
            throw new DisabledException("Usuario desactivado");
        }
        List<UsuarioRol> asignaciones = usuarioRolRepository.findByUsernameWithRol(username);
        String[] roles = asignaciones.stream().map(ur -> ur.getRol().getNombre()).toArray(String[]::new);
        var builder = User.builder().username(u.getUsername()).password(u.getPasswordHash());
        if (roles.length > 0) {
            builder.roles(roles);
        } else {
            builder.authorities(Collections.emptyList());
        }
        return builder.build();
    }
}
