/**
 * Lista de atenciones de la historia (ya vienen ordenadas por fecha desde el backend).
 *
 * Props:
 *   - detalles: array de detalles
 *   - idEditando: para resaltar el que se está modificando
 *   - onEditar(detalle), onEliminar(detalle): avisan al padre
 */

// "2026-10-01" (formato de la API) → "01/10/2026" (formato para mostrar)
function formatearFecha(fechaIso) {
  return fechaIso ? fechaIso.split("-").reverse().join("/") : "";
}

export default function DetallesLista({ detalles, idEditando, onEditar, onEliminar }) {
  if (detalles.length === 0) {
    return <div className="alert alert-light border">Todavía no hay atenciones registradas.</div>;
  }

  return (
    <div className="list-group">
      {detalles.map((d) => (
        <div key={d.id} className={"list-group-item" + (d.id === idEditando ? " list-group-item-warning" : "")}>
          <div className="d-flex justify-content-between align-items-start">
            <div>
              <div className="fw-semibold">
                <i className="bi bi-calendar-event me-2"></i>{formatearFecha(d.fechaHistoria)}
                <span className="text-muted fw-normal ms-3">
                  <i className="bi bi-person-badge me-1"></i>
                  {d.medico ? `${d.medico.apellido}, ${d.medico.nombre}` : "Sin médico"}
                </span>
              </div>
              {/* white-space: pre-wrap respeta los saltos de línea que escribió el médico */}
              <div className="mt-2" style={{ whiteSpace: "pre-wrap" }}>{d.detalleHistoria}</div>
            </div>
            <div className="d-flex gap-1 ms-3">
              <button className="btn btn-sm btn-outline-primary" title="Editar" onClick={() => onEditar(d)}>
                <i className="bi bi-pencil"></i>
              </button>
              <button className="btn btn-sm btn-outline-danger" title="Eliminar" onClick={() => onEliminar(d)}>
                <i className="bi bi-trash"></i>
              </button>
            </div>
          </div>
        </div>
      ))}
    </div>
  );
}
