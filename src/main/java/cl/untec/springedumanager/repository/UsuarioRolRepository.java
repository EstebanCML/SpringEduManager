package cl.untec.springedumanager.repository;

import cl.untec.springedumanager.model.UsuarioRol;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UsuarioRolRepository extends JpaRepository<UsuarioRol, Integer> {

    @Query("SELECT ur FROM UsuarioRol ur JOIN FETCH ur.rol JOIN ur.usuario u WHERE u.username = :username")
    List<UsuarioRol> findByUsernameWithRol(@Param("username") String username);

    @Query("SELECT ur FROM UsuarioRol ur JOIN FETCH ur.rol JOIN FETCH ur.usuario")
    List<UsuarioRol> findAllWithRolAndUsuario();

    void deleteByUsuario_Id(Integer usuarioId);
}
