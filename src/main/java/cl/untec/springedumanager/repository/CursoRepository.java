package cl.untec.springedumanager.repository;

import cl.untec.springedumanager.model.Curso;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * L3-3: consultas derivadas {@code findBy...} junto al listado enriquecido con JPQL.
 */
public interface CursoRepository extends JpaRepository<Curso, Integer> {

    boolean existsByCodigo(String codigo);
    boolean existsByCodigoAndIdNot(String codigo, Integer id);

    Optional<Curso> findByCodigo(String codigo);

    List<Curso> findByActivoTrue();

    List<Curso> findByProfesor_Id(Integer profesorId);

    List<Curso> findBySemestre_Id(Integer semestreId);

    @Query(
        "SELECT DISTINCT c FROM Curso c "
            + "JOIN FETCH c.profesor p JOIN FETCH p.usuario "
            + "JOIN FETCH c.semestre "
            + "ORDER BY c.id"
    )
    List<Curso> findAllWithProfesorYPeriodo();
}
