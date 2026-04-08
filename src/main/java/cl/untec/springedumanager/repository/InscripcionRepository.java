package cl.untec.springedumanager.repository;

import cl.untec.springedumanager.model.Inscripcion;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface InscripcionRepository extends JpaRepository<Inscripcion, Integer> {

    boolean existsByEstudiante_IdAndCurso_Id(Integer estudianteId, Integer cursoId);

    Optional<Inscripcion> findByEstudiante_IdAndCurso_Id(Integer estudianteId, Integer cursoId);

    @Query("""
        SELECT i FROM Inscripcion i
        JOIN FETCH i.estudiante e
        JOIN FETCH e.usuario
        JOIN FETCH i.curso c
        JOIN FETCH c.profesor p
        JOIN FETCH p.usuario
        ORDER BY i.id
        """)
    List<Inscripcion> findAllWithCursoYEstudiante();

    @Query("""
        SELECT i FROM Inscripcion i
        JOIN FETCH i.estudiante e
        JOIN FETCH e.usuario
        JOIN FETCH i.curso c
        JOIN FETCH c.profesor p
        JOIN FETCH p.usuario
        WHERE c.id = :cursoId
        ORDER BY i.id
        """)
    List<Inscripcion> findByCursoIdWithCursoYEstudiante(Integer cursoId);

    @Query("""
        SELECT i FROM Inscripcion i
        JOIN FETCH i.estudiante e
        JOIN FETCH e.usuario u
        JOIN FETCH i.curso c
        JOIN FETCH c.profesor p
        JOIN FETCH p.usuario
        WHERE u.username = :username
        ORDER BY i.id
        """)
    List<Inscripcion> findByUsernameWithCurso(String username);

    @Query("""
        SELECT i FROM Inscripcion i
        JOIN FETCH i.estudiante e
        JOIN FETCH e.usuario
        JOIN FETCH i.curso c
        JOIN FETCH c.profesor p
        JOIN FETCH p.usuario pu
        WHERE pu.username = :profesorUsername
        ORDER BY i.id
        """)
    List<Inscripcion> findByProfesorUsernameWithCursoYEstudiante(String profesorUsername);

    boolean existsByIdAndCurso_Profesor_Usuario_Username(Integer id, String profesorUsername);
}
