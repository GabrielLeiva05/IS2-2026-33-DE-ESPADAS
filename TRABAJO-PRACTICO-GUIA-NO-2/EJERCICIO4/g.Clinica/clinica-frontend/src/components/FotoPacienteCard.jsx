import { useState } from "react";
import { subirFoto, urlFoto } from "../api/pacientes.js";

/**
 * Tarjeta con la foto del paciente y el botón para subir / cambiar la foto.
 *
 * Props:
 *   - pacienteId: de quién es la foto
 *   - onError(mensaje): función del componente padre para mostrar errores
 *
 * Cómo sabe si el paciente tiene foto: intenta mostrar la imagen; si el backend responde
 * 400 ("El paciente no tiene foto"), el <img> dispara onError y mostramos un ícono genérico.
 */
export default function FotoPacienteCard({ pacienteId, onError }) {
  // "version" cambia cada vez que subimos una foto: así la URL cambia (…/foto?v=2)
  // y el navegador vuelve a pedirla en vez de mostrar la vieja guardada en caché.
  const [version, setVersion] = useState(0);
  const [sinFoto, setSinFoto] = useState(false);
  const [subiendo, setSubiendo] = useState(false);

  async function alElegirArchivo(evento) {
    const archivo = evento.target.files[0]; // el archivo que eligió el usuario
    if (!archivo) return;
    setSubiendo(true);
    try {
      await subirFoto(pacienteId, archivo);
      setSinFoto(false);
      setVersion(version + 1);
    } catch (e) {
      onError(e.message);
    } finally {
      setSubiendo(false);
      evento.target.value = ""; // permite volver a elegir el mismo archivo
    }
  }

  return (
    <div className="card mb-4">
      <div className="card-header"><i className="bi bi-camera me-2"></i>Foto</div>
      <div className="card-body text-center">
        {sinFoto ? (
          <i className="bi bi-person-bounding-box text-secondary" style={{ fontSize: "6rem" }}></i>
        ) : (
          <img src={urlFoto(pacienteId, version)} alt="Foto del paciente"
               className="img-fluid rounded" style={{ maxHeight: 220 }}
               onError={() => setSinFoto(true)} />
        )}

        {/* El <label> funciona como botón: al hacer clic abre el selector de archivos del input oculto */}
        <label className="btn btn-outline-primary btn-sm w-100 mt-3">
          {subiendo ? "Subiendo..." : sinFoto ? "Subir foto" : "Cambiar foto"}
          <input type="file" accept="image/*" hidden onChange={alElegirArchivo} disabled={subiendo} />
        </label>
      </div>
    </div>
  );
}
