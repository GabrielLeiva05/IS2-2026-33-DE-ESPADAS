import { pedir } from "./cliente.js";

export const buscarHistoriaDePaciente = (pacienteId) => pedir(`/historias/paciente/${pacienteId}`);
// Con el Adapter, la API recibe los ids "planos" (HistoriaClinicaDTO), no objetos anidados.
export const crearHistoria = (pacienteId, usuarioId) =>
  pedir("/historias", { method: "POST", body: { pacienteId, usuarioId } });

// Detalles: siempre dentro de una historia (composición)
export const listarDetalles = (historiaId) => pedir(`/historias/${historiaId}/detalles`);
export const agregarDetalle = (historiaId, detalle) =>
  pedir(`/historias/${historiaId}/detalles`, { method: "POST", body: detalle });
export const modificarDetalle = (historiaId, detalleId, detalle) =>
  pedir(`/historias/${historiaId}/detalles/${detalleId}`, { method: "PUT", body: detalle });
export const eliminarDetalle = (historiaId, detalleId) =>
  pedir(`/historias/${historiaId}/detalles/${detalleId}`, { method: "DELETE" });
