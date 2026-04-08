package cl.untec.springedumanager.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "ponderaciones_curso")
public class PonderacionCurso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "porcentaje_practicas", nullable = false, precision = 5, scale = 2)
    private BigDecimal porcentajePracticas = new BigDecimal("60.00");

    @Column(name = "porcentaje_evaluaciones", nullable = false, precision = 5, scale = 2)
    private BigDecimal porcentajeEvaluaciones = new BigDecimal("40.00");

    @Column(name = "porcentaje_evaluaciones_parciales", nullable = false, precision = 5, scale = 2)
    private BigDecimal porcentajeEvaluacionesParciales = new BigDecimal("60.00");

    @Column(name = "porcentaje_examen_final", nullable = false, precision = 5, scale = 2)
    private BigDecimal porcentajeExamenFinal = new BigDecimal("40.00");

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public BigDecimal getPorcentajePracticas() { return porcentajePracticas; }
    public void setPorcentajePracticas(BigDecimal porcentajePracticas) { this.porcentajePracticas = porcentajePracticas; }
    public BigDecimal getPorcentajeEvaluaciones() { return porcentajeEvaluaciones; }
    public void setPorcentajeEvaluaciones(BigDecimal porcentajeEvaluaciones) { this.porcentajeEvaluaciones = porcentajeEvaluaciones; }
    public BigDecimal getPorcentajeEvaluacionesParciales() { return porcentajeEvaluacionesParciales; }
    public void setPorcentajeEvaluacionesParciales(BigDecimal porcentajeEvaluacionesParciales) { this.porcentajeEvaluacionesParciales = porcentajeEvaluacionesParciales; }
    public BigDecimal getPorcentajeExamenFinal() { return porcentajeExamenFinal; }
    public void setPorcentajeExamenFinal(BigDecimal porcentajeExamenFinal) { this.porcentajeExamenFinal = porcentajeExamenFinal; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
