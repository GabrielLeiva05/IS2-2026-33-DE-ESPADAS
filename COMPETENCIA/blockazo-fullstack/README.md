# Blockazo – Proyecto completo (Spring Boot + React)

```
blockazo-fullstack/
├── backend/    Spring Boot 4 · Java 25 · MySQL · JWT + OAuth2 Google · patrón Template (MVC: Model/Repository/Service/Controller)
└── frontend/   React 19 · Vite · Tailwind v4 · React Router
```

## Cómo se comunican

| Pantalla (frontend) | Endpoint REST (backend) |
|---|---|
| Registro | `POST /api/auth/register` → `{ token, usuario }` (queda logueado) |
| Login | `POST /api/auth/login` `{ correo, clave }` → `{ token, usuario }` |
| Continuar con Google | `GET /oauth2/authorization/google` → vuelve a `/auth/callback?token=...` |
| Mi perfil | `GET /api/perfil` |
| Editar perfil | `PUT /api/perfil` → devuelve token nuevo (el correo es el usuario) |
| Cambiar contraseña | `PUT /api/perfil/clave` `{ claveActual, claveNueva, confirmacionClave }` |
| Usuarios (solo ADMIN) | `GET/POST /api/usuarios`, `GET/PUT/DELETE /api/usuarios/{id}` |

- El token JWT se guarda en `localStorage` y viaja en `Authorization: Bearer <token>`.
- Errores del backend siempre en JSON `{ status, mensaje, errores }` (también 401 y 403), y el frontend muestra `mensaje` y los errores por campo.
- Si el token vence (401), el frontend cierra la sesión y vuelve al login.

## Opción A – Todo junto en http://localhost:8080 (más simple)

El frontend ya viene compilado dentro de `backend/src/main/resources/static`.

1. Tener MySQL corriendo (la base `blockazo` se crea sola; usuario/clave en `backend/src/main/resources/application.properties`).
2. `cd backend` y `mvnw.cmd spring-boot:run` (Windows) o `./mvnw spring-boot:run`.
3. Abrir http://localhost:8080 → pantalla de login.

Si se modifica el frontend: `cd frontend`, `npm install` y `npm run build`, y reiniciar el backend.

## Opción B – Desarrollo (cambios del frontend en caliente)

1. Backend corriendo como en la opción A (puerto 8080).
2. En otra terminal: `cd frontend`, `npm install` y `npm run dev`.
3. Abrir http://localhost:5173. Vite reenvía `/api`, `/oauth2` y `/login/oauth2` al 8080, así no hay problemas de CORS.

## Usuario administrador

`admin@blockazo.com` / `admin123` (se crea al iniciar; cambiar en `application.properties`).

## Login con Google

1. Google Cloud Console → Credenciales → ID de cliente OAuth (Aplicación web).
2. URIs de redirección autorizadas (cargar las dos):
   - `http://localhost:8080/login/oauth2/code/google`
   - `http://localhost:5173/login/oauth2/code/google`
3. Variables de entorno `GOOGLE_CLIENT_ID` y `GOOGLE_CLIENT_SECRET` (o pegarlas en `application.properties`).
4. El usuario que entra con Google se crea sin DNI ni clave: el Perfil le pide completar sus datos y puede definir una contraseña.

## Tests del backend

`cd backend` y `mvnw.cmd test` (usa H2 en memoria, no necesita MySQL).
