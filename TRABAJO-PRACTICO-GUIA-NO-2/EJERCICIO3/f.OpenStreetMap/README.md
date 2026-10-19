# 🛒 Carrito de compras – Spring Boot + MVC + Template Method + JPA + MySQL + Roles

Aplicación web de carrito de compras construida a partir del diagrama de clases del enunciado
(Carrito, Usuario, Detalle, Artículo, Imagen, Proveedor) y del template gratuito
**Start Bootstrap – Shop Homepage** (MIT).  
**Versión 2:** se incorporan **roles (ADMIN / CLIENTE)** y se reestructura el código siguiendo la forma de
implementación del patrón plantilla del proyecto de referencia **`patronTemplate`**.

| Capa / tema        | Tecnología                                                                    |
|--------------------|-------------------------------------------------------------------------------|
| Lenguaje           | Java 17 (corre también en 21)                                                 |
| Framework          | Spring Boot 3.5.16 (Spring MVC, Spring Data JPA) + Lombok                     |
| Vista              | Thymeleaf + template Start Bootstrap (Bootstrap 5.2.3)                        |
| Persistencia       | JPA / Hibernate sobre **MySQL 8**                                             |
| Patrón de diseño   | **Template Method**: `BaseEntity`, `BaseRepository`, `BaseService`, `BaseController` |
| Arquitectura       | **MVC** en capas: Controlador → Servicio → Repositorio                        |
| Seguridad          | Login por sesión, claves BCrypt, **roles verificados en servidor en cada petición** |

---

### 1.1 Roles y control de acceso
| Problema de la v1 | Solución en la v2 |
|---|---|
| Cualquier usuario logueado veía y modificaba proveedores, usuarios y artículos`/admin/**` queda **solo para ADMIN** (un CLIENTE recibe *403*). El menú "Administración" tampoco se le muestra. |
| Nadie puede eliminar **su propia cuenta** (regla en `UsuarioService`). Además, si una cuenta es eliminada por otro admin, su sesión **se invalida en la siguiente petición** (`SesionInterceptor` vuelve a leer al usuario de la base). |
| — | El **rol se lee de la base en cada petición**, nunca de la sesión: si un admin cambia el rol de alguien, rige de inmediato. |
| — | **Siempre debe existir al menos un ADMIN**: no se puede eliminar ni degradar al último. |
| — | El **registro público siempre crea CLIENTE** (`UsuarioService.registrarCliente`); nadie puede auto-registrarse como ADMIN, aunque manipule el formulario. |
| Baja de registros con un simple link (GET) | Las bajas son **POST**. |

| Rol | Puede |
|---|---|
| Sin sesión | Ver el catálogo, registrarse e ingresar |
| **CLIENTE** | Todo lo anterior + usar su carrito (agregar, quitar, vaciar, confirmar compra) |
| **ADMIN** | Todo lo anterior + ABM de artículos, proveedores y usuarios (incluida la asignación de roles) |

Mapa de acceso (`WebConfig`):

| Ruta | Acceso |
|---|---|
| `/`, `/login`, `/registro`, `/imagenes/{id}` | Público |
| `/carrito/**` | Usuario logueado (cualquier rol) |
| `/admin/**` | **Solo ADMIN** |

Usuarios de ejemplo (se crean al iniciar): **`admin` / `admin123`** (ADMIN) y **`cliente` / `cliente123`** (CLIENTE).

---

## 2. Requisitos y ejecución

- **JDK 17+**, **Maven 3.9+**, **MySQL 8** en `localhost:3306`. Internet la primera vez (dependencias Maven y CDN de Bootstrap).
- **Lombok**: el IDE necesita el plugin de Lombok / *annotation processing* activado (igual que en `patronTemplate`).

```bash
# opcional: credenciales de MySQL (por defecto root / root)
export DB_USER=root
export DB_PASSWORD=tu_clave      # Windows PowerShell:  $env:DB_PASSWORD="tu_clave"
mvn spring-boot:run
```
Abrí **http://localhost:8080**. La app crea sola la base `tienda_carrito` y las tablas.
Otras variables: `DB_HOST`, `DB_PORT`, `DB_NAME`. Datos de ejemplo: `app.seed.enabled=false` para desactivarlos.

### ⚠️ Si ya tenías la base de la versión 1
La tabla `usuarios` gana una columna `rol`. Dos opciones:
1. **Más simple** (si no te importan los datos de prueba): `DROP DATABASE tienda_carrito;` y arrancar la app.
2. **Conservando datos**: arrancar igual. Hibernate agrega `rol` con valor por defecto `CLIENTE` y al iniciar la app **promueve a ADMIN al usuario `admin`** si no hay ningún administrador. Ver `docs/migracion-roles.sql`.

---

## 3. Estructura

```
src/main/java/com/tienda/carrito
├── business
│   ├── domain/entity        BaseEntity, Rol, Usuario, Proveedor, Imagen, Articulo, Carrito, Detalle
│   ├── logic
│   │   ├── error            ErrorServiceException (checked)
│   │   └── service          BaseService, BaseUseCaseService, y un servicio por entidad  ← TODAS las reglas de negocio
│   └── percistence/repository   BaseRepository + un repositorio por entidad
├── controller/view          BaseController, BaseUseCaseController, ProveedorController, UsuarioController,
│                            ArticuloController, InicioController, CarritoController, AuthController,
│                            ImagenController, FalloController
├── config                   WebConfig (mapa de acceso), SesionInterceptor, AutorizacionInterceptor,
│                            AtributosGlobales, ConfiguracionSeguridad (BCrypt), DataLoader, SesionUsuario
└── dto                      ArticuloCatalogoDTO, ArchivoImagen

src/main/resources/templates
├── fragments/               layout (head, mensajes, scripts), menubody, footer
└── view/                    inicio, carrito, login, registro, error,
                             lProveedor/eProveedor, lUsuario/eUsuario, lArticulo/eArticulo
```

## 4. Diagrama de clases → JPA

| Diagrama | Implementación |
|---|---|
| `id: String`, `eliminado: boolean` | `id` UUID por entidad; `eliminado` en `BaseEntity` (**baja lógica**) |
| `Carrito → Usuario` | `Carrito.usuario` `@ManyToOne` |
| `Carrito ◆— Detalle` (composición) | `Carrito.detalles` `@OneToMany(mappedBy, cascade=ALL)`; `Detalle.carrito` `@ManyToOne` |
| `Detalle → Articulo` | `Detalle.articulo` `@ManyToOne` |
| `Articulo → Imagen (1…*)` | `Articulo.imagenes` `@OneToMany` + `@JoinColumn` |
| `Articulo → Proveedor` | `Articulo.proveedor` `@ManyToOne` |
| `contenido: [] byte` | `byte[]` `@Lob` → `LONGBLOB` |
|  `Usuario.rol` (`Rol`: `ADMIN`, `CLIENTE`) |

Supuestos: "Uusario" es un error de tipeo (`Usuario`); `Detalle` no tiene cantidad, así que **cada detalle = una unidad**;
`Carrito` no tiene estado, así que un carrito con `eliminado=true` es un **carrito cerrado** (compra confirmada).

## 5. Arquitectura y reglas de capas

```
Navegador ──► Controlador ──► Servicio ──► Repositorio ──► MySQL
                  │       (reglas de negocio)
                  └──► Vista Thymeleaf
```
1. **Las reglas de negocio viven únicamente en los servicios.** Los controladores delegan y eligen la vista.
2. **Un servicio habla con UN solo repositorio** (el suyo). Para otra entidad usa el **otro servicio**.

```
CarritoService ──► UsuarioService
      └────────► DetalleService ──► ArticuloService ──► ProveedorService
                                           └──────────► ImagenService
```

## 6. Patrón Template Method

**`BaseService<T, ID>`** (métodos plantilla, orden fijo):

| Plantilla | Pasos |
|---|---|
| `alta(e)` | `validar(ALTA)` → `preAlta` → guardar → `postAlta` |
| `modificar(id, e)` | `validar(MODIFICACION)` → `preModificacion` → guardar *(la entidad recibida reemplaza a la guardada)* |
| `baja(id)` | `validar(BAJA)` → `preBaja` → `eliminado=true` → guardar |

Paso obligatorio: `validar(useCase, entidad)`. Hooks opcionales: `preAlta`, `postAlta`, `preModificacion`, `preBaja`.


```java
@Controller
@RequestMapping("/admin/proveedor")
public class ProveedorController extends BaseController<Proveedor, String> {
    public ProveedorController(ProveedorService service) {
        super(service);
        initController(new Proveedor(), "Listado de proveedores", "Proveedor");
    }
}
```
Hooks usados: `UsuarioController` (carga roles y pasa el usuario logueado a la baja), `ArticuloController` (carga proveedores y procesa imágenes).

### Receta: agregar una entidad nueva (ej. `Categoria`)
1. `Categoria extends BaseEntity<String>` (`@Entity`, id UUID, getters/setters).
2. `CategoriaRepository extends BaseRepository<Categoria, String>`.
3. `CategoriaService extends BaseService<Categoria, String>` → implementar `validar(...)`.
4. `CategoriaController extends BaseController<Categoria, String>` con `@RequestMapping("/admin/categoria")` e `initController(...)`.
5. Vistas `view/lCategoria.html` y `view/eCategoria.html` (copiar las de Proveedor).
> Todo lo que cuelgue de `/admin/**` queda automáticamente protegido para ADMIN.

> Los métodos de `BaseService` no son `final` a propósito: Spring aplica `@Transactional` con un proxy CGLIB que no puede interceptar métodos `final`.

## 7. Reglas de negocio (todas en `business/logic/service`)

| Servicio | Reglas |
|---|---|
| `UsuarioService` | Nombre 3–50 y único. Clave 6–72 (obligatoria al crear; al editar, vacía = se conserva) con BCrypt. Rol obligatorio. Registro público ⇒ siempre CLIENTE. **Siempre ≥ 1 ADMIN** (no se elimina ni degrada al último). **Nadie elimina su propia cuenta.** |
| `ProveedorService` | Nombre obligatorio (≤100) y único entre activos. Dirección opcional (≤255). Latitud y longitud van juntas (o ninguna) y en rango (−90..90 / −180..180). |
| `ImagenService` | Solo PNG/JPEG/GIF/WEBP, ≤2 MB, *magic bytes* coherentes con el tipo. SVG excluido (puede traer scripts). |
| `ArticuloService` | Nombre ≤150, precio > 0, proveedor existente, ≥1 imagen al crear, no se quita la última. Alta con imágenes en una transacción. |
| `DetalleService` | Pertenece a un carrito activo y apunta a un artículo disponible. |
| `CarritoService` | Un carrito activo por usuario, total recalculado en el servidor, solo se quitan detalles propios, no se confirma vacío, confirmar = cerrar. El usuario dueño sale **siempre de la sesión**, nunca del formulario. |

## 8. Rutas

| Ruta | Acción | Acceso |
|---|---|---|
| `GET /` | Catálogo | Público |
| `GET/POST /login`, `POST /logout`, `GET/POST /registro` | Sesión y alta de cuenta (CLIENTE) | Público |
| `GET /carrito`, `POST /carrito/agregar/{id}`, `/detalle/{id}/quitar`, `/vaciar`, `/confirmar` | Carrito | Logueado |
| `GET /admin/{articulo,proveedor,usuario}/list` | Listado | **ADMIN** |
| `GET …/alta`, `…/consultar/{id}`, `…/modificar/{id}`, `…/cancelar` | Pantallas | **ADMIN** |
| `POST …/actualizar`, `POST …/baja/{id}` | Guardar / eliminar | **ADMIN** |
| `POST /admin/articulo/{id}/imagen/{imagenId}/baja` | Quitar imagen | **ADMIN** |
| `GET /admin/proveedor/mapa` | Mapa (OpenStreetMap) con todos los proveedores que tienen ubicación | **ADMIN** |
| `GET /admin/proveedor/ubicaciones` | JSON `[{id, nombre, direccion, latitud, longitud}]` que consume el mapa | **ADMIN** |

## 9. Cómo probar

### 9.1 Pruebas de roles (lo nuevo)
| # | Acción | Resultado esperado |
|---|---|---|
| 1 | Sin sesión, abrir `http://localhost:8080/admin/usuario/list` | Redirige a `/login` |
| 2 | Ingresar como `cliente` / `cliente123` | El menú **no** muestra "Administración" ni el badge "Admin" |
| 3 | Con ese cliente, escribir a mano `http://localhost:8080/admin/articulo/list` (y `/admin/proveedor/list`, `/admin/usuario/list`) | Página de error **403** "No tenés permisos…" |
| 4 | El cliente agrega artículos y usa su carrito | Funciona normalmente |
| 5 | Registrar una cuenta nueva desde **Registrarme** | La cuenta es CLIENTE (probá el punto 3 con ella) |
| 6 | Ingresar como `admin` / `admin123` | Aparece **Administración** y el badge **Admin**; las 3 pantallas funcionan |
| 7 | Admin → Usuarios → **Eliminar** al propio `admin` | Error "No podés eliminar tu propia cuenta." |
| 8 | Admin → Usuarios → **Modificar** `admin` y poner rol Cliente (siendo el único admin) | Error "Debe existir al menos un administrador…" |
| 9 | Dos navegadores: A = `cliente` logueado; B = `admin` que elimina a `cliente` | En A, al navegar a cualquier página, la sesión se cierra sola (aparecen "Ingresar/Registrarme") |
| 10 | Admin cambia a `cliente` a rol Administrador | En A, al recargar aparece **Administración** (rige de inmediato) |
| 11 | Admin → Usuarios → **Modificar** un usuario sin escribir clave | Se conserva la clave anterior (puede seguir ingresando) |
| 12 | Admin → Artículos → **Modificar** un artículo | Conserva sus imágenes |

### 9.2 Pruebas del carrito y ABM 
Catálogo con 6 artículos → agregar sin login redirige a login → agregar 2 → badge 2 → carrito suma el total → quitar → vaciar →
confirmar vacío da error → confirmar compra cierra el carrito (`carritos.eliminado = 1`). En ABM: proveedor duplicado, artículo sin imagen,
precio 0, archivo `.txt` renombrado a `.png`, imagen > 2 MB, quitar la última imagen, eliminar artículo (baja lógica).

### 9.3 Verificación en MySQL
```sql
USE tienda_carrito;
SELECT id, nombre, rol, eliminado FROM usuarios;   -- clave = hash BCrypt; rol = ADMIN / CLIENTE
SELECT id, total, usuario_id, eliminado FROM carritos;
```

### 9.4 Tests unitarios
```bash
mvn test
```
- `BaseServiceTemplateTest`: orden de los pasos del Template Method (alta, modificar, baja).
- `UsuarioServiceTest`: roles, último admin, autoeliminación, registro siempre CLIENTE, conserva clave, login.
- `InterceptoresTest`: sin sesión → login; CLIENTE en `/admin` → 403; ADMIN pasa; sesión de usuario eliminado se invalida.
- `ProveedorServiceTest`, `CarritoServiceTest`, `ArticuloServiceTest`: reglas de negocio.

Usan Mockito: **no necesitan MySQL**.

## 10. Aspectos importantes / límites conocidos
- **Sin CSRF**: no hay Spring Security completo ni tokens CSRF. Las bajas son POST y las cookies de sesión modernas usan `SameSite=Lax` por defecto en los navegadores, pero para producción conviene Spring Security (CSRF, HTTPS, *rate limit* de login).
- **Autorización en dos niveles**: ruta (interceptor, `/admin/**`) y reglas de dominio en servicios (último admin, autoeliminación). Cualquier controlador nuevo bajo `/admin/**` queda protegido solo.
- **Un chequeo extra por página**: `SesionInterceptor` consulta al usuario en la base en cada petición (excepto estáticos e imágenes). Es el costo de que rol y bajas rijan al instante.
- **OSIV activo** (`open-in-view=true`) para que Thymeleaf lea relaciones LAZY; el catálogo usa DTO para no cargar los bytes de las imágenes.
- **`ddl-auto=update`** es para desarrollo; en producción, `validate` + Flyway/Liquibase.
- **Imágenes en LONGBLOB** (lo pide el diagrama); a gran escala conviene almacenamiento externo.
- **Precios con `double`** (como el diagrama); el precio no se congela en `Detalle`.
- **`listarActivos()`** filtra en memoria (como en `patronTemplate`): correcto para este volumen; con muchos registros conviene una consulta `WHERE eliminado = FALSE` y paginación.
- **Spring Boot 3.5.x** finalizó su soporte OSS el 30/06/2026; se usa 3.5.16 por estabilidad con Java 17. Para producción nueva, migrar a Boot 4.x.
- **Lombok** requiere el plugin/annotation processing habilitado en el IDE.

## 11. Problemas frecuentes
| Síntoma | Solución |
|---|---|
| `Access denied for user 'root'@'localhost'` | Definir `DB_PASSWORD` |
| `Communications link failure` | MySQL apagado o `DB_HOST`/`DB_PORT` incorrectos |
| Errores "cannot find symbol … getXxx" en el IDE | Activar Lombok / annotation processing |
| `No enum constant ...Rol.` al iniciar sesión | Base v1 con la columna `rol` vacía: ejecutar `docs/migracion-roles.sql` o recrear la base |
| Iconos / menú no funcionan | El navegador no cargó los CDN de Bootstrap (sin internet) |

**Créditos:** template [Start Bootstrap – Shop Homepage](https://startbootstrap.com/template/shop-homepage) (MIT).

## 10. Mapa de proveedores (OpenStreetMap + Leaflet)

- **Datos:** `Proveedor` guarda `direccion`, `latitud` y `longitud` (Hibernate agrega las columnas solo con `ddl-auto=update`; los proveedores viejos quedan con esos campos en `NULL`).
- **Formulario** (`eProveedor.html`): se escribe la dirección y **Buscar en el mapa** la convierte en coordenadas con *Nominatim* (geocodificador de OpenStreetMap), o se hace **clic en el mapa** para marcar el punto exacto. En *Consultar* el mapa es de solo lectura.
- **Mapa general** (`mProveedor.html`, menú *Administración → Mapa de proveedores* o botón *Ver mapa* del listado): dibuja un marcador por proveedor con popup (nombre, dirección y link a *Modificar*) y ajusta el zoom para que se vean todos.
- **Librería:** Leaflet 1.9.4 por CDN (fragmentos `leaflet-css` y `leaflet-js` en `fragments/layout.html`). Hace falta internet para cargar los mosaicos del mapa.
- **Reglas** (en `ProveedorService`): latitud y longitud juntas o ninguna, y dentro de rango.
- **Atribución:** el mapa muestra "© OpenStreetMap", que es obligatoria. Nominatim pide uso moderado (la búsqueda se dispara solo con el botón o Enter).
- Los proveedores de ejemplo (`DataLoader`) se crean con ubicaciones aproximadas en Mendoza. Si tu base ya tenía proveedores, no se vuelven a cargar: editalos y marcá su ubicación.
