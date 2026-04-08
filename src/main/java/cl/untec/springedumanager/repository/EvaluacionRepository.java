package cl.untec.springedumanager.repository;

import cl.untec.springedumanager.model.Evaluacion;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EvaluacionRepository extends JpaRepository<Evaluacion, Integer> {

    @Query("""
        SELECT e FROM Evaluacion e
        JOIN FETCH e.inscripcion i
        JOIN FETCH i.estudiante es
        JOIN FETCH es.usuario
        JOIN FETCH e.curso c
        ORDER BY e.id
        """)
    List<Evaluacion> findAllWithInscripcion();

    @Query("""
        SELECT e FROM Evaluacion e
        JOIN FETCH e.inscripcion i
        JOIN FETCH i.estudiante es
        JOIN FETCH es.usuario
        JOIN FETCH e.curso c
        WHERE c.id = :cursoId
        ORDER BY e.id
        """)
    List<Evaluacion> findByCursoIdWithInscripcion(Integer cursoId);

    @Query("""
        SELECT e FROM Evaluacion e
        JOIN FETCH e.inscripcion i
        JOIN FETCH i.estudiante es
        JOIN FETCH es.usuario
        JOIN FETCH e.curso c
        JOIN FETCH c.profesor p
        JOIN FETCH p.usuario pu
        WHERE pu.username = :profesorUsername
        ORDER BY e.id
        """)
    List<Evaluacion> findByProfesorUsernameWithInscripcion(String profesorUsername);

    @Query("""
        SELECT e FROM Evaluacion e
        JOIN FETCH e.inscripcion i
        JOIN FETCH i.estudiante es
        JOIN FETCH es.usuario u
        JOIN FETCH e.curso c
        WHERE u.username = :username
        ORDER BY e.id
        """)
    List<Evaluacion> findByUsernameWithCurso(String username);

    List<Evaluacion> findByInscripcion_Id(Integer inscripcionId);

    Optional<Evaluacion> findFirstByInscripcion_IdAndTipoOrderByFechaEvaluacionDesc(Integer inscripcionId, String tipo);

    boolean existsByIdAndCurso_Profesor_Usuario_Username(Integer evaluacionId, String profesorUsername);

    Optional<Evaluacion> findByInscripcion_IdAndNombreAndFechaEvaluacionAndTipo(
        Integer inscripcionId,
        String nombre,
        LocalDate fechaEvaluacion,
        String tipo
    );

    @Query("""
        SELECT e FROM Evaluacion e
        JOIN FETCH e.inscripcion i
        JOIN FETCH i.estudiante es
        JOIN FETCH es.usuario
        WHERE e.curso.id = :cursoId
          AND e.nombre = :nombre
          AND e.fechaEvaluacion = :fecha
          AND e.tipo = :tipo
        """)
    List<Evaluacion> findGrupoPorCursoNombreFechaTipo(
        @Param("cursoId") Integer cursoId,
        @Param("nombre") String nombre,
        @Param("fecha") LocalDate fecha,
        @Param("tipo") String tipo
    );
}
