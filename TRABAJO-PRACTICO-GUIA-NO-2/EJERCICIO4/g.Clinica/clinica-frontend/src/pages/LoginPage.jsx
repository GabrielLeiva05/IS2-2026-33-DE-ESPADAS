import { useState } from "react";
import { Navigate, useNavigate } from "react-router";
import { useAuth } from "../auth/AuthContext.jsx";

/**
 * EJEMPLO COMPLETO del molde que vas a repetir en las demás páginas:
 *   1) estado del formulario con useState
 *   2) al enviar: llamar a la API dentro de try/catch
 *   3) si falla: mostrar el mensaje que mandó el backend
 */
export default function LoginPage() {
  const { usuario, iniciarSesion } = useAuth();
  const navigate = useNavigate();

  // 1) Estado: lo que el usuario escribe + el error a mostrar + si está enviando
  const [nombreUsuario, setNombreUsuario] = useState("");
  const [clave, setClave] = useState("");
  const [error, setError] = useState("");
  const [enviando, setEnviando] = useState(false);

  if (usuario) return <Navigate to="/" replace />; // ya logueado

  // 2) Envío
  async function enviar(evento) {
    evento.preventDefault(); // evita que el navegador recargue la página (comportamiento por defecto de un <form>)
    setError("");
    setEnviando(true);
    try {
      await iniciarSesion(nombreUsuario, clave);
      navigate("/");
    } catch (e) {
      setError(e.message); // 3) mensaje del backend: "Usuario o clave incorrectos"
    } finally {
      setEnviando(false);
    }
  }

  return (
    <div className="bg-primary min-vh-100 d-flex align-items-center">
      <div className="container">
        <div className="row justify-content-center">
          <div className="col-lg-5">
            <div className="card shadow-lg border-0 rounded-lg">
              <div className="card-header">
                <h3 className="text-center font-weight-light my-4">
                  <i className="bi bi-heart-pulse me-2"></i>Consultorio
                </h3>
              </div>
              <div className="card-body">
                {error && <div className="alert alert-danger">{error}</div>}
                <form onSubmit={enviar}>
                  <div className="form-floating mb-3">
                    <input className="form-control" id="usuario" placeholder="usuario"
                           value={nombreUsuario} onChange={(e) => setNombreUsuario(e.target.value)} />
                    <label htmlFor="usuario">Usuario</label>
                  </div>
                  <div className="form-floating mb-3">
                    <input className="form-control" id="clave" type="password" placeholder="clave"
                           value={clave} onChange={(e) => setClave(e.target.value)} />
                    <label htmlFor="clave">Clave</label>
                  </div>
                  <button className="btn btn-primary w-100" disabled={enviando}>
                    {enviando ? "Ingresando..." : "Ingresar"}
                  </button>
                </form>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
