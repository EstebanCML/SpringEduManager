package cl.untec.springedumanager.repository;

import cl.untec.springedumanager.model.Practica;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PracticaRepository extends JpaRepository<Practica, Integer> {

    @Query("""
        SELECT p FROM Practica p
        JOIN FETCH p.curso c
        JOIN FETCH c.profesor pr
        JOIN FETCH pr.usuario
        ORDER BY p.id
        """)
    List<Practica> findAllWithCurso();

    @Query("""
        SELECT p FROM Practica p
        JOIN FETCH p.curso c
        JOIN FETCH c.profesor pr
        JOIN FETCH pr.usuario
        WHERE c.id = :cursoId
        ORDER BY p.id
        """)
    List<Practica> findByCursoIdWithCurso(Integer cursoId);

    @Query("""
        SELECT p FROM Practica p
        JOIN FETCH p.curso c
        JOIN FETCH c.profesor pr
        JOIN FETCH pr.usuario u
        WHERE u.username = :profesorUsername
        ORDER BY p.id
        """)
    List<Practica> findByProfesorUsernameWithCurso(String profesorUsername);

    boolean existsByIdAndCurso_Profesor_Usuario_Username(Integer practicaId, String profesorUsername);
}
