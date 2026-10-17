import { useState } from "react";

/**
 * Barra de búsqueda de atenciones. Cada opción del desplegable corresponde a una
 * ESTRATEGIA del backend (@Component("medico"), @Component("texto"), @Component("desde")).
 * Agregar un criterio nuevo = una clase nueva en el backend + una línea en CRITERIOS.
 *
 * Props:
 *   - onBuscar(criterio, valor): el padre pide los detalles filtrados
 *   - onLimpiar(): el padre vuelve a pedir todos
 *   - filtroActivo: { criterio, valor } o null, para mostrar qué se está filtrando
 */
const CRITERIOS = [
  { clave: "medico", etiqueta: "Médico", tipoInput: "text", ayuda: "Nombre o apellido del médico" },
  { clave: "texto", etiqueta: "Texto de la atención", tipoInput: "text", ayuda: "Ej: presión, control..." },
  { clave: "desde", etiqueta: "Desde la fecha", tipoInput: "date", ayuda: "" },
];

export default function FiltroDetalles({ onBuscar, onLimpiar, filtroActivo }) {
  const [criterio, setCriterio] = useState("medico");
  const [valor, setValor] = useState("");

  const seleccionado = CRITERIOS.find((c) => c.clave === criterio);

  function cambiarCriterio(nuevo) {
    setCriterio(nuevo);
    setValor(""); // un texto no sirve como fecha (y al revés)
  }

  function enviar(evento) {
    evento.preventDefault();
    onBuscar(criterio, valor);
  }

  function limpiar() {
    setValor("");
    onLimpiar();
  }

  return (
    <form onSubmit={enviar} className="card mb-3">
      <div className="card-body py-2">
        <div className="row g-2 align-items-center">
          <div className="col-auto">
            <i className="bi bi-funnel text-secondary"></i>
          </div>
          <div className="col-md-3">
            <select className="form-select form-select-sm" value={criterio}
                    onChange={(e) => cambiarCriterio(e.target.value)}>
              {CRITERIOS.map((c) => (
                <option key={c.clave} value={c.clave}>{c.etiqueta}</option>
              ))}
            </select>
          </div>
          <div className="col">
            <input className="form-control form-control-sm" type={seleccionado.tipoInput}
                   placeholder={seleccionado.ayuda} value={valor}
                   onChange={(e) => setValor(e.target.value)} />
          </div>
          <div className="col-auto d-flex gap-1">
            <button className="btn btn-sm btn-outline-primary">Buscar</button>
            {filtroActivo && (
              <button type="button" className="btn btn-sm btn-outline-secondary" onClick={limpiar}>
                Quitar filtro
              </button>
            )}
          </div>
        </div>
      </div>
    </form>
  );
}
