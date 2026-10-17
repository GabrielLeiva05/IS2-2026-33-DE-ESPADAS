import { Navigate, Route, Routes } from "react-router";
import RequireAuth from "./auth/RequireAuth.jsx";
import Layout from "./layout/Layout.jsx";
import LoginPage from "./pages/LoginPage.jsx";
import MedicosPage from "./pages/MedicosPage.jsx";
import PacientesPage from "./pages/PacientesPage.jsx";
import HistoriaPacientePage from "./pages/HistoriaPacientePage.jsx";

/**
 * Mapa de rutas del frontend (URL del navegador → componente).
 * Ojo: estas rutas son del FRONTEND (localhost:5173), no del backend.
 */
export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />

      {/* Todo lo que está adentro exige haber iniciado sesión y se dibuja dentro del Layout */}
      <Route element={<RequireAuth><Layout /></RequireAuth>}>
        <Route path="/" element={<Navigate to="/pacientes" replace />} />
        <Route path="/medicos" element={<MedicosPage />} />
        <Route path="/pacientes" element={<PacientesPage />} />
        <Route path="/pacientes/:pacienteId/historia" element={<HistoriaPacientePage />} />
      </Route>

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
