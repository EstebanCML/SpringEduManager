package cl.untec.springedumanager.web.dto;

import java.math.BigDecimal;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public class EvaluacionCalificacionForm {

    @NotNull
    @DecimalMin("0.00")
    @DecimalMax("7.00")
    private BigDecimal nota;

    private String comentario;

    public BigDecimal getNota() {
        return nota;
    }

    public void setNota(BigDecimal nota) {
        this.nota = nota;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }
}
