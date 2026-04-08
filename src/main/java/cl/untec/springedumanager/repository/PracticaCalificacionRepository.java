package cl.untec.springedumanager.repository;

import cl.untec.springedumanager.model.PracticaCalificacion;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PracticaCalificacionRepository extends JpaRepository<PracticaCalificacion, Integer> {

    Optional<PracticaCalificacion> findByPractica_IdAndInscripcion_Id(Integer practicaId, Integer inscripcionId);

    @Query("""
        SELECT pc FROM PracticaCalificacion pc
        JOIN FETCH pc.practica p
        JOIN FETCH p.curso
        JOIN FETCH pc.inscripcion i
        JOIN FETCH i.estudiante e
        JOIN FETCH e.usuario
        WHERE p.id = :practicaId
        ORDER BY pc.id
        """)
    List<PracticaCalificacion> findByPracticaIdWithInscripcion(Integer practicaId);

    @Query("""
        SELECT pc FROM PracticaCalificacion pc
        JOIN FETCH pc.practica p
        JOIN FETCH p.curso
        JOIN FETCH pc.inscripcion i
        JOIN FETCH i.estudiante e
        JOIN FETCH e.usuario u
        WHERE u.username = :username
        ORDER BY pc.id
        """)
    List<PracticaCalificacion> findByUsername(String username);

    @Query("""
        SELECT pc FROM PracticaCalificacion pc
        JOIN FETCH pc.practica p
        JOIN FETCH pc.inscripcion i
        WHERE i.id = :inscripcionId
        ORDER BY pc.id
        """)
    List<PracticaCalificacion> findByInscripcionId(Integer inscripcionId);
}
