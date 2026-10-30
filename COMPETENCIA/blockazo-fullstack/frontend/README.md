# Blockazo – Frontend (React + Vite + Tailwind v4)

Ver el README de la carpeta raíz para levantar todo el proyecto.

- `npm install`
- `npm run dev` → http://localhost:5173 (con el backend corriendo en el 8080; Vite hace de proxy de `/api`, `/oauth2` y `/login/oauth2`)
- `npm run build` → compila dentro de `../backend/src/main/resources/static` para que Spring lo sirva en http://localhost:8080

Estructura:
- `src/api/api.js`: cliente REST (agrega el JWT, traduce los errores del backend).
- `src/context/AuthContext.jsx`: sesión (token en localStorage + datos del usuario).
- `src/components/`: Login, Registro, Perfil (ver / editar / cambiar contraseña), AdminUsuarios (ABM), OAuthCallback (vuelta de Google), RutaProtegida.
