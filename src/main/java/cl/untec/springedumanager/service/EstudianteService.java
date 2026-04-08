package cl.untec.springedumanager.service;

import cl.untec.springedumanager.model.Estudiante;
import cl.untec.springedumanager.model.Rol;
import cl.untec.springedumanager.model.Usuario;
import cl.untec.springedumanager.model.UsuarioRol;
import cl.untec.springedumanager.repository.EstudianteRepository;
import cl.untec.springedumanager.repository.RolRepository;
import cl.untec.springedumanager.repository.UsuarioRepository;
import cl.untec.springedumanager.repository.UsuarioRolRepository;
import cl.untec.springedumanager.web.dto.NuevoEstudianteForm;
import cl.untec.springedumanager.web.dto.ApiEstudianteUpdateRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * L3-1: listados. L3-2: registro transaccional (usuario + rol ESTUDIANTE + estudiante).
 */
@Service
public class EstudianteService {

    private static final String ROL_ESTUDIANTE = "ESTUDIANTE";
    private static final List<String> SEDES_DISPONIBLES = Arrays.asList(
        "Santiago Centro",
        "Santiago Norte",
        "Santiago Sur",
        "Valparaiso",
        "Concepcion"
    );

    private final EstudianteRepository estudianteRepository;
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final UsuarioRolRepository usuarioRolRepository;
    private final PasswordEncoder passwordEncoder;
    private final String emailDomain;

    public EstudianteService(
        EstudianteRepository estudianteRepository,
        UsuarioRepository usuarioRepository,
        RolRepository rolRepository,
        UsuarioRolRepository usuarioRolRepository,
        PasswordEncoder passwordEncoder,
        @Value("${app.estudiantes.email-domain:edu.cl}") String emailDomain
    ) {
        this.estudianteRepository = estudianteRepository;
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.usuarioRolRepository = usuarioRolRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailDomain = limpiarDominio(emailDomain);
    }

    @Transactional(readOnly = true)
    public List<Estudiante> listarTodos() {
        return estudianteRepository.findAllWithUsuario();
    }

    @Transactional(readOnly = true)
    public List<String> listarSedesDisponibles() {
        return SEDES_DISPONIBLES;
    }

    @Transactional
    public Estudiante registrar(NuevoEstudianteForm form) {
        String sede = form.getSede().trim();
        if (!SEDES_DISPONIBLES.contains(sede)) {
            throw new IllegalArgumentException("La sede seleccionada no es válida.");
        }
        String emailPersonal = blankToNull(form.getEmailPersonal());
        if (emailPersonal != null && usuarioRepository.existsByEmailPersonal(emailPersonal.trim().toLowerCase(Locale.ROOT))) {
            throw new IllegalArgumentException("Ya existe un usuario con ese correo personal.");
        }
        String username = generarUsernameUnico(form.getNombre(), form.getPrimerApellido(), form.getSegundoApellido());
        String email = generarEmailInstitucional(username);
        String codigoEstudiante = generarCodigoEstudianteUnico();

        Rol rolEstudiante = rolRepository
            .findByNombre(ROL_ESTUDIANTE)
            .orElseThrow(() -> new IllegalStateException("Falta el rol ESTUDIANTE en la tabla roles (ejecuta el seed en schema.sql)."));

        Usuario usuario = new Usuario();
        usuario.setNombre(form.getNombre().trim());
        usuario.setSegundoNombre(blankToNull(form.getSegundoNombre()));
        usuario.setPrimerApellido(form.getPrimerApellido().trim());
        usuario.setSegundoApellido(form.getSegundoApellido().trim());
        usuario.setUsername(username);
        usuario.setEmail(email);
        usuario.setEmailPersonal(emailPersonal == null ? null : emailPersonal.trim().toLowerCase(Locale.ROOT));
        usuario.setPasswordHash(passwordEncoder.encode(form.getPassword()));
        usuario.setActivo(true);
        usuarioRepository.save(usuario);

        UsuarioRol ur = new UsuarioRol();
        ur.setUsuario(usuario);
        ur.setRol(rolEstudiante);
        usuarioRolRepository.save(ur);

        Estudiante estudiante = new Estudiante();
        estudiante.setUsuario(usuario);
        estudiante.setCodigoEstudiante(codigoEstudiante);
        estudiante.setSede(sede);
        return estudianteRepository.save(estudiante);
    }

    @Transactional(readOnly = true)
    public Estudiante buscarPorId(Integer id) {
        return estudianteRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Estudiante no válido."));
    }

    @Transactional
    public void actualizarDesdeApi(Integer estudianteId, ApiEstudianteUpdateRequest request) {
        Estudiante estudiante = buscarPorId(estudianteId);
        Usuario usuario = estudiante.getUsuario();
        String sede = request.getSede().trim();
        if (!SEDES_DISPONIBLES.contains(sede)) {
            throw new IllegalArgumentException("La sede seleccionada no es válida.");
        }
        String emailPersonal = blankToNull(request.getEmailPersonal());
        if (emailPersonal != null
            && usuarioRepository.existsByEmailPersonalAndIdNot(emailPersonal.trim().toLowerCase(Locale.ROOT), usuario.getId())) {
            throw new IllegalArgumentException("Ya existe un usuario con ese correo personal.");
        }

        usuario.setNombre(request.getNombre().trim());
        usuario.setSegundoNombre(blankToNull(request.getSegundoNombre()));
        usuario.setPrimerApellido(request.getPrimerApellido().trim());
        usuario.setSegundoApellido(request.getSegundoApellido().trim());
        usuario.setEmailPersonal(emailPersonal == null ? null : emailPersonal.trim().toLowerCase(Locale.ROOT));
        usuario.setActivo(request.isActivo());
        usuarioRepository.save(usuario);

        estudiante.setSede(sede);
        estudianteRepository.save(estudiante);
    }

    @Transactional
    public void eliminarDesdeApi(Integer estudianteId) {
        Estudiante estudiante = buscarPorId(estudianteId);
        Integer usuarioId = estudiante.getUsuario().getId();
        estudianteRepository.delete(estudiante);
        usuarioRolRepository.deleteByUsuario_Id(usuarioId);
        usuarioRepository.deleteById(usuarioId);
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

    private String generarCodigoEstudianteUnico() {
        int secuencia = 1;
        while (estudianteRepository.existsByCodigoEstudiante("STD-%04d".formatted(secuencia))) {
            secuencia++;
        }
        return "STD-%04d".formatted(secuencia);
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
}
