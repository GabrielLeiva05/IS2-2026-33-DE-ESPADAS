import { useEffect, useState } from "react";
import { listarPacientes, crearPaciente, eliminarPaciente, modificarPaciente } from "../api/pacientes.js"
import { Link } from "react-router";
/**
 * TU TAREA (O8, paso 2): ABM de pacientes. Es el MISMO molde que MedicosPage
 * (copiá tu MedicosPage y cambiá las funciones por las de ../api/pacientes.js).
 *
 * Extra: en cada fila, un botón/link "Historia" que navegue a /pacientes/{id}/historia
 * (con <Link to={`/pacientes/${p.id}/historia`}> de "react-router").
 */
export default function PacientesPage() {
// Datos de prueba escritos a mano (todavía no hay backend)
  // --- LISTAR MEDICOS ---
  const [pacientes, setPacientes] = useState([])

  async function cargar() {
    const listaPacientes = await listarPacientes();
      
    setPacientes(listaPacientes);
  }  

  useEffect(() => {

    cargar();
  }, []);
  // --- ---

  // --- DAR DE ALTA ---
    // 1) Estado: lo que el usuario escribe + el error a mostrar + si está enviando
  const [nombre, setNombre] = useState("");
  const [apellido, setApellido] = useState("");
  const [documento, setDocumento] = useState("");
  const [error, setError] = useState("");
  const [enviando, setEnviando] = useState(false);

  function limpiarFormulario() {
  setIdEditando(null);
  setNombre("");
  setApellido("");
  setDocumento("");
}
  async function guardar(evento) {
    evento.preventDefault();
    setError("");
    setEnviando(true);

    try {

      if (idEditando) {
        await modificarPaciente(idEditando, {nombre, apellido, documento});
      } else {
        await crearPaciente( {nombre, apellido, documento});
      }
      await cargar();
      limpiarFormulario();
    } catch (e) {
      setError(e.message)
    } finally {
      setEnviando(false);
    }
  }
// --- ---

// --- ELIMINAR ---
  async function eliminar(id) {
      const confirmacion = window.confirm("¿Eliminar este paciente?");
      if (confirmacion) {
        try {
          await eliminarPaciente(id);
          await cargar();
        } catch (e) {
          setError(e.message);
        }
      }
  }

// --- ---

// --- MODIFICAR ---
//1. Un estado nuevo: idEditando, que arranca en null. Indica qué médico estás editando; null significa que estás creando uno nuevo.
  const [idEditando, setIdEditando] = useState(null);

  async function editar(m) {
    setIdEditando(m.id);
    setNombre(m.nombre);
    setApellido(m.apellido);
    setDocumento(m.documento);
    
  }

  return (
    <>
      <h1 className="mt-2 mb-4">Pacientes</h1>
      
      {error && <div className="alert alert-danger">{error}</div>}
      
      <form onSubmit={guardar} className="row g-2 mb-4">
        <div className="col-md-3">
          <input className="form-control" placeholder="Nombre"
                value={nombre} onChange={(e) => setNombre(e.target.value)} />
        </div>

       <div className="col-md-3">
          <input className="form-control" placeholder="Apellido"
                value={apellido} onChange={(e) => setApellido(e.target.value)} />
        </div>

        <div className="col-md-3">
          <input className="form-control" placeholder="Documento"
                value={documento} onChange={(e) => setDocumento(e.target.value)} />
        </div>

        {/* acá, dos <div className="col-md-3"> más: apellido y documento (mismo patrón) */}
        <div className="col-md-3 d-flex gap-2">
          <button className="btn btn-primary flex-fill" disabled={enviando}>
            {idEditando ? "Guardar cambios" : "Agregar"}
          </button>

          {idEditando && (
            <button type="button" className="btn btn-secondary" onClick={limpiarFormulario}>
              Cancelar
            </button>
          )}
        </div>
      </form>
      <table className="table table-striped">
        <thead>
          <tr><th>Nombre</th><th>Apellido</th><th>Documento</th><th>Acciones</th></tr>
        </thead>
        <tbody>
          {pacientes.map((m) => (            // por cada médico del array...
            <tr key={m.id}>                {/* ...una fila */}
              <td>{m.nombre}</td>
              <td>{m.apellido}</td>
              <td>{m.documento}</td>

              <td>
                <button className="btn btn-sm btn-outline-danger" onClick={() => eliminar(m.id)}>
                  <i className="bi bi-trash"></i>
                </button>
                <button className="btn btn-sm btn-outline-primary" onClick={() => editar(m)}>
                  <i className="bi bi-pencil"></i>
                </button>
                <Link className="btn btn-sm btn-outline-success" to={`/pacientes/${m.id}/historia`}>
                  <i className="bi bi-journal-medical"></i>
                </Link>

              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </>
  );
}
