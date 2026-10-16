import { Navigate } from "react-router";
import { useAuth } from "./AuthContext.jsx";

// Si no hay usuario logueado, manda al login; si hay, muestra el contenido.
export default function RequireAuth({ children }) {
  const { usuario } = useAuth();
  return usuario ? children : <Navigate to="/login" replace />;
}
