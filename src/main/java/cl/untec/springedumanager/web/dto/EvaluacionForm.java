package cl.untec.springedumanager.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class EvaluacionForm {

    @NotNull
    private Integer inscripcionId;

    @NotBlank
    private String tipo;

    @NotBlank
    @Size(max = 120)
    private String nombre;

    @Size(max = 5000)
    private String descripcion;

    @NotNull
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaEvaluacion;

    @NotNull
    @DecimalMin("0.00")
    @DecimalMax("7.00")
    private BigDecimal nota;

    private String comentario;

    public Integer getInscripcionId() { return inscripcionId; }
    public void setInscripcionId(Integer inscripcionId) { this.inscripcionId = inscripcionId; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public LocalDate getFechaEvaluacion() { return fechaEvaluacion; }
    public void setFechaEvaluacion(LocalDate fechaEvaluacion) { this.fechaEvaluacion = fechaEvaluacion; }
    public BigDecimal getNota() { return nota; }
    public void setNota(BigDecimal nota) { this.nota = nota; }
    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }
}
