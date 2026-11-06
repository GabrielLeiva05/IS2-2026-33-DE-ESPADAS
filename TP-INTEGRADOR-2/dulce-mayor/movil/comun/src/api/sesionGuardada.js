// La sesión (token JWT + datos del usuario) se guarda en sessionStorage: dura lo que la pestaña
// y no queda en el teléfono al cerrarla. El acceso está protegido porque algunos navegadores
// (modo privado, almacenamiento bloqueado) lanzan excepciones.
const CLAVE = 'dulcemayor.sesion'

export function leerSesion() {
  try {
    const texto = window.sessionStorage.getItem(CLAVE)
    return texto ? JSON.parse(texto) : null
  } catch {
    return null
  }
}

export function guardarSesion(sesion) {
  try {
    window.sessionStorage.setItem(CLAVE, JSON.stringify(sesion))
  } catch {
    // Sin almacenamiento la sesión vive solo en memoria (AuthContext).
  }
}

export function borrarSesion() {
  try {
    window.sessionStorage.removeItem(CLAVE)
  } catch {
    // nada que borrar
  }
}
