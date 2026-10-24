/**
 * Formulario de una atención (detalle de la historia clínica).
 * Es un componente "de presentación": NO llama a la API. Recibe los valores y las funciones
 * del padre (HistoriaPacientePage) por props y le avisa cuando el usuario hace algo.
 *
 * Props:
 *   - valores: { fechaHistoria, medicoId, detalleHistoria }
 *   - onCambio(campo, valor): el padre actualiza su estado
 *   - onGuardar(evento), onCancelar()
 *   - medicos: lista para el desplegable
 *   - editando: true si se está modificando un detalle existente
 *   - enviando: deshabilita el botón mientras se guarda
 */
export default function DetalleForm({ valores, onCambio, onGuardar, onCancelar, medicos, editando, enviando }) {
  return (
    <form onSubmit={onGuardar} className="card mb-4">
      <div className="card-header">
        <i className="bi bi-plus-circle me-2"></i>{editando ? "Modificar atención" : "Nueva atención"}
      </div>
      <div className="card-body">
        <div className="row g-2 mb-2">
          <div className="col-md-4">
            <label className="form-label small">Fecha</label>
            <input type="date" className="form-control" value={valores.fechaHistoria}
                   onChange={(e) => onCambio("fechaHistoria", e.target.value)} />
          </div>
          <div className="col-md-8">
            <label className="form-label small">Médico</label>
            <select className="form-select" value={valores.medicoId}
                    onChange={(e) => onCambio("medicoId", e.target.value)}>
              <option value="">Seleccionar médico...</option>
              {medicos.map((m) => (
                <option key={m.id} value={m.id}>{m.apellido}, {m.nombre}</option>
              ))}
            </select>
          </div>
        </div>
        <label className="form-label small">Detalle de la atención</label>
        <textarea className="form-control mb-3" rows={3} value={valores.detalleHistoria}
                  placeholder="Motivo de consulta, diagnóstico, indicaciones..."
                  onChange={(e) => onCambio("detalleHistoria", e.target.value)} />
        <div className="d-flex gap-2">
          <button className="btn btn-primary" disabled={enviando}>
            {editando ? "Guardar cambios" : "Agregar atención"}
          </button>
          {editando && (
            <button type="button" className="btn btn-secondary" onClick={onCancelar}>Cancelar</button>
          )}
        </div>
      </div>
    </form>
  );
}
