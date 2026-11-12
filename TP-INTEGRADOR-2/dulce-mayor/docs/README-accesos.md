# Accesos y credenciales de prueba

> **Solo para desarrollo y demostración.** Estas cuentas las crea la precarga del backend
> (`InicializadorDatos` e `InicializadorDatosDemo`). No usar en producción.
> Requiere `DATOS_DEMO_HABILITADO=true` en `backend/.env` (es el valor por defecto).

## Aplicaciones y puertos

| Aplicación | Carpeta | Puerto | Quién ingresa |
|---|---|---|---|
| API REST | `backend/` | 8080 | Nadie ingresa directamente (lo consumen las apps) |
| Sistema administrativo | `admin-web/` | 8081 | Jefe y administrativo |
| Sitio institucional (clientes) | `institucional-web/` | 8082 | Clientes |
| App del preventista | `movil/preventista/` | 5173 | Preventistas |
| App del distribuidor | `movil/distribuidor/` | 5174 | Distribuidores |

## Credenciales

| Rol | Correo | Clave | Dónde ingresar |
|---|---|---|---|
| **Jefe** | `jefe@dulcemayor.com.ar` | `dulcemayor` | admin-web (8081) |
| **Administrativo** | `lucia.fernandez@demo.dulcemayor.com.ar` | `dulcemayor` | admin-web (8081) |
| **Preventista** | `martin.gimenez@demo.dulcemayor.com.ar` | `dulcemayor` | App preventista (5173) |
| | `sofia.herrera@demo.dulcemayor.com.ar` | `dulcemayor` | |
| | `diego.quiroga@demo.dulcemayor.com.ar` | `dulcemayor` | |
| | `valeria.ortiz@demo.dulcemayor.com.ar` | `dulcemayor` | |
| **Distribuidor** | `raul.benitez@demo.dulcemayor.com.ar` | `dulcemayor` | App distribuidor (5174) |
| | `pablo.sosa@demo.dulcemayor.com.ar` | `dulcemayor` | |
| | `nicolas.ruiz@demo.dulcemayor.com.ar` | `dulcemayor` | |
| **Cliente** | `ricardo.gomez@demo.dulcemayor.com.ar` | `Cliente2026` | Sitio institucional (8082) |
| | `marta.perez@demo.dulcemayor.com.ar` | `Cliente2026` | |
| | `hugo.sanchez@demo.dulcemayor.com.ar` | `Cliente2026` | |

### Clientes de demostración

Formato de correo: **`nombre.apellido@demo.dulcemayor.com.ar`** (minúsculas, sin acentos).
Clave de todos: **`Cliente2026`**.

`ricardo.gomez`, `marta.perez`, `hugo.sanchez`, `laura.diaz`, `carlos.romero`, `silvia.torres`,
`jorge.alvarez`, `patricia.ruiz`, `fernando.castro`, `gabriela.molina`, `alberto.medina`,
`claudia.suarez`, `oscar.benavidez`, `monica.rojas`, `daniel.vega`, `liliana.acosta`,
`sergio.paredes`, `beatriz.luna`, `gustavo.ibanez`, `elena.cabrera`, `marcelo.arias`, `noemi.funes`.

## Puntos importantes

- **Los empleados (jefe, administrativo, preventistas y distribuidores) tienen clave temporal
  `dulcemayor`** y el sistema **obliga a cambiarla en el primer ingreso**. Si ya la cambiaste en tu base,
  `dulcemayor` deja de servir para esa cuenta.
- Los empleados también pueden ingresar a admin-web para cambiar la contraseña; sus apps son las de 5173/5174.
- El **jefe** crea a los demás empleados; no hay otro jefe que se pueda crear desde el sistema.
- Los **clientes de demo** no tienen cambio de clave obligatorio.
- El backend debe estar en marcha (8080) para que funcionen todas las apps.

## Habilitar el ingreso con Google

Tener `GOOGLE_CLIENT_ID` y `GOOGLE_CLIENT_SECRET` en `backend/.env` **no alcanza**: las credenciales solo se
usan si la API arranca con el perfil `oauth-google`. Son tres pasos.

### 1. Registrar la URI de redirección en Google Cloud

En <https://console.cloud.google.com/> → *Google Auth Platform → Clients*, en el cliente OAuth de tipo
**Web application** agregar como **Authorized redirect URI**:

```
http://localhost:8080/login/oauth2/code/google
```

Para producción: `https://api.<tu-dominio>/login/oauth2/code/google`.
Usar siempre `localhost` (no `127.0.0.1`): para Google son URIs distintas.

### 2. Activar el perfil `oauth-google`

El perfil se puede definir **en `backend/.env`** (recomendado) o como variable de entorno/argumento:

```properties
# backend/.env
SPRING_PROFILES_ACTIVE=oauth-google
```

```powershell
# Alternativa: variable de entorno
cd backend
$env:SPRING_PROFILES_ACTIVE="oauth-google"; .\mvnw.cmd spring-boot:run
```

```bat
:: cmd
cd backend
set SPRING_PROFILES_ACTIVE=oauth-google
mvnw.cmd spring-boot:run
```

En IntelliJ/VSCode: configurar *Active profiles* = `oauth-google`.

Al arrancar, el log debe mostrar `The following 1 profile is active: "oauth-google"`.

Variables que deben estar en `backend/.env` (no las subas al repositorio):

```properties
SPRING_PROFILES_ACTIVE=oauth-google
GOOGLE_CLIENT_ID=<client id de Google>
GOOGLE_CLIENT_SECRET=<client secret de Google>
OAUTH2_REDIRECT_CLIENTE=http://localhost:8082/ingresar/oauth2
```

### 3. Mostrar el botón en el sitio institucional

El botón «Continuar con Google» está oculto por defecto. Se activa **desde `institucional-web/.env`**:

```properties
# institucional-web/.env
OAUTH_GOOGLE_HABILITADO=true
```

El sitio lee ese archivo automáticamente (o la variable de entorno equivalente al arrancar):

```powershell
cd institucional-web
$env:OAUTH_GOOGLE_HABILITADO="true"; .\mvnw.cmd spring-boot:run
```

Si el sitio se sirve desde otro host/puerto, `API_PUBLIC_URL` debe apuntar a la API
(`http://localhost:8080` por defecto), porque el botón enlaza a
`<API_PUBLIC_URL>/oauth2/authorization/google`.

### 4. Probar

1. Abrir <http://localhost:8082/ingresar> y elegir «Continuar con Google».
2. Ingresar con una cuenta agregada como **usuario de prueba** en *Google Auth Platform → Audience*
   (mientras la app esté en modo *Testing* solo entran esos correos).
3. Debe volver a `http://localhost:8082/mi-cuenta` con la sesión iniciada.
4. Reglas: solo ingresan **clientes**; el correo de un empleado se rechaza, y un cliente registrado con
   contraseña debe ingresar con su contraseña (no se vinculan cuentas automáticamente).

> **Seguridad:** el `client secret` y los tokens nunca se documentan ni se suben al repositorio. Este README
> solo referencia las variables; los valores reales viven en `backend/.env` (ignorado por Git).

Para el detalle completo (pantalla de consentimiento, producción, proxy inverso, Facebook y problemas
frecuentes) ver [`CONFIGURACION-OAUTH.md`](../CONFIGURACION-OAUTH.md).
