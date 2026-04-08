package cl.untec.springedumanager.repository;

import cl.untec.springedumanager.model.Profesor;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/** L3-3: {@code findBy...} para búsqueda por claves de negocio o usuario. */
public interface ProfesorRepository extends JpaRepository<Profesor, Integer> {

    Optional<Profesor> findByCodigoProfesor(String codigoProfesor);

    Optional<Profesor> findByUsuario_Username(String username);

    @Query("SELECT DISTINCT p FROM Profesor p JOIN FETCH p.usuario ORDER BY p.id")
    List<Profesor> findAllWithUsuario();
}
