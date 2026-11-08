# Dulce Mayor — Etapa 2: Personas, Usuarios, Autenticación y Autorización

Continuación del proyecto del Trabajo Práctico Integrador N.º 2 (mayorista de golosinas, Mendoza).
Esta etapa suma, sobre el módulo de **Catálogo y Stock** ya existente:

- **HU-07** Alta de empleados (administrativo, preventista y distribuidor) con su cuenta, rol y contraseña temporal.
- **HU-06** Cambio obligatorio de la contraseña temporal, **aplicado en el backend**.
- **HU-09** Registro de clientes desde el sitio institucional, con su propia contraseña y rol CLIENTE fijo.
- Ingreso con correo y contraseña (JWT), autorización por roles con Spring Security y una matriz de permisos protegida en el servidor.
- Ingreso de **clientes** con Google (OpenID Connect) y Facebook (OAuth 2.0), preparado y documentado; los empleados no pueden usarlo.
- Vistas Thymeleaf de **admin-web** (panel administrativo) e **institucional-web** (sitio comercial), responsive y con apariencia de aplicación en celulares.

La configuración de Google y Facebook está explicada paso a paso en [`CONFIGURACION-OAUTH.md`](CONFIGURACION-OAUTH.md).

> **Importante sobre la verificación:** en el entorno donde se desarrolló esta etapa no había acceso a Maven Central,
> así que **no se pudo ejecutar `mvnw`, levantar Spring Boot ni correr las pruebas JUnit**. Lo que sí se ejecutó
> y lo que queda por ejecutar está detallado en la [sección 12](#12-qué-se-verificó-y-qué-no). Antes de la entrega,
> el equipo tiene que correr `mvnw test` en los tres proyectos.

---

## Índice

1. [Estructura del proyecto](#1-estructura-del-proyecto)
2. [Cómo ejecutar](#2-cómo-ejecutar)
3. [Funcionalidades implementadas](#3-funcionalidades-implementadas)
4. [Clases reutilizadas, modificadas y nuevas](#4-clases-reutilizadas-modificadas-y-nuevas)
5. [Integración con Catálogo y Stock](#5-integración-con-catálogo-y-stock)
6. [Autenticación](#6-autenticación)
7. [Autorización y matriz de permisos](#7-autorización-y-matriz-de-permisos)
8. [Endpoints nuevos](#8-endpoints-nuevos)
9. [Vistas: escritorio y celular](#9-vistas-escritorio-y-celular)
10. [Decisiones de diseño](#10-decisiones-de-diseño)
11. [Funcionalidades pendientes](#11-funcionalidades-pendientes)
12. [Qué se verificó y qué no](#12-qué-se-verificó-y-qué-no)

---

## 1. Estructura del proyecto

```
dulce-mayor/
├── backend/             API REST (Spring Boot 4.1, Java 25, JPA, Spring Security, JWT, OAuth2 Client) — puerto 8080
├── admin-web/           Sistema administrativo (Spring MVC + Thymeleaf)                               — puerto 8081
├── institucional-web/   Sitio institucional para clientes (Spring MVC + Thymeleaf)                    — puerto 8082
├── movil/               Apps React del preventista y distribuidor
├── docker-compose.yml   PostgreSQL local para desarrollo
├── Jenkinsfile          Pipeline de validación de los cuatro clientes y el backend
├── .gitignore
├── README.md
└── CONFIGURACION-OAUTH.md
```

La arquitectura se mantiene: **cliente-servidor**, **MVC**, **tres capas** y **REST**. Las dos aplicaciones web son
clientes de la API: no tienen base de datos propia.

| Capa | backend | admin-web / institucional-web |
|---|---|---|
| Controlador | `controller/*` — recibe el pedido, delega en un servicio, devuelve DTO. Sin lógica de negocio ni repositorios. | `controller/*` — recibe el formulario, delega en un servicio, elige la vista. |
| Servicio | `service/*` — toda la lógica y las validaciones. Usa **solo su repositorio**; para otras entidades llama al servicio correspondiente. | `service/*` — arma las solicitudes y las respuestas para las vistas. |
| Datos | `repository/*` — solo persistencia (Spring Data JPA). | `cliente/ClienteApi` — acceso a la API (cumple el rol del repositorio). |

### Paquetes nuevos o ampliados del backend (`com.ejercicioIntegrador.dulcemayor`)

```
config/        SecurityConfig (reemplaza la provisoria), SeguridadOAuth2Config, ConfiguracionAutenticacion, InicializadorDatos
controller/    ControladorAutenticacion, ControladorEmpleado, ControladorCliente, ControladorUsuario,
               ControladorPais, ControladorProvincia, ControladorDepartamento, ControladorLocalidad
dto/           Requests/Responses de personas, usuarios, ubicación y sesión (ver sección 4)
enumeraciones/ RolUsuario, ProveedorAutenticacion, TipoDocumento
model/         Persona (abstracta), Empleado, Preventista, Distribuidor, Cliente, Usuario,
               Pais, Provincia, Departamento, Localidad, Direccion
repository/    Un repositorio por entidad nueva
security/      JwtService, JwtAuthenticationFilter, DetallesUsuarioService, ManejadorSeguridadJson,
               AutoridadesSeguridad, CodigosCanjeOAuth2, OAuth2ExitoHandler, OAuth2FalloHandler
service/       ServicioUsuario, ServicioEmpleado, ServicioCliente, ServicioAutenticacion,
               ServicioPais, ServicioProvincia, ServicioDepartamento, ServicioLocalidad, ServicioDireccion,
               ValidacionesPersona
```

---

## 2. Cómo ejecutar

### Requisitos

- JDK 25 y conexión a Maven Central (cada proyecto trae su `mvnw`).
- PostgreSQL 17 (recomendado; para desarrollo local puede iniciarse con Docker Compose, ver abajo). MySQL sigue disponible configurando explícitamente `DB_URL` y usando credenciales válidas.

### Configuración del backend

1. Copiar `backend/.env.example` como `backend/.env` y completarlo. **El `.env` no se sube al repositorio** (ya figura en `.gitignore`). Ver la sección [Configuración con archivos `.env`](#configuración-con-archivos-env) para el detalle de todas las claves.
2. Generar un secreto propio para el JWT (mínimo 32 caracteres), por ejemplo `openssl rand -base64 48`, y ponerlo en `JWT_SECRET`.
   Si no se define, en desarrollo se usa un valor de ejemplo y la API muestra una advertencia en el log; **con el perfil `prod` la API no arranca sin él**.

| Variable | Para qué | Valor por defecto (desarrollo) |
|---|---|---|
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | Base de datos | `jdbc:postgresql://localhost:5432/dulcemayor`, `dulcemayor`, `dulcemayor_dev` (solo desarrollo local) |
| `JWT_SECRET` | Firma de los tokens (HS256) | valor de ejemplo, solo para desarrollo |
| `JWT_EXPIRATION_MS` | Duración del token | `28800000` (8 h) |
| `JEFE_INICIAL_CORREO` | Correo del jefe que se crea al iniciar | `jefe@dulcemayor.com.ar` |
| `CORS_ALLOWED_ORIGINS` | Orígenes permitidos | `http://localhost:5173,…:5174,…:8081,…:8082` |
| `OAUTH2_REDIRECT_CLIENTE` | Página del sitio que recibe el código tras Google/Facebook | `http://localhost:8082/ingresar/oauth2` |
| `APP_OAUTH2_HABILITADO` | Habilita el ingreso de clientes con Google/Facebook desde el `.env` | `false` |
| `GOOGLE_CLIENT_ID/SECRET`, `FACEBOOK_CLIENT_ID/SECRET` | Credenciales de los proveedores | sin valor |

### Configuración de las aplicaciones web

| Variable | admin-web | institucional-web |
|---|---|---|
| `SERVER_PORT` | `8081` | `8082` |
| `API_BASE_URL` (llamadas servidor → API) | `http://localhost:8080` | `http://localhost:8080` |
| `API_PUBLIC_URL` (navegador → API: imágenes y botones sociales) | `http://localhost:8080` | `http://localhost:8080` |
| `SITIO_URL` | `http://localhost:8082` | — |
| `OAUTH_GOOGLE_HABILITADO` / `OAUTH_FACEBOOK_HABILITADO` | — | `false` / `false` |
| `COOKIE_SECURE` | `false` (poner `true` con HTTPS) | ídem |

### Base de desarrollo PostgreSQL con Docker Compose

Requiere Docker Desktop iniciado. Desde la raíz del repositorio:

```bat
docker compose up -d postgres
docker compose ps
```

Compose conserva los datos en el volumen `dulce-mayor_postgres-data`. Para eliminar la base y volver a empezar, ejecutar explícitamente `docker compose down -v` (borra los datos). Usuario/base/contraseña por defecto son únicamente para desarrollo local y pueden sustituirse con `POSTGRES_USER`, `POSTGRES_DB`, `POSTGRES_PASSWORD` y `POSTGRES_PORT` en el entorno.

### Arranque (Windows)

```bat
cd backend           && mvnw.cmd spring-boot:run
cd admin-web         && mvnw.cmd spring-boot:run
cd institucional-web && mvnw.cmd spring-boot:run
```

El backend toma PostgreSQL local de `application.properties` de forma predeterminada. Si se configura `backend/.env`, establecer `DB_URL=jdbc:postgresql://localhost:5432/dulcemayor`, `DB_USERNAME=dulcemayor` y `DB_PASSWORD=dulcemayor_dev`, o los valores que se hayan configurado en Compose. En entornos persistentes existentes mantener `JPA_DDL_AUTO=update` hasta que haya migraciones revisadas; no usar `create` salvo que se quiera borrar/recrear el esquema.

### Primer ingreso

Al iniciar, `InicializadorDatos` carga (a través de los servicios, con sus validaciones) Argentina, Mendoza, sus 18
departamentos con la localidad cabecera de cada uno, y el **jefe administrativo inicial**:

- Correo: el de `JEFE_INICIAL_CORREO` (por defecto `jefe@dulcemayor.com.ar`).
- Contraseña temporal: `dulcemayor`. Al ingresar en <http://localhost:8081> se lo lleva a cambiarla y no puede hacer nada más hasta hacerlo.

Es idempotente: si los datos ya existen, no los duplica.

### Configuración con archivos `.env`

Toda la configuración vive en archivos `.env`: **no hace falta definir variables de entorno del sistema**.
Cada aplicación trae su `.env.example`; se copia a `.env` (ignorado por git) y se edita:

| Aplicación | Archivo a crear | Qué se configura |
|---|---|---|
| API | `backend/.env` | base de datos, `JWT_SECRET`, jefe inicial, OAuth2, Mercado Pago, correo, newsletter, datos demo |
| Panel | `admin-web/.env` | puerto, URLs de la API y del sitio, cookies de sesión |
| Sitio | `institucional-web/.env` | puerto, URLs de la API, botones sociales |
| Apps móviles | `movil/*/.env` | solo si la API no está en `http://localhost:8080` |

```bat
copy backend\.env.example backend\.env
copy admin-web\.env.example admin-web\.env
copy institucional-web\.env.example institucional-web\.env
```

El backend carga el `.env` con `spring.config.import`, el panel y el sitio hacen lo mismo desde su propia
carpeta y desde la raíz del proyecto; las apps Vite leen `.env` de su carpeta.

> `APP_OAUTH2_HABILITADO=true` + credenciales en `backend/.env` habilitan el ingreso de clientes con Google o
> Facebook (sin perfiles de Spring). Para que el sitio muestre los botones hay que activar además
> `OAUTH_GOOGLE_HABILITADO` / `OAUTH_FACEBOOK_HABILITADO` en `institucional-web/.env`.

### Datos de demostración (opcional)

Para probar el flujo completo sin cargar todo a mano, el backend incluye un dataset **apagado por defecto**.
Se activa desde el `.env`:

```properties
DATOS_DEMO_HABILITADO=true
```

`InicializadorDemo` es idempotente y crea todo con los mismos servicios que usa la API (mismas validaciones):

| Qué | Detalle |
|---|---|
| Empleados | `admin@dulcemayor.test` (ADMINISTRATIVO), `preventista@dulcemayor.test`, `distribuidor@dulcemayor.test` |
| Clave | `dulcemayor` (temporal: el primer ingreso obliga a cambiarla, igual que cualquier alta del panel) |
| Catálogo | 2 categorías, 3 subcategorías y **12 productos** `DEMO-001`…`DEMO-012` con precio, stock e imagen |
| Comercios | **12 puntos de venta** distribuidos en Capital, Godoy Cruz, Las Heras, Guaymallén, Luján y Maipú, todos con coordenadas para el mapa |
| Pedidos | 12 pedidos: confirmados a la espera de pago, con transferencia acreditada, entregados por ruta y uno cancelado |
| Rutas | Hoja de ruta del día con varias paradas (algunas entregadas) para el mapa del distribuidor |
| Visitas | Plan del día con toda la cartera, para el mapa del preventista |
| Comercial | Promoción `DEMO10` (10 %), dos sucursales y dos suscriptores de newsletter |

El comercio `Kiosco Demo` usa el correo **`kiosco.demo@dulcemayor.test`**. Ese correo es el vínculo
cliente ↔ pedidos: registrate con él en <http://localhost:8082/registro> y vas a ver sus pedidos en
«Mis pedidos». Los demás comercios usan `comercioNN@dulcemayor.test`.

### Pruebas

```bat
cd backend && mvnw.cmd test
cd admin-web && mvnw.cmd test
cd institucional-web && mvnw.cmd test
```

El `Jenkinsfile` ejecuta `mvnw.cmd -B -ntp verify` para los tres proyectos Java y `npm ci`, lint y build para ambas apps móviles. Requiere un agente Jenkins Windows con JDK 25, Node.js/npm instalados y acceso a los repositorios de dependencias. No configura ni almacena credenciales.

---

## 3. Funcionalidades implementadas

### HU-07 — Alta de empleados (jefe administrativo)

- Formulario en **admin-web → Empleados → Nuevo empleado** y endpoint `POST /api/empleados`.
- Datos: nombre, apellido, tipo y número de documento, fecha de nacimiento, correo, rol, legajo (opcional: se genera `EMP-00001`, `EMP-00002`…), fecha de ingreso (por defecto hoy) y dirección completa (país → provincia → departamento → localidad, calle, número, barrio, manzana/piso, casa/depto., referencia).
- Datos según el rol: **zona** para el preventista; **vehículo y patente** (formato `ABC123` o `AB123CD`) para el distribuidor.
- Validaciones en `ServicioEmpleado` y `ValidacionesPersona`: obligatorios, formato de correo, documento según su tipo (DNI 7–8 dígitos, CUIL/CUIT 11, pasaporte 6–9 alfanuméricos), mayoría de edad, localidad activa, **correo duplicado** (entre cuentas activas) y **documento duplicado**.
- Rol: se instancia la subclase que corresponde (`Empleado`, `Preventista`, `Distribuidor`). CLIENTE no es un rol de empleado.
- **El jefe puede dar de alta todos los roles menos JEFE**: el formulario ofrece Administrativo, Preventista y Distribuidor, y la API rechaza un segundo JEFE mientras exista uno activo. El jefe tampoco se puede dar de baja.
- Se crea el **usuario** asociado (`nombreUsuario` = correo) con la contraseña temporal **`dulcemayor`**, guardada con **BCrypt**, y `debeCambiarClave = true`.
- Modificación (sin cambiar rol ni correo, que identifican la cuenta) y baja lógica, que **deshabilita la cuenta**.
- Si una validación falla después de crear el usuario o la dirección, la transacción (`@Transactional(rollbackFor = Exception.class)`) deshace todo.

### HU-06 — Cambio obligatorio de contraseña

- El login con la contraseña temporal devuelve el token igual, pero con `debeCambiarClave: true`.
- Mientras la cuenta tenga la contraseña pendiente, **en cada pedido** el filtro JWT le asigna una única autoridad, `CAMBIO_CLAVE_PENDIENTE`, en lugar de su rol. Con ella solo puede usar `GET /api/auth/yo`, `PUT /api/auth/clave` y lo público. Cualquier otro endpoint responde **403** con el encabezado `X-Accion-Requerida: CAMBIAR_CLAVE`. No alcanza con guardar un token viejo o llamar a otro endpoint: las autoridades se recalculan desde la base en cada pedido.
- `PUT /api/auth/clave` exige la contraseña actual, la confirmación y la política (8–72 caracteres, letras y números, distinta de `dulcemayor` y del correo); apaga `debeCambiarClave` y devuelve un **token nuevo** con los permisos del rol.
- En admin-web, el interceptor también redirige a `/cambiar-clave` hasta que se complete.

### HU-09 — Registro de clientes

- Formulario en **institucional-web → Crear cuenta** y endpoint público `POST /api/auth/registro`.
- El cliente **elige su contraseña** (misma política) y la confirma; **no queda obligado a cambiarla**.
- `RegistroClienteDTO` **no tiene campo rol**: la API asigna siempre CLIENTE. Si alguien envía `"rol": "JEFE"` en el JSON, se ignora.
- Documento y fecha de nacimiento son opcionales; si se informan, se validan (y el documento no puede repetirse).
- Tras registrarse queda con la sesión iniciada.
- En **Mi cuenta** el cliente edita sus datos personales (`PUT /api/clientes/yo`, con `DatosPersonalesDTO`, que no tiene correo, clave ni rol) y puede cambiar su contraseña voluntariamente.

### Ingreso con Google y Facebook (solo clientes)

Implementado en la API con `spring-boot-starter-security-oauth2-client`. Se activa con los perfiles `oauth-google` /
`oauth-facebook` y las credenciales en variables de entorno. **Hasta que el equipo cree las credenciales en las
consolas de Google y Meta, no está operativo**: el código está listo, pero falta esa configuración externa.

Reglas (`ServicioAutenticacion.ingresarConRedSocial`):

| Situación | Resultado |
|---|---|
| Google informa el correo **no verificado** | Rechazado |
| Correo nuevo | Se crea un CLIENTE sin contraseña local, con el proveedor registrado |
| Correo de un cliente que ya ingresó con **ese mismo** proveedor | Ingresa a su cuenta |
| Correo de una cuenta **local** (correo y contraseña) | Rechazado: «Ya existe una cuenta con ese correo: ingresá con tu contraseña» |
| Correo de un cliente de **otro** proveedor | Rechazado: «… ingresá con Google» (o Facebook) |
| Correo de un **empleado** | Rechazado: «Los empleados ingresan con correo y contraseña desde el sistema administrativo» |
| Cuenta dada de baja | Rechazado |

Así no se duplican cuentas ni se vincula una cuenta existente por el solo hecho de que un proveedor externo diga
tener ese correo. Facebook no informa si el correo está verificado: por eso **solo puede crear cuentas nuevas**,
nunca tomar una existente.

El token **no viaja en la URL**: al terminar, la API redirige al sitio con un **código de un solo uso**
(256 bits aleatorios, vence a los 60 segundos) y el sitio lo canjea por el token con `POST /api/auth/oauth2/canje`.

---

## 4. Clases reutilizadas, modificadas y nuevas

### Reutilizadas sin cambios (de la etapa de Catálogo y Stock)

`BaseEntity`, `BaseResponseDTO`, `ErrorDTO`, `BaseController`, `BaseUseCaseController`, `BaseUseCaseService`,
`MiException`, `RecursoNoEncontradoException`, `OperacionNoPermitidaException`, y todo el módulo de catálogo y stock.

Todos los servicios nuevos (`ServicioUsuario`, `ServicioEmpleado`, `ServicioCliente`, `ServicioPais`,
`ServicioProvincia`, `ServicioDepartamento`, `ServicioLocalidad`, `ServicioDireccion`) **extienden `BaseService`**
e implementan sus pasos (`crearEntidad`, `actualizarEntidad`, `validar`, `convertirAResponse`, `nombreEntidad`)
y los ganchos que necesitan (`preAlta` para cifrar la clave, `postBaja` para deshabilitar la cuenta).
Todos los controladores de recursos **extienden `BaseController`** y usan `operacionHabilitada` para cerrar las
operaciones que no corresponden (por ejemplo, `ControladorCliente` no permite alta ni modificación genérica, y
`ControladorUsuario` es de solo lectura). `ServicioAutenticacion` no hereda de `BaseService` porque no administra
una entidad propia: orquesta a los demás servicios.

### Modificadas, con justificación

| Clase | Cambio | Por qué |
|---|---|---|
| `BaseService` | `alta(dto)` ahora delega en un nuevo método plantilla público `altaEntidad(dto)` que devuelve la **entidad** guardada (mismos pasos, mismo orden). | Un servicio necesita crear una entidad de otro servicio para asociarla (el empleado crea su `Usuario` y su `Direccion`). Antes solo existía `alta`, que devuelve el DTO, y obligaba a duplicar el algoritmo o a volver a buscar la entidad. El algoritmo sigue definido en un único lugar. |
| `ManejadorErrores` | Dos manejadores nuevos: `BadCredentialsException` → 401 «Correo o contraseña incorrectos» y `AccountStatusException` → 401. | El login necesita respuestas uniformes en formato `ErrorDTO` que no revelen si el correo existe. |
| `SecurityConfig` | Reemplaza la configuración provisoria (todo permitido) por la matriz de la sección 7. | Era el punto previsto para esta etapa. |
| `pom.xml` (backend) | jjwt 0.12.6, OAuth2 Client, H2 y `spring-boot-starter-data-jpa-test` para pruebas. | JWT, ingreso social y pruebas de integración sin MySQL. |

### Nuevas en la API

- **Modelo:** `Persona` (abstracta, herencia `JOINED`), `Empleado`, `Preventista`, `Distribuidor`, `Cliente`, `Usuario`; y las de ubicación del Integrador 1 adaptadas: `Pais`, `Provincia`, `Departamento`, `Localidad`, `Direccion`.
- **DTO:** `EmpleadoRequestDTO/ResponseDTO`, `ClienteRequestDTO/ResponseDTO`, `RegistroClienteDTO`, `DatosPersonalesDTO`, `UsuarioRequestDTO/ResponseDTO` (sin clave), `LoginRequestDTO`, `CambioClaveRequestDTO`, `SesionDTO`, `UsuarioSesionDTO`, `CanjeCodigoDTO`, `IdentidadExternaDTO`, y Request/Response de país, provincia, departamento, localidad y dirección.
- **Seguridad:** `JwtService`, `JwtAuthenticationFilter`, `DetallesUsuarioService`, `AutoridadesSeguridad`, `ManejadorSeguridadJson`, `CodigosCanjeOAuth2`, `OAuth2ExitoHandler`, `OAuth2FalloHandler`, `ConfiguracionAutenticacion`, `SeguridadOAuth2Config`, `InicializadorDatos`.

### Nuevas en las aplicaciones web

| admin-web (`com.dulcemayor.admin`) | institucional-web (`com.dulcemayor.institucional`) |
|---|---|
| `cliente/ClienteApi`, `ApiException` | ídem |
| `seguridad/SesionUsuario`, `GestorSesion`, `InterceptorAcceso` | `SesionCliente`, `GestorSesion`, `InterceptorAcceso` |
| `service/ServicioAcceso`, `ServicioEmpleados`, `ServicioUbicacion`, `ServicioCatalogo`, `ServicioClientes`, `ServicioPanel` | `ServicioAcceso`, `ServicioCuenta`, `ServicioCatalogo` |
| `controller/ControladorAcceso`, `ControladorInicio`, `ControladorEmpleados`, `ControladorUbicacion`, `ControladorCatalogo`, `ControladorClientes`, `AtributosComunes`, `ManejadorErroresWeb` | `ControladorAcceso`, `ControladorInicio`, `ControladorCatalogo`, `ControladorCuenta`, `AtributosComunes`, `ManejadorErroresWeb` |
| DTO de respuesta (clases), de solicitud (records) y de formulario | ídem |

Patrones: solo **Template Method** y **DTO**, como se pidió. No se agregaron otros.

---

## 5. Integración con Catálogo y Stock

- Los endpoints del módulo quedan protegidos: el **catálogo público** (`/api/catalogo`, categorías, subcategorías e imágenes) sigue abierto; la **gestión** (productos, imágenes, vigencias de precio, stock) pasa a requerir ADMINISTRATIVO (o JEFE); el **preventista** puede consultar productos y la disponibilidad de stock (`GET /api/productos/**`, `GET /api/stock/producto/{id}/disponibilidad`), que es lo que necesitará para tomar pedidos.
- `Persona.imagen` reutiliza la entidad `Imagen` del catálogo (tipo `PERSONA`); el servicio de empleados la obtiene a través de `ServicioImagen`.
- admin-web muestra el catálogo publicado con precio y stock y arma el panel con datos reales de stock (alertas bajo mínimo y sin stock).
- institucional-web muestra el catálogo público (portada, ofertas, filtros por categoría, búsqueda).
- Las pruebas del módulo anterior no se modificaron.

---

## 6. Autenticación

```
admin-web / institucional-web            API (8080)
  formulario de ingreso  ──POST /api/auth/login {correo, clave}──▶  AuthenticationManager (BCrypt)
                         ◀── 200 {token, usuario{rol, debeCambiarClave,…}}
  guarda el token en la HttpSession (servidor)
  cada pedido: Authorization: Bearer <token> ──▶ JwtAuthenticationFilter
                                                  ├─ valida firma y vencimiento
                                                  ├─ vuelve a leer la cuenta (activa, rol, debeCambiarClave)
                                                  └─ autoridades: ROLE_<rol>  o  CAMBIO_CLAVE_PENDIENTE
```

- **Contraseñas:** BCrypt. Las respuestas nunca incluyen la clave ni su hash (`UsuarioResponseDTO`, `EmpleadoResponseDTO`, `UsuarioSesionDTO` no tienen ese campo).
- **JWT:** HS256 con `JWT_SECRET`; contiene el correo, el rol y el id de usuario; vence en 8 h. Como las autoridades se recalculan desde la base, una baja o un cambio de clave pendiente surten efecto en el siguiente pedido, aunque el token siga vigente.
- **API sin estado:** la cadena `/api/**` es `STATELESS` y sin CSRF (no usa cookies). Solo el flujo OAuth2 (`/oauth2/**`, `/login/oauth2/**`) usa una sesión breve para el `state`.
- **Aplicaciones web:** el token queda en la **sesión del servidor**; el navegador solo recibe una cookie `HttpOnly`, `SameSite=Lax`, con nombre propio por aplicación (`DM_ADMIN_SESION`, `DM_SITIO_SESION`), porque los navegadores comparten cookies entre puertos de `localhost`. Al ingresar se descarta la sesión anterior y se crea una nueva (evita la fijación de sesión).
- **Separación de accesos:** admin-web no abre sesión para cuentas CLIENTE e institucional-web no la abre para empleados, aunque la API las autentique. El login de empleados **no tiene botones sociales**; los de Google y Facebook están solo en el ingreso y registro de clientes, y solo se muestran si el proveedor está habilitado.

---

## 7. Autorización y matriz de permisos

Spring Security aplica las reglas en el servidor (`SecurityConfig`). Jerarquía de roles: **JEFE ⊃ ADMINISTRATIVO** y
**JEFE ⊃ PREVENTISTA** (`RoleHierarchy`). Lo que no está expresamente permitido queda **denegado** (`denyAll`),
incluidos los módulos que todavía no existen.

### Matriz por endpoint

| Recurso | Público | CLIENTE | PREVENTISTA | DISTRIBUIDOR | ADMINISTRATIVO | JEFE |
|---|:-:|:-:|:-:|:-:|:-:|:-:|
| `POST /api/auth/login`, `/registro`, `/oauth2/canje` | ✔ | ✔ | ✔ | ✔ | ✔ | ✔ |
| `GET /api/auth/yo`, `PUT /api/auth/clave` | | ✔ | ✔ | ✔ | ✔ | ✔ |
| `GET` catálogo, categorías, subcategorías, contenido de imágenes | ✔ | ✔ | ✔ | ✔ | ✔ | ✔ |
| `GET /api/productos/**`, `GET /api/stock/producto/{id}/disponibilidad` | | | ✔ | | ✔ | ✔ |
| `/api/preventista/puntos-venta/**`, `/api/preventista/pedidos/**`, `/api/preventista/visitas/**` (cartera, pedidos, cobros y plan de visitas propios) | | | ✔ | | | ✔ |
| `/api/pedidos/**` (bandeja, detalle, pagos y cierre autorizado de la reserva) | | | | | ✔ | ✔ |
| `/api/pagos/**` (conciliación de transferencias, cobro en efectivo y consulta) | | | | | ✔ | ✔ |
| `/api/promociones/**` (códigos promocionales y vista previa) | | | | | ✔ | ✔ |
| `/api/rutas/**` (asignación de pedidos a la jornada de reparto) | | | | | ✔ | ✔ |
| `/api/distribuidor/**` (solo la hoja de ruta propia) | | | | ✔ | ✔ | |
| `/api/cliente/**` (pedidos y comprobantes propios del cliente) | | ✔ | | | | |
| `GET /api/sucursales`, `GET /api/cotizacion-dolar` (sitio público) | ✔ | ✔ | ✔ | ✔ | ✔ | ✔ |
| `POST /api/pagos/mercadopago/notificacion` (webhook autenticado con firma HMAC) | ✔ | | | | | |
| Gestión de categorías, subcategorías, productos, imágenes, precios y stock | | | | | ✔ | ✔ |
| `GET` países, provincias, departamentos, localidades | | | ✔ | ✔ | ✔ | ✔ |
| Alta, modificación y baja de ubicaciones | | | | | ✔ | ✔ |
| `/api/empleados/**` (alta, consulta, modificación, baja) | | | | | | ✔ |
| `/api/usuarios/**` (solo lectura) | | | | | | ✔ |
| `GET`/`PUT /api/clientes/yo` | | ✔ | | | | |
| `GET /api/clientes/**` | | | | | ✔ | ✔ |
| `DELETE /api/clientes/{id}` | | | | | | ✔ |
| Cualquier otra ruta (rutas, promociones, facturación, pagos…) | ✖ | ✖ | ✖ | ✖ | ✖ | ✖ |

Con la contraseña temporal pendiente, cualquier rol queda limitado a la segunda fila y a lo público.

### Resumen por rol

| Rol | Lectura | Creación | Modificación | Eliminación | Pendiente (módulos futuros) |
|---|---|---|---|---|---|
| **JEFE** | Todo lo del administrativo; empleados y usuarios | Empleados (todos los roles menos JEFE); todo lo del administrativo | Empleados (sin cambiar rol ni correo); todo lo del administrativo | Empleados (no al jefe); clientes; todo lo del administrativo | Reportes, aprobación de promociones |
| **ADMINISTRATIVO** | Catálogo, stock, precios, ubicaciones, clientes | Categorías, subcategorías, productos, imágenes, precios, stock, ubicaciones | Ídem; cierre de pedidos confirmados (completar/cancelar) | Bajas lógicas de lo mismo | Bandeja de pedidos, facturación, pagos, promociones, rutas, sucursales |
| **PREVENTISTA** | Productos, disponibilidad de stock, ubicaciones, su cartera y sus pedidos, su sesión | Puntos de venta propios y pedidos de su cartera | Su contraseña y los borradores propios | — | Mapa y ruta de visitas, promociones |
| **DISTRIBUIDOR** | Ubicaciones, su sesión | — | Su contraseña | — | Hoja de ruta, entregas, cobros |
| **CLIENTE** | Catálogo público, sus datos | Su cuenta (registro) | Sus datos personales y su contraseña (si es local) | — | Pedidos online, seguimiento |

En las vistas, el menú se arma según el rol (`SesionUsuario`), y el interceptor de admin-web aplica la misma matriz
para la navegación; pero **la autorización que vale es la de la API**: aunque alguien escriba una URL a mano, la API
responde 401/403.

---

## 8. Endpoints nuevos

Todos responden JSON; los errores usan el `ErrorDTO` de siempre (`estado`, `error`, `mensaje`, `fecha`).

| Método y ruta | Descripción |
|---|---|
| `POST /api/auth/login` | Ingreso con `{correo, clave}` → `SesionDTO` |
| `POST /api/auth/registro` | Registro de cliente (`RegistroClienteDTO`) → 201 + `SesionDTO` |
| `GET /api/auth/yo` | Datos de la cuenta autenticada |
| `PUT /api/auth/clave` | Cambio de contraseña `{claveActual, nuevaClave, confirmarClave}` → token nuevo |
| `POST /api/auth/oauth2/canje` | Canje del código de un solo uso `{codigo}` → `SesionDTO` |
| `GET /oauth2/authorization/{google\|facebook}` | Inicio del ingreso social (con el perfil activo) |
| `GET /login/oauth2/code/{google\|facebook}` | Retorno del proveedor (lo maneja Spring Security) |
| `GET/POST /api/empleados`, `GET/PUT/DELETE /api/empleados/{id}`, `GET /api/empleados/todos` | CRUD de empleados (`BaseController`) |
| `GET /api/empleados/rol/{rol}`, `GET /api/empleados/legajo/{legajo}` | Búsquedas |
| `GET /api/usuarios`, `/{id}`, `/todos`, `/buscar?correo=` | Consulta de cuentas (sin clave) |
| `GET /api/clientes`, `/{id}`, `/todos`, `DELETE /api/clientes/{id}` | Consulta y baja de clientes |
| `GET/PUT /api/clientes/yo` | Datos del cliente autenticado |
| `/api/paises`, `/api/provincias`, `/api/departamentos`, `/api/localidades` | CRUD (`BaseController`) + `/buscar?nombre=` |
| `GET /api/provincias/pais/{id}`, `/api/departamentos/provincia/{id}`, `/api/localidades/departamento/{id}` | Activos por padre (selects en cascada); `…/todas` incluye bajas |
| `GET /api/localidades/codigo-postal/{cp}` | Búsqueda por código postal |
| `GET/POST /api/preventista/puntos-venta`, `GET /api/preventista/puntos-venta/cliente/{idCliente}` | Cartera propia del preventista: alta del responsable con uno o varios puntos de venta |
| `GET/POST /api/preventista/pedidos`, `PUT /api/preventista/pedidos/{id}/lineas`, `POST …/confirmar`, `POST …/cancelar` | Pedido presencial: borrador, ajuste de líneas con recálculo en el servidor, confirmación con reserva de stock y liberación |
| `POST /api/pedidos/{id}/completar`, `POST /api/pedidos/{id}/cancelar` | Cierre autorizado por ADMINISTRATIVO: consume (solo con pago acreditado) o libera la reserva |
| `GET /api/pedidos`, `GET /api/pedidos/{id}` | Bandeja y detalle de pedidos para administración |
| `GET/POST /api/preventista/pedidos/{id}/pagos`, `GET …/comprobante` | Cobro del pedido (transferencia, Mercado Pago o efectivo) y comprobante interno descargable |
| `POST /api/pedidos/{id}/pagos`, `POST /api/pedidos/{id}/pagos/efectivo` | Iniciar un cobro o registrar el cobro en efectivo al entregar |
| `POST /api/pagos/{id}/acreditar`, `POST /api/pagos/{id}/rechazar` | Conciliación de transferencias y verificación de Mercado Pago |
| `POST /api/pagos/mercadopago/notificacion` | Webhook de Mercado Pago (firma HMAC + consulta server-to-server) |
| `GET /api/pedidos/{id}/comprobante` | Comprobante interno no fiscal (HTML imprimible) |
| `GET/POST /api/promociones`, `PUT/DELETE /{id}`, `POST /api/promociones/previsualizar` | Códigos promocionales: un solo código por pedido, sin acumulación |
| `POST /api/preventista/pedidos/{id}/promocion`, `DELETE …/promocion` | Aplicar o quitar el código en un borrador (descuento calculado en el servidor) |
| `POST /api/rutas`, `GET /api/rutas`, `GET /api/rutas/{id}` | Asignar pedidos a la jornada de reparto de un distribuidor |
| `GET /api/distribuidor/ruta`, `GET /{id}`, `PUT /{id}/orden`, `POST /api/distribuidor/paradas/{id}/entregar`, `…/fallar` | Hoja de ruta del distribuidor: entrega con cobro en efectivo o intento fallido con motivo |
| `GET /api/sucursales` (pública), `GET /api/sucursales/todas`, `POST/PUT/DELETE` | Sucursales del sitio institucional |
| `GET /api/cotizacion-dolar` (pública) | Cotización informativa del BCRA con caché y degradación controlada |
| `GET /api/cliente/pedidos`, `/{id}`, `/{id}/comprobante` | Portal del cliente: solo sus pedidos y comprobantes |
| `GET/POST /api/preventista/visitas`, `POST …/generar`, `PUT …/orden`, `DELETE …/{id}` | Planificación diaria de visitas por cercanía, con reordenamiento manual |

### Pagos

El estado del pago (`PENDIENTE`, `ACREDITADO`, `RECHAZADO`, `EXPIRADO`, `CANCELADO`) se lleva por separado del
estado del pedido. Solo la acreditación del total consume la reserva de stock y emite el comprobante interno no
fiscal; la cancelación o el vencimiento liberan la reserva, en la misma transacción.

| Medio | Cómo se acredita | Reserva |
|---|---|---|
| Transferencia | Conciliación manual del administrativo con el número de operación (obligatorio) | 24 h |
| Mercado Pago | Webhook con firma HMAC + consulta server-to-server; nunca se confía en el retorno del navegador | 24 h |
| Efectivo | Registro manual al entregar (interino hasta la app del distribuidor) | 7 días |

Configuración por variables de entorno (todas opcionales; sin credenciales Mercado Pago responde «no configurado»
y **no** se simula ninguna acreditación):

| Variable | Uso |
|---|---|
| `MERCADOPAGO_ACCESS_TOKEN` | Token de la cuenta; habilita crear preferencias y verificar pagos |
| `MERCADOPAGO_WEBHOOK_SECRET` | Secreto para validar `x-signature`; sin él el webhook responde 401 |
| `MERCADOPAGO_NOTIFICACION_URL` | URL pública del webhook que se registra en la preferencia |
| `MERCADOPAGO_RUTA_PREFERENCIAS` | Ruta de creación de preferencias (por defecto `/preferences`) |
| `PAGOS_TRANSFERENCIA_VIGENCIA_HORAS`, `PAGOS_EFECTIVO_VIGENCIA_HORAS` | Ventanas de reserva (24 y 168) |
| `PAGOS_EXPIRACION_INTERVALO_MS`, `PAGOS_EXPIRACION_RETRASO_INICIAL_MS` | Cadencia de la tarea que expira pagos |
| `PAGOS_EXPIRACION_HABILITADA` | `false` desactiva la tarea (se usa en las pruebas) |

Ejemplo:

```bash
curl -s -X POST http://localhost:8080/api/auth/login -H "Content-Type: application/json" \
     -d "{\"correo\":\"jefe@dulcemayor.com.ar\",\"clave\":\"dulcemayor\"}"
# -> {"token":"…","tipo":"Bearer","expiraEnSegundos":28800,"usuario":{"rol":"JEFE","debeCambiarClave":true,…}}

curl -s http://localhost:8080/api/empleados -H "Authorization: Bearer <token>"
# -> 403 y encabezado X-Accion-Requerida: CAMBIAR_CLAVE (hasta cambiar la contraseña)
```

---

## 9. Vistas: escritorio y celular

Tecnologías: **Thymeleaf + HTML + CSS + JavaScript propio**, sin frameworks nuevos (el proyecto no usaba Bootstrap,
así que no se agregó). No es una PWA ni requiere instalación.

### Identidad visual común

Las maquetas de referencia se usaron como guía de paleta, tipografía y composición, sin copiar componentes que no
tienen datos todavía (ventas, pedidos, promociones, sucursales).

- **Estilos centralizados** en `static/css/dulcemayor.css`, con tokens (`--dm-*`) para colores, tipografía, espaciados, bordes y sombras. El archivo es **idéntico** en ambas aplicaciones, igual que `static/js/dulcemayor.js` y `templates/fragments/iconos.html`; si se modifica, hay que copiarlo en las dos.
- **Paleta:** magenta `#d10f8c`, violeta `#7a35e0`, azul `#1f6feb` y amarillo `#ffc52e` de acento; degradado magenta→violeta→azul para acciones principales; verde, naranja y rojo para éxito, advertencia y error.
- **Tipografía:** Poppins (títulos) e Inter (texto), con fuentes del sistema como respaldo.
- **Componentes:** botones (primario, secundario, acento, fantasma, peligro, social) con estados; campos de 48 px de alto; tarjetas; insignias; alertas; tablas; indicadores; íconos SVG propios en un sprite (sin librerías externas).

### admin-web (escritorio: dashboard)

Menú lateral fijo con las secciones del rol y los **módulos futuros marcados como «Pronto»**, barra superior con el
usuario, indicadores calculados con datos reales de la API, control de stock, catálogo por categoría (gráfico de dona
en CSS), personal por rol y accesos rápidos. Pantallas: ingreso, cambio de contraseña, panel, empleados (lista con
filtros por rol, alta, edición, baja), catálogo y clientes.

### institucional-web (escritorio: comercial)

Cinta superior, cabecera con buscador, menú (Inicio, Catálogo mayorista, Ofertas, Nosotros), portada con categorías,
ofertas y productos, ventajas del mayorista y pie. Pantallas: inicio, catálogo con filtros, ingresar, crear cuenta y
mi cuenta. Los productos **no tienen botón de compra**: los pedidos online todavía no existen, y el sitio lo aclara.

### En celulares (≤ 991 px en admin-web, ≤ 899 px en el sitio)

- **Barra de navegación inferior** tipo aplicación (Inicio, Catálogo, Empleados/Clientes u Ofertas, Cuenta, Menú) y encabezado compacto; en admin-web el menú lateral se convierte en un cajón deslizable.
- Pantallas de acceso a pantalla completa con encabezado de color, campos amplios y botones de ancho completo.
- Las **tablas se transforman en tarjetas** (cada fila apila sus datos con su etiqueta): no hay desplazamiento horizontal de la página. Las únicas filas deslizables son las pestañas de filtro y las categorías, a propósito.
- En formularios largos, la barra de acciones (Cancelar / Guardar) queda fija sobre la navegación inferior.
- Respeta las zonas seguras del celular (`env(safe-area-inset-*)`), `inputmode`/`autocomplete` adecuados en cada campo y `prefers-reduced-motion`.
- No hay vistas duplicadas: es la misma plantilla con media queries (mobile-first).

### Capturas

En `docs/capturas/` hay capturas de escritorio (1440 px) y celular (390 px). **Son vistas previas con datos de
ejemplo**, generadas con el renderizador de prueba descripto en la sección 12, no con la aplicación en ejecución.
En las de celular de página completa, la barra inferior aparece a mitad de la imagen porque es fija.

---

## 10. Decisiones de diseño

- **`nombreUsuario` = correo.** Es el identificador de ingreso y se normaliza (minúsculas, sin espacios).
- **Unicidad del correo entre cuentas activas.** Una cuenta dada de baja conserva su historial; si la persona vuelve (por ejemplo, un reingreso), se le crea una cuenta nueva con contraseña temporal.
- **Un solo jefe administrativo** creado al iniciar; el jefe crea a los demás. No se puede crear otro jefe ni dar de baja al actual desde el sistema.
- **Contraseña temporal fija `dulcemayor`**, como pide la HU-06; el riesgo se acota porque no sirve para otra cosa que cambiarla.
- **Ingreso social existente: «rechazar y avisar»**, sin vinculación automática de cuentas.
- **Códigos de canje en memoria.** Sirven para una única instancia de la API; si se escala a varias, hay que moverlos a un almacenamiento compartido (ver `CONFIGURACION-OAUTH.md`).
- **CSRF en las apps web:** no se agregó Spring Security a admin-web ni a institucional-web (habría sido una dependencia nueva); los formularios se protegen con la cookie de sesión `SameSite=Lax`, que el navegador no envía en envíos de formularios desde otros sitios. Si se quiere una defensa adicional, el paso siguiente es sumar Spring Security con CSRF en esas apps.
- **Duración:** el token dura 8 h, la sesión web vence a los 30 min de inactividad.

---

## 11. Fuera de alcance en esta etapa

El sistema está completo para el circuito comercial. Quedan explícitamente afuera, por decisión de alcance:

| Pendiente | Motivo |
|---|---|
| Chatbot público de presupuesto | Se incorpora en una etapa posterior, con proveedor aprobado |
| Pipeline Jenkins | La etapa de CI/despliegue se retoma después de cerrar la funcionalidad |
| Facturación fiscal (CAE/AFIP) | El comprobante emitido es **interno y no fiscal**, como se acordó |
| Pedidos online desde el portal | El pedido lo carga el preventista en la visita; el cliente solo consulta |
| Verificación de correo del responsable | El alta del preventista no crea cuenta utilizable todavía |
| Recuperación de contraseña por correo | Hoy el jefe puede dar de baja y volver a dar de alta |
| Facebook en producción | Requiere crear la app en Meta y su revisión; Google queda operativo con credenciales |

---

## 12. Qué se verificó

Todas las verificaciones se ejecutaron en esta máquina, con Maven Central y Node disponibles:

| Verificación | Resultado |
|---|---|
| Backend `mvnw clean verify` | **BUILD SUCCESS — 158 pruebas**, 0 fallas/errores (unitarias, de seguridad e integración con Spring Security real, H2 y flyway de datos) |
| admin-web `mvnw verify` | **BUILD SUCCESS — 13 pruebas**, incluidas las que renderizan con Thymeleaf real cada pantalla nueva (pedidos, rutas, promociones, reportes, notificaciones, puntos de venta) |
| institucional-web `mvnw verify` | **BUILD SUCCESS — 3 pruebas**, con render de la página pública de sucursales |
| App del preventista | `npm run lint` y `npm run build` sin errores |
| App del distribuidor | `npm run lint` y `npm run build` sin errores |
| Dataset de demostración | Prueba de integración que verifica 12 comercios, 12 productos, pedidos en cuatro estados, hoja de ruta con paradas y plan de visitas, y que la carga es **idempotente** |

**No verificado (requiere infraestructura o credenciales externas):**
- PostgreSQL real: las pruebas corren sobre H2 en memoria (el daemon de Docker no estaba disponible). El esquema se
  genera con `ddl-auto=update`; no hay migraciones versionadas todavía.
- Envío real de correo: sin `MAIL_HOST` los envíos quedan registrados como `SIMULADO`.
- Mercado Pago: sin credenciales la API responde «no configurado», sin simular acreditaciones.
- Google/Facebook: el flujo OAuth está implementado y es configurable por `.env`, pero no se ejecutó contra los
  proveedores.
- Comparación pixel a pixel en navegador: no se ejecutó Playwright; la consistencia visual se verificó por tokens
  compartidos y estilos.

---

## 13. Guía de prueba de punta a punta

### Preparación (una sola vez)

```bat
docker compose up -d postgres

copy backend\.env.example backend\.env
copy admin-web\.env.example admin-web\.env
copy institucional-web\.env.example institucional-web\.env
```

Editá `backend/.env`: `JWT_SECRET` con una clave de 32+ caracteres y **`DATOS_DEMO_HABILITADO=true`**.

```bat
cd backend           && mvnw.cmd spring-boot:run     REM API        → http://localhost:8080
cd admin-web         && mvnw.cmd spring-boot:run     REM Panel      → http://localhost:8081
cd institucional-web && mvnw.cmd spring-boot:run     REM Sitio      → http://localhost:8082
cd movil\preventista  && npm run dev                 REM Preventista→ http://localhost:5173
cd movil\distribuidor && npm run dev                 REM Distribuidor→ http://localhost:5174
```

### Credenciales

| Rol | Correo | Clave |
|---|---|---|
| Jefe administrativo | `jefe@dulcemayor.com.ar` | `dulcemayor` (cambia en el primer ingreso) |
| Administrativo | `admin@dulcemayor.test` | `dulcemayor` (cambia en el primer ingreso) |
| Preventista | `preventista@dulcemayor.test` | `dulcemayor` (ídem) |
| Distribuidor | `distribuidor@dulcemayor.test` | `dulcemayor` (ídem) |
| Cliente | `kiosco.demo@dulcemayor.test` | la elegís al registrarte en el sitio |

### Recorrido completo (30–40 minutos)

**1. Alta y seguridad (panel, 8081)**
1. Ingresá con el jefe y cambiá la clave: es obligatorio y hasta hacerlo todo responde 403.
2. **Empleados** → verificá que estén los tres usuarios demo y creá uno nuevo si querés ver el alta.
3. Entrá con `admin@dulcemayor.test`, `preventista@dulcemayor.test` y `distribuidor@dulcemayor.test`
   (uno por vez) y cambiá la clave temporal de cada uno. **Sin este paso las apps móviles rechazan la cuenta.**

**2. Catálogo y stock (panel)**
4. **Catálogo y stock** → 12 productos `DEMO-001…012` con precio vigente, imagen y stock.
5. Editá un producto, cargá stock a otro y comprobá que el catálogo público lo refleja.

**3. Cartera y visita (app del preventista, 5173)**
6. Ingresá con el preventista: **Cartera** muestra los 12 comercios.
7. **Planificar ruta** → *Generar plan*: ordena la visita por cercanía y dibuja los 12 puntos en el mapa.
   Reordená con las flechas y comprobá que el orden se guarda.
8. **Nuevo comercio** → tocá el mapa para fijar el pin manual (latitud/longitud se completan solas) y guardá.

**4. Pedido y cobro (app del preventista)**
9. Cargá un pedido: comercio → productos y cantidades → código `DEMO10` → **Confirmar pedido**.
   El total aparece con descuento y el stock queda **reservado** (no consumido).
10. En **Pedidos** tocá **Cobrar** y elegí el medio: transferencia, Mercado Pago o efectivo.

**5. Conciliación (panel)**
11. **Pedidos** → abrí el pedido: iniciá el cobro si no lo hiciste y **Acreditá** la transferencia con un número
    de operación. Al acreditar se **consume el stock** y se emite el comprobante.
12. **Pedidos → Comprobante** descarga el comprobante interno (HTML imprimible).
13. Probá **Cancelar**: libera la reserva y el stock vuelve a estar disponible.

**6. Promociones (panel)**
14. **Promociones** → creá un código porcentual y otro de monto fijo. Aplicá uno y verificá el tope y el uso único.

**7. Ruta y entrega (panel + app del distribuidor)**
15. **Rutas** → seleccioná pedidos *Confirmados* o *Completados*, asigná al distribuidor y creá la hoja.
16. En la **app del distribuidor (5174)** ingresá con su cuenta: ves la hoja del día, el mapa con las paradas y la
    **siguiente parada sugerida**.
17. **Registrar entrega**: si el pago era en efectivo se acredita en ese momento, se consume el stock y se emite el
    comprobante; el pedido pasa a `ENTREGADO`.
18. En otra parada usá **intento fallido** con un motivo: el pedido sigue reservado y se puede reprogramar.

**8. Portal del cliente (sitio, 8082)**
19. **Registro** con el correo `kiosco.demo@dulcemayor.test` (el email de contacto del comercio es el vínculo).
20. **Mis pedidos**: estado, detalle con descuento y descarga del comprobante cuando el pago está acreditado.
21. **Catálogo** público, **Sucursales** con mapa y **cotización del BCRA** en el pie.

**9. Newsletter y correo (sitio + panel)**
22. Suscribite desde el pie del sitio con cualquier correo.
23. Panel → **Notificaciones**: ves el registro de correos (`SIMULADO` si no configuraste SMTP) y los suscriptores;
    **Enviar newsletter** dispara el envío mensual sin esperar al día 5.

**10. Reportes (panel)**
24. **Reportes** → filtrá por fechas y agrupá por departamento, preventista, comercio o estado.
25. **Exportar CSV** abre el archivo en Excel con acentos correctos; abajo están la serie por preventista y el stock
    por producto con los que están bajo mínimo.

**11. Ingreso con Google (opcional)**
26. Poné `APP_OAUTH2_HABILITADO=true`, `GOOGLE_CLIENT_ID` y `GOOGLE_CLIENT_SECRET` en `backend/.env`, y
    `OAUTH_GOOGLE_HABILITADO=true` en `institucional-web/.env`. Reiniciá API y sitio: en `/ingresar` aparece
    «Continuar con Google» y el cliente entra con su cuenta existente (la API le crea la cuenta `CLIENTE` en el
    primer ingreso).

### Verificación rápida por consola (opcional)

```bat
REM Login y token
curl -s -X POST http://localhost:8080/api/auth/login -H "Content-Type: application/json" ^
     -d "{\"correo\":\"jefe@dulcemayor.com.ar\",\"clave\":\"dulcemayor\"}"

REM Catálogo público y cotización
curl -s http://localhost:8080/api/catalogo
curl -s http://localhost:8080/api/cotizacion-dolar

REM Reporte de ventas del mes
curl -s "http://localhost:8080/api/reportes/ventas?agrupacion=preventista" -H "Authorization: Bearer <TOKEN>"
```