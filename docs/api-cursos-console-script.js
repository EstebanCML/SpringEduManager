// Script de prueba para consola del navegador (F12 -> Console)
// Requisito: estar logeado en la app con rol ADMIN.
// Nota: esta versión usa sesión por cookie (JSESSIONID), no JWT obligatorio.
// Ejecuta: crear curso -> editar curso -> eliminar curso.

(async () => {
  const baseUrl = "http://localhost:8080/api/cursos";
  const uniqueCode = `API-${Date.now()}`;

  const request = async (url, options = {}) => {
    const response = await fetch(url, {
      headers: { "Content-Type": "application/json", ...(options.headers || {}) },
      ...options
    });

    let body = null;
    try {
      body = await response.json();
    } catch (e) {
      body = await response.text().catch(() => null);
    }

    return {
      ok: response.ok,
      status: response.status,
      body
    };
  };

  console.log("1) Creando curso...");
  const createPayload = {
    codigo: uniqueCode,
    nombre: "Curso API Demo",
    descripcion: "Creado desde script de consola",
    profesorId: 1,
    anual: false,
    periodoIndice: 0,
    fechaInicio: "2026-03-01",
    fechaFin: "2026-07-15",
    activo: true
  };
  const created = await request(baseUrl, {
    method: "POST",
    body: JSON.stringify(createPayload)
  });
  console.log("POST /api/cursos =>", created.status, created.body);
  if (!created.ok || !created.body?.id) {
    console.warn("No se pudo crear el curso. Se detiene el script.");
    return;
  }

  const cursoId = created.body.id;

  console.log("2) Editando curso...");
  const updatePayload = {
    ...createPayload,
    nombre: "Curso API Demo (Editado)",
    descripcion: "Actualizado desde script de consola"
  };
  const updated = await request(`${baseUrl}/${cursoId}`, {
    method: "PUT",
    body: JSON.stringify(updatePayload)
  });
  console.log(`PUT /api/cursos/${cursoId} =>`, updated.status, updated.body);
  if (!updated.ok) {
    console.warn("No se pudo editar el curso. Se detiene el script.");
    return;
  }

  console.log("3) Eliminando curso...");
  const deleted = await request(`${baseUrl}/${cursoId}`, { method: "DELETE" });
  console.log(`DELETE /api/cursos/${cursoId} =>`, deleted.status, deleted.body);

  if (deleted.status === 204) {
    console.log("Flujo completo OK: crear -> editar -> eliminar.");
  } else {
    console.warn("El DELETE no devolvió 204. Revisa respuesta.");
  }
})();
