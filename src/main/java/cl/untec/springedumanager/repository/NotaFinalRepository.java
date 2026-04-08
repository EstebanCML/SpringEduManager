package cl.untec.springedumanager.repository;

import cl.untec.springedumanager.model.NotaFinal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface NotaFinalRepository extends JpaRepository<NotaFinal, Integer> {

    @Query("""
        SELECT nf FROM NotaFinal nf
        JOIN FETCH nf.estudiante e
        JOIN FETCH e.usuario
        JOIN FETCH nf.curso c
        ORDER BY nf.id DESC
        """)
    List<NotaFinal> findAllWithCursoYEstudiante();

    @Query("""
        SELECT nf FROM NotaFinal nf
        JOIN FETCH nf.estudiante e
        JOIN FETCH e.usuario u
        JOIN FETCH nf.curso c
        WHERE u.username = :username
        ORDER BY nf.id DESC
        """)
    List<NotaFinal> findByUsernameWithCurso(String username);

    List<NotaFinal> findByCurso_IdOrderByIdDesc(Integer cursoId);

    Optional<NotaFinal> findByEstudiante_IdAndCurso_IdAndAcademicYearAndTermNumber(
        Integer estudianteId,
        Integer cursoId,
        Integer academicYear,
        Integer termNumber
    );
}
