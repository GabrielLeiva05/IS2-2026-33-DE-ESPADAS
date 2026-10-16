import { pedir } from "./cliente.js";

export const login = (nombreUsuario, clave) =>
  pedir("/usuarios/login", { method: "POST", body: { nombreUsuario, clave } });
