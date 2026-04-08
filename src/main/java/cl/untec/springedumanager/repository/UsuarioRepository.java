package cl.untec.springedumanager.repository;

import cl.untec.springedumanager.model.Usuario;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    Optional<Usuario> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByEmailPersonal(String emailPersonal);

    boolean existsByEmailPersonalAndIdNot(String emailPersonal, Integer id);
}
