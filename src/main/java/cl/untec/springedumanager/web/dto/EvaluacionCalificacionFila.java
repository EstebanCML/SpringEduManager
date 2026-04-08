package cl.untec.springedumanager.web.dto;

import cl.untec.springedumanager.model.Evaluacion;
import cl.untec.springedumanager.model.Inscripcion;

/**
 * Una fila en la vista de calificación masiva: estudiante del curso + evaluación existente o null si falta crear.
 */
public class EvaluacionCalificacionFila {

    private final Inscripcion inscripcion;
    private final Evaluacion evaluacion;

    public EvaluacionCalificacionFila(Inscripcion inscripcion, Evaluacion evaluacion) {
        this.inscripcion = inscripcion;
        this.evaluacion = evaluacion;
    }

    public Inscripcion getInscripcion() {
        return inscripcion;
    }

    /** Puede ser null si aún no existe fila en {@code evaluaciones} para ese estudiante y esta prueba. */
    public Evaluacion getEvaluacion() {
        return evaluacion;
    }
}
