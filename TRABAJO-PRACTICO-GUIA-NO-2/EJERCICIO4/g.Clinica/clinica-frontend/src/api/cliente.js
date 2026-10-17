/**
 * Único lugar que habla con el backend.
 * Todas las funciones de api/*.js usan "pedir", así el manejo de errores está en un solo sitio.
 */
export const API_URL = "http://localhost:8080/api";

/**
 * Hace un request al backend y devuelve el JSON de la respuesta.
 * - Si el backend responde 400/500, lanza un Error con el mensaje que mandó
 *   (el texto de nuestra ErrorServiceException, vía ManejadorErrores).
 * - Si responde 204 (sin cuerpo), devuelve null.
 */
export async function pedir(ruta, { method = "GET", body } = {}) {
  const opciones = { method, headers: {} };

  if (body instanceof FormData) {
    // Archivos (la foto): el navegador arma solo el Content-Type multipart.
    opciones.body = body;
  } else if (body !== undefined) {
    opciones.headers["Content-Type"] = "application/json";
    opciones.body = JSON.stringify(body);
  }

  let respuesta;
  try {
    respuesta = await fetch(API_URL + ruta, opciones);
  } catch {
    throw new Error("No se pudo conectar con el servidor. ¿Está corriendo el backend?");
  }

  if (!respuesta.ok) {
    const mensaje = await respuesta.text();
    throw new Error(mensaje || `Error ${respuesta.status}`);
  }

  if (respuesta.status === 204) return null;
  return respuesta.json();
}
