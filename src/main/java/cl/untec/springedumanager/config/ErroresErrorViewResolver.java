package cl.untec.springedumanager.config;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.springframework.boot.webmvc.autoconfigure.error.ErrorViewResolver;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;

/**
 * Spring Boot busca por defecto {@code templates/error.html}. Usamos {@code errores/error.html}
 * junto a acceso denegado y el layout común.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ErroresErrorViewResolver implements ErrorViewResolver {

    private static final String VISTA_ERROR_HTML = "errores/error";

    @Override
    public ModelAndView resolveErrorView(HttpServletRequest request, HttpStatus status, Map<String, Object> model) {
        return new ModelAndView(VISTA_ERROR_HTML, model);
    }
}
