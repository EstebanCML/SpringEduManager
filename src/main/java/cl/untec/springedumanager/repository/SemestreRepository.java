package cl.untec.springedumanager.repository;

import cl.untec.springedumanager.model.Semestre;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** L3-3: {@code findBy...} sobre periodos académicos. */
public interface SemestreRepository extends JpaRepository<Semestre, Integer> {

    Optional<Semestre> findByTermSystem(String termSystem);

    List<Semestre> findByActivoTrue();
}
