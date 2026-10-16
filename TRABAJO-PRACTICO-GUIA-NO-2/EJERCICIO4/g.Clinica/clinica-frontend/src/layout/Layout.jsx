import { useEffect, useState } from "react";
import { NavLink, Outlet, useNavigate } from "react-router";
import { useAuth } from "../auth/AuthContext.jsx";

/**
 * Estructura de pantalla del template SB Admin: barra superior + menú lateral + contenido.
 * <Outlet /> es el "hueco" donde React Router dibuja la página de la ruta actual.
 */
export default function Layout() {
  const { usuario, cerrarSesion } = useAuth();
  const navigate = useNavigate();
  const [menuOculto, setMenuOculto] = useState(false);

  // El template oculta el menú lateral con una clase en <body> (lo que hacía su scripts.js).
  useEffect(() => {
    document.body.classList.add("sb-nav-fixed");
    document.body.classList.toggle("sb-sidenav-toggled", menuOculto);
  }, [menuOculto]);

  function salir() {
    cerrarSesion();
    navigate("/login");
  }

  return (
    <>
      <nav className="sb-topnav navbar navbar-expand navbar-dark bg-dark">
        <span className="navbar-brand ps-3">
          <i className="bi bi-heart-pulse me-2"></i>Consultorio
        </span>
        <button className="btn btn-link btn-sm order-1 order-lg-0 me-4 me-lg-0 text-white"
                onClick={() => setMenuOculto(!menuOculto)} title="Mostrar/ocultar menú">
          <i className="bi bi-list fs-5"></i>
        </button>
        <div className="ms-auto me-3 d-flex align-items-center text-white-50">
          <i className="bi bi-person-circle me-2"></i>
          <span className="me-3">{usuario?.nombreUsuario}</span>
          <button className="btn btn-outline-light btn-sm" onClick={salir}>Salir</button>
        </div>
      </nav>

      <div id="layoutSidenav">
        <div id="layoutSidenav_nav">
          <nav className="sb-sidenav accordion sb-sidenav-dark">
            <div className="sb-sidenav-menu">
              <div className="nav">
                <div className="sb-sidenav-menu-heading">Gestión</div>
                <NavLink className="nav-link" to="/pacientes">
                  <div className="sb-nav-link-icon"><i className="bi bi-people"></i></div>
                  Pacientes
                </NavLink>
                <NavLink className="nav-link" to="/medicos">
                  <div className="sb-nav-link-icon"><i className="bi bi-person-badge"></i></div>
                  Médicos
                </NavLink>
              </div>
            </div>
            <div className="sb-sidenav-footer">
              <div className="small">Sesión iniciada como:</div>
              {usuario?.nombreUsuario}
            </div>
          </nav>
        </div>

        <div id="layoutSidenav_content">
          <main>
            <div className="container-fluid px-4 py-4">
              <Outlet />
            </div>
          </main>
          <footer className="py-3 bg-light mt-auto">
            <div className="container-fluid px-4 small text-muted">
              Historia Clínica · Ingeniería del Software II · Template: SB Admin (Start Bootstrap, MIT)
            </div>
          </footer>
        </div>
      </div>
    </>
  );
}
