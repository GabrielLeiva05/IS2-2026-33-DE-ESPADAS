import { useEffect, useState } from "react";
import { Link, useParams } from "react-router";
import { useAuth } from "../auth/AuthContext.jsx";
import { buscarPaciente } from "../api/pacientes.js";
import { listarMedicos } from "../api/medicos.js";
import {
  buscarHistoriaDePaciente, crearHistoria,
  listarDetalles, agregarDetalle, modificarDetalle, eliminarDetalle,
} from "../api/historias.js";
import FotoPacienteCard from "../components/FotoPacienteCard.jsx";
import DetalleForm from "../components/DetalleForm.jsx";
import DetallesLista from "../components/DetallesLista.jsx";

/**
 * Ficha del paciente: datos + foto + historia clínica con sus atenciones.
 *
 * Esta página es el componente "padre": tiene el estado y habla con la API.
 * Los componentes hijos (FotoPacienteCard, DetalleForm, DetallesLista) solo muestran
 * lo que reciben por props y avisan con las funciones que les pasamos.
 */

// Fecha de HOY en formato "AAAA-MM-DD" según la hora LOCAL.
// (No usamos toISOString(): da la fecha en UTC, y en Argentina después de las 21 h
//  ya sería "mañana" → el backend la rechazaría por "fecha futura".)
function hoy() {
  const d = new Date();
  const mes = String(d.getMonth() + 1).padStart(2, "0");
  const dia = String(d.getDate()).padStart(2, "0");
  return `${d.getFullYear()}-${mes}-${dia}`;
}

const FORMULARIO_VACIO = { fechaHistoria: hoy(), medicoId: "", detalleHistoria: "" };

export default function HistoriaPacientePage() {
  const { pacienteId } = useParams(); // viene de la URL: /pacientes/:pacienteId/historia
  const { usuario } = useAuth();      // el usuario logueado (la historia guarda quién la creó)

  const [paciente, setPaciente] = useState(null);
  const [historia, setHistoria] = useState(null); // null = el paciente todavía no tiene historia
  const [detalles, setDetalles] = useState([]);
  const [medicos, setMedicos] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState("");

  // Formulario de atención: un solo objeto de estado con los tres campos
  const [form, setForm] = useState(FORMULARIO_VACIO);
  const [idEditando, setIdEditando] = useState(null);
  const [enviando, setEnviando] = useState(false);

  // ---------- carga inicial ----------

  async function cargarHistoria() {
    try {
      const h = await buscarHistoriaDePaciente(pacienteId);
      setHistoria(h);
      setDetalles(await listarDetalles(h.id));
    } catch (e) {
      // Si el backend dice que no tiene historia, no es un error: mostramos el botón para crearla.
      if (e.message.includes("no tiene historia")) {
        setHistoria(null);
        setDetalles([]);
      } else {
        setError(e.message);
      }
    }
  }

  useEffect(() => {
    async function cargarTodo() {
      setCargando(true);
      try {
        setPaciente(await buscarPaciente(pacienteId));
        setMedicos(await listarMedicos());
        await cargarHistoria();
      } catch (e) {
        setError(e.message);
      } finally {
        setCargando(false);
      }
    }
    cargarTodo();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [pacienteId]); // se vuelve a cargar si cambia el paciente de la URL

  // ---------- historia ----------

  async function crear() {
    setError("");
    try {
      await crearHistoria(pacienteId, usuario.id);
      await cargarHistoria();
    } catch (e) {
      setError(e.message);
    }
  }

  // ---------- atenciones (detalles) ----------

  // Lo usa DetalleForm cada vez que cambia un campo: copia el objeto y reemplaza solo ese campo.
  function cambiarCampo(campo, valor) {
    setForm({ ...form, [campo]: valor });
  }

  function limpiarFormulario() {
    setForm({ ...FORMULARIO_VACIO, fechaHistoria: hoy() });
    setIdEditando(null);
  }

  async function guardar(evento) {
    evento.preventDefault();
    setError("");
    setEnviando(true);

    // El backend espera el médico como objeto con id: {"medico": {"id": "..."}}
    const detalle = {
      fechaHistoria: form.fechaHistoria,
      detalleHistoria: form.detalleHistoria,
      medico: form.medicoId ? { id: form.medicoId } : null,
    };

    try {
      if (idEditando) {
        await modificarDetalle(historia.id, idEditando, detalle);
      } else {
        await agregarDetalle(historia.id, detalle);
      }
      limpiarFormulario();
      setDetalles(await listarDetalles(historia.id));
    } catch (e) {
      setError(e.message); // ej: "La fecha no puede ser futura", "Debe indicar el médico"
    } finally {
      setEnviando(false);
    }
  }

  function editar(detalle) {
    setIdEditando(detalle.id);
    setForm({
      fechaHistoria: detalle.fechaHistoria,
      medicoId: detalle.medico ? detalle.medico.id : "",
      detalleHistoria: detalle.detalleHistoria,
    });
    window.scrollTo({ top: 0, behavior: "smooth" });
  }

  async function eliminar(detalle) {
    if (!window.confirm("¿Eliminar esta atención?")) return;
    setError("");
    try {
      await eliminarDetalle(historia.id, detalle.id);
      if (detalle.id === idEditando) limpiarFormulario();
      setDetalles(await listarDetalles(historia.id));
    } catch (e) {
      setError(e.message);
    }
  }

  // ---------- pantalla ----------

  if (cargando) return <p className="text-muted mt-4">Cargando...</p>;

  if (!paciente) {
    return (
      <>
        {error && <div className="alert alert-danger mt-4">{error}</div>}
        <Link to="/pacientes">← Volver a pacientes</Link>
      </>
    );
  }

  return (
    <>
      <div className="d-flex align-items-center justify-content-between mt-2 mb-4">
        <h1 className="mb-0">Historia clínica</h1>
        <Link to="/pacientes" className="btn btn-outline-secondary btn-sm">
          <i className="bi bi-arrow-left me-1"></i>Volver
        </Link>
      </div>

      {error && <div className="alert alert-danger">{error}</div>}

      <div className="row">
        {/* Columna izquierda: datos y foto */}
        <div className="col-lg-3">
          <div className="card mb-4">
            <div className="card-header"><i className="bi bi-person me-2"></i>Paciente</div>
            <div className="card-body">
              <h5 className="mb-1">{paciente.apellido}, {paciente.nombre}</h5>
              <div className="text-muted">Documento: {paciente.documento}</div>
            </div>
          </div>
          <FotoPacienteCard pacienteId={pacienteId} onError={setError} />
        </div>

        {/* Columna derecha: historia */}
        <div className="col-lg-9">
          {historia === null ? (
            <div className="card">
              <div className="card-body text-center py-5">
                <i className="bi bi-journal-plus text-secondary" style={{ fontSize: "3rem" }}></i>
                <p className="mt-3">Este paciente todavía no tiene historia clínica.</p>
                <button className="btn btn-primary" onClick={crear}>Crear historia clínica</button>
              </div>
            </div>
          ) : (
            <>
              <DetalleForm
                valores={form}
                onCambio={cambiarCampo}
                onGuardar={guardar}
                onCancelar={limpiarFormulario}
                medicos={medicos}
                editando={idEditando !== null}
                enviando={enviando}
              />
              <h5 className="mb-3">Atenciones ({detalles.length})</h5>
              <DetallesLista
                detalles={detalles}
                idEditando={idEditando}
                onEditar={editar}
                onEliminar={eliminar}
              />
            </>
          )}
        </div>
      </div>
    </>
  );
}

