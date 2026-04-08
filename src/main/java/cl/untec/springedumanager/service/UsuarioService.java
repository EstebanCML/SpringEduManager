package cl.untec.springedumanager.service;

import cl.untec.springedumanager.model.Usuario;
import cl.untec.springedumanager.model.UsuarioRol;
import cl.untec.springedumanager.model.Rol;
import cl.untec.springedumanager.repository.RolRepository;
import cl.untec.springedumanager.repository.UsuarioRepository;
import cl.untec.springedumanager.repository.UsuarioRolRepository;
import cl.untec.springedumanager.web.dto.AdminUsuarioCreateForm;
import cl.untec.springedumanager.web.dto.AdminUsuarioUpdateForm;
import cl.untec.springedumanager.web.dto.CambioPasswordForm;
import cl.untec.springedumanager.web.dto.PerfilUpdateForm;
import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioRolRepository usuarioRolRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final String emailDomain;

    public UsuarioService(
        UsuarioRepository usuarioRepository,
        UsuarioRolRepository usuarioRolRepository,
        RolRepository rolRepository,
        PasswordEncoder passwordEncoder,
        @Value("${app.estudiantes.email-domain:edu.cl}") String emailDomain
    ) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioRolRepository = usuarioRolRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailDomain = limpiarDominio(emailDomain);
    }

    @Transactional(readOnly = true)
    public Usuario buscarPorUsername(String username) {
        return usuarioRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalStateException("No se encontró el usuario autenticado."));
    }

    @Transactional(readOnly = true)
    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Map<Integer, String> obtenerRolPrincipalPorUsuario() {
        return usuarioRolRepository.findAllWithRolAndUsuario().stream()
            .collect(Collectors.toMap(
                ur -> ur.getUsuario().getId(),
                ur -> ur.getRol().getNombre(),
                UsuarioService::seleccionarRolMasAlto
            ));
    }

    @Transactional(readOnly = true)
    public Usuario buscarPorId(Integer id) {
        return usuarioRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Usuario no válido."));
    }

    @Transactional(readOnly = true)
    public List<String> listarRolesAsignables() {
        return rolRepository.findAll().stream()
            .map(Rol::getNombre)
            .filter(nombre -> Set.of("ADMIN", "PROFESOR", "AYUDANTE", "ESTUDIANTE").contains(nombre))
            .sorted()
            .toList();
    }

    @Transactional
    public void crearComoAdmin(AdminUsuarioCreateForm form) {
        String username = generarUsernameUnico(form.getNombre(), form.getPrimerApellido(), form.getSegundoApellido());
        String email = generarEmailInstitucional(username);
        String emailPersonal = normalizarEmailPersonal(form.getEmailPersonal());
        String rolNombre = form.getRol().trim().toUpperCase(Locale.ROOT);

        if (usuarioRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("El correo institucional ya existe.");
        }
        validarEmailPersonalUnico(emailPersonal, -1);

        Rol rol = rolRepository.findByNombre(rolNombre)
            .orElseThrow(() -> new IllegalArgumentException("Rol no válido."));

        Usuario usuario = new Usuario();
        usuario.setNombre(form.getNombre().trim());
        usuario.setSegundoNombre(blankToNull(form.getSegundoNombre()));
        usuario.setPrimerApellido(form.getPrimerApellido().trim());
        usuario.setSegundoApellido(form.getSegundoApellido().trim());
        usuario.setUsername(username);
        usuario.setEmail(email);
        usuario.setEmailPersonal(emailPersonal);
        usuario.setPasswordHash(passwordEncoder.encode(form.getPassword()));
        usuario.setActivo(form.isActivo());
        usuarioRepository.save(usuario);

        UsuarioRol ur = new UsuarioRol();
        ur.setUsuario(usuario);
        ur.setRol(rol);
        usuarioRolRepository.save(ur);
    }

    private String generarUsernameUnico(String nombre, String primerApellido, String segundoApellido) {
        String n = normalizarToken(nombre);
        String ap1 = normalizarToken(primerApellido);
        String ap2 = normalizarToken(segundoApellido);
        if (n.isBlank() || ap1.isBlank() || ap2.isBlank()) {
            throw new IllegalArgumentException("No se pudo generar el usuario: revisa nombre y apellidos.");
        }

        String base3 = recortar(n, 3) + ap1 + recortar(ap2, 1);
        String candidato = base3;
        if (usuarioRepository.existsByUsername(candidato)) {
            String base4 = recortar(n, 4) + ap1 + recortar(ap2, 1);
            candidato = base4;
            int secuencia = 2;
            while (usuarioRepository.existsByUsername(candidato)) {
                candidato = base4 + secuencia;
                secuencia++;
            }
        }
        return candidato.toLowerCase(Locale.ROOT);
    }

    private String generarEmailInstitucional(String username) {
        return username.toLowerCase(Locale.ROOT) + "@" + emailDomain;
    }

    @Transactional
    public void actualizarPerfil(Usuario usuario, PerfilUpdateForm form) {
        String emailPersonal = normalizarEmailPersonal(form.getEmailPersonal());
        validarEmailPersonalUnico(emailPersonal, usuario.getId());
        usuario.setNombre(form.getNombre().trim());
        usuario.setSegundoNombre(blankToNull(form.getSegundoNombre()));
        usuario.setPrimerApellido(form.getPrimerApellido().trim());
        usuario.setSegundoApellido(form.getSegundoApellido().trim());
        usuario.setEmailPersonal(emailPersonal);
        usuarioRepository.save(usuario);
    }

    @Transactional
    public void cambiarPassword(Usuario usuario, CambioPasswordForm form) {
        if (!passwordEncoder.matches(form.getPasswordActual(), usuario.getPasswordHash())) {
            throw new IllegalArgumentException("La contraseña actual no es correcta.");
        }
        if (!form.getPasswordNueva().equals(form.getConfirmarPasswordNueva())) {
            throw new IllegalArgumentException("La confirmación de la nueva contraseña no coincide.");
        }
        usuario.setPasswordHash(passwordEncoder.encode(form.getPasswordNueva()));
        usuarioRepository.save(usuario);
    }

    @Transactional
    public void actualizarComoAdmin(Usuario usuario, AdminUsuarioUpdateForm form) {
        String emailPersonal = normalizarEmailPersonal(form.getEmailPersonal());
        validarEmailPersonalUnico(emailPersonal, usuario.getId());
        usuario.setNombre(form.getNombre().trim());
        usuario.setSegundoNombre(blankToNull(form.getSegundoNombre()));
        usuario.setPrimerApellido(form.getPrimerApellido().trim());
        usuario.setSegundoApellido(form.getSegundoApellido().trim());
        usuario.setEmailPersonal(emailPersonal);
        usuario.setActivo(form.isActivo());
        usuarioRepository.save(usuario);
    }

    @Transactional
    public void eliminar(Integer usuarioId) {
        Usuario usuario = buscarPorId(usuarioId);
        usuarioRepository.delete(usuario);
    }

    private void validarEmailPersonalUnico(String emailPersonal, Integer idActual) {
        if (emailPersonal != null && usuarioRepository.existsByEmailPersonalAndIdNot(emailPersonal, idActual)) {
            throw new IllegalArgumentException("El correo personal ya está en uso por otro usuario.");
        }
    }

    private static String normalizarEmailPersonal(String emailPersonal) {
        if (emailPersonal == null || emailPersonal.isBlank()) {
            return null;
        }
        return emailPersonal.trim().toLowerCase(Locale.ROOT);
    }

    private static String normalizarToken(String valor) {
        if (valor == null) {
            return "";
        }
        String sinTildes = Normalizer.normalize(valor.trim(), Normalizer.Form.NFD)
            .replaceAll("\\p{M}", "");
        return sinTildes.replaceAll("[^A-Za-z]", "").toLowerCase(Locale.ROOT);
    }

    private static String recortar(String valor, int max) {
        if (valor.length() <= max) {
            return valor;
        }
        return valor.substring(0, max);
    }

    private static String limpiarDominio(String dominio) {
        if (dominio == null || dominio.isBlank()) {
            return "edu.cl";
        }
        return dominio.trim().toLowerCase(Locale.ROOT).replaceFirst("^@", "");
    }

    private static String blankToNull(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        return s.trim();
    }

    private static String seleccionarRolMasAlto(String rolA, String rolB) {
        return prioridad(rolA) >= prioridad(rolB) ? rolA : rolB;
    }

    private static int prioridad(String rol) {
        return switch (rol) {
            case "ADMIN" -> 100;
            case "PROFESOR" -> 80;
            case "AYUDANTE" -> 60;
            case "ESTUDIANTE" -> 20;
            default -> 0;
        };
    }
}

