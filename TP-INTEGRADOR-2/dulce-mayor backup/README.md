# Dulce Mayor — Correcciones de las Olas 1 y 2 y Facturación (Ola 3)

Dulce Mayor es un mayorista de golosinas de Mendoza. Esta entrega **continúa el proyecto existente**: no se rehízo nada.
Se mantienen la arquitectura cliente-servidor, MVC en tres capas, REST, los DTO, el patrón Repository, el
**Template Method** de `BaseService` y `BaseController`, la autenticación JWT/OAuth y las mismas tecnologías de
frontend (Thymeleaf y React con Vite).

Resumen de esta entrega:

- **Ola 1 corregida.**
  - El DNI no se modifica una vez registrado.
  - DNI y fecha de nacimiento son obligatorios en los formularios de alta.
  - El cliente dado de alta en la calle recibe la clave inicial `dulcemayor` y tiene que cambiarla.
  - La sesión se mantiene abierta, con un límite.
- **Ola 2 corregida.**
  - El preventista ya no tiene zona.
  - El alta del comercio ubica la dirección en el mapa (geocodificación directa e inversa) y valida que la dirección
    coincida con el pin.
  - Hay ABM de productos en administración.
  - Hay gráficos con datos reales (HU-43).
- **Rutas.** Se compararon OSRM y GraphHopper y se eligió **OSRM**. El orden de las paradas de preventistas y
  distribuidores ahora se **optimiza** con una matriz de tiempos de manejo.
- **Ola 3, Facturación** (sin Pagos):
  - `FacturaCliente` se emite al confirmar el pedido y `FacturaProveedor` la carga administración.
  - Se aplica el patrón **Factory Method**.
  - El PDF se arma con **JasperReports** sobre la base real y cada cliente descarga solo sus facturas.
- **Pagos (Mercado Pago, Checkout API).** El preventista confirma el pedido en forma presencial y elige el medio de
  pago: efectivo, transferencia o QR de Mercado Pago. Se envía por correo el detalle, el total y las instrucciones;
  al acreditarse el QR, la factura pasa a `PAGADA` y llega un correo de confirmación. Ver [sección 21](#21-pagos-con-mercado-pago-checkout-api).
- **Tablets.** Las apps React y las web se adaptan a tablets, verticales y horizontales.

> **Sobre la verificación:** el entorno de desarrollo no tenía acceso a Maven Central, así que `mvnw test` y el
> arranque de Spring Boot **no se pudieron ejecutar**. La [sección 19](#19-cómo-ejecutar-y-probar-el-proyecto) detalla
> qué se ejecutó de verdad y qué falta correr antes de entregar.
>
> La documentación anterior sigue disponible:
> - Ola 2: [`docs/README-ola2.md`](docs/README-ola2.md).
> - Etapa 2: [`docs/README-etapa2.md`](docs/README-etapa2.md).
> - OAuth: [`CONFIGURACION-OAUTH.md`](CONFIGURACION-OAUTH.md).
> - Accesos y credenciales de prueba: [`docs/README-accesos.md`](docs/README-accesos.md).

## Índice

1. [Correcciones realizadas](#1-correcciones-realizadas)
2. [Cambios en usuarios y sesiones](#2-cambios-en-usuarios-y-sesiones)
3. [Cambios en el alta de clientes](#3-cambios-en-el-alta-de-clientes)
4. [Cambios en el ABM de productos](#4-cambios-en-el-abm-de-productos)
5. [Funcionamiento de los gráficos](#5-funcionamiento-de-los-gráficos)
6. [Implementación del módulo de Facturación](#6-implementación-del-módulo-de-facturación)
7. [Relaciones entre Facturación y los demás módulos](#7-relaciones-entre-facturación-y-los-demás-módulos)
8. [Reutilización de clases del Integrador 1](#8-reutilización-de-clases-del-integrador-1)
9. [Factory Method y clases participantes](#9-factory-method-y-clases-participantes)
10. [Integración del mini proyecto de PDF](#10-integración-del-mini-proyecto-de-pdf)
11. [Configuración y funcionamiento del JRXML](#11-configuración-y-funcionamiento-del-jrxml)
12. [Conexión con la base de datos](#12-conexión-con-la-base-de-datos)
13. [OSRM vs. GraphHopper: comparación y decisión](#13-osrm-vs-graphhopper-comparación-y-decisión)
14. [Arquitectura final de optimización de rutas](#14-arquitectura-final-de-optimización-de-rutas)
15. [Configuración del motor de rutas](#15-configuración-del-motor-de-rutas)
16. [Endpoints nuevos o modificados](#16-endpoints-nuevos-o-modificados)
17. [Variables de entorno](#17-variables-de-entorno)
18. [Mejoras responsive para tablets](#18-mejoras-responsive-para-tablets)
19. [Cómo ejecutar y probar el proyecto](#19-cómo-ejecutar-y-probar-el-proyecto)
20. [Funcionalidades pendientes (Pagos y otras)](#20-funcionalidades-pendientes)
21. [Pagos con Mercado Pago (Checkout API)](#21-pagos-con-mercado-pago-checkout-api)

```
dulce-mayor/
├── backend/             API REST (Spring Boot 4.1, Java 25, JPA, Spring Security + JWT, JasperReports) — 8080
├── admin-web/           Sistema administrativo (Spring MVC + Thymeleaf + Chart.js)                   — 8081
├── institucional-web/   Sitio para clientes (Spring MVC + Thymeleaf)                                — 8082
├── movil/comun          Código React compartido (sesión, Axios, mapa, estilos)
├── movil/preventista    App React del preventista                                                   — 5173
├── movil/distribuidor   App React del distribuidor                                                  — 5174
└── docs/                Documentación anterior y capturas (docs/capturas/ola3)
```

---

## 1. Correcciones realizadas

| Ola | Corrección | Dónde se controla |
|---|---|---|
| 1 | El **DNI no se puede modificar** una vez registrado, tampoco desde administración. | `ValidacionesPersona.aplicarDocumentoEnModificacion` (backend), usado por `ServicioCliente` y `ServicioEmpleado`. En admin-web el campo queda de solo lectura, pero lo que cuenta es el backend. |
| 1 | **DNI y fecha de nacimiento obligatorios** en el registro institucional y en el alta móvil. | `ServicioCliente.exigirDocumentoYNacimiento`, más los formularios. |
| 1 | **Clave inicial `dulcemayor`** para el cliente dado de alta por el preventista, con cambio obligatorio. | Sección 2. |
| 1 | **Persistencia de sesión.** | Sección 2. |
| 2 | Se **elimina la zona** del preventista. | `Preventista`, DTO, formularios de admin-web e `InicializadorDatos`. |
| 2 | **Alta de cliente y punto de venta** con geocodificación directa e inversa, y control de que dirección y pin coincidan. | Sección 3. |
| 2 | **ABM de productos** para JEFE y ADMINISTRATIVO, reutilizando Catálogo y Stock. | Sección 4. |
| 2 | **Gráficos HU-43** con datos de la base. | Sección 5. |
| 2 | **Motor de rutas**: optimización del orden con OSRM. | Secciones 13 a 15. |
| 3 | **Facturación** (sin Pagos). | Secciones 6 a 12. |
| — | **Tablets.** | Sección 18. |

Hay otros cambios que vienen de los anteriores:

- **Un pedido confirmado no se puede anular.** Tampoco lo puede anular administración, por decisión del equipo: ya
  tiene factura.
- El **monto a cobrar** que ve el distribuidor ahora es el **total facturado con IVA**.

**Decisión sobre el DNI.** El DNI sigue siendo **opcional en la base de datos** porque un cliente que entra con
Google o Facebook todavía no lo tiene, y lo completa desde "Mi cuenta". Es **obligatorio en los dos formularios de
alta**: el registro institucional y el alta móvil. Una vez cargado, no cambia.

---

## 2. Cambios en usuarios y sesiones

### Clave inicial solo para el alta móvil

El equipo decidió que la clave inicial exista **solo en el alta desde el celular del preventista**. En el registro
institucional, el cliente elige su propia clave.

- `ServicioUsuario.crearCuentaClienteConClaveTemporal(correo)` crea la cuenta CLIENTE con:
  - la clave `dulcemayor` **hasheada** (BCrypt en la aplicación; nunca se guarda en texto plano);
  - `debeCambiarClave = true`.
- `ClienteCampoDTO` ya **no recibe clave**. La app avisa al preventista que la cuenta se crea con la clave
  provisoria, para que se lo diga al cliente.
- **El cambio obligatorio no se puede saltear desde el frontend.** Mientras `debeCambiarClave` sea `true`,
  `DetallesUsuarioService` le da al usuario una sola autoridad, `CAMBIO_CLAVE_PENDIENTE`, que no es un rol. Con
  ella, la API solo permite `/api/auth/yo`, `/api/auth/clave` y `/api/auth/renovar`. Cualquier otro endpoint
  (pedidos, facturas, etc.) responde **403**, aunque alguien manipule la app.
- En el sitio institucional:
  - después de ingresar, `ControladorAcceso` redirige a la página nueva `/cambiar-clave`;
  - `InterceptorAcceso` impide navegar a otra página mientras el cambio esté pendiente.
- La nueva clave:
  - sigue la política existente: de 8 a 72 caracteres, con letras y números;
  - no puede ser igual al correo, a `dulcemayor` ni a la clave actual.
- La clave no aparece en ninguna respuesta de la API ni en los logs.

### Persistencia de sesión, sin sesiones eternas

| Dónde | Antes | Ahora |
|---|---|---|
| Apps React | `sessionStorage`: se perdía al cerrar la pestaña. | `localStorage` (`comun/src/api/sesionGuardada.js`). Al leerlo se mira el `exp` del JWT y, si venció, se descarta. Una hora antes del vencimiento, y al volver a la app, `AuthContext` pide un token nuevo con `POST /api/auth/renovar`. "Cerrar sesión" lo borra. |
| API (JWT) | Token de 8 h sin renovación. | El token lleva el *claim* `inicioSesion`. `/api/auth/renovar` emite otro **conservando el inicio**, y el vencimiento nunca supera `inicioSesion + jwt.sesion-maxima` (24 h por defecto). Pasado ese límite responde 401 (`SesionVencidaException`) y hay que volver a ingresar. Un token adulterado o vencido no se renueva. |
| admin-web e institucional-web | Cookie de sesión del navegador. | Sesión del servidor con **60 min de inactividad** (`SESSION_TIMEOUT`) y cookie persistente de **8 h** (`SESSION_COOKIE_MAX_AGE`, igual que el token). La cookie es `HttpOnly` y `SameSite`; `Secure` se activa con `COOKIE_SECURE`. |

---

## 3. Cambios en el alta de clientes

**Paso 1 – Responsable del comercio** (`NuevoCliente.jsx`)

- Correo, nombre, apellido, **tipo y número de documento** y **fecha de nacimiento**. El selector no permite elegir
  a un menor de edad, y el backend lo vuelve a validar.
- Ya no se pide la clave: la cuenta se crea con la clave inicial (sección 2).

**Paso 2 – Punto de venta** (`NuevoPuntoVenta.jsx`). Hay dos alternativas, y en las dos la ubicación pasa por el
backend (`/api/mapas/…`, que respeta la política de Nominatim):

1. **Escribir la dirección y buscarla (geocodificación).** Se elige uno de los resultados y el pin se ubica ahí. Si
   la búsqueda no encuentra nada, no se inventan coordenadas: se pide tocar el mapa.
2. **Tocar el mapa (geocodificación inversa).** Las coordenadas son las del punto tocado. Calle, número, barrio,
   departamento y localidad se **completan solos** con la dirección aproximada que devuelve Nominatim (la localidad
   se busca entre las del departamento cargadas en la base), y el usuario las puede corregir.

**Coherencia entre dirección y coordenadas.** La app guarda una "firma" de la dirección que corresponde al pin. Si
después se cambia la calle, el número o el departamento, el pin queda **"a revisar"** y no se puede guardar hasta
que el usuario:

- vuelva a buscar la dirección,
- toque el mapa, o
- confirme con **"El pin está bien"**.

El backend sigue rechazando un comercio sin coordenadas, con una sola coordenada o con coordenadas fuera de rango.
**No hay coordenadas por defecto.**

---

## 4. Cambios en el ABM de productos

Antes el catálogo solo se podía cargar por la API. Ahora hay pantallas en **admin-web** (`/productos`) para el
**ADMINISTRATIVO** y el **JEFE**, que hereda el rol administrativo:

- **Listado** con búsqueda, precio vigente, stock e indicador de bajo mínimo.
- **Alta**, en un solo formulario:
  - datos del producto;
  - categoría y subcategoría en cascada;
  - presentación;
  - foto;
  - precio mayorista inicial (sin IVA);
  - stock inicial y stock mínimo.
- **Modificación** de los datos y fotos. El código no se modifica.
- **Nuevo precio** con fecha de vigencia, que crea una `VigenciaPrecio`. Queda el historial de precios.
- **Ingreso de mercadería**, que suma al `Stock`.
- **Baja lógica.** El backend la **impide si el producto está en pedidos CARGADOS o PENDIENTES DE ENTREGA**
  (`ServicioProducto.validar` → `ServicioPedido.hayPedidosAbiertosConProducto`). Los pedidos y facturas históricos
  conservan su producto.

No se duplicó lógica: admin-web llama a los endpoints existentes del módulo Catálogo y Stock (`/api/productos`,
`/api/imagenes`, `/api/vigencias-precio`, `/api/stock`), que ya validaban todo con sus servicios.

---

## 5. Funcionamiento de los gráficos

Los dos gráficos están en el inicio de admin-web. Se dibujan con **Chart.js 4.4.1**, servido localmente en
`static/js/vendor` junto con su licencia MIT, y los datos salen **de la base** por la API.

| Gráfico | Endpoint | Quién lo ve | Cómo se calcula |
|---|---|---|---|
| **Barras: pedidos por preventista por día** | `GET /api/estadisticas/pedidos-por-preventista?mes=AAAA-MM` | Solo **JEFE**, en la API y en la web | Consulta JPQL de los pedidos cargados en el mes, **sin los anulados** (`RepositorioPedido.pedidosCargadosEntre`). El servicio los agrupa por preventista y día. Se puede elegir el mes. |
| **Torta: stock por categoría** | `GET /api/estadisticas/stock-por-categoria` | **ADMINISTRATIVO** y **JEFE** | `SUM(cantidadActual)` y `COUNT` agrupados por la **categoría real** del producto (Stock → Producto → SubCategoría → Categoría), solo con productos y stock activos. |

- Si no hay datos, se muestra un mensaje en lugar de un gráfico vacío.
- Cada lienzo tiene una descripción accesible (`role="img"` y `aria-label`).
- La torta se actualiza sola cada minuto, consultando `/estadisticas/stock` (JSON) de admin-web.
- El código está en `templates/inicio.html` y `static/js/graficos.js`.
- `ControladorInicio` le pasa a la vista estructuras simples (`Map` y `List`), no `record`, para que el *inlining*
  de Thymeleaf en JavaScript sea confiable.

---

## 6. Implementación del módulo de Facturación

### Entidades

Todas están en `backend/model`, con baja lógica heredada de `BaseEntity`:

```
Factura (abstracta, tabla facturas, herencia JOINED)
 ├─ numeroFactura, fechaFactura, total (con IVA), estado: EstadoFactura, detalles: List<DetalleFactura>
 ├─ getSubtotal()  = Σ detalles (sin IVA)          getDescuento() = 0 (lo redefine FacturaCliente)
 ├─ getIva()       = total − (subtotal − descuento)
 ├─ FacturaCliente   (facturas_clientes)   pedido: Pedido (1 a 1, pedido_id ÚNICO), codigoQrPago (para Pagos)
 └─ FacturaProveedor (facturas_proveedores) proveedor: Proveedor
DetalleFactura (detalles_factura): producto, cantidad, precioUnitario (sin IVA), subtotal
Proveedor (proveedores): razonSocial, cuit (validado con dígito verificador, único), direccion
EstadoFactura: PENDIENTE_PAGO, PAGADA, ANULADA
```

### Reglas

- **Cuándo se emite la factura del cliente.** Al **confirmar el pedido**, en la **misma transacción**:
  `ServicioPedido.confirmarPedido` → `ServicioFacturaCliente.facturarPedido`. Si falla la factura, tampoco queda
  confirmado el pedido.
- **Sin duplicados.** Una sola factura por pedido: se valida en el servicio y, además, la columna `pedido_id` es
  única.
- **Numeración.** Correlativo simple (1, 2, 3…), elegido por el equipo en lugar de un formato tipo `FC-0001-…`.
  Las facturas de proveedor llevan **el número del comprobante del proveedor**, que es único por proveedor.
- **IVA discriminado.**
  - El precio de lista es **sin IVA**.
  - Subtotal = Σ renglones.
  - Base = subtotal − descuento.
  - IVA = base × alícuota (21 % por defecto, `app.facturacion.alicuota-iva`).
  - Total = base + IVA, redondeado a centavos.
  - Ejemplo: 3 × $1.000 = $3.000 + IVA $630 = **$3.630**.
- **Estado inicial `PENDIENTE_PAGO`.** No hay ninguna operación que la marque `PAGADA`: eso le corresponde al módulo
  de Pagos, que no se implementó (sección 20). No se simulan pagos.
- **Las facturas no se modifican ni se dan de baja.** Responde 405. La de proveedor se puede **corregir mientras
  esté `PENDIENTE_PAGO`**: se vuelve a armar con su creador.
- **La factura de proveedor no mueve el stock.** El ingreso de mercadería se registra aparte, en el ABM de productos.
  Está documentado como pendiente vincularlos.

### Pantallas

- **admin-web:**
  - `/facturas`: pestañas de clientes y proveedores, con PDF;
  - `/facturas/proveedores/nueva`: renglones dinámicos y total estimado;
  - `/proveedores`: ABM con dirección.
- **institucional-web:**
  - `/mis-facturas`: listado y descarga del PDF;
  - el detalle del pedido muestra la factura y el total con IVA.
- **App del distribuidor:** cobra el total facturado.

---

## 7. Relaciones entre Facturación y los demás módulos

```
Pedidos ──confirmarPedido──▶ ServicioFacturaCliente ──CreadorFacturaCliente──▶ FacturaCliente ─1:1─ Pedido
   │                                                                                   │
   │ PedidoResponseDTO.idFactura / numeroFactura / totalFacturado ◀────────────────────┘
   │ PedidoResumenDTO.total (monto a cobrar por el distribuidor) = total facturado
Personas ──Pedido → PuntoVenta → Cliente──▶ propiedad de la factura (el cliente solo ve las suyas)
Catálogo ──Producto──▶ DetalleFactura (precio del pedido, no el vigente al facturar)
          Baja de producto: bloqueada con pedidos abiertos; las facturas históricas lo conservan
Proveedores ──▶ FacturaProveedor ──CreadorFacturaProveedor──▶ productos por ServicioProducto
Direcciones ──▶ Proveedor.direccion (ServicioDireccion)
Pagos (pendiente) ──▶ EstadoFactura PAGADA, FormaDePago, codigoQrPago
```

- Cada servicio usa **solo su repositorio**. `ServicioFacturaCliente` llega al cliente por `ServicioCliente`, y
  `ServicioFacturaProveedor` llega a proveedores y productos por sus servicios.
- La relación con el cliente **no se duplica**: se deriva de `Pedido → PuntoVenta → Cliente`.

---

## 8. Reutilización de clases del Integrador 1

Del Integrador 1 (`tiendaderopa`) se tomó el **diseño del módulo de facturas**, adaptándolo. No se copió el `.env`
que venía en el ZIP: tenía valores reales y no se usó.

| Integrador 1 | Dulce Mayor | Adaptación |
|---|---|---|
| `Factura` abstracta, `@Inheritance(JOINED)`, tabla `facturas` | `Factura` | Hereda de `BaseEntity` (id y baja lógica comunes). `fechaFactura` pasa a `LocalDateTime`, en lugar de `Date` con `@Temporal`. Los detalles se manejan con `cascade` y `orphanRemoval`. El IVA se calcula. |
| `FacturaCliente`, `FacturaProveedor` | Mismas clases | Relación con `Pedido` (1:1 única) y con `Proveedor`. |
| `DetalleFactura` | `DetalleFactura` | Relación bidireccional con la factura; precio sin IVA. |
| `EstadoFactura` (PAGADA, ANULADA, SIN_DEFINIR) | `EstadoFactura` (PENDIENTE_PAGO, PAGADA, ANULADA) | `SIN_DEFINIR` pasa a `PENDIENTE_PAGO`, como en el diagrama. |
| `RepositorioFacturaCliente`, `RepositorioFacturaProveedor` | Mismos nombres | Consultas derivadas para la propiedad del cliente, el correlativo y el número por proveedor. |
| `ServicioFacturaCliente`, `ServicioFacturaProveedor` | Mismos nombres | Ahora extienden `BaseService` (Template Method) y crean las facturas con Factory Method. |
| `Factura.formaDePago` (`FormaDePago`) | — | **No se trajo.** Pertenece a Pagos, que no se implementa. En el PDF figura "A definir". |

Las clases genéricas que ya venían del Integrador 1 en etapas anteriores (`Pais`, `Provincia`, `Departamento`,
`Localidad`, `Direccion`, `Persona`, `Usuario`, `MiException`, `ServicioImagen`…) se siguieron usando sin cambios
de diseño.

---

## 9. Factory Method y clases participantes

Se siguió la estructura del ejemplo del equipo (`factory_method_facturas.zip`): `Envio` es el producto,
`EnvioCreator.crearEnvio()` es el método fábrica, `procesarEnvio()` es la operación común y hay un creador concreto
por tipo.

| Rol del patrón | Clase en Dulce Mayor (`service/facturacion`) |
|---|---|
| Producto | `Factura` (abstracta) |
| Productos concretos | `FacturaCliente`, `FacturaProveedor` |
| Creador | `CreadorFactura<D>`, donde `D` son los datos de origen |
| **Método fábrica** | `protected abstract Factura crearFactura(D datos)` |
| Operación común, la que usa el método fábrica | `public final Factura emitir(D datos)`: crea la factura, agrega y valida los renglones, calcula subtotal, descuento, IVA y total, y pone el estado y la fecha |
| Creadores concretos | `CreadorFacturaCliente` (D = `Pedido`) y `CreadorFacturaProveedor` (D = `DatosFacturaProveedor`) |
| Otros pasos que varían | `renglones(D)`, `descuento(D)`, `fechaFactura(D)` |
| Clientes del patrón | `ServicioFacturaCliente.crearEntidad` y `ServicioFacturaProveedor.crearEntidad`/`actualizarEntidad` |

**Por qué Factory Method y no otra cosa.** Hay dos familias de factura que comparten el procedimiento de emisión
pero se instancian y se completan distinto. El patrón deja la parte común en un solo lugar, `emitir`. Los servicios
no hacen `new FacturaCliente(...)`: piden al creador que emita. Para agregar otra clase de factura, por ejemplo una
nota de crédito, se suma una subclase de `Factura` y su creador, sin tocar `emitir` ni a quien la usa.

**Por qué no se usó para "factura A / factura B".** Lo hablamos en el equipo: A y B (según la condición de IVA del
comercio) no son clases distintas en el diagrama. Diferenciarlas es una regla fiscal que se puede aplicar después
en el creador o como una estrategia, sin crear subclases.

**Convivencia con Template Method.** El creador **solo arma** la factura. El número, la validación y el guardado
siguen en el Template Method de `BaseService`:

```
altaEntidad → crearEntidad (Factory Method) → validar → preAlta (número) → save
```

---

## 10. Integración del mini proyecto de PDF

El mini proyecto (`dulce_mayor_facturacion`: Spring Boot 3.2, JasperReports 6.20.6, H2) generaba el PDF de esta
forma:

- compilaba el `.jrxml` en **cada pedido de descarga**;
- lo llenaba con un `JRBeanCollectionDataSource`, es decir, con objetos armados a mano en Java;
- usaba su propia base H2, con usuario y clave en `application.yml`.

Así quedó la adaptación:

| Mini proyecto | Dulce Mayor (`service/GeneradorPdfFactura`) |
|---|---|
| Plantilla `factura_dulce_mayor.jrxml` | Es la base de `reportes/factura_cliente.jrxml`. A partir de ella se hizo `reportes/factura_proveedor.jrxml`, como pidió el equipo. |
| Datos desde objetos en memoria (`JRBeanCollectionDataSource`) | Datos **desde la base real**: cada plantilla tiene su `queryString` SQL con el parámetro `$P{ID_FACTURA}` y se llena con una conexión del **`DataSource` de Spring** (sección 12). |
| Compilación por pedido | Compilación **una sola vez** por plantilla, en caché (`ConcurrentHashMap`). |
| Base H2 propia con credenciales en `application.yml` | La misma base de la aplicación. No hay credenciales nuevas en ningún lado. |
| `jasperreports` 6.20.6 | La **misma versión**, agregada al `pom.xml` del backend. |

- Se mantuvieron los colores y el diseño. El título pasó a "FACTURA" (sin la "B") y "Comp. N°" en proveedores.
- Se agregaron **descuento** e **IVA discriminado** (con la alícuota usada) y **"Forma de pago: A definir"**, que
  queda reservado para Pagos.

### Descarga protegida por propietario

| Quién | Endpoint | Control |
|---|---|---|
| Cliente | `GET /api/facturas/mis-facturas/{id}/pdf` | El cliente sale **del token**. Si la factura no es suya, responde **404**, igual que si no existiera, así que cambiar el id en la URL no sirve. |
| Administración | `GET /api/facturas/clientes/{id}/pdf`, `GET /api/facturas/proveedores/{id}/pdf` | Rol ADMINISTRATIVO o JEFE. |

- Las web hacen de *proxy*: el navegador nunca ve el token de la API.
- Se responde `application/pdf` como adjunto (`Content-Disposition: attachment`).
- Un preventista o distribuidor recibe 403.

---

## 11. Configuración y funcionamiento del JRXML

Las plantillas están en `backend/src/main/resources/reportes/`:

| Parámetro | Tipo | Valor |
|---|---|---|
| `ID_FACTURA` | `String` | Id de la factura. Lo pone el backend después de controlar los permisos; **no viaja desde el navegador**. |
| `ALICUOTA_IVA` | `Double` | `app.facturacion.alicuota-iva`, para mostrar "IVA (21 %)". |
| `MEDIO_PAGO` | `String` | Por defecto, "A definir" (Pagos pendiente). |
| `REPORT_LOCALE` | `Locale` | `es-AR`, para formatear `$ 45.072,50` y las fechas `dd/MM/yyyy`. |

- **Factura de cliente.** La consulta une `facturas → facturas_clientes → pedidos → puntos_venta → personas
  (cliente) → direcciones → localidades → detalles_factura → productos`. Trae una fila por renglón y, por subconsulta,
  el subtotal de la factura.
- **Factura de proveedor.** Une `facturas → facturas_proveedores → proveedores → direcciones → localidades →
  detalles_factura → productos`.
- Los nombres de tablas y columnas son los que genera Hibernate (`snake_case`).
- Las variables del reporte calculan:
  - el subtotal sin IVA;
  - el descuento;
  - el IVA, como total − (subtotal − descuento);
  - el total con IVA.
- El estado de la factura se ve en la leyenda ("pendiente de pago").

Para cambiar el diseño: abrir el `.jrxml` con Jaspersoft Studio, compatible con 6.20, y conservar los nombres de
parámetros y campos. **No hace falta configurar un *Data Adapter* con usuario y clave en la plantilla.** Para
probar en Studio, el adaptador se configura en Studio y queda fuera del proyecto.

---

## 12. Conexión con la base de datos

```java
try (Connection conexion = dataSource.getConnection()) {          // DataSource de Spring (HikariCP)
    JasperPrint impreso = JasperFillManager.fillReport(plantilla, parametros, conexion);
    return JasperExportManager.exportReportToPdf(impreso);
}
```

- Se inyecta el **mismo `DataSource`** que usa JPA, configurado con `DB_URL`, `DB_USERNAME` y `DB_PASSWORD` desde
  `backend/.env`.
- **Ni el `.jrxml` ni el código fuente tienen credenciales.**
- La conexión se toma del *pool* y se devuelve al terminar, con *try-with-resources*.
- El reporte solo lee.
- Las consultas usan `$P{ID_FACTURA}`, que JasperReports pasa como **parámetro de `PreparedStatement`**: no es
  concatenación de texto, así que no hay inyección SQL.
- Si algo falla, se registra el error técnico en el log del servidor y el usuario recibe un mensaje genérico, sin
  detalles internos.

---

## 13. OSRM vs. GraphHopper: comparación y decisión

**Qué necesitamos.** Ordenar las paradas del día (de 5 a 30 comercios) partiendo de la ubicación del preventista o
del distribuidor. Eso requiere los **tiempos de manejo entre todos los puntos** (una matriz) y después **dibujar** el
recorrido por calles.

| Criterio | OSRM | GraphHopper |
|---|---|---|
| Licencia del motor | BSD-2, *open source* | Apache 2.0, *open source* (el motor de ruteo) |
| Servicio hospedado gratuito | Servidor de demostración `router.project-osrm.org`: sin clave, sin garantía de disponibilidad, para uso razonable | Plan gratuito con **clave**: unos 500 créditos por día, **uso no comercial**, límites de puntos por pedido |
| Matriz de distancias y tiempos | `/table`, incluido y gratuito (en el servidor propio, sin límite de licencia) | Matrix API: en el plan gratuito, con límites muy bajos y solo para uso no comercial |
| Optimización de recorrido (TSP/VRP) | `/trip`: TSP aproximado (fuerza bruta con pocos puntos y heurística de inserción con más), un solo vehículo, sin ventanas horarias | Route Optimization API (VRP con ventanas horarias, capacidades, varios vehículos): **producto pago**. Existe `jsprit`, la biblioteca *open source* de optimización, pero hay que integrarla y operarla aparte |
| Instalación propia | Docker más un extracto de OpenStreetMap de Mendoza o Argentina | Java más el mismo extracto |
| Lo que ya tenía el proyecto | Trazado con `/route` (Ola 2) y el respaldo en línea recta | Nada |

**Decisión: OSRM.**

- Ya estaba integrado.
- La matriz `/table` se puede usar **sin clave y sin restricción comercial** en una instancia propia.
- El servidor de demostración alcanza para desarrollar y presentar.
- GraphHopper solo agregaría valor con su optimizador VRP pago o integrando `jsprit`, que es más de lo que piden
  estas historias: un vehículo por ruta, sin ventanas horarias.

No se usó `/trip` de OSRM, por dos motivos:

- con recorrido abierto (`roundtrip=false`) exige fijar el destino final;
- su resultado es aproximado sin decirlo.

Con la matriz y un optimizador propio sabemos **cuándo el orden es exacto y cuándo es heurístico**, y lo informamos
(sección 14).

Fuentes consultadas en noviembre de 2026. Las condiciones comerciales pueden cambiar.

---

## 14. Arquitectura final de optimización de rutas

```
App (preventista / distribuidor) ── POST …/ordenar {lat, lon} ──▶ ServicioRuta.ordenarRuta
   1. Pendientes de SU ruta de hoy (por el usuario del token)
   2. MotorRecorridos.matriz([ubicación, p1…pn])  ── ServicioMapas (OSRM /table/v1/driving?annotations=duration)
         └─ si OSRM no responde, devuelve un tramo sin ruta o hay > 100 puntos → MatrizRecorrido.lineaRecta (Haversine)
   3. OptimizadorRecorrido.optimizar(matriz)
         ├─ ≤ 12 paradas: Held-Karp (programación dinámica) → orden ÓPTIMO para esa matriz
         └─ > 12 paradas: vecino más cercano + mejora 2-opt → orden APROXIMADO (no garantiza el óptimo)
   4. Guarda el orden y Ruta.criterioOrden (por ejemplo: "Orden óptimo (búsqueda exacta Held-Karp) por tiempo de
      manejo estimado por calles (OSRM)")
   5. MotorRecorridos.trazar(ubicación + pendientes en orden) ── OSRM /route → geometría, km y minutos
```

- **Mismo algoritmo para los dos roles.** El preventista ordena desde "Reordenar desde acá". El distribuidor lo hace
  al armar o actualizar su ruta de entregas.
- **Recorrido abierto.** Termina en el último comercio y no vuelve al inicio.
- **Matriz asimétrica.** Se respetan las calles de una mano; el 2-opt recalcula el costo completo de cada candidato.
- **Siguiente punto.** Es el primer pendiente del orden guardado, el que decidió el optimizador.
- **Honestidad.** "Óptimo" significa óptimo **para los tiempos estimados por OSRM**, sin tránsito real, horarios de
  los comercios ni capacidad del vehículo. La app muestra siempre `criterioOrden` y la fuente del trazado (por calles
  o línea recta, con línea punteada). Si administración agrega o quita paradas, `criterioOrden` vuelve a `null` y la
  app indica que todavía no se optimizó.
- **Sin dependencia del proveedor.** `MotorRecorridos` es una interfaz. Para cambiar a GraphHopper alcanza con otra
  implementación.

---

## 15. Configuración del motor de rutas

| Propiedad / variable | Por defecto | Uso |
|---|---|---|
| `app.mapas.osrm-url` / `OSRM_URL` | `https://router.project-osrm.org` (demo) | `/table` (matriz) y `/route` (trazado). Para producción, una instancia propia. |
| `app.mapas.nominatim-url` / `NOMINATIM_URL` | `https://nominatim.openstreetmap.org` | Geocodificación directa e inversa. |
| `app.mapas.user-agent` / `MAPAS_USER_AGENT` | `DulceMayor/1.0 (… completar-correo)` | **Completar con el correo del equipo**: lo exige la política de Nominatim. |
| `OptimizadorRecorrido.LIMITE_EXACTO` | 12 | Hasta cuántas paradas se busca el óptimo exacto. Con 12 son unas 600 mil operaciones; con 20 serían unos 400 millones. |
| Tiempos de espera | 4 s de conexión y 8 s de lectura | Si OSRM no responde, se ordena en línea recta y se informa. |

Instancia propia de OSRM, en Docker con el extracto de Argentina de Geofabrik:

```bash
docker run -t -v "${PWD}:/data" ghcr.io/project-osrm/osrm-backend osrm-extract -p /opt/car.lua /data/argentina-latest.osm.pbf
docker run -t -v "${PWD}:/data" ghcr.io/project-osrm/osrm-backend osrm-partition /data/argentina-latest.osrm
docker run -t -v "${PWD}:/data" ghcr.io/project-osrm/osrm-backend osrm-customize /data/argentina-latest.osrm
docker run -t -i -p 5000:5000 -v "${PWD}:/data" ghcr.io/project-osrm/osrm-backend osrm-routed --algorithm mld /data/argentina-latest.osrm
# luego OSRM_URL=http://localhost:5000
```

---

## 16. Endpoints nuevos o modificados

### API (backend)

| Método y ruta | Rol | Nota |
|---|---|---|
| `POST /api/auth/renovar` | Autenticado | Nuevo. Renueva el token hasta la sesión máxima. |
| `PUT /api/auth/clave` | Autenticado | Sin cambios; ahora también lo usa el cliente con clave inicial. |
| `POST /api/clientes/campo` | PREVENTISTA | **Modificado**: sin clave, con DNI y fecha obligatorios; crea la cuenta con clave inicial. |
| `POST /api/auth/registro` | Público | **Modificado**: DNI y fecha obligatorios. |
| `PUT /api/clientes/{id}`, `PUT /api/empleados/{id}` | ADMINISTRATIVO / JEFE | **Modificados**: rechazan un DNI distinto del registrado (400). |
| `POST/PUT /api/empleados` | JEFE | **Modificado**: sin `zona`. |
| `DELETE /api/productos/{id}` | ADMINISTRATIVO | **Modificado**: 400 si el producto está en pedidos abiertos. |
| `GET /api/estadisticas/pedidos-por-preventista?mes=` | JEFE | Nuevo (barras). |
| `GET /api/estadisticas/stock-por-categoria` | ADMINISTRATIVO | Nuevo (torta). |
| `GET /api/facturas/clientes`, `GET /api/facturas/clientes/{id}`, `GET /api/facturas/clientes/{id}/pdf` | ADMINISTRATIVO | Nuevos. |
| `GET /api/facturas/mis-facturas`, `GET /api/facturas/mis-facturas/{id}`, `GET /api/facturas/mis-facturas/{id}/pdf` | CLIENTE | Nuevos. Solo las propias (si no, 404). |
| `GET/POST /api/facturas/proveedores`, `GET/PUT /api/facturas/proveedores/{id}`, `GET /api/facturas/proveedores/proveedor/{idProveedor}`, `GET /api/facturas/proveedores/{id}/pdf` | ADMINISTRATIVO | Nuevos. Sin baja (405). |
| `GET/POST /api/proveedores`, `GET/PUT/DELETE /api/proveedores/{id}`, `GET /api/proveedores/cuit/{cuit}` | ADMINISTRATIVO | Nuevos. |
| `POST /api/pedidos/mis-pedidos/{id}/confirmar` | CLIENTE | **Modificado**: emite la factura en la misma transacción. |
| `PedidoResponseDTO` | — | **Modificado**: suma `idFactura`, `numeroFactura` y `totalFacturado`. |
| `RutaResponseDTO` | — | **Modificado**: suma `criterioOrden`. |
| `POST /api/rutas/mia/ordenar`, `POST /api/pedidos/entregas/ruta` | PREVENTISTA / DISTRIBUIDOR | **Modificados**: el orden sale del optimizador (sección 14). |

El JEFE hereda los permisos de ADMINISTRATIVO.

### admin-web (8081)

- `/productos`, `/productos/nuevo`, `/productos/{id}/editar`, `/productos/{id}/precio`, `/productos/{id}/stock`,
  `/productos/{id}/baja`.
- `/facturas?tipo=clientes|proveedores`, `/facturas/clientes/{id}/pdf`, `/facturas/proveedores/{id}/pdf`,
  `/facturas/proveedores/nueva`.
- `/proveedores`, `/proveedores/nuevo`, `/proveedores/{id}/editar`, `/proveedores/{id}/baja`.
- `/` con gráficos y `/estadisticas/stock` (JSON).

### institucional-web (8082)

- `/cambiar-clave` (GET y POST).
- `/mis-facturas` y `/mis-facturas/{id}/pdf`.

---

## 17. Variables de entorno

Sin cambios respecto de la etapa anterior: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, OAuth, `NOMINATIM_URL`,
`OSRM_URL`, `MAPAS_USER_AGENT` y `CORS_ALLOWED_ORIGINS`. Las nuevas o documentadas ahora son estas:

| Variable | Aplicación | Por defecto | Uso |
|---|---|---|---|
| `JWT_EXPIRATION_MS` | backend | `28800000` (8 h) | Vida de cada token. |
| `JWT_SESION_MAXIMA_MS` | backend | `86400000` (24 h) | Máximo de una sesión renovada desde el ingreso. |
| `FACTURACION_ALICUOTA_IVA` | backend | `0.21` | IVA discriminado en las facturas. |
| `SESSION_TIMEOUT` | admin-web e institucional-web | `60m` | Inactividad de la sesión web. |
| `SESSION_COOKIE_MAX_AGE` | admin-web e institucional-web | `8h` | Duración de la cookie de sesión. |
| `COOKIE_SECURE` | admin-web e institucional-web | `false` | Ponerla en `true` con HTTPS. |

Ver `backend/.env.example`. **El ZIP no incluye ningún `.env`, contraseña, token ni secreto.** El `backend/.env` que
circulaba en el equipo tenía secretos reales: conviene regenerar `JWT_SECRET` y los secretos de OAuth.

Sobre la base de datos: `JPA_DDL_AUTO=update` agrega las tablas y columnas nuevas sin borrar datos:

- tablas `facturas`, `facturas_clientes`, `facturas_proveedores`, `detalles_factura` y `proveedores`;
- columna `rutas.criterio_orden`.

La columna `zona` de `empleados` **no se borra sola**: queda en desuso. Si se quiere quitar, hay que hacerlo a mano
después de un respaldo:

```sql
ALTER TABLE empleados DROP COLUMN zona;
```

---

## 18. Mejoras responsive para tablets

### Apps React (`movil/comun/src/estilos.css`)

- **Desde 768 px** (tablet vertical):
  - la app usa hasta 1180 px de ancho, en lugar de 560 px;
  - el catálogo, "Mis pedidos" y "Pendientes" se ven en **dos columnas** de tarjetas;
  - los formularios se centran con un ancho cómodo de leer;
  - los botones tienen 48 px de alto mínimo, pensando en el tacto;
  - las hojas inferiores se ven como diálogos centrados.
- **Desde 960 px** (tablet horizontal): en "Ruta del día" y "Mis entregas", **el mapa y la lista quedan lado a lado**.
  El mapa queda fijo al desplazarse y es más alto.
- **Desde 1100 px:** tres columnas en los listados.

### admin-web e institucional-web

- Entre 768 y 1199 px, las tablas ocultan las columnas secundarias (`col-secundaria`) y se desplazan dentro de su
  tarjeta, sin mover la página.
- Los formularios de producto y de factura usan grillas de dos columnas.
- Los gráficos se adaptan al ancho.

Las capturas están en `docs/capturas/ola3/`, tomadas con datos de ejemplo:

- ruta, catálogo, alta de cliente y entregas en 768×1024 y 1180×820;
- facturas en tablet;
- los PDF generados.

---

## 19. Cómo ejecutar y probar el proyecto

```bat
cd backend           && copy .env.example .env && mvnw.cmd spring-boot:run   &rem completar .env antes
cd admin-web         && mvnw.cmd spring-boot:run
cd institucional-web && mvnw.cmd spring-boot:run
cd movil\preventista  && copy .env.example .env && npm install && npm run dev
cd movil\distribuidor && copy .env.example .env && npm install && npm run dev
```

Pruebas: `mvnw.cmd test` en `backend`, `admin-web` e `institucional-web`, y `npm run lint` / `npm run build` en
cada app React.

**Prueba manual del flujo de facturación:**

1. El jefe (clave temporal `dulcemayor`) da de alta un preventista y carga productos en `/productos`, con precio y
   stock.
2. El preventista da de alta un cliente desde la app. Se le asigna la clave `dulcemayor`.
3. El preventista carga el comercio, ubicado en el mapa, y toma un pedido.
4. El cliente entra en <http://localhost:8082>. Se le pide cambiar la clave.
5. El cliente confirma el pedido y descarga la factura en "Mis facturas".
6. En admin-web, `/facturas` muestra la factura con el PDF; en `/proveedores` y en
   `/facturas/proveedores/nueva` se cargan compras.

### Pruebas incluidas en el proyecto

| Prueba | Qué cubre |
|---|---|
| `service/facturacion/CreadorFacturaTest` | Factory Method: producto concreto, IVA, descuento, validaciones y alícuota. |
| `service/rutas/OptimizadorRecorridoTest` | Held-Karp contra fuerza bruta (60 matrices, simétricas y asimétricas); la heurística no empeora al vecino más cercano; una trampa del vecino más cercano. |
| `service/ServicioRutaTest` | Orden con la matriz del motor; respaldo en línea recta informado. |
| `service/ValidacionesPersonaTest` | El DNI no se modifica, se conserva o se completa. |
| `service/ServicioClienteTest` (ampliada) | DNI y fecha obligatorios; alta en campo con clave inicial; DNI inmodificable. |
| `service/ServicioUsuarioTest` (ampliada) | Clave inicial hasheada y cambio obligatorio. |
| `security/JwtServiceTest` | Renovación que conserva el inicio, límite de sesión y token adulterado. |
| `integracion/FlujoFacturacionIntegrationTest` | De punta a punta con Spring Security y H2: factura al confirmar, sin duplicados, 404 con una factura ajena, PDF `application/pdf`, 403 sin cambiar la clave, proveedores, número repetido, baja de producto bloqueada, permisos de gráficos, DNI y `/renovar`. |
| `integracion/FlujoPedidosIntegrationTest`, `FlujoSeguridadIntegrationTest` (actualizadas) | Sin zona, registro con DNI y clave, monto a cobrar con IVA. |

### Qué se ejecutó realmente y qué no

**Sí se ejecutó**, sin Maven, porque Maven Central no era accesible:

- **Compilación** de `backend` (código y todas las pruebas), `admin-web` e `institucional-web` con `javac`, contra
  implementaciones mínimas de Spring, JPA y Lombok.
- **143 escenarios** con los **servicios reales** del backend y repositorios en memoria, con *rollback* emulado.
  **143 OK, 0 fallas.** Cubren:
  - todos los de la Ola 2;
  - alta en campo (sin DNI, sin fecha, menor de edad, clave inicial hasheada y cambio);
  - factura emitida al confirmar (N.º 1, subtotal 2000, IVA 420, total 2420), sin segunda factura, sin facturar
    pedidos sin confirmar;
  - propiedad: una factura o PDF ajeno da 404, y un empleado no tiene "mis facturas";
  - proveedores (CUIT, dirección, duplicado);
  - factura de proveedor: totales, número repetido, fecha futura, renglones, modificación, sin baja, no mueve stock;
  - baja de producto bloqueada o permitida;
  - datos de los dos gráficos;
  - DNI inmodificable para cliente y empleado;
  - orden óptimo con su `criterioOrden`.
- Regresión de etapas anteriores con el mismo método: catálogo **45/45** y personas **83/83** (este último,
  actualizado con el registro con DNI y sin zona).
- **Pruebas unitarias sin dependencias de Mockito ni Spring**, ejecutadas con un JUnit mínimo: `CreadorFacturaTest`,
  `OptimizadorRecorridoTest`, `ServicioRutaTest`, `ValidacionesPersonaTest`, `ServicioPuntoVentaTest` y
  `GestorSesionTest` (institucional). **20 OK.**
- **PDF real con JasperReports 6.20.6.** Las dos plantillas se compilaron y llenaron con datos de prueba, en Java 21,
  contra **PostgreSQL 16** y contra **MariaDB 10.11** (compatible con MySQL, usando el conector MySQL 8.0.21).
  Se revisaron visualmente: una página cada una, montos con formato argentino.
- **Apps React:** `eslint` y `vite build` sin errores en las dos apps. Navegación en Chromium (Playwright) con la API
  **simulada** en teléfono y en tablet vertical y horizontal.
- **Plantillas Thymeleaf nuevas** (facturas y cambio de clave) renderizadas con datos de ejemplo y un motor
  simplificado, sin desbordes en tablet ni en celular.

**No se pudo ejecutar:**

- `mvnw test`. Esto deja sin correr:
  - `FlujoFacturacionIntegrationTest` y las demás pruebas de integración y de Mockito: `JwtServiceTest`,
    `ServicioClienteTest`, `ServicioUsuarioTest` e `InterceptorAccesoTest`;
  - el arranque real de Spring Boot.
  - **Hay que correrlas antes de entregar.**
- Las plantillas JRXML contra **H2**, que es la base de las pruebas de integración, y contra **MySQL 8 real**. Se
  probaron en MariaDB y PostgreSQL.
- JasperReports en **Java 25**: su compilador de expresiones (ECJ) se probó en Java 21. Si falla en 25, la
  alternativa es precompilar las plantillas a `.jasper`.
- Las llamadas reales a **OSRM** (`/table` y `/route`), **Nominatim** y **GraphHopper**: no había salida a esos
  sitios. El respaldo en línea recta sí se ejecutó.
- El GPS de un celular o una tablet real.

---

## 20. Funcionalidades pendientes

### Pagos (implementado; ver sección 21)

Ya están implementados: `FormaDePago`, `Pago`, la transición `PENDIENTE_PAGO → PAGADA` al acreditarse un QR,
el QR de Mercado Pago (Checkout API, sin redirección) enviado por correo, el pago por transferencia con alias y
el pago en efectivo al entregar. Falta:

- **Cobro registrado por el distribuidor en la entrega** para el pago en efectivo (hoy el `Pago` queda PENDIENTE).
- **"Forma de pago" y descuentos por medio de pago en el PDF**: hoy figuran como "A definir" y $0.
- **Anulación de facturas con nota de crédito** (`ANULADA`).
- **Confirmación automática de transferencias**: hoy el cliente envía el comprobante y no hay conciliación bancaria.

### Otros pendientes

- **Factura A / B** según la condición de IVA del comercio. Hoy hay una sola letra genérica y el IVA siempre
  discriminado.
- **Numeración con secuencia de base de datos.** Hoy es último + 1 con restricción única: dos confirmaciones
  simultáneas pueden hacer que una tenga que reintentar.
- **Vincular la factura de proveedor con el ingreso de stock.**
- **Contacto del proveedor**, que está en el diagrama.
- **Optimización con ventanas horarias o capacidad del vehículo (VRP).** Habría que evaluar `jsprit` o un servicio
  pago.
- **Instancias propias de OSRM y Nominatim** para producción.
- **Correos al cliente**, por ejemplo con la factura adjunta.
- **Pantallas para editar puntos de venta.**
- **Reducir consultas repetidas** en algunos listados de productos de admin-web.

---

## 21. Pagos con Mercado Pago (Checkout API)

### Flujo

El preventista, al confirmar un pedido **en forma presencial**, elige el medio de pago preferido por el cliente:

| Medio | Qué se envía por correo | Estado del pago |
|---|---|---|
| **Efectivo** | Detalle del pedido, monto total y aviso de que se paga en efectivo al recibirlo. | `PENDIENTE` (se cobra en la entrega) |
| **Transferencia** | Detalle, monto total, alias y titular para transferir. | `PENDIENTE` |
| **QR** | Detalle, monto total y el **QR de Mercado Pago** adjunto como imagen. | `PENDIENTE` → `APROBADO` |

Cuando Mercado Pago acredita el pago del QR, su **webhook** consulta el estado real en la API, el `Pago` pasa a
`APROBADO`, la `FacturaCliente` pasa a `PAGADA` y se envía **otro correo** al cliente confirmando el pago.
Todo el circuito es **idempotente** (varias notificaciones no duplican estados ni correos).

Se usa la **Checkout API con QR dinámico** (sin Checkout Pro y **sin redirigir** al cliente):
`PUT /instore/orders/qr/seller/collectors/{user_id}/pos/{external_pos_id}/orders`, que devuelve `qr_data`
(contenido que el backend dibuja como QR con ZXing) e `in_store_order_id`. La `external_reference` enviada es el
**id del pedido**, con la que el webhook encuentra el pago.

### Clases nuevas

- `enumeraciones/FormaDePago` (`EFECTIVO`, `TRANSFERENCIA`, `QR`) y `enumeraciones/EstadoPago`
  (`PENDIENTE`, `APROBADO`, `RECHAZADO`).
- `model/Pago` (1 Pago ── 1 Pedido) y `repository/RepositorioPago`.
- `service/ServicioPago` (registrar pago, procesar webhook, acreditar de forma idempotente).
- `service/ServicioCorreo` (correos HTML con el detalle y las instrucciones; QR adjunto vía `cid:qrPago`; aviso
  al comercio que el distribuidor visitará a continuación).
- `service/pagos/MercadoPagoQrCliente` (Checkout API QR) y `service/pagos/GeneradorQrPng` (ZXing).
- `controller/ControladorPago` (webhook público) y `dto/ConfirmarPedidoRequestDTO`.
- `ServicioPedido.confirmarPedidoPresencial(...)` y `ServicioFacturaCliente.marcarPagada(...)`.
- `Pedido.formaDePago`, `Pago`, y en `PedidoResponseDTO`: `formaDePago` y `estadoPago`.
- App del preventista: `DetallePedido.jsx` permite elegir el medio y confirmar; `servicios.js` agrega
  `pedidos.confirmar(id, formaDePago)`.
- Sitio institucional: muestra el medio y el estado del pago. Se retiró la demo de QR anterior.

### Endpoints

| Método | Ruta | Quién | Uso |
|---|---|---|---|
| `POST` | `/api/pedidos/preventista/{id}/confirmar` | `PREVENTISTA` | Confirma el pedido con `{ "formaDePago": "EFECTIVO\|TRANSFERENCIA\|QR" }` |
| `POST` | `/api/pagos/mercadopago/webhook` | público | Notificaciones de Mercado Pago (valida el estado contra su API) |

### Variables de entorno

| Variable | Uso |
|---|---|
| `MP_ACCESS_TOKEN` | *Access token* de la aplicación de Mercado Pago. **Sin él, QR no está disponible.** |
| `MP_USER_ID` | Id de usuario de Mercado Pago (collector) del punto de venta. |
| `MP_EXTERNAL_POS_ID` | Identificador de la caja/punto de venta creado en Mercado Pago. |
| `MP_NOTIFICATION_URL` | URL pública del webhook (en desarrollo, la de ngrok). |
| `MP_BASE_URL` | Por defecto `https://api.mercadopago.com`. |
| `ALIAS_TRANSFERENCIA` / `TITULAR_TRANSFERENCIA` | Datos del correo de transferencia. |
| `CORREO_HABILITADO` | `true` para enviar correos; si es `false`, se registran en el log. |
| `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`, `MAIL_SMTP_AUTH`, `MAIL_STARTTLS` | SMTP. |
| `CORREO_REMITENTE`, `CORREO_NOMBRE_REMITENTE` | Remitente de los correos. |

> Los nombres deben coincidir exactamente con los que lee `application.properties` (`app.pagos.mercadopago.*`).
> Si el `.env` usa otros nombres (por ejemplo `MERCADOPAGO_ACCESS_TOKEN`), el QR queda "no configurado".

### Sintaxis: cómo se completan los valores entre llaves

En los ejemplos de este README y en la documentación de Mercado Pago los valores variables se escriben entre
llaves, por ejemplo `{MP_ACCESS_TOKEN}`. **Las llaves no se escriben nunca**: son solo la marca de "acá va un
valor". Si se copian tal cual, el servidor recibe el texto literal y responde `401 invalid_token`.

| Dónde aparece | Qué es | Qué hacer |
|---|---|---|
| `{MP_ACCESS_TOKEN}` en un `curl` | Marca de posición | Reemplazarlo por el valor real, sin llaves |
| `{MP_USER_ID}`, `{STORE_ID}` en una URL | Marca de posición | Reemplazarlo por el número real |
| `${MP_ACCESS_TOKEN:}` en `application.properties` | Variable de Spring con valor por defecto vacío | No tocarlo: Spring la resuelve desde `.env` o del entorno |
| `{userId}` / `{posId}` dentro de una ruta | Plantilla de URI de Spring | No se tocan: las completa el código (`RestClient.uri(...)`) |

En una terminal Linux (bash) las variables del shell llevan `$` adelante; sin el `$`, las llaves quedan literales:

```bash
-H 'Authorization: Bearer {MP_ACCESS_TOKEN}'   # ❌ manda las llaves literales
-H 'Authorization: Bearer APP_USR-6739...'     # ✅ valor pegado, comillas simples
-H "Authorization: Bearer $MP_ACCESS_TOKEN"    # ✅ recomendado: comillas dobles expanden
```

- Comillas simples `'...'`: **no** expanden `$VAR`.
- Comillas dobles `"..."`: **sí** expanden `$VAR` (también `${VAR}`, útil cuando hay texto pegado detrás).
- En el archivo `.env` el formato es sin llaves, sin comillas y sin espacios: `MP_ACCESS_TOKEN=APP_USR-...`.

Para usar el `.env` en los `curl` de la terminal:

```bash
set -a; source backend/.env; set +a
echo "$MP_ACCESS_TOKEN"          # verifica que se cargó
curl -s https://api.mercadopago.com/users/me -H "Authorization: Bearer $MP_ACCESS_TOKEN"
```

### Cómo probar la integración paso a paso

**Paso 1 — Crear la aplicación y obtener las credenciales de prueba**

1. Entrar a <https://www.mercadopago.com.ar/developers/panel/app> con la cuenta real (solo administra la
   aplicación; los cobros los hace la cuenta de prueba).
2. *Crear aplicación* → nombre (por ejemplo `Dulce Mayor`) → solución orientada a **cobros presenciales / QR**.
3. Dentro de la aplicación: **Credenciales** → pestaña **Prueba**. De ahí salen:
   - **Access Token** de prueba (`APP_USR-...`) → `MP_ACCESS_TOKEN`. No se expone en el frontend ni se sube al repo.
   - Sección **Cuenta de prueba**: el **User ID** de la cuenta de prueba vendedora → `MP_USER_ID`.

> El *access token* de prueba pertenece a una **cuenta de prueba vendedora (collector)** que Mercado Pago crea
> junto con la aplicación. La sucursal y la caja se crean con **ese mismo token**, así quedan en el collector que
> después usa la orden. Si se crean con otro usuario, la orden falla.

**Paso 2 — Crear la sucursal (obligatoria)**

La caja es **obligatoria** (sin ella no se puede crear la orden) y toda caja debe pertenecer a una sucursal, así
que hay que crear las dos. **No se crean desde un formulario del panel de desarrolladores**: se crean por API con
el Access Token de prueba, una sola vez. El panel se usa solo para credenciales, cuentas de prueba y webhooks.

```bash
curl -X POST "https://api.mercadopago.com/users/$MP_USER_ID/stores" \
  -H "Authorization: Bearer $MP_ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Sucursal Dulce Mayor",
    "external_id": "DULCEMAYOR",
    "business_hours": {
      "monday": [{ "open": "08:00", "close": "18:00" }],
      "tuesday": [{ "open": "08:00", "close": "18:00" }]
    },
    "location": {
      "street_number": "123",
      "street_name": "San Martin",
      "city_name": "San Miguel de Tucuman",
      "state_name": "Tucuman",
      "latitude": -26.8241,
      "longitude": -65.2226,
      "reference": "Local de prueba"
    }
  }'
```

- `external_id`: alfanumérico, único, hasta 60 caracteres.
- `location` es obligatorio y debe ser coherente: `state_name` y `city_name` tienen que coincidir con el catálogo
  de Mercado Libre (`https://api.mercadolibre.com/countries/AR`) y `latitude`/`longitude` van en decimal simple.
  Datos mal puestos generan errores de impuestos y facturación.
- La respuesta devuelve `"id": <store_id>`, que se usa en el paso siguiente.

**Paso 3 — Crear la caja (punto de venta)**

```bash
curl -X POST "https://api.mercadopago.com/v2/pos" \
  -H "Authorization: Bearer $MP_ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -H "X-Idempotency-Key: dulcemayor-pos-001" \
  -d '{
    "name": "Caja Dulce Mayor",
    "store_id": "{STORE_ID}",
    "external_id": "CAJA01",
    "fixed_amount": false,
    "config": { "qr": { "operating_mode": "pdv" } }
  }'
```

- `{STORE_ID}` se reemplaza por el `id` devuelto en el paso 2 (sin llaves).
- `external_id` debe ser **solo alfanumérico, sin guiones, espacios ni símbolos**; ese valor es
  `MP_EXTERNAL_POS_ID`. Un `external_id` como `DULCEMAYOR-POS` devuelve `400 INVALID_EXTERNAL_ID`.
- `operating_mode: "pdv"` = caja atendida con monto por orden (la que usa este proyecto). `standalone` es el QR
  estático y no sirve para este flujo.
- `fixed_amount: false` porque el monto viaja en cada orden (`total_amount`).
- El `qr_response` que devuelve la respuesta es el QR **estático** de la caja: este proyecto no lo usa, el QR se
  arma con el `qr_data` de cada orden.
- `X-Idempotency-Key` evita duplicar la caja si se reintenta el `curl`.

**Paso 4 — Verificar que quedaron creadas**

```bash
curl -s "https://api.mercadopago.com/users/$MP_USER_ID/stores" -H "Authorization: Bearer $MP_ACCESS_TOKEN"
curl -s "https://api.mercadopago.com/pos" -H "Authorization: Bearer $MP_ACCESS_TOKEN"
```

Ambas deben devolver la sucursal y la caja con `"status": "active"` y el mismo `user_id` del token. Este chequeo
es el que evita la mayoría de los errores al generar el QR.

**Paso 5 — Completar `backend/.env`**

Copiar [`.env.example`](backend/.env.example) a `backend/.env` y completar, respetando los nombres exactos de la
tabla de variables (deben coincidir con `application.properties`):

```properties
MP_BASE_URL=https://api.mercadopago.com
MP_ACCESS_TOKEN=APP_USR-...
MP_USER_ID=3729681128
MP_EXTERNAL_POS_ID=CAJA01
MP_NOTIFICATION_URL=https://<subdominio>.ngrok-free.app/api/pagos/mercadopago/webhook
```

**Paso 6 — Exponer el backend para el webhook**

`ngrok http 8080` y copiar la URL HTTPS **con el path completo** en `MP_NOTIFICATION_URL`. El proyecto envía
`notification_url` dentro de cada orden (topic `merchant_order`), así que con ngrok alcanza; opcionalmente se puede
declarar además el webhook en la sección *Webhooks* de la aplicación.

**Paso 7 — Probar el circuito completo**

1. **Configurar el correo**: Mailtrap (sandbox) o una cuenta con clave de aplicación. Completar `MAIL_HOST`,
   `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD` y `CORREO_HABILITADO=true`.
2. **Arrancar el backend** (`mvnw.cmd spring-boot:run`) y la app del preventista.
3. **Preparar el pedido**: tomar un pedido de un comercio y quedar en el detalle (`/pedidos/{id}`).
4. **Confirmar con QR**: elegir *QR de Mercado Pago*. Verificar que el pedido pasa a `PENDIENTE_DE_ENTREGA` y que
   llega el correo con el detalle, el total y el QR adjunto. Si no se configuró el correo, el `qr_data` queda
   guardado en `Pago.qrData` y se puede generar el QR desde la base.
5. **Pagar el QR**: escanearlo con la app de Mercado Pago usando la **cuenta de prueba compradora**.
6. **Verificar la acreditación**: en los logs se procesa el webhook; el `Pago` pasa a `APROBADO`, la factura a
   `PAGADA` y llega el **segundo correo**.
7. **Probar transferencia y efectivo**: repetir la confirmación con esos medios y verificar el correo (alias y
   titular, o aviso de pago en efectivo).
8. **Probar sin credenciales de MP**: con `MP_ACCESS_TOKEN` vacío, elegir QR debe devolver un error claro y **no**
   confirmar el pedido (la transacción se revierte).

### Cuentas de prueba para escanear el QR

Usar siempre la cuenta de prueba **compradora**: la app no permite pagarle a uno mismo (la cuenta vendedora es la
dueña del token y de la caja).

- **Dónde verlas**: <https://www.mercadopago.com.ar/developers/panel/app> → tarjeta de la aplicación →
  **Credenciales** → entorno **Prueba** → sección **Cuenta de prueba** (User ID, usuario, contraseña y código de
  verificación de 6 dígitos). Para el selector comprador/vendedor y para crear más: menú lateral
  **Cuentas de prueba** (también *Credenciales → Cuenta de prueba → Gestionar cuentas de prueba*).
- **Cómo crear más**: *Cuentas de prueba → Crear cuenta de prueba* → país (**AR**, no se puede cambiar después),
  una identificación reconocible, tipo de cuenta, saldo ficticio y aceptar los términos. Máximo **15 cuentas** por
  aplicación; Mercado Pago ya genera una automáticamente al crear la aplicación.
- **Cómo iniciar sesión en la app**: instalar la app de Mercado Pago en el celular, ingresar con el **usuario y la
  contraseña de la cuenta de prueba compradora** y, si pide validación por correo, usar el **código de
  verificación** de la sección anterior (no llega un correo real).
- **Tarjetas de prueba** (las cuentas creadas automáticamente con la aplicación ya las tienen disponibles):

| Tipo | Marca | Número | CVV | Venc. | Resultado |
|---|---|---|---|---|---|
| Crédito | Mastercard | `5031 7557 3453 0604` | `123` | `11/30` | Pago aprobado |
| Crédito | Visa | `4509 9535 6623 3704` | `123` | `11/30` | Pago aprobado |
| Débito | Mastercard Débito | `5287 3388 1025 3304` | `123` | `11/30` | Pago rechazado |
| Crédito | Mastercard | `5502 7557 4471 4602` | `123` | `4/44` | Aprobado sin pedir CVV |

Con un DNI cualquiera (por ejemplo `12345678`) ya alcanza. Permiten probar tres escenarios: **aprobado** (webhook,
`Pago` `APROBADO` y factura `PAGADA`), **rechazado** (la orden sigue abierta y se puede reintentar sin re-escanear)
y **en espera**.

### Errores típicos y qué revisar

| Síntoma | Causa habitual |
|---|---|
| `401 invalid_token` en un `curl` | Se copiaron las llaves `{...}` literales, o sobra un espacio/`...` en el token |
| `400 INVALID_EXTERNAL_ID` al crear la caja | `external_id` con guion, espacio o símbolo → usar `CAJA01` |
| `500 in_store_order_creation_error` al generar el QR | La caja no existe o pertenece a otro `user_id`: correr la verificación del paso 4 |
| La app dice "QR inválido" | El `qr_data` se dibujó alterado, o el QR ya expiró |
| No llega el webhook | `MP_NOTIFICATION_URL` con `localhost`/`http`, ngrok caído o falta el path `/api/pagos/mercadopago/webhook` |
| No llega el correo con el QR | `CORREO_HABILITADO=false` o SMTP sin configurar (el pago se registra igual) |
| Se cobra dinero real en pruebas | Se usaron credenciales de **producción**; en pruebas el token es el de la pestaña *Prueba* y `live_mode` debe ser `false` |
| La API responde "no configurado" | `MP_ACCESS_TOKEN` o `MP_USER_ID` vacíos, o el `.env` usa otros nombres de variable |

> **Nota sobre versiones de la API:** este proyecto usa el endpoint legacy de QR dinámico
> `PUT /instore/orders/qr/seller/collectors/{user_id}/pos/{external_pos_id}/orders`, que sigue documentado en
> *qr-code-legacy* y es el que devuelve `qr_data` + `in_store_order_id`. Mercado Pago ya publica la versión nueva
> con `POST /v1/orders`; si el legacy se retira, el único archivo a tocar es `MercadoPagoQrCliente` (el resto del
> flujo —correo, webhook y estados— no cambia).

> **Nota:** en este entorno no se ejecutaron las pruebas de integración (por indicación expresa). Este instructivo
> es el procedimiento para validarlas contra el sandbox de Mercado Pago.

### Aviso al próximo comercio de la ruta de entregas

Cuando el distribuidor registra una entrega (`POST /api/pedidos/entregas/{idPuntoVenta}/entregado`), el backend
avisa **por correo al comercio que sigue** en la ruta de ese día. El texto es un mensaje corto ("en los próximos
minutos estaremos visitando tu comercio para entregar tu pedido") y no incluye datos del pedido.

- Destinatario: el usuario (correo) del cliente dueño del comercio, igual que los demás correos.
- El aviso se calcula con `ServicioRuta.proximaParadaPendiente(ruta)`: la parada pendiente con menor `orden`.
  Si no quedan paradas pendientes no se envía nada.
- Cada parada se avisa una sola vez, porque el aviso sale después de marcarla como atendida y una parada no
  puede marcarse dos veces en el día.
- Si el correo falla, la entrega se registra igual (mismo criterio que el resto: `ServicioCorreo` absorbe el error).

**Cómo probarlo con Mailtrap:**

1. Configurar el SMTP como en el paso 2 y arrancar el backend y la app del distribuidor (`movil/distribuidor`, 5174).
2. Ingresar con `raul.benitez@demo.dulcemayor.com.ar` / `dulcemayor` y generar la ruta de entregas del día
   (necesita al menos dos comercios con pedidos pendientes asignados a ese distribuidor).
3. En la primera parada tocar **Entregado**. En Mailtrap debe llegar el aviso al cliente de la **segunda** parada,
   con asunto `Tu pedido está por llegar · Dulce Mayor`.
4. Registrar la entrega de la segunda parada: ahora el aviso llega al cliente de la tercera, y así hasta la última.
