package cl.untec.springedumanager.repository;

import cl.untec.springedumanager.model.PonderacionCurso;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PonderacionCursoRepository extends JpaRepository<PonderacionCurso, Integer> {

    Optional<PonderacionCurso> findFirstByOrderByIdAsc();
}
