import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import { BrowserRouter } from "react-router";

// Template gratuito: SB Admin (Start Bootstrap, licencia MIT). Trae Bootstrap 5 incluido.
import "startbootstrap-sb-admin/dist/css/styles.css";
// Íconos de Bootstrap (se usan con <i className="bi bi-..."></i>)
import "bootstrap-icons/font/bootstrap-icons.css";

import { AuthProvider } from "./auth/AuthContext.jsx";
import App from "./App.jsx";

createRoot(document.getElementById("root")).render(
  <StrictMode>
    <BrowserRouter>
      <AuthProvider>
        <App />
      </AuthProvider>
    </BrowserRouter>
  </StrictMode>
);
