# Ejemplo de consumo con RestTemplate

Este ejemplo muestra consumo básico de la API REST desde otro cliente Java.
Puedes reutilizar la misma idea para `/api/estudiantes`, `/api/inscripciones`, `/api/practicas` y `/api/evaluaciones`.

```java
import java.util.Map;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class ApiClientDemo {
    public static void main(String[] args) {
        RestTemplate restTemplate = new RestTemplate();
        String baseUrl = "http://localhost:8080/api/cursos";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        // Si usas sesión, agrega cookie JSESSIONID aquí:
        // headers.add("Cookie", "JSESSIONID=...");

        Map<String, Object> body = Map.of(
            "codigo", "EXT101",
            "nombre", "Curso desde RestTemplate",
            "descripcion", "Cliente externo",
            "profesorId", 1,
            "anual", false,
            "periodoIndice", 0,
            "fechaInicio", "2026-03-01",
            "fechaFin", "2026-07-15",
            "activo", true
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.exchange(baseUrl, HttpMethod.POST, entity, String.class);
        System.out.println("Status: " + response.getStatusCode());
        System.out.println("Body: " + response.getBody());
    }
}
```

Notas:
- La versión actual del proyecto usa autenticación por sesión (`JSESSIONID`), no JWT obligatorio.
- Si no envías sesión válida, la API responderá `401`.
- Si el usuario no tiene rol permitido para el endpoint, responderá `403`.
- Para cerrar semestre por API puedes invocar `POST /api/notas-finales/inscripcion/{id}/cerrar`.
