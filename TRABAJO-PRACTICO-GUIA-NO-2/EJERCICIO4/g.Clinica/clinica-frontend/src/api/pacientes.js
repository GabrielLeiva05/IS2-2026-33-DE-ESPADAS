import { API_URL, pedir } from "./cliente.js";

export const listarPacientes = () => pedir("/pacientes");
export const buscarPaciente = (id) => pedir(`/pacientes/${id}`);
export const crearPaciente = (paciente) => pedir("/pacientes", { method: "POST", body: paciente });
export const modificarPaciente = (id, paciente) => pedir(`/pacientes/${id}`, { method: "PUT", body: paciente });
export const eliminarPaciente = (id) => pedir(`/pacientes/${id}`, { method: "DELETE" });

// Foto: se sube como archivo (multipart, campo "archivo") y se muestra con un <img src=...>
export function subirFoto(pacienteId, archivo) {
  const datos = new FormData();
  datos.append("archivo", archivo);
  return pedir(`/pacientes/${pacienteId}/foto`, { method: "POST", body: datos });
}
// URL directa de la imagen. El "?v=" evita que el navegador muestre una foto vieja guardada en caché.
export const urlFoto = (pacienteId, version = 0) => `${API_URL}/pacientes/${pacienteId}/foto?v=${version}`;
