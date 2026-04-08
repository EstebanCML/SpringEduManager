package cl.untec.springedumanager.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice(assignableTypes = {
    HomeController.class,
    CursoController.class,
    EstudianteController.class,
    AdminUsuarioController.class,
    InscripcionController.class,
    PracticaController.class,
    EvaluacionController.class,
    PonderacionController.class,
    NotaFinalController.class
})
public class WebExceptionHandler {

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public String handleTypeMismatch(
        MethodArgumentTypeMismatchException ex,
        HttpServletRequest request,
        RedirectAttributes redirectAttributes
    ) {
        redirectAttributes.addFlashAttribute("errorMessage", "El identificador solicitado no es válido.");
        return "redirect:" + resolveFallbackPath(request.getRequestURI());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgument(
        IllegalArgumentException ex,
        HttpServletRequest request,
        RedirectAttributes redirectAttributes
    ) {
        String message = ex.getMessage() == null || ex.getMessage().isBlank()
            ? "No se pudo completar la operación solicitada."
            : ex.getMessage();
        redirectAttributes.addFlashAttribute("errorMessage", message);
        return "redirect:" + resolveFallbackPath(request.getRequestURI());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public String handleMethodNotSupported(
        HttpRequestMethodNotSupportedException ex,
        HttpServletRequest request,
        RedirectAttributes redirectAttributes
    ) {
        redirectAttributes.addFlashAttribute("errorMessage", "La ruta solicitada no está disponible con ese método.");
        return "redirect:" + resolveFallbackPath(request.getRequestURI());
    }

    private String resolveFallbackPath(String uri) {
        if (uri == null || uri.isBlank() || "/".equals(uri)) {
            return "/home";
        }
        String[] parts = uri.split("/");
        if (parts.length >= 2 && !parts[1].isBlank()) {
            return "/" + parts[1];
        }
        return "/home";
    }
}
