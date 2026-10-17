import { pedir } from "./cliente.js";

// Cada función corresponde a un endpoint de MedicoController.
export const listarMedicos = () => pedir("/medicos");
export const buscarMedico = (id) => pedir(`/medicos/${id}`);
export const crearMedico = (medico) => pedir("/medicos", { method: "POST", body: medico });
export const modificarMedico = (id, medico) => pedir(`/medicos/${id}`, { method: "PUT", body: medico });
export const eliminarMedico = (id) => pedir(`/medicos/${id}`, { method: "DELETE" });
