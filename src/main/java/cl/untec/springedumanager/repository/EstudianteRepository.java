package cl.untec.springedumanager.repository;

import cl.untec.springedumanager.model.Estudiante;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * L3-3: consultas derivadas {@code findBy...} además del listado con {@link #findAllWithUsuario()}.
 */
public interface EstudianteRepository extends JpaRepository<Estudiante, Integer> {

    boolean existsByCodigoEstudiante(String codigoEstudiante);

    Optional<Estudiante> findByCodigoEstudiante(String codigoEstudiante);

    Optional<Estudiante> findByUsuario_Username(String username);

    /** Trae estudiantes con su usuario en una sola consulta (evita consultas N+1). */
    @Query("SELECT DISTINCT e FROM Estudiante e JOIN FETCH e.usuario ORDER BY e.id")
    List<Estudiante> findAllWithUsuario();
}
