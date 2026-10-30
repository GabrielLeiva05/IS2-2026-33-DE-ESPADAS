# Blockazo – Backend

Spring Boot 4.1 · Java 25 · MySQL · Spring Security (JWT + OAuth2 Google / Auth0)

Une el **registro/login/seguridad de Tinder-REST** con el **patrón Template de patronTemplate**,
aplicado a un **ABM de usuarios**, manteniendo las capas **Model – Repository – Service – Controller**.

## Estructura

```
com.competencia.blockazo
├── model
│   ├── entity      BaseEntity (template), Usuario
│   ├── enums       Rol, Proveedor
│   └── dto         RegistroRequest, LoginRequest, PerfilRequest, CambioClaveRequest, AuthResponse, UsuarioResponse...
├── repository      BaseRepository (template), UsuarioRepository
├── service         BaseService (template), UsuarioService, AuthService, UsuarioDetailsService
├── controller      BaseController (template), UsuarioController (ABM), AuthController, PerfilController, GlobalExceptionHandler
├── security        SecurityConfig, JwtService, JwtAuthenticationFilter, OAuth2 success/failure handlers
├── error           ErrorServiceException, EntidadNoEncontradaException
└── config          DataInitializer (crea el admin)
```

## Patrón Template

* `BaseService.alta / modificar / baja` son los **métodos plantilla** (algoritmo fijo):
  * alta: `validar(ALTA) → preAlta → save → postAlta`
  * modificar: `existe? → validar(MODIFICACION) → preModificacion → save → postModificacion`
  * baja: `existe? → validar(BAJA) → preBaja → eliminado = true → save → postBaja` (baja lógica)
* `UsuarioService` sólo redefine los **hooks**: `validar` (obligatorios, nombre/apellido solo letras, formato de correo/DNI,
  unicidad, un admin no puede darse de baja a sí mismo), `preAlta` (encripta la clave con BCrypt, rol y fecha de alta),
  `preModificacion` (correo, DNI y fecha de nacimiento no se pueden modificar; conserva la clave si no se envía).
* `BaseController` define los endpoints REST del ABM; `UsuarioController` sólo hereda. **Las reglas de negocio
  viven únicamente en el servicio**, los controllers no tienen lógica de negocio.
* `UsuarioRepository` extiende `BaseRepository` (que extiende `JpaRepository`) y usa métodos derivados
  (`findByCorreoIgnoreCase`, `findByDni`).
* El registro público y la edición del perfil **también pasan por los métodos plantilla** (`alta` y `modificar`).

## Datos de la persona

nombre, apellido, dni, fechaNacimiento (`yyyy-MM-dd`), direccion, **correo (= nombre de usuario)**, clave.

Reglas:

* `nombre` y `apellido`: solo letras (con acentos), separadas por espacio, apóstrofe o guion. No se admiten números.
* `correo`, `dni` y `fechaNacimiento` **no se pueden modificar** (ni desde el Perfil ni desde el ABM).
  Excepción: quien ingresó con Google/Auth0 y todavía no tiene DNI o fecha de nacimiento los puede cargar **una vez**.

## Endpoints

| Método | URL | Acceso | Descripción |
|---|---|---|---|
| POST | `/api/auth/register` | público | Alta de una persona. Devuelve `{token, tipo, usuario}` |
| POST | `/api/auth/login` | público | Login `{correo, clave}`. Devuelve `{token, tipo, usuario}` |
| GET | `/oauth2/authorization/google` | público (navegador) | Login con Google → redirige a `app.frontend.oauth2-redirect?token=...` |
| GET | `/api/perfil` | logueado | **Mi perfil**: ver mis datos |
| PUT | `/api/perfil` | logueado | Modificar mis datos (nombre, apellido, dirección; correo/DNI/fecha no se pueden cambiar). Devuelve `{token, tipo, usuario}` |
| PUT | `/api/perfil/clave` | logueado | Cambiar clave `{claveActual, claveNueva, confirmacionClave}` |
| GET | `/api/usuarios` | ADMIN | Listar usuarios activos |
| GET | `/api/usuarios/{id}` | ADMIN | Consultar |
| POST | `/api/usuarios` | ADMIN | Alta |
| PUT | `/api/usuarios/{id}` | ADMIN | Modificación (si no se manda `clave`, se conserva) |
| DELETE | `/api/usuarios/{id}` | ADMIN | Baja lógica |

Todas las rutas protegidas llevan el header `Authorization: Bearer <token>`.

Admin inicial: `admin@blockazo.com` / `admin123` (configurable en `application.properties`).

### Ejemplo de registro

```json
POST /api/auth/register
{
  "nombre": "Juan", "apellido": "Perez", "dni": "30111222",
  "fechaNacimiento": "1995-05-20", "direccion": "San Martin 123",
  "correo": "juan@mail.com", "clave": "secreta1", "confirmacionClave": "secreta1"
}
```

## Login con Google

1. En Google Cloud Console crear credenciales OAuth (aplicación web) con la URI de redirección
   `http://localhost:8080/login/oauth2/code/google`.
2. Definir las variables de entorno `GOOGLE_CLIENT_ID` y `GOOGLE_CLIENT_SECRET`
   (o pegarlas en `application.properties`).
3. Desde el frontend, el botón "Ingresar con Google" navega a `http://localhost:8080/oauth2/authorization/google`.
4. Al volver, el backend crea el usuario si no existía (sin clave ni DNI) y redirige a
   `app.frontend.oauth2-redirect?token=<jwt>`. El frontend guarda ese token y lo usa como en el login normal.
5. Quien entra con Google puede completar DNI / fecha / dirección desde el Perfil y definir una clave
   (el campo `claveActual` puede ir vacío mientras `tieneClave = false`).

## Login con Auth0 (opcional)

1. En Auth0 crear una aplicación de tipo **Regular Web Application** (no SPA: necesita client secret).
2. En *Allowed Callback URLs* cargar `http://localhost:8080/login/oauth2/code/auth0`
   (y `http://localhost:5173/login/oauth2/code/auth0` si se usa el proxy de Vite).
3. Definir `AUTH0_DOMAIN` (solo el dominio, ej. `mi-tenant.us.auth0.com`), `AUTH0_CLIENT_ID` y `AUTH0_CLIENT_SECRET`
   (variables de entorno o archivo `.env`, ver `.env.example`).
4. Arrancar con el perfil `auth0`: `SPRING_PROFILES_ACTIVE=auth0` o `./mvnw spring-boot:run -Dspring-boot.run.profiles=auth0`.
5. El login queda en `/oauth2/authorization/auth0`.

La configuración está en `application-auth0.properties`. Si falla con `invalid_client`, la causa real se imprime en el log
del backend (`Login OAuth2 fallido: ...`). Revisar: client id/secret sin espacios, tipo de aplicación, y que el
*Token Endpoint Authentication Method* de Auth0 coincida con `client-authentication-method` (por defecto `Post` = `client_secret_post`).

## Cómo levantarlo

1. Tener MySQL corriendo (la base `blockazo` se crea sola). Usuario/clave por defecto `root/2003`,
   se cambian con `DB_USERNAME` / `DB_PASSWORD` o en `application.properties`.
   Las variables también se pueden poner en un archivo `.env` (copiar `.env.example`), que Spring lee automáticamente.
2. `./mvnw spring-boot:run` (Windows: `mvnw.cmd spring-boot:run`).
3. Tests de punta a punta (usan H2 en memoria, no necesitan MySQL): `./mvnw test`.
