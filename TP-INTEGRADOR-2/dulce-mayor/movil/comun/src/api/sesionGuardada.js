// Sesión de la app (token JWT + datos del usuario).
// Corrección Ola 1 (persistencia de sesión): se guarda en localStorage, así la sesión sobrevive a cerrar la
// pestaña o a que el navegador del celular saque la app de memoria. Para no dejarla abierta para siempre:
//  - al leerla se descarta si el token ya venció (se mira el campo "exp" del JWT);
//  - el backend limita la renovación a jwt.sesion-maxima (24 h por defecto) desde el ingreso;
//  - "Cerrar sesión" la borra.
// El acceso está protegido porque algunos navegadores (modo privado, almacenamiento bloqueado) lanzan excepciones.
const CLAVE = 'dulcemayor.sesion'

/** Vencimiento del token en milisegundos (campo "exp" del JWT), o null si no se puede leer. */
export function vencimientoDelToken(token) {
  try {
    const carga = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')
    const datos = JSON.parse(atob(carga.padEnd(carga.length + ((4 - (carga.length % 4)) % 4), '=')))
    return typeof datos.exp === 'number' ? datos.exp * 1000 : null
  } catch {
    return null
  }
}

export function leerSesion() {
  try {
    const texto = window.localStorage.getItem(CLAVE)
    if (!texto) return null
    const sesion = JSON.parse(texto)
    const vence = vencimientoDelToken(sesion?.token ?? '')
    if (!vence || vence <= Date.now()) {
      window.localStorage.removeItem(CLAVE)
      return null
    }
    return sesion
  } catch {
    return null
  }
}

export function guardarSesion(sesion) {
  try {
    window.localStorage.setItem(CLAVE, JSON.stringify(sesion))
  } catch {
    // Sin almacenamiento la sesión vive solo en memoria (AuthContext).
  }
}

export function borrarSesion() {
  try {
    window.localStorage.removeItem(CLAVE)
    window.sessionStorage.removeItem(CLAVE) // sesiones guardadas por la versión anterior
  } catch {
    // nada que borrar
  }
}
