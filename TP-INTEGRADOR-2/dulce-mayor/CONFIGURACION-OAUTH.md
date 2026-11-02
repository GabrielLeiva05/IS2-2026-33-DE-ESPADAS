# Configuración de OAuth2 / OpenID Connect — Dulce Mayor

Guía para habilitar el ingreso de **clientes** con **Google** y **Facebook** en desarrollo (local) y en producción.

> **Estado actual:** el código está implementado en la API (`backend`) y en el sitio institucional, pero **no está
> operativo hasta que el equipo cree las credenciales** en Google Cloud y en Meta for Developers y las cargue como
> variables de entorno. Este repositorio no incluye ninguna credencial real. Los pasos de las consolas de Google y
> Meta pueden cambiar de nombre o de lugar: si algo no coincide, guiarse por la documentación oficial enlazada al final.

---

## Índice

1. [Conceptos](#1-conceptos)
2. [Cómo está implementado en Dulce Mayor](#2-cómo-está-implementado-en-dulce-mayor)
3. [Google: configuración paso a paso](#3-google-configuración-paso-a-paso)
4. [Entorno local](#4-entorno-local)
5. [Producción](#5-producción)
6. [Facebook](#6-facebook)
7. [Seguridad y problemas frecuentes](#7-seguridad-y-problemas-frecuentes)

---

## 1. Conceptos

**OAuth 2.0** es un protocolo de **autorización**: permite que una aplicación (la API de Dulce Mayor) obtenga permiso
de un usuario para acceder a datos que guarda otro servicio (Google, Facebook) **sin conocer su contraseña**.
Intervienen el *usuario*, el *cliente* (nuestra API, identificada con un `client_id` y un `client_secret`), el
*servidor de autorización* (Google/Meta) y el *servidor de recursos* (donde están los datos del perfil).

El flujo que usamos es el **Authorization Code**:

1. El navegador va a `https://api…/oauth2/authorization/google`.
2. La API lo redirige a Google con su `client_id`, los permisos pedidos (*scopes*), una URI de retorno (`redirect_uri`) y un valor aleatorio `state` contra falsificaciones.
3. El usuario inicia sesión en Google y acepta.
4. Google redirige a la `redirect_uri` registrada con un **código de autorización**.
5. La API cambia ese código por tokens en una llamada directa servidor a servidor, usando el `client_secret` (que nunca pasa por el navegador).

**OpenID Connect (OIDC)** es una capa de **autenticación** sobre OAuth 2.0: además del token de acceso, el proveedor
entrega un **ID Token** que dice *quién* es el usuario (correo, si el correo está verificado, nombre). Google
implementa OIDC (scope `openid`). Facebook usa OAuth 2.0 y los datos se leen de su Graph API.

**JWT (JSON Web Token)** es un formato de token firmado. Aparece dos veces, con funciones distintas:
- el **ID Token de Google** es un JWT firmado por Google; Spring Security lo valida;
- el **token de Dulce Mayor** es nuestro propio JWT, firmado con `JWT_SECRET`, que usan las aplicaciones para llamar a la API. Los tokens de Google/Facebook no se usan fuera del ingreso.

---

## 2. Cómo está implementado en Dulce Mayor

```
Navegador          institucional-web (8082)           API (8080)                         Google / Meta
   │ clic "Continuar con Google"
   ├──────────────────────────────────────────────▶ GET /oauth2/authorization/google
   │◀──────────── 302 al proveedor (client_id, scope, state, redirect_uri) ─────────────────────────────┤
   ├────────────────────────────────────────────────────────────────────────────────────▶ inicia sesión y acepta
   │◀──────────── 302 a /login/oauth2/code/google?code=…&state=… ───────────────────────────────────────┤
   ├──────────────────────────────────────────────▶ Spring Security valida state, canjea el code,
   │                                                valida el ID Token  ──────────────────────────▶ (servidor a servidor)
   │                                                OAuth2ExitoHandler:
   │                                                  · solo clientes, sin duplicar cuentas
   │                                                  · emite un CÓDIGO DE UN SOLO USO (60 s)
   │◀────── 302 a http://localhost:8082/ingresar/oauth2?codigo=…  (nunca el JWT en la URL)
   ├──────────────▶ GET /ingresar/oauth2?codigo=…
   │                 POST /api/auth/oauth2/canje {codigo} ─▶ devuelve nuestro JWT
   │                 guarda el JWT en su sesión de servidor
   │◀────────────── 302 a /mi-cuenta (cookie HttpOnly)
```

| Pieza | Dónde |
|---|---|
| Credenciales de Google | `backend/src/main/resources/application-oauth-google.properties` (lee `GOOGLE_CLIENT_ID` y `GOOGLE_CLIENT_SECRET`) |
| Credenciales de Facebook | `application-oauth-facebook.properties` (lee `FACEBOOK_CLIENT_ID` y `FACEBOOK_CLIENT_SECRET`) |
| Activación | Perfiles de Spring: `SPRING_PROFILES_ACTIVE=oauth-google`, `oauth-facebook` o ambos |
| Cadena de seguridad del flujo | `config/SeguridadOAuth2Config` (solo existe con alguno de los perfiles) |
| Reglas de cuentas | `ServicioAutenticacion.ingresarConRedSocial` (ver README, sección 3) |
| Lectura de la identidad | `security/OAuth2ExitoHandler` (Google: `email`, `email_verified`, `given_name`, `family_name`; Facebook: `email`, `name`) |
| Código de un solo uso | `security/CodigosCanjeOAuth2` (en memoria, 256 bits, 60 s) |
| Página de retorno del sitio | `OAUTH2_REDIRECT_CLIENTE` → `http://localhost:8082/ingresar/oauth2` |
| Botones en el sitio | `institucional-web`: `OAUTH_GOOGLE_HABILITADO` / `OAUTH_FACEBOOK_HABILITADO` |

La **URI de redirección** que hay que registrar en cada proveedor es la de la **API**, con el formato que usa Spring
Security por defecto: `{URL pública de la API}/login/oauth2/code/{google|facebook}`.

Puertos reales en desarrollo: **API 8080**, **admin-web 8081**, **institucional-web 8082**.

---

## 3. Google: configuración paso a paso

En la consola de Google Cloud, la antigua «Pantalla de consentimiento OAuth» ahora se llama **Google Auth Platform**
y tiene las secciones *Overview*, *Branding*, *Audience*, *Clients*, *Data Access* y *Verification Center*.

1. **Crear un proyecto.** Entrar a <https://console.cloud.google.com/> con la cuenta del equipo, abrir el selector de proyectos y crear uno, por ejemplo `dulce-mayor`.
2. **Configurar la pantalla de consentimiento** (*Google Auth Platform → Branding*): nombre de la aplicación («Dulce Mayor»), correo de soporte y correo de contacto del desarrollador. Para producción también: logo, página principal, política de privacidad, términos y **dominios autorizados** (el dominio propio, verificado en Google Search Console).
3. **Público** (*Audience*): elegir **External** (cualquier cuenta de Google). Mientras esté en **Testing**, solo pueden ingresar los **usuarios de prueba** que se agreguen ahí con su correo (hasta 100): agregar las cuentas del equipo.
4. **Permisos** (*Data Access*): declarar solo `openid`, `.../auth/userinfo.email` y `.../auth/userinfo.profile`. Coinciden con `scope=openid,profile,email` de `application-oauth-google.properties`. Son permisos no sensibles.
5. **Crear las credenciales** (*Clients → Create client*): tipo **Web application**, nombre «Dulce Mayor API». En **Authorized redirect URIs** agregar:
   - `http://localhost:8080/login/oauth2/code/google` (desarrollo)
   - `https://api.dulcemayor.com.ar/login/oauth2/code/google` (producción, con el dominio real)

   *Authorized JavaScript origins* puede quedar vacío: el flujo lo hace el servidor.
6. **Guardar el Client ID y el Client Secret** que muestra al crear. Copiarlos al gestor de secretos o al `.env` local; **nunca** a un archivo del repositorio. Si se pierde el secreto, se genera otro desde el mismo cliente.
7. Los cambios en las URIs pueden tardar desde unos minutos hasta algunas horas en aplicarse.

---

## 4. Entorno local

1. En `backend/.env` (copiado de `.env.example`):
   ```properties
   GOOGLE_CLIENT_ID=<el client id de Google>
   GOOGLE_CLIENT_SECRET=<el client secret de Google>
   OAUTH2_REDIRECT_CLIENTE=http://localhost:8082/ingresar/oauth2
   ```
2. Activar el perfil al iniciar la API. El perfil **no** se toma del `.env`: va como variable del sistema, argumento o configuración del IDE.
   ```bat
   :: Windows (cmd)
   cd backend
   set SPRING_PROFILES_ACTIVE=oauth-google
   mvnw.cmd spring-boot:run
   ```
   ```powershell
   # PowerShell
   $env:SPRING_PROFILES_ACTIVE="oauth-google"; .\mvnw.cmd spring-boot:run
   ```
   En IntelliJ: *Run → Edit Configurations → Active profiles: `oauth-google`*.
3. Mostrar el botón en el sitio institucional:
   ```bat
   cd institucional-web
   set OAUTH_GOOGLE_HABILITADO=true
   mvnw.cmd spring-boot:run
   ```
4. Probar: abrir <http://localhost:8082/ingresar>, «Continuar con Google», ingresar con una cuenta agregada como usuario de prueba. Debería volver a `http://localhost:8082/mi-cuenta` con la sesión iniciada.
5. Comprobar las reglas: intentar con el correo del jefe (debe rechazarse con el mensaje para empleados) y con el de un cliente registrado con contraseña (debe pedir ingresar con la contraseña).

Notas:
- Usar `localhost` en todas partes, no `127.0.0.1`: para Google son URIs distintas.
- `CORS_ALLOWED_ORIGINS` no interviene en este flujo (son redirecciones del navegador, no llamadas `fetch`).

---

## 5. Producción

### 5.1 Dominios

Ejemplo de dominios (reemplazar por los reales):

| Aplicación | Dominio público | Puerto interno |
|---|---|---|
| API | `https://api.dulcemayor.com.ar` | 8080 |
| admin-web | `https://admin.dulcemayor.com.ar` | 8081 |
| institucional-web | `https://www.dulcemayor.com.ar` | 8082 |

### 5.2 URIs de redirección exactas

Registrar en Google (y en Meta) **exactamente**, sin barra final ni comodines:

```
https://api.dulcemayor.com.ar/login/oauth2/code/google
https://api.dulcemayor.com.ar/login/oauth2/code/facebook
```

Y en la API: `OAUTH2_REDIRECT_CLIENTE=https://www.dulcemayor.com.ar/ingresar/oauth2`.

### 5.3 HTTPS

Google acepta `http` solo para `localhost`; Meta exige `https` para dominios reales. Todo el tráfico de producción
debe ir por HTTPS (certificado de Let's Encrypt u otro). Activar también `COOKIE_SECURE=true` en las dos apps web.

### 5.4 Secretos y variables de entorno

Nunca en el repositorio ni en archivos `.properties`. Cargarlos como variables del servicio (systemd, Docker,
panel del hosting) o desde un gestor de secretos.

```bash
# API
SPRING_PROFILES_ACTIVE=prod,oauth-google,oauth-facebook
JWT_SECRET=<48+ caracteres aleatorios, distinto del de desarrollo>
DB_URL=jdbc:mysql://<host>:3306/dulcemayor
DB_USERNAME=<usuario de la base, sin privilegios de administrador>
DB_PASSWORD=<…>
GOOGLE_CLIENT_ID=<…>       GOOGLE_CLIENT_SECRET=<…>
FACEBOOK_CLIENT_ID=<…>     FACEBOOK_CLIENT_SECRET=<…>
OAUTH2_REDIRECT_CLIENTE=https://www.dulcemayor.com.ar/ingresar/oauth2
CORS_ALLOWED_ORIGINS=https://admin.dulcemayor.com.ar,https://www.dulcemayor.com.ar
JEFE_INICIAL_CORREO=<correo real del jefe>

# institucional-web
API_BASE_URL=http://127.0.0.1:8080            # llamada interna del servidor
API_PUBLIC_URL=https://api.dulcemayor.com.ar  # la que ve el navegador
OAUTH_GOOGLE_HABILITADO=true
OAUTH_FACEBOOK_HABILITADO=true
COOKIE_SECURE=true
THYMELEAF_CACHE=true
FORWARD_HEADERS_STRATEGY=framework

# admin-web
API_BASE_URL=http://127.0.0.1:8080
API_PUBLIC_URL=https://api.dulcemayor.com.ar
SITIO_URL=https://www.dulcemayor.com.ar
COOKIE_SECURE=true
THYMELEAF_CACHE=true
FORWARD_HEADERS_STRATEGY=framework
```

### 5.5 Perfiles de Spring

| Perfil | Archivo | Efecto |
|---|---|---|
| (ninguno) | `application.properties` | Desarrollo, sin ingreso social |
| `oauth-google` | `application-oauth-google.properties` | Registra Google y crea la cadena `/oauth2/**` |
| `oauth-facebook` | `application-oauth-facebook.properties` | Registra Facebook |
| `prod` | `application-prod.properties` | Sin valores por defecto para secretos (si falta uno, **no arranca**), sin SQL en el log, respeta los encabezados del proxy |

Se combinan separados por coma: `prod,oauth-google,oauth-facebook`.

### 5.6 Proxy inverso

Cuando Nginx (o un balanceador) termina HTTPS y reenvía a `http://127.0.0.1:8080`, la API tiene que saber que el
pedido original era `https://api.dulcemayor.com.ar`; si no, arma la `redirect_uri` como `http://127.0.0.1:8080/…` y
Google responde `redirect_uri_mismatch`. Para eso el perfil `prod` define `server.forward-headers-strategy=framework`
y el proxy debe enviar los encabezados `X-Forwarded-*`:

```nginx
server {
    listen 443 ssl;
    server_name api.dulcemayor.com.ar;
    # ssl_certificate / ssl_certificate_key …

    location / {
        proxy_pass         http://127.0.0.1:8080;
        proxy_set_header   Host              $host;
        proxy_set_header   X-Forwarded-Proto $scheme;
        proxy_set_header   X-Forwarded-Host  $host;
        proxy_set_header   X-Forwarded-Port  443;
        proxy_set_header   X-Forwarded-For   $proxy_add_x_forwarded_for;
    }
}
server { listen 80; server_name api.dulcemayor.com.ar; return 301 https://$host$request_uri; }
```

Repetir el bloque para `admin.` (→ 8081) y `www.` (→ 8082). Los puertos 8080–8082 no deben quedar expuestos a internet.

### 5.7 Publicar la aplicación en Google

Mientras el público esté en *Testing* solo ingresan los usuarios de prueba. Para clientes reales: completar Branding
(logo, política de privacidad y términos publicados en el dominio verificado) y en *Audience* pasar a **In production**.
Con solo `openid`, `email` y `profile` el proceso es más simple que con permisos sensibles, pero Google puede pedir
verificar la marca; sin verificar, muestra una advertencia y limita la cantidad de usuarios nuevos.

### 5.8 Verificar después del despliegue

1. `https://api.dulcemayor.com.ar/api/catalogo` responde 200 (la API está viva).
2. `https://api.dulcemayor.com.ar/oauth2/authorization/google` redirige a `accounts.google.com` y la URL contiene `redirect_uri=https%3A%2F%2Fapi.dulcemayor.com.ar%2Flogin%2Foauth2%2Fcode%2Fgoogle` (con **https** y el dominio público).
3. Ingresar con una cuenta de prueba desde `https://www.dulcemayor.com.ar/ingresar`: tiene que terminar en `/mi-cuenta`.
4. La URL de vuelta al sitio contiene `?codigo=…` y **nunca** un token; recargarla debe dar «El código de ingreso no es válido o venció».
5. Repetir con Facebook si está habilitado.
6. Revisar el log de la API: no debe aparecer la advertencia del secreto JWT de desarrollo.

---

## 6. Facebook

1. Entrar a <https://developers.facebook.com/apps> y **crear una app**. Elegir el caso de uso de autenticación con **Facebook Login** («Authenticate and request data from users with Facebook Login»).
2. En la configuración del caso de uso, verificar que estén los permisos **`public_profile`** y **`email`** (coinciden con `scope=public_profile,email`).
3. En **App settings → Basic** copiar el **App ID** (`FACEBOOK_CLIENT_ID`) y el **App Secret** (`FACEBOOK_CLIENT_SECRET`); completar URL de política de privacidad, términos y dominio de la app (necesarios para publicarla).
4. En **Facebook Login → Settings**, en **Valid OAuth Redirect URIs**, agregar:
   - `https://api.dulcemayor.com.ar/login/oauth2/code/facebook` (producción)
   - para desarrollo, `http://localhost:8080/login/oauth2/code/facebook`. Meta exige HTTPS para dominios reales; si la consola no acepta la URI de `localhost`, usar un túnel HTTPS (por ejemplo, ngrok) y registrar esa URL, poniendo también `OAUTH2_REDIRECT_CLIENTE` y `API_PUBLIC_URL` acordes.
5. Mientras la app esté en **modo de desarrollo**, solo pueden ingresar las cuentas con un rol en la app (administradores, desarrolladores, testers). Para usuarios reales hay que pasarla a **modo en vivo**, lo que puede requerir verificación del negocio y revisión de Meta.
6. Activar el perfil `oauth-facebook` en la API y `OAUTH_FACEBOOK_HABILITADO=true` en el sitio.

Spring Security ya conoce los endpoints de Facebook (`CommonOAuth2Provider.FACEBOOK`), por eso alcanza con el
`client-id`, el `client-secret` y los scopes.

**Limitaciones importantes:**
- Facebook **no informa si el correo está verificado** y una cuenta puede no tener correo o no compartirlo. Por eso, en Dulce Mayor, Facebook **solo crea cuentas nuevas**; nunca toma una cuenta existente con el mismo correo. Si el usuario no compartió el correo, el ingreso se rechaza con un mensaje.
- Si no se completa la publicación de la app, dejar `OAUTH_FACEBOOK_HABILITADO=false`: el botón no aparece y el resto del sistema funciona igual.

---

## 7. Seguridad y problemas frecuentes

### Buenas prácticas aplicadas

- Los secretos (`client_secret`, `JWT_SECRET`, base de datos) solo en variables de entorno; `.env` está en `.gitignore`; el ZIP no incluye ningún `.env`.
- El `state` de OAuth2 lo genera y valida Spring Security (protege contra CSRF en el flujo).
- Google: se exige `email_verified = true`.
- Sin vinculación automática de cuentas: un correo existente nunca se «toma» desde un proveedor externo.
- Los empleados no pueden usar el ingreso social (ni el botón aparece en admin-web).
- El JWT nunca viaja en una URL: se usa un código de un solo uso, de 60 segundos.
- Al terminar el flujo se invalida la sesión temporal que usó la API para el `state`.
- En producción: HTTPS en todo, cookies `Secure`, puertos internos cerrados, `JWT_SECRET` largo y aleatorio, distinto en cada entorno.
- Si un secreto se filtra: regenerarlo en la consola (o cambiar `JWT_SECRET`, lo que invalida todas las sesiones) y actualizar la variable.

### Varias instancias de la API

`CodigosCanjeOAuth2` guarda los códigos en memoria: el canje tiene que llegar a la misma instancia que emitió el
código, y la sesión del `state` también es local. Con más de una instancia: usar afinidad de sesión en el balanceador
o mover los códigos y las sesiones a un almacenamiento compartido (por ejemplo, Redis con Spring Session).

### Problemas frecuentes

| Síntoma | Causa probable | Solución |
|---|---|---|
| `redirect_uri_mismatch` (Google) / «URL bloqueada» (Facebook) | La URI no coincide letra por letra, o el proxy no envía `X-Forwarded-*` | Comparar la `redirect_uri` de la URL de Google con la registrada; revisar 5.6 |
| La API no arranca: `Could not resolve placeholder 'GOOGLE_CLIENT_ID'` | Perfil activo sin la variable | Definir las variables o quitar el perfil |
| `/oauth2/authorization/google` responde 401 o 403 | La API corre sin el perfil `oauth-google` (la ruta cae en la cadena que deniega todo) | Activar el perfil |
| El botón no aparece en el sitio | `OAUTH_GOOGLE_HABILITADO` no es `true` | Configurarlo en institucional-web |
| «Google no confirmó que tu correo esté verificado» | Cuenta de Google con correo no verificado | Usar otra cuenta o verificar el correo |
| «Ya existe una cuenta con ese correo: ingresá con tu contraseña» | El correo ya se registró con contraseña | Es el comportamiento elegido: ingresar con la contraseña |
| «El código de ingreso no es válido o venció» | Pasaron más de 60 s, se recargó la página o hay varias instancias | Volver a ingresar; ver «Varias instancias» |
| Error 403 `access_denied` en Google | El usuario no está en la lista de prueba (*Testing*) | Agregarlo o publicar la app |

### Documentación oficial

- Google — Configurar OAuth 2.0 / Google Auth Platform: <https://support.google.com/cloud/answer/15549257>
- Google — OpenID Connect: <https://developers.google.com/identity/openid-connect/openid-connect>
- Meta — Facebook Login para la web: <https://developers.facebook.com/docs/facebook-login/web>
- Spring Security — OAuth2 Login: <https://docs.spring.io/spring-security/reference/servlet/oauth2/login/index.html>
