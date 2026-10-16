import { createContext, useContext, useState } from "react";
import { login as loginApi } from "../api/usuarios.js";

/**
 * Guarda "quién inició sesión" para que cualquier componente lo pueda leer con useAuth().
 * Se guarda también en localStorage para no perder la sesión al recargar la página.
 * (Es un login simple de TP: no hay token ni seguridad real del lado del servidor.)
 */
const AuthContext = createContext(null);
const CLAVE_STORAGE = "clinica.usuario";

function leerUsuarioGuardado() {
  try {
    const texto = localStorage.getItem(CLAVE_STORAGE);
    return texto ? JSON.parse(texto) : null;
  } catch {
    return null;
  }
}

export function AuthProvider({ children }) {
  const [usuario, setUsuario] = useState(leerUsuarioGuardado);

  async function iniciarSesion(nombreUsuario, clave) {
    const u = await loginApi(nombreUsuario, clave); // si falla, lanza Error con el mensaje del backend
    setUsuario(u);
    try { localStorage.setItem(CLAVE_STORAGE, JSON.stringify(u)); } catch { /* sin storage: sigue en memoria */ }
  }

  function cerrarSesion() {
    setUsuario(null);
    try { localStorage.removeItem(CLAVE_STORAGE); } catch { /* nada */ }
  }

  return (
    <AuthContext.Provider value={{ usuario, iniciarSesion, cerrarSesion }}>
      {children}
    </AuthContext.Provider>
  );
}

export const useAuth = () => useContext(AuthContext);
