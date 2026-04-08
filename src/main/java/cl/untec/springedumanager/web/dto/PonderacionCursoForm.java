package cl.untec.springedumanager.web.dto;

import java.math.BigDecimal;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public class PonderacionCursoForm {

    @NotNull
    @DecimalMin("0.00")
    @DecimalMax("100.00")
    private BigDecimal porcentajePracticas;

    @NotNull
    @DecimalMin("0.00")
    @DecimalMax("100.00")
    private BigDecimal porcentajeEvaluaciones;

    @NotNull
    @DecimalMin("0.00")
    @DecimalMax("100.00")
    private BigDecimal porcentajeEvaluacionesParciales;

    @NotNull
    @DecimalMin("0.00")
    @DecimalMax("100.00")
    private BigDecimal porcentajeExamenFinal;

    public BigDecimal getPorcentajePracticas() { return porcentajePracticas; }
    public void setPorcentajePracticas(BigDecimal porcentajePracticas) { this.porcentajePracticas = porcentajePracticas; }
    public BigDecimal getPorcentajeEvaluaciones() { return porcentajeEvaluaciones; }
    public void setPorcentajeEvaluaciones(BigDecimal porcentajeEvaluaciones) { this.porcentajeEvaluaciones = porcentajeEvaluaciones; }
    public BigDecimal getPorcentajeEvaluacionesParciales() { return porcentajeEvaluacionesParciales; }
    public void setPorcentajeEvaluacionesParciales(BigDecimal porcentajeEvaluacionesParciales) { this.porcentajeEvaluacionesParciales = porcentajeEvaluacionesParciales; }
    public BigDecimal getPorcentajeExamenFinal() { return porcentajeExamenFinal; }
    public void setPorcentajeExamenFinal(BigDecimal porcentajeExamenFinal) { this.porcentajeExamenFinal = porcentajeExamenFinal; }
}
