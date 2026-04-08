package cl.untec.springedumanager.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "notas_finales")
public class NotaFinal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "estudiante_id", nullable = false)
    private Estudiante estudiante;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "curso_id", nullable = false)
    private Curso curso;

    @Column(name = "academic_year", nullable = false)
    private Integer academicYear;

    @Column(name = "term_number", nullable = false)
    private Integer termNumber;

    @Column(name = "promedio_practica", nullable = false, precision = 3, scale = 2)
    private BigDecimal promedioPractica;

    @Column(name = "promedio_evaluacion", nullable = false, precision = 3, scale = 2)
    private BigDecimal promedioEvaluacion;

    @Column(name = "nota_examen_final", nullable = false, precision = 3, scale = 2)
    private BigDecimal notaExamenFinal;

    @Column(name = "nota_final_semestre", nullable = false, precision = 3, scale = 2)
    private BigDecimal notaFinalSemestre;

    @Column(name = "fecha_cierre", nullable = false)
    private LocalDate fechaCierre;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Estudiante getEstudiante() { return estudiante; }
    public void setEstudiante(Estudiante estudiante) { this.estudiante = estudiante; }
    public Curso getCurso() { return curso; }
    public void setCurso(Curso curso) { this.curso = curso; }
    public Integer getAcademicYear() { return academicYear; }
    public void setAcademicYear(Integer academicYear) { this.academicYear = academicYear; }
    public Integer getTermNumber() { return termNumber; }
    public void setTermNumber(Integer termNumber) { this.termNumber = termNumber; }
    public BigDecimal getPromedioPractica() { return promedioPractica; }
    public void setPromedioPractica(BigDecimal promedioPractica) { this.promedioPractica = promedioPractica; }
    public BigDecimal getPromedioEvaluacion() { return promedioEvaluacion; }
    public void setPromedioEvaluacion(BigDecimal promedioEvaluacion) { this.promedioEvaluacion = promedioEvaluacion; }
    public BigDecimal getNotaExamenFinal() { return notaExamenFinal; }
    public void setNotaExamenFinal(BigDecimal notaExamenFinal) { this.notaExamenFinal = notaExamenFinal; }
    public BigDecimal getNotaFinalSemestre() { return notaFinalSemestre; }
    public void setNotaFinalSemestre(BigDecimal notaFinalSemestre) { this.notaFinalSemestre = notaFinalSemestre; }
    public LocalDate getFechaCierre() { return fechaCierre; }
    public void setFechaCierre(LocalDate fechaCierre) { this.fechaCierre = fechaCierre; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
