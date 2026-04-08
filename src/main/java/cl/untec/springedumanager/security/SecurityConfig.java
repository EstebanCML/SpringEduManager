package cl.untec.springedumanager.security;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.http.HttpMethod;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;

/**
 * L4: login por formulario; la portada y recursos estáticos son públicos; el resto requiere sesión.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Matriz de permisos actual (L4):
     * - Público: /, /login, /error, /acceso-denegado, /css/**, /js/**
     * - /estudiantes (GET): ADMIN, PROFESOR, AYUDANTE
     * - /estudiantes/nuevo, POST /estudiantes: ADMIN
     * - /usuarios/**: ADMIN
     * - /cursos (GET): ADMIN, PROFESOR, AYUDANTE, ESTUDIANTE
     * - /cursos/nuevo, POST /cursos: ADMIN, PROFESOR
     * - /cursos/{id}/editar, POST /cursos/{id}, POST /cursos/{id}/eliminar: ADMIN
     * - /perfil: cualquier autenticado
     * - /api/me (GET): cualquier autenticado
     * - /api/cursos (GET): ADMIN, PROFESOR, AYUDANTE, ESTUDIANTE
     * - /api/cursos/{id} (GET): ADMIN, PROFESOR, AYUDANTE, ESTUDIANTE
     * - /api/cursos (POST): ADMIN, PROFESOR
     * - /api/cursos/{id} (PUT, DELETE): ADMIN
     * - /api/estudiantes (GET), /api/estudiantes/{id} (GET): ADMIN, PROFESOR, AYUDANTE
     * - /api/estudiantes (POST), /api/estudiantes/{id} (PUT, DELETE): ADMIN
     * - Módulo académico:
     *   - /inscripciones, /practicas, /evaluaciones, /notas-finales (GET): ADMIN, PROFESOR, AYUDANTE
     *   - /inscripciones (POST/editar/eliminar): ADMIN
     *   - /practicas, /evaluaciones (POST/PUT/DELETE + calificar / cargar notas): ADMIN, PROFESOR
     *   - /ponderaciones/** y cierre de semestre: ADMIN
     *   - /mis-notas y endpoints personales de API: autenticado
     */
    @Bean
    public SecurityFilterChain securityFilterChain(
        HttpSecurity http,
        RoleBasedLoginSuccessHandler roleBasedLoginSuccessHandler
    ) throws Exception {
        AuthenticationEntryPoint apiAuthEntryPoint = (request, response, authException) -> {
            response.setStatus(401);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("status", 401);
            body.put("error", "Unauthorized");
            body.put("message", "Debes iniciar sesión para acceder a este recurso.");
            body.put("path", request.getRequestURI());
            response.getWriter().write(toJson(body));
        };
        AccessDeniedHandler apiAccessDeniedHandler = (request, response, accessDeniedException) -> {
            response.setStatus(403);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("status", 403);
            body.put("error", "Forbidden");
            body.put("message", "No tienes permisos para realizar esta acción.");
            body.put("path", request.getRequestURI());
            response.getWriter().write(toJson(body));
        };

        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/login", "/error", "/acceso-denegado", "/css/**", "/js/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/estudiantes").hasAnyRole("ADMIN", "PROFESOR", "AYUDANTE")
                .requestMatchers("/estudiantes/nuevo").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/estudiantes").hasRole("ADMIN")
                .requestMatchers("/usuarios/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/cursos").hasAnyRole("ADMIN", "PROFESOR", "AYUDANTE", "ESTUDIANTE")
                .requestMatchers("/cursos/nuevo").hasAnyRole("ADMIN", "PROFESOR")
                .requestMatchers(HttpMethod.POST, "/cursos").hasAnyRole("ADMIN", "PROFESOR")
                .requestMatchers("/cursos/*/editar").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/cursos/*").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/cursos/*/eliminar").hasRole("ADMIN")
                .requestMatchers("/perfil").authenticated()
                .requestMatchers("/mis-notas").authenticated()
                .requestMatchers("/mis-eventos-notas").authenticated()
                .requestMatchers(HttpMethod.GET, "/inscripciones", "/practicas", "/evaluaciones", "/notas-finales", "/notas-finales/inscripcion/*")
                    .hasAnyRole("ADMIN", "PROFESOR", "AYUDANTE")
                .requestMatchers("/inscripciones/nueva").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/inscripciones/*/editar").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/inscripciones").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/inscripciones/*", "/inscripciones/*/eliminar").hasRole("ADMIN")
                .requestMatchers("/practicas/nueva", "/practicas/*/editar", "/practicas/*/calificaciones").hasAnyRole("ADMIN", "PROFESOR")
                .requestMatchers(HttpMethod.POST, "/practicas", "/practicas/*", "/practicas/*/calificaciones/*").hasAnyRole("ADMIN", "PROFESOR")
                .requestMatchers(HttpMethod.POST, "/practicas/*/eliminar").hasAnyRole("ADMIN", "PROFESOR")
                .requestMatchers("/evaluaciones/nueva", "/evaluaciones/*/editar", "/evaluaciones/*/calificar").hasAnyRole("ADMIN", "PROFESOR")
                .requestMatchers(HttpMethod.POST, "/evaluaciones", "/evaluaciones/*", "/evaluaciones/*/eliminar", "/evaluaciones/*/calificaciones/*").hasAnyRole("ADMIN", "PROFESOR")
                .requestMatchers("/ponderaciones").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/ponderaciones", "/notas-finales/inscripcion/*/cerrar").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/me").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/cursos", "/api/cursos/*").hasAnyRole("ADMIN", "PROFESOR", "AYUDANTE", "ESTUDIANTE")
                .requestMatchers(HttpMethod.POST, "/api/cursos").hasAnyRole("ADMIN", "PROFESOR")
                .requestMatchers(HttpMethod.PUT, "/api/cursos/*").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/cursos/*").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/estudiantes", "/api/estudiantes/*").hasAnyRole("ADMIN", "PROFESOR", "AYUDANTE")
                .requestMatchers(HttpMethod.POST, "/api/estudiantes").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/estudiantes/*").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/estudiantes/*").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/inscripciones", "/api/inscripciones/*").hasAnyRole("ADMIN", "PROFESOR", "AYUDANTE")
                .requestMatchers(HttpMethod.POST, "/api/inscripciones").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/inscripciones/*").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/inscripciones/*").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/practicas/mis").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/practicas", "/api/practicas/*", "/api/practicas/*/calificaciones").hasAnyRole("ADMIN", "PROFESOR", "AYUDANTE")
                .requestMatchers(HttpMethod.POST, "/api/practicas").hasAnyRole("ADMIN", "PROFESOR")
                .requestMatchers(HttpMethod.PUT, "/api/practicas/*", "/api/practicas/*/calificaciones/*").hasAnyRole("ADMIN", "PROFESOR")
                .requestMatchers(HttpMethod.DELETE, "/api/practicas/*").hasAnyRole("ADMIN", "PROFESOR")
                .requestMatchers(HttpMethod.GET, "/api/evaluaciones/mis").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/evaluaciones", "/api/evaluaciones/*").hasAnyRole("ADMIN", "PROFESOR", "AYUDANTE")
                .requestMatchers(HttpMethod.POST, "/api/evaluaciones").hasAnyRole("ADMIN", "PROFESOR")
                .requestMatchers(HttpMethod.PUT, "/api/evaluaciones/*").hasAnyRole("ADMIN", "PROFESOR")
                .requestMatchers(HttpMethod.DELETE, "/api/evaluaciones/*").hasAnyRole("ADMIN", "PROFESOR")
                .requestMatchers(HttpMethod.GET, "/api/ponderaciones").hasAnyRole("ADMIN", "PROFESOR", "AYUDANTE")
                .requestMatchers(HttpMethod.PUT, "/api/ponderaciones").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/notas-finales/mis").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/notas-finales", "/api/notas-finales/inscripcion/*/resumen").hasAnyRole("ADMIN", "PROFESOR", "AYUDANTE")
                .requestMatchers(HttpMethod.POST, "/api/notas-finales/inscripcion/*/cerrar").hasRole("ADMIN")
                .anyRequest().authenticated())
            .formLogin(form -> form
                .loginPage("/login")
                .successHandler(roleBasedLoginSuccessHandler)
                .permitAll())
            .rememberMe(remember -> remember
                .rememberMeParameter("remember-me")
                .tokenValiditySeconds((int) Duration.ofDays(7).getSeconds()))
            .sessionManagement(session -> session
                .invalidSessionUrl("/login?expired")
                .maximumSessions(1)
                .expiredUrl("/login?expired")
                .maxSessionsPreventsLogin(false))
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID", "remember-me")
                .permitAll())
            .exceptionHandling(ex -> ex
                .defaultAuthenticationEntryPointFor(apiAuthEntryPoint, request -> request.getRequestURI().startsWith("/api/"))
                .defaultAccessDeniedHandlerFor(apiAccessDeniedHandler, request -> request.getRequestURI().startsWith("/api/"))
                .accessDeniedPage("/acceso-denegado")
                .authenticationEntryPoint((request, response, authException) ->
                    response.sendRedirect(request.getContextPath() + "/login?auth")))
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/**"));
        return http.build();
    }

    private static String toJson(Map<String, Object> values) {
        String path = String.valueOf(values.get("path")).replace("\"", "\\\"");
        return "{\"status\":"
            + values.get("status")
            + ",\"error\":\""
            + values.get("error")
            + "\",\"message\":\""
            + values.get("message")
            + "\",\"path\":\""
            + path
            + "\"}";
    }

    /**
     * L4: bcrypt. En BD solo se guarda el hash (p. ej. en {@code schema.sql} el hash de la contraseña {@code 1234});
     * al hacer login, Spring compara lo que escribes con ese hash. Las altas desde la app usan el mismo encoder.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
