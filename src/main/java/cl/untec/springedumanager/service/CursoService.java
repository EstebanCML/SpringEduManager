package cl.untec.springedumanager.service;

import cl.untec.springedumanager.model.Curso;
import cl.untec.springedumanager.model.Profesor;
import cl.untec.springedumanager.model.Semestre;
import cl.untec.springedumanager.repository.CursoRepository;
import cl.untec.springedumanager.repository.ProfesorRepository;
import cl.untec.springedumanager.repository.SemestreRepository;
import cl.untec.springedumanager.web.dto.NuevoCursoForm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class CursoService {

    private final CursoRepository cursoRepository;
    private final ProfesorRepository profesorRepository;
    private final SemestreRepository semestreRepository;
    private final String sistemaPeriodos;
    private final List<String> etiquetasPeriodos;

    public CursoService(
        CursoRepository cursoRepository,
        ProfesorRepository profesorRepository,
        SemestreRepository semestreRepository,
        @Value("${app.periodos.sistema:SEMESTRAL}") String sistemaPeriodos,
        @Value("${app.periodos.etiquetas:Primer semestre,Segundo semestre}") String etiquetasPeriodos
    ) {
        this.cursoRepository = cursoRepository;
        this.profesorRepository = profesorRepository;
        this.semestreRepository = semestreRepository;
        this.sistemaPeriodos = normalizarSistema(sistemaPeriodos);
        this.etiquetasPeriodos = parsearEtiquetas(etiquetasPeriodos);
    }

    @Transactional(readOnly = true)
    public List<Curso> listarTodos() {
        return cursoRepository.findAllWithProfesorYPeriodo();
    }

    @Transactional(readOnly = true)
    public List<Curso> listarPorProfesorUsername(String username) {
        Profesor profesor = profesorRepository.findByUsuario_Username(username)
            .orElseThrow(() -> new IllegalArgumentException("Profesor no válido."));
        return cursoRepository.findByProfesor_Id(profesor.getId());
    }

    /** Para rellenar el &lt;select&gt; del formulario de alta. */
    @Transactional(readOnly = true)
    public List<Profesor> listarProfesoresParaFormulario() {
        return profesorRepository.findAllWithUsuario();
    }

    @Transactional(readOnly = true)
    public List<String> listarPeriodosParaFormulario() {
        return etiquetasPeriodos;
    }

    @Transactional
    public void registrar(NuevoCursoForm form) {
        if (cursoRepository.existsByCodigo(form.getCodigo().trim())) {
            throw new IllegalArgumentException("Ya existe un curso con ese código.");
        }
        if (form.getFechaInicio().isAfter(form.getFechaFin())) {
            throw new IllegalArgumentException("La fecha de inicio no puede ser posterior al término.");
        }

        Profesor profesor = profesorRepository
            .findById(form.getProfesorId())
            .orElseThrow(() -> new IllegalArgumentException("Profesor no válido."));
        Semestre semestre = semestreRepository
            .findByTermSystem(sistemaPeriodos)
            .orElseThrow(() -> new IllegalStateException(
                "No existe el tipo de periodo '" + sistemaPeriodos + "' en la tabla semestres."
            ));
        String periodoAcademico = resolverPeriodoAcademico(form, etiquetasPeriodos);

        Curso curso = new Curso();
        curso.setCodigo(form.getCodigo().trim());
        curso.setNombre(form.getNombre().trim());
        curso.setDescripcion(blankToNull(form.getDescripcion()));
        curso.setProfesor(profesor);
        curso.setSemestre(semestre);
        curso.setAnual(form.isAnual());
        curso.setPeriodoAcademico(periodoAcademico);
        curso.setFechaInicio(form.getFechaInicio());
        curso.setFechaFin(form.getFechaFin());
        curso.setActivo(form.isActivo());
        cursoRepository.save(curso);
    }

    @Transactional(readOnly = true)
    public Curso buscarPorId(Integer id) {
        return cursoRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Curso no válido."));
    }

    @Transactional(readOnly = true)
    public Curso buscarPorCodigo(String codigo) {
        return cursoRepository.findByCodigo(codigo)
            .orElseThrow(() -> new IllegalArgumentException("Curso no válido."));
    }

    @Transactional(readOnly = true)
    public NuevoCursoForm crearFormEdicion(Curso curso) {
        NuevoCursoForm form = new NuevoCursoForm();
        form.setCodigo(curso.getCodigo());
        form.setNombre(curso.getNombre());
        form.setDescripcion(curso.getDescripcion());
        form.setProfesorId(curso.getProfesor().getId());
        form.setAnual(Boolean.TRUE.equals(curso.getAnual()));
        form.setPeriodoIndice(resolverIndicePeriodo(curso.getPeriodoAcademico()));
        form.setFechaInicio(curso.getFechaInicio());
        form.setFechaFin(curso.getFechaFin());
        form.setActivo(Boolean.TRUE.equals(curso.getActivo()));
        return form;
    }

    @Transactional
    public void actualizar(Integer cursoId, NuevoCursoForm form) {
        Curso curso = buscarPorId(cursoId);
        String codigo = form.getCodigo().trim();
        if (cursoRepository.existsByCodigoAndIdNot(codigo, cursoId)) {
            throw new IllegalArgumentException("Ya existe otro curso con ese código.");
        }
        if (form.getFechaInicio().isAfter(form.getFechaFin())) {
            throw new IllegalArgumentException("La fecha de inicio no puede ser posterior al término.");
        }
        Profesor profesor = profesorRepository
            .findById(form.getProfesorId())
            .orElseThrow(() -> new IllegalArgumentException("Profesor no válido."));
        Semestre semestre = semestreRepository
            .findByTermSystem(sistemaPeriodos)
            .orElseThrow(() -> new IllegalStateException(
                "No existe el tipo de periodo '" + sistemaPeriodos + "' en la tabla semestres."
            ));
        String periodoAcademico = resolverPeriodoAcademico(form, etiquetasPeriodos);

        curso.setCodigo(codigo);
        curso.setNombre(form.getNombre().trim());
        curso.setDescripcion(blankToNull(form.getDescripcion()));
        curso.setProfesor(profesor);
        curso.setSemestre(semestre);
        curso.setAnual(form.isAnual());
        curso.setPeriodoAcademico(periodoAcademico);
        curso.setFechaInicio(form.getFechaInicio());
        curso.setFechaFin(form.getFechaFin());
        curso.setActivo(form.isActivo());
        cursoRepository.save(curso);
    }

    @Transactional
    public void eliminar(Integer cursoId) {
        Curso curso = buscarPorId(cursoId);
        cursoRepository.delete(curso);
    }

    private static String resolverPeriodoAcademico(NuevoCursoForm form, List<String> etiquetas) {
        if (form.isAnual()) {
            return "ANUAL";
        }
        Integer indice = form.getPeriodoIndice();
        if (indice == null) {
            throw new IllegalArgumentException("Debes seleccionar un periodo académico.");
        }
        if (indice < 0 || indice >= etiquetas.size()) {
            throw new IllegalArgumentException("El periodo académico seleccionado no es válido.");
        }
        return etiquetas.get(indice);
    }

    private Integer resolverIndicePeriodo(String periodoAcademico) {
        if (periodoAcademico == null || periodoAcademico.isBlank() || "ANUAL".equalsIgnoreCase(periodoAcademico)) {
            return null;
        }
        int idx = etiquetasPeriodos.indexOf(periodoAcademico);
        return idx >= 0 ? idx : null;
    }

    private static String normalizarSistema(String sistema) {
        if (sistema == null || sistema.isBlank()) {
            return "SEMESTRAL";
        }
        return sistema.trim().toUpperCase(Locale.ROOT);
    }

    private static List<String> parsearEtiquetas(String etiquetasCsv) {
        List<String> etiquetas = Arrays.stream(etiquetasCsv.split(","))
            .map(String::trim)
            .filter(s -> !s.isBlank())
            .collect(Collectors.toList());
        if (etiquetas.isEmpty()) {
            throw new IllegalStateException("app.periodos.etiquetas no puede quedar vacío.");
        }
        return etiquetas;
    }

    private static String blankToNull(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        return s.trim();
    }
}
