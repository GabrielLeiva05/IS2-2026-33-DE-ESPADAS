# Dulce Mayor — Reportes administrativos (clientes y pedidos, e histórico de ventas)

> **Estado de verificación (leer primero).** En el entorno donde se hizo este trabajo **no hay acceso a Maven Central ni JDK 25**, por lo que **el proyecto no se compiló, no se ejecutó ninguna prueba y la aplicación no se inició**. Tampoco se generó ningún PDF ni Excel real. Lo que sí se comprobó está en la [sección 10](#10-pruebas). Antes de entregar hay que correr `./mvnw test` en `backend/` y en `admin-web/` y probar los reportes con datos reales.

## Índice
1. [Introducción](#1-introducción)
2. [Análisis de la arquitectura existente](#2-análisis-de-la-arquitectura-existente)
3. [Funcionalidades implementadas](#3-funcionalidades-implementadas)
4. [Implementación del patrón Strategy](#4-implementación-del-patrón-strategy)
5. [Integración del patrón Template Method](#5-integración-del-patrón-template-method)
6. [Separación de responsabilidades MVC](#6-separación-de-responsabilidades-mvc)
7. [Endpoints](#7-endpoints)
8. [Archivos y clases modificadas](#8-archivos-y-clases-modificadas)
9. [Dependencias](#9-dependencias)
10. [Pruebas](#10-pruebas)
11. [Instrucciones de ejecución](#11-instrucciones-de-ejecución)
12. [Decisiones, supuestos y puntos a defender](#12-decisiones-supuestos-y-puntos-a-defender)
13. [Datos de demostración (precarga automática)](#13-datos-de-demostración-precarga-automática)

---

## 1. Introducción

El requerimiento pide que el sistema administrativo tenga un apartado con información detallada de los clientes minoristas, sus pedidos, los estados de envío y los montos facturados, visible en pantalla y descargable en PDF, y que se pueda exportar a Excel el histórico de ventas por departamento y preventista en un período.

Se agregaron **dos reportes** al panel administrativo (`admin-web`), respaldados por nuevos endpoints en la API (`backend`):

| Reporte | Pantalla | PDF | Excel |
|---|---|---|---|
| Clientes y pedidos | Sí | Sí | Sí |
| Histórico de ventas por departamento y preventista | Sí | No (no se pidió) | Sí |

La generación de archivos usa el patrón **Strategy**. Solo el **jefe administrativo** accede a los reportes.

## 2. Análisis de la arquitectura existente

El proyecto (`dulce-mayor/`) tiene estos módulos: `backend` (API REST, Spring Boot 4.1, Java 25, JPA, Spring Security + JWT), `admin-web` (Spring MVC + Thymeleaf, puerto 8081), `institucional-web` (Thymeleaf) y `movil/` (React). **No se tocaron `institucional-web` ni `movil/`.**

Hallazgos que condicionaron el diseño:

- **admin-web no tiene base de datos ni repositorios JPA.** Es un cliente de la API: su capa de datos es `ClienteApi` (RestClient) y guarda el JWT del empleado en la `HttpSession`. Por eso las reglas y la generación de archivos van en el **backend**, y admin-web solo presenta y reenvía.
- **Template Method** en `BaseService` (`alta`, `modificar`, `baja`, `consultar`, `listar`; pasos `crearEntidad`, `actualizarEntidad`, `validar`, `convertirAResponse`; ganchos `preAlta`, `postAlta`, etc.) y en `BaseController`. `ServicioPedido` hereda de `BaseService` y usa `preAlta`/`postAlta`.
- **Regla servicio → solo su repositorio.** Los servicios llegan a otros módulos por sus servicios (`obtenerEntidad`, `listarEntidadesActivas`). `ServicioPedido` usa `RepositorioPedido` y habla con `ServicioStock`, `ServicioRuta`, etc.
- **Servicios sin entidad propia** existen: `ServicioCatalogo` no hereda de `BaseService` y `ControladorCatalogo` no hereda de `BaseController`. Los reportes siguen ese mismo criterio.
- **Seguridad:** `SecurityConfig` con JWT, `RoleHierarchy` (JEFE > ADMINISTRATIVO y PREVENTISTA) y `anyRequest().denyAll()` al final: un endpoint nuevo queda cerrado hasta que se le asigna un rol. En admin-web, `InterceptorAcceso` aplica una matriz de navegación por ruta.
- **Modelo relevante:** `Pedido` → `PuntoVenta` (comercio, con `Direccion` → `Localidad` → `Departamento`) → `Cliente` (`Persona`; correo en `Usuario.nombreUsuario`); `Pedido` → `Preventista`; `Pedido` → `Distribuidor` (opcional). Estados: `CARGADO`, `PENDIENTE_DE_ENTREGA`, `ENTREGADO`, `ANULADO`.
- **No existen módulos de facturación ni de pagos** (el menú los muestra como "Pronto"). El propio código documenta que `Pedido.fechaFacturacion` registra **la confirmación del cliente**. Por lo tanto *facturado* significa *confirmado*, y **no existe medio de pago** en el modelo.

## 3. Funcionalidades implementadas

### 3.1 Reporte de clientes y pedidos (`/reportes/clientes-pedidos`)

Una fila por pedido con: documento, nombre y correo del cliente; nombre de fantasía, departamento y domicilio del comercio; N.º de pedido y fecha de carga; preventista; estado del pedido; estado de envío; distribuidor; fecha de facturación y de entrega; monto facturado. Arriba se muestran totales (comercios, pedidos, entregados, pendientes de entrega, total facturado).

Filtros (todos opcionales): desde, hasta, departamento, estado del pedido. **PDF y Excel respetan los mismos filtros** porque se generan con el mismo `ReporteClientesPedidosDTO` que arma el servicio.

Reglas (en `ServicioReporte`):

- Los pedidos se ubican en el período por su **fecha de carga**; se incluyen todos los estados.
- **Monto facturado** = total del pedido solo si el cliente lo confirmó y no está anulado; si no, queda vacío.
- **Estado de envío**, derivado de datos reales: `CARGADO` → "No confirmado"; `ANULADO` → "Anulado"; `PENDIENTE_DE_ENTREGA` sin distribuidor → "Pendiente de asignación"; con distribuidor → "En reparto"; `ENTREGADO` → "Entregado".
- Solo aparecen los comercios con pedidos en el filtro (el reporte tiene una fila por pedido).

### 3.2 Histórico de ventas (`/reportes/ventas`)

Muestra y exporta a Excel las ventas de un período, discriminadas por departamento y preventista.

- **Período obligatorio**: fecha de inicio y de fin.
- **Una venta** es un pedido confirmado por el cliente (tiene `fechaFacturacion`) y no anulado; se ubica en el período por su **fecha de facturación**.
- **Intervalo inclusivo**: de las 00:00 del día de inicio hasta el final del día de fin. En el servicio se consulta `[desde 00:00, hasta+1 día 00:00)`, por lo que no entra nada fuera del período.
- **Validación**: la fecha de inicio no puede ser posterior a la de fin; faltar alguna de las dos en el histórico es un error. Los errores llegan a la pantalla con el mensaje de la API.
- Filtros opcionales: departamento y preventista (se valida que el empleado sea realmente un `Preventista`).
- Se calculan: total general, cantidad de pedidos y de clientes, **subtotales por departamento, por preventista y por departamento + preventista**.
- El Excel tiene dos hojas: **Resumen** (totales y los tres bloques de subtotales) y **Detalle** (una fila por venta con cliente, comercio, N.º de pedido, fecha, subtotal, descuento y total, ordenada por departamento, preventista y fecha, con filtro automático y fila de total).

No se generan datos de ejemplo: todo sale de los pedidos guardados.

### 3.3 Plantilla JRXML del PDF

Se usó la plantilla `reporte_clientes_pedidos.jrxml` suministrada, copiada a `backend/src/main/resources/reportes/`. Se adaptó al modelo real; los cambios son:

| Original | Adaptación | Motivo |
|---|---|---|
| Campo y columna `medioPago` | Reemplazado por `nombreDistribuidor` ("Distribuidor") | No existe medio de pago en el modelo |
| Estados `En camino`, `Facturado`, `Sin stock` | `En reparto`, `Pendiente de asignación`, `Anulado` (y `Entregado`) | Son los valores reales de `estadoEnvio` |
| Grupo por `dniCliente` | Grupo por `clavePuntoVenta` (id del comercio); resumen "Comercios" | Un cliente puede tener varios comercios en distintos departamentos; la cabecera muestra un solo comercio/departamento |
| `DNI:` / `Email:` | `Documento:` / `Correo:` con "-" si no hay dato | `numeroDocumento` es opcional en `Persona` |
| Variable "Pendientes" = total − entregados | Variable `cantPendientes` | La resta incluiría cargados sin confirmar y anulados |
| — | Campo `estadoPedido`, parámetro `FILTROS` y línea de filtros en la portada | Mostrar el estado del pedido y los filtros aplicados |
| Distribución de columnas | Reacomodadas para agregar "Estado pedido" | Ancho de página |

## 4. Implementación del patrón Strategy

**Problema que resuelve.** Hay que generar el mismo reporte en distintos formatos (PDF, Excel) con técnicas totalmente distintas (JasperReports vs. Apache POI). Con `if/else` por formato en el servicio o el controlador, cada formato nuevo obligaría a modificar esa lógica. Con Strategy, cada formato es una clase y la elección es polimórfica.

Paquete `com.ejercicioIntegrador.dulcemayor.exportacion`:

| Clase | Rol | Responsabilidad |
|---|---|---|
| `EstrategiaExportacion<R>` | **Strategy** (interfaz) | `tipoDeReporte()`, `formato()`, `byte[] exportar(R reporte)` |
| `ExportadorClientesPedidosPdf` | Estrategia concreta | Clientes y pedidos → PDF con JasperReports y la plantilla JRXML (compilada una sola vez, de forma perezosa y segura entre hilos) |
| `ExportadorClientesPedidosExcel` | Estrategia concreta | Clientes y pedidos → `.xlsx` con POI |
| `ExportadorVentasExcel` | Estrategia concreta | Histórico de ventas → `.xlsx` con POI (hojas Resumen y Detalle) |
| `ExportadorReportes` | **Context** | Recibe todas las estrategias por inyección y ejecuta la que coincide con el tipo de reporte y el formato |
| `FormatoExportacion` | Enum | `PDF`, `EXCEL` con extensión y tipo de contenido |
| `LibroExcel`, `TextoReporte` | Utilitarios del paquete | Estilos POI y textos de encabezado compartidos, para no duplicar código |
| `ErrorExportacionException` | Excepción técnica | Falla de la biblioteca → 500 por `ManejadorErrores` |

**Cómo se selecciona y ejecuta.** `ServicioReporte` arma el reporte y llama `exportadorReportes.exportar(reporte, formato)`. El contexto recorre las estrategias y ejecuta la que cumple `estrategia.formato() == formato && estrategia.tipoDeReporte().isInstance(reporte)`. No hay `if/else` ni `switch` por formato; la selección vive solo en `ExportadorReportes` (ni en controladores ni en servicios). Si no hay estrategia (por ejemplo, ventas en PDF) se responde con un error de negocio claro.

**Cómo agregar un formato (p. ej. CSV del histórico).** Crear una clase `@Component` que implemente `EstrategiaExportacion<ReporteVentasDTO>` con `formato() = CSV` (y agregar `CSV` al enum `FormatoExportacion`). `ExportadorReportes` la descubre sola: no se modifican el servicio ni el controlador.

**Ventajas.** Extensible sin modificar código existente (abierto/cerrado), una responsabilidad por clase, sin lógica de formato en los servicios, y la pantalla, el PDF y el Excel muestran exactamente los mismos datos porque todos parten del mismo DTO. Es una aplicación real: hay dos tipos de reporte y dos formatos con bibliotecas distintas.

## 5. Integración del patrón Template Method

No se modificó `BaseService`, `BaseController` ni ningún paso o gancho existente. La relación con los cambios es:

- `ServicioPedido` **sigue heredando de `BaseService`** y no se alteró su alta, modificación, baja, `validar`, `preAlta` ni `postAlta`. Solo se **agregaron dos métodos de consulta** (`listarEntidadesCargadasEntre`, `listarEntidadesFacturadasEntre`), en la misma línea que `listarEntidadesActivas()` que el propio `BaseService` ofrece "para la comunicación entre servicios".
- `ServicioReporte` y `ControladorReporte` **no heredan** de las bases porque no administran una entidad (no hay alta/modificación/baja de un "reporte"); es el mismo criterio ya usado por `ServicioCatalogo`/`ControladorCatalogo`. Forzar la herencia obligaría a implementar pasos vacíos, lo contrario de lo que pide el patrón.

## 6. Separación de responsabilidades MVC

| Capa | Componente | Responsabilidad |
|---|---|---|
| Modelo/Repositorio (backend) | `RepositorioPedido` (modificado) | Dos consultas derivadas por rango de fechas. Sin reglas |
| Servicio (backend) | `ServicioPedido` (modificado) | Entrega pedidos por rango; es el único dueño de `RepositorioPedido` |
| Servicio (backend) | `ServicioReporte` (nuevo) | **Todas las reglas**: validación de fechas, intervalo, definición de venta, monto facturado, estado de envío, filtros, subtotales. **No accede a ningún repositorio**: usa `ServicioPedido`, `ServicioDepartamento`, `ServicioEmpleado` |
| Estrategias (backend) | `exportacion/*` | Solo dar formato a un DTO ya armado |
| Controlador (backend) | `ControladorReporte` (nuevo) | Recibe parámetros, delega en el servicio y arma la respuesta HTTP/descarga |
| Seguridad (backend) | `SecurityConfig` (modificado) | `/api/reportes/**` solo `JEFE` |
| Servicio (admin-web) | `ServicioReportes` (nuevo) | Traduce el formulario a parámetros y llama a la API vía `ClienteApi`; sin reglas |
| Controlador (admin-web) | `ControladorReportes` (nuevo) | Recibe filtros, llama al servicio, muestra errores de filtro y entrega el archivo |
| Vista | `reportes/clientes-pedidos.html`, `reportes/ventas.html` | Solo presentan datos y envían filtros; sin lógica de negocio |

Se cumple que ningún servicio accede al repositorio de otro módulo y que ningún controlador ni vista toca repositorios.

## 7. Endpoints

### API (`backend`, puerto 8080) — rol `JEFE`

| Método y ruta | Parámetros (query) | Respuesta |
|---|---|---|
| `GET /api/reportes/clientes-pedidos` | `desde`, `hasta` (`yyyy-MM-dd`), `idDepartamento`, `estado` — todos opcionales | `200` JSON `ReporteClientesPedidosDTO` |
| `GET /api/reportes/clientes-pedidos/exportar` | `formato` (`PDF` o `EXCEL`, obligatorio) + los mismos filtros | `200` archivo (`application/pdf` o `.xlsx`) con `Content-Disposition: attachment` |
| `GET /api/reportes/ventas` | `desde`, `hasta` (obligatorios), `idDepartamento`, `idPreventista` | `200` JSON `ReporteVentasDTO` |
| `GET /api/reportes/ventas/exportar` | `formato` (solo `EXCEL`) + los mismos filtros | `200` archivo `.xlsx` |

Errores: `400` fechas inválidas, período faltante, formato sin estrategia, departamento/preventista inexistente o no válido; `403` sin rol `JEFE`; `401` sin token; `500` falla técnica al generar el archivo.

### admin-web (puerto 8081) — solo jefe

| Método y ruta | Descripción |
|---|---|
| `GET /reportes` | Redirige a Clientes y pedidos |
| `GET /reportes/clientes-pedidos` | Pantalla con filtros y tabla |
| `GET /reportes/clientes-pedidos/pdf` · `/excel` | Descarga con los filtros de pantalla |
| `GET /reportes/ventas` | Pantalla del histórico (consulta al enviar el formulario, parámetro `consultar`) |
| `GET /reportes/ventas/excel` | Descarga del Excel |

## 8. Archivos y clases modificadas

**Backend — nuevos** (`com.ejercicioIntegrador.dulcemayor`):
`controller/ControladorReporte`; `service/ServicioReporte`; `exportacion/` (`EstrategiaExportacion`, `ExportadorReportes`, `FormatoExportacion`, `ExportadorClientesPedidosPdf`, `ExportadorClientesPedidosExcel`, `ExportadorVentasExcel`, `LibroExcel`, `TextoReporte`, `ErrorExportacionException`); `dto/` (`FiltroReporteDTO`, `PedidoReporteDTO`, `ReporteClientesPedidosDTO`, `VentaDTO`, `SubtotalVentasDTO`, `ReporteVentasDTO`, `ArchivoReporteDTO`); recurso `resources/reportes/reporte_clientes_pedidos.jrxml`; pruebas `service/ServicioReporteTest`, `exportacion/ExportadorReportesTest`.

**Backend — modificados:**
- `repository/RepositorioPedido`: dos consultas derivadas por rango de fechas.
- `service/ServicioPedido`: dos métodos de consulta (sección 5).
- `config/SecurityConfig`: una línea `.requestMatchers("/api/reportes/**").hasRole(JEFE)` antes del `denyAll()`.
- `pom.xml`: dependencias y propiedades de versión (sección 9).

**admin-web — nuevos** (`com.dulcemayor.admin`): `controller/ControladorReportes`; `service/ServicioReportes`; `dto/` (`PedidoReporteDTO`, `ReporteClientesPedidosDTO`, `VentaReporteDTO`, `SubtotalVentasDTO`, `ReporteVentasDTO`, `FiltroReporteFormularioDTO`, `ArchivoDescargaDTO`, `FechasLegibles`); vistas `templates/reportes/clientes-pedidos.html` y `ventas.html`.

**admin-web — modificados:**
- `cliente/ClienteApi`: métodos `obtenerConFiltros` y `descargar` (los existentes no se tocaron).
- `seguridad/InterceptorAcceso`: `/reportes` exige jefe.
- `templates/fragments/base.html`: opción "Reportes" en el menú lateral (solo jefe).
- Prueba `InterceptorAccesoTest`: nuevo caso de permisos de `/reportes`.

Las vistas reutilizan `dulcemayor.css` y los fragmentos (`head`, `sidebar`, `topbar`, `alertas`, `navInferior`), los íconos existentes y las clases `tarjeta`, `kpis`, `pestanias`, `tabla-apilable`, `insignia-*`. No se agregó CSS ni JavaScript.

## 9. Dependencias

Solo en `backend/pom.xml` (admin-web no cambia):

| Biblioteca | Versión | Finalidad |
|---|---|---|
| `org.apache.poi:poi-ooxml` | 5.4.1 | Excel `.xlsx` |
| `net.sf.jasperreports:jasperreports` | 6.20.6 | PDF a partir de la plantilla JRXML (misma versión con que se creó la plantilla en Jaspersoft Studio) |

Ninguna está gestionada por el BOM de Spring Boot, por eso la versión es explícita. **Las versiones no pudieron resolverse ni descargarse en este entorno.**

## 10. Pruebas

**Realmente ejecutado:**
- La plantilla JRXML adaptada es XML bien formado (parser XML) y no queda ninguna referencia a `medioPago` ni al nombre de grupo anterior.
- Revisión estática de los archivos Java nuevos o modificados: llaves y paréntesis balanceados, sin imports sin usar.
- Balance de etiquetas HTML en las dos vistas nuevas.
- Se verificó leyendo el código que `RecursoNoEncontradoException` hereda de `MiException` (se corrigió un multi-catch que no habría compilado).

**NO ejecutado (sin Maven Central ni JDK 25):** compilación, `mvnw test`, arranque de Spring, llamadas a los endpoints, generación real de PDF/Excel, renderizado de las vistas y comprobación visual.

**Pruebas automatizadas escritas pero sin ejecutar:**
- `ServicioReporteTest` (Mockito): fecha de inicio posterior a la de fin; histórico sin período; intervalo consultado `[1/10 00:00, 1/11 00:00)` (inclusivo del último día); solo pedidos confirmados y no anulados cuentan como venta; subtotales y totales; filtro por departamento sin repositorios; estados de envío y monto facturado; filtro por estado.
- `ExportadorReportesTest`: el contexto elige la estrategia correcta por formato, rechaza combinaciones sin estrategia y entradas nulas.
- `InterceptorAccesoTest`: los reportes solo para el jefe.

**Pendiente de probar a mano, por riesgo:**
1. El PDF con datos reales (compilación de la plantilla JRXML).
2. Abrir los `.xlsx` en Excel/LibreOffice.
3. Que la fecha de fin incluya el día completo con datos reales.

## 11. Instrucciones de ejecución

Requisitos: los del proyecto (Java 25, MySQL/PostgreSQL según `application.properties`, variables de `backend/.env.example`). El `pom.xml` del backend necesita acceso a Maven Central para descargar POI y JasperReports.

1. Backend: `cd backend && ./mvnw spring-boot:run` (puerto 8080).
2. Admin: `cd admin-web && ./mvnw spring-boot:run` (puerto 8081).
3. Ingresar a `http://localhost:8081` con el **jefe administrativo** (el administrativo común no ve la opción y recibe 403 si entra por URL).
4. Menú lateral → **Reportes**: pestañas *Clientes y pedidos* e *Histórico de ventas*. Elegir filtros, **Consultar** y usar **Descargar PDF / Excel**.

Para correr las pruebas: `./mvnw test` en `backend/` y en `admin-web/`.

**Si falla la generación del PDF con un error de compilación de la plantilla** (puede ocurrir con JDK muy nuevos, porque JasperReports 6.20.6 usa por defecto el compilador Eclipse JDT), una alternativa es crear `backend/src/main/resources/jasperreports.properties` con `net.sf.jasperreports.compiler.java=net.sf.jasperreports.engine.design.JRJavacCompiler` (requiere ejecutar con JDK, no solo JRE). **Es una sugerencia sin verificar.** En servidores Linux sin fuentes, instalar `fontconfig` y una fuente (p. ej. DejaVu) para el PDF.

## 12. Decisiones, supuestos y puntos a defender

- **"Facturado" = confirmado por el cliente**, porque el sistema no tiene módulo de facturación; así lo documenta el propio código de `Pedido`. Cuando exista facturación habrá que revisar la definición de venta en `ServicioReporte`.
- **Sin "medio de pago"**: no existe en el modelo; no se inventó.
- **Contacto del cliente = correo**: es el único dato de contacto del modelo (no hay teléfono).
- **Fechas distintas en cada reporte**: clientes y pedidos por fecha de carga (incluye pedidos aún sin confirmar); ventas por fecha de facturación (solo confirmados).
- **Solo jefe**: la sección 7 del enunciado habla del Jefe Administrativo con reportes globales; por eso el administrativo común no tiene acceso. Si se quisiera abrir, se cambia en `SecurityConfig` e `InterceptorAcceso`.
- **Filtros en memoria**: las consultas del repositorio filtran por fecha; el departamento, el estado y el preventista se filtran en `ServicioReporte`. Es simple y suficiente para el volumen de un mayorista; para volúmenes grandes convendría llevarlos a la consulta.
- **El archivo se genera dentro de una transacción de solo lectura** (los datos son perezosos y el DTO se arma ahí); es una duración corta.
- **No se corrigieron inconsistencias ajenas**, por ejemplo el pom usa `<n>` en lugar de `<name>`.
- El `README.md` general del proyecto no se modificó; este documento también quedó copiado en `docs/README-reportes.md` dentro del ZIP.

---

## 13. Datos de demostración (precarga automática)

Para poder observar los reportes (PDF y Excel) sin cargar nada a mano, al iniciar el backend se precargan datos ficticios. **Esta precarga tampoco se pudo ejecutar en el entorno de desarrollo** (sin Maven ni JDK 25): lo único comprobado es que las tablas de datos cumplen las reglas del backend (CUIT con dígito verificador, DNI de 7-8 dígitos, patentes válidas, nombres solo con letras, correos únicos, códigos de producto únicos, los 18 departamentos con comercios). Faltan compilar y probar el arranque.

### Cómo funciona
- Clases nuevas en `backend/.../config/`: `InicializadorDatosDemo` (la lógica) y `DatosDemo` (las tablas de datos). Es un `ApplicationRunner` con `@Order(2)`; a `InicializadorDatos` (ubicaciones y jefe) se le agregó `@Order(1)` para que corra antes.
- **Todo se carga a través de los servicios** (`ServicioProducto`, `ServicioStock`, `ServicioEmpleado`, `ServicioPedido`, etc.), por lo que pasa por las mismas validaciones que el sistema real.
- **Idempotente:** cada elemento se busca por su clave natural (código de producto, legajo, correo, CUIT) y solo se crea si falta. Los pedidos se generan una única vez (si el primer preventista ya tiene pedidos, no se repiten).
- **Se controla con propiedades:** `app.datos-demo.habilitado` (`DATOS_DEMO_HABILITADO`, por defecto `true`; el perfil `prod` lo fuerza a `false`) y `app.datos-demo.pedidos` (`DATOS_DEMO_PEDIDOS`, por defecto 120). Las pruebas automáticas no la ejecutan porque su `application.properties` no define la propiedad.
- Si falla, solo se registra en el log: nunca impide que la aplicación arranque.

### Qué se carga
| Entidad | Cantidad | Detalle |
|---|---|---|
| Categorías / subcategorías | 6 / 14 | Chocolates y alfajores, Golosinas, Galletitas y snacks, Bebidas, Kiosco y almacén, Repostería |
| Productos | 63 | Con presentación, unidades, algunos en oferta, **una imagen SVG propia cada uno** (el sistema exige una imagen por producto y no permite compartirla) y precio vigente desde hace 6 meses |
| Stock | 63 | Abundante: entre 400 y 2.600 unidades. Casos de prueba: 3 productos **sin stock** (`ENE-002`, `REP-005`, `GAL-005`) y 3 con **stock bajo el mínimo** (`SNA-005`, `CHO-003`, `GOM-005`) |
| Empleados | 8 | 1 administrativo, 4 preventistas (una zona cada uno), 3 distribuidores (con vehículo y patente) |
| Clientes / comercios | 22 / 30 | Repartidos en **los 18 departamentos** de Mendoza, algunos clientes con dos comercios y distintas condiciones de IVA |
| Pedidos | 120 | Últimos 120 días, más densos cerca de hoy. Los preventistas cargan en su zona (con un 12 % de cruces para que la tabla departamento × preventista tenga variedad) |

Estados de los pedidos: los de más de 5 días son ~86 % entregados y ~14 % anulados; los recientes se reparten entre cargados, pendientes sin asignar (*Pendiente de asignación*), en reparto y entregados. Así aparecen **todos los estados de envío** del reporte de clientes y pedidos.

### Cuentas para ingresar
| Rol | Correo | Clave |
|---|---|---|
| Jefe (reportes) | `jefe@dulcemayor.com.ar` | `dulcemayor` (pide cambiarla al ingresar) |
| Administrativo | `lucia.fernandez@demo.dulcemayor.com.ar` | `dulcemayor` (pide cambiarla) |
| Preventistas | `martin.gimenez`, `sofia.herrera`, `diego.quiroga`, `valeria.ortiz` `@demo.dulcemayor.com.ar` | `dulcemayor` (pide cambiarla) |
| Distribuidores | `raul.benitez`, `pablo.sosa`, `nicolas.ruiz` `@demo.dulcemayor.com.ar` | `dulcemayor` (pide cambiarla) |
| Clientes | p. ej. `ricardo.gomez`, `marta.perez`, `hugo.sanchez` `@demo.dulcemayor.com.ar` (formato `nombre.apellido`, sin acentos) | `Cliente2026` |

Los empleados quedan con la clave temporal porque así lo impone el sistema (`ServicioUsuario`) y no hay otra forma de crearlos por los servicios.

### Decisiones y límites a tener presentes
- **Los pedidos se "retrofechan".** Los servicios siempre fechan un pedido con la hora actual, así que sin ajuste todas las ventas serían de hoy y no se podrían probar los filtros por período. Por eso, **después** de crear cada pedido por los servicios (cargar → confirmar → asignar o anular), se actualizan con un `UPDATE` JPQL acotado sus fechas de carga, facturación, despacho y entrega, y el estado `ENTREGADO`. Es una excepción deliberada a "todo por los servicios" y existe solo en este modo demo, porque la entrega real exige que el distribuidor genere su ruta (que consulta un servicio de mapas externo). Los pedidos entregados tienen distribuidor asignado y fechas coherentes (carga ≤ confirmación ≤ despacho ≤ entrega, nunca en el futuro).
- Las coordenadas de los comercios son **aproximadas** (cabecera de cada departamento con un pequeño desplazamiento): sirven para el mapa y las rutas de prueba, no son direcciones reales.
- Son datos inventados (nombres, comercios, CUIT, precios); **no usar en producción**.
- Para volver a cargar desde cero hay que vaciar la base (por ejemplo, borrar el esquema `dulcemayor`); si solo se reinicia, la precarga detecta lo existente y no duplica.
- Si el primer inicio queda a medias (por ejemplo, un error), al reiniciar se completan los elementos que faltan, pero los pedidos solo se generan si el primer preventista aún no tiene ninguno.
