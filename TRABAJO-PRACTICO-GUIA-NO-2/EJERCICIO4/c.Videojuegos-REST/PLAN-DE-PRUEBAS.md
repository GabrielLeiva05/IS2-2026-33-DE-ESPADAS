# Plan de pruebas - Videojuegos Bootstrap

## 1. Propósito

Diseñar las pruebas necesarias para comprobar que el sistema permite consultar el catálogo de videojuegos y administrar videojuegos, categorías y estudios. Este documento define niveles, técnicas, datos, criterios de aprobación y ejemplos. Las pruebas están diseñadas, pero no fueron ejecutadas.

## 2. Alcance

### Incluido

- Página inicial y catálogo de videojuegos activos.
- Consulta de detalle y búsqueda por título.
- Alta, modificación y desactivación lógica de videojuegos.
- Alta, modificación y desactivación lógica de categorías y estudios.
- Validación de título, descripción, precio, stock, fecha, estudio y categoría.
- Validación y persistencia de imágenes.
- Servicios `ServicioVideojuego`, `ServicioCategoria` y `ServicioEstudio`.
- Repositorios JPA y consultas de videojuegos activos.
- Vistas Thymeleaf y navegación HTTP.

### Fuera de alcance inicial

- Pruebas visuales exhaustivas de cada navegador.
- Pruebas de disponibilidad de MySQL en producción.
- Pruebas de despliegue y recuperación ante desastres.

## 3. Riesgos prioritarios

| Riesgo | Impacto | Prioridad | Evidencia a obtener |
|---|---:|---:|---|
| Un videojuego desactivado aparece en el catálogo | Alto | P0 | `findAllByActivo` y pantalla `/` o `/inicio` solo devuelven activos |
| Se guarda un videojuego con datos inválidos | Alto | P0 | Errores de validación y ausencia de llamada a `saveOne` |
| Se acepta un archivo que no es una imagen o supera 15 MB | Alto | P0 | Mensaje de error y ausencia de escritura/guardado |
| La baja lógica elimina datos o no cambia `activo` | Alto | P1 | Registro persistente conserva sus datos y alterna `activo` |
| Búsqueda o detalle muestran registros inactivos | Medio | P1 | Resultado vacío/error para registros inactivos |
| Excepciones de servicio producen una respuesta no controlada | Medio | P1 | Vista `error` y mensaje disponible en el modelo |
| Consultas con texto o IDs manipulados exponen datos | Alto | P0 | Parámetros enlazados y pruebas negativas de entrada |

## 4. Estrategia por niveles

| Nivel/tipo | Objetivo | Componentes | Técnica | Criterio de salida |
|---|---|---|---|---|
| Unitarias | Verificar una clase aislada | Servicios y validaciones de entidades | JUnit 5 + Mockito / Validator | Todos los casos P0 del servicio pasan |
| Integración | Verificar colaboración con persistencia | Repositorios y servicios + base de prueba | `@DataJpaTest` con H2 o MySQL de pruebas | CRUD, filtros y consultas pasan sin datos cruzados |
| Web/API | Verificar rutas, binding y vistas | Controladores MVC | `MockMvc` + mocks de servicios | Respuestas, vistas, modelo y redirecciones correctas |
| Sistema | Verificar el flujo completo | Aplicación, MySQL y Thymeleaf | Entorno aislado y datos semilla | Flujos de catálogo y CRUD completan de punta a punta |
| Aceptación | Verificar criterios del usuario | Catálogo y administración | Casos Given/When/Then | Los criterios de aceptación son observables por usuario |
| Regresión | Evitar que cambios rompan funciones existentes | Suite automatizada P0/P1 | Ejecución en cada cambio | Sin fallos P0 y sin aumento de defectos conocidos |
| Rendimiento | Medir tiempos y capacidad | Búsqueda, catálogo y altas | JMeter/Gatling o herramienta equivalente | Cumple objetivos definidos en sección 8 |
| Seguridad | Detectar entradas y accesos indebidos | Formularios, archivos y consultas | OWASP ZAP + pruebas negativas | Sin inyección, path traversal ni carga peligrosa aceptada |
| Exploratoria | Encontrar comportamientos no previstos | Toda la interfaz | Sesiones de 45 minutos, charters | Hallazgos registrados y clasificados |

## 5. Datos de prueba

Usar una base aislada y reiniciable. No usar credenciales ni datos de producción.

| Dato | Valor | Uso |
|---|---|---|
| Categoría activa | `Acción` | Asociación válida |
| Categoría inactiva | `RPG inactiva` | Verificar filtros y baja lógica |
| Estudio activo | `Estudio Demo` | Asociación válida |
| Videojuego válido | `Galactic Run`, precio `59.99`, stock `10`, fecha actual o anterior | Alta y consulta |
| Título vacío | `""` | `@NotEmpty` |
| Descripción corta | `abc` | `@Size(min=5)` |
| Precio inválido | `4.99` y `10000.01` | `@Min`/`@Max` |
| Stock inválido | `0` | `@Min` |
| Fecha futura | día posterior al actual | `@PastOrPresent` |
| Archivo válido | PNG o JPEG menor de 15 MB | Alta/edición |
| Archivo inválido | TXT renombrado, archivo vacío y archivo de 15 MB o más | Reglas de imagen |

## 6. Casos de prueba principales

### 6.1 Pruebas unitarias

| ID | Unidad | Preparación | Acción | Resultado esperado |
|---|---|---|---|---|
| UT-01 | `ServicioVideojuego.findAllByActivo` | Repositorio devuelve un activo y un inactivo | Invocar método | Devuelve exactamente la colección del repositorio y delega una vez |
| UT-02 | `ServicioVideojuego.findByTitle` | Repositorio devuelve coincidencias activas | Buscar `gal` | Devuelve coincidencias y conserva el término enviado |
| UT-03 | `deleteById` | Repositorio devuelve videojuego activo | Desactivar ID existente | Cambia `activo` a `false` y guarda la misma entidad |
| UT-04 | `deleteById` | Repositorio devuelve `Optional.empty()` | Desactivar ID inexistente | Lanza excepción y no guarda |
| UT-05 | Validación de `Videojuego` | Título vacío, precio 4, stock 0, fecha futura y relaciones nulas | Ejecutar Bean Validation | Se informan violaciones; no se considera válida la entidad |

**Ejemplo aplicado (JUnit 5 + Mockito):** el archivo [ServicioVideojuegoUnitTest.java](src/test/java/com/uncuyo/tp1_ej4/services/ServicioVideojuegoUnitTest.java) muestra UT-01 y UT-03. Está preparado como plantilla y no fue ejecutado.

### 6.2 Pruebas de integración de persistencia

| ID | Caso | Acción | Resultado esperado |
|---|---|---|---|
| IT-01 | Guardar y recuperar categoría | Guardar categoría activa y buscar por ID | Se recuperan ID, nombre y `activo=true` |
| IT-02 | Baja lógica | Guardar videojuego activo y ejecutar servicio de baja | El registro permanece; `activo=false` |
| IT-03 | Filtro de activos | Insertar activos e inactivos | `findAllByActivo` no devuelve inactivos |
| IT-04 | Detalle de activo | Buscar ID activo y luego inactivo | Solo el activo devuelve resultado |
| IT-05 | Búsqueda parcial | Insertar `Galactic Run` y buscar `Run` | Devuelve el título esperado; no devuelve inactivos |

Ejemplo de diseño: `@DataJpaTest` con una base de prueba, datos insertados por cada caso y limpieza automática al terminar. No conectar esta suite a la base configurada para desarrollo.

### 6.3 Pruebas web/MVC

| ID | Ruta | Entrada | Resultado esperado |
|---|---|---|---|
| WEB-01 | `GET /` | Servicios con videojuegos activos | HTTP 200, vista `index`, atributo `videojuegos` |
| WEB-02 | `GET /busqueda?query=gal` | Coincidencias simuladas | HTTP 200, vista `views/busqueda`, atributo `resultado=gal` |
| WEB-03 | `GET /formulario/categoria/0` | ID 0 | Formulario y categoría nueva en el modelo |
| WEB-04 | `POST /formulario/categoria/0` | Categoría válida | Redirección a `/categorias` y llamada a `saveOne` |
| WEB-05 | `POST /formulario/categoria/0` | Nombre inválido o binding con errores | Se mantiene el formulario y no se guarda |
| WEB-06 | `POST /formulario/videojuego/0` | Multipart vacío | Mensaje `La imagen es requerida`; no se guarda |
| WEB-07 | `POST /formulario/videojuego/0` | Imagen no válida | Mensaje `La extension no es valida`; no se escribe archivo |
| WEB-08 | `POST /eliminar/videojuego/7` | ID existente | Redirección a `/crud` y baja lógica |
| WEB-09 | Cualquier ruta | Servicio lanza excepción | Vista `error` y atributo `error` |

### 6.4 Pruebas de sistema y aceptación

**AC-01 - Consultar catálogo**

- Dado que existen videojuegos activos e inactivos.
- Cuando el usuario ingresa a `/`.
- Entonces ve solo los activos, con título, precio e imagen.

**AC-02 - Buscar un videojuego**

- Dado que existe `Galactic Run` activo.
- Cuando el usuario busca `gal`.
- Entonces recibe el videojuego coincidente y el término buscado.

**AC-03 - Registrar videojuego válido**

- Dado un estudio, una categoría, datos válidos y una imagen PNG menor de 15 MB.
- Cuando el administrador completa el formulario y confirma.
- Entonces se crea el registro, se persiste la imagen y se redirige a `/crud`.

**AC-04 - Rechazar videojuego inválido**

- Dado un formulario con fecha futura, precio inválido o imagen vacía.
- Cuando el administrador confirma.
- Entonces se muestran los errores, se conserva el formulario y no se crea el registro.

**AC-05 - Desactivación lógica**

- Dado un videojuego activo.
- Cuando el administrador confirma su eliminación.
- Entonces deja de aparecer en el catálogo y permanece disponible en el CRUD con `activo=false`.

### 6.5 Pruebas de rendimiento

Diseñar una prueba de carga con datos representativos y una base separada:

| Escenario | Carga inicial | Objetivo provisional |
|---|---:|---:|
| `GET /` | 50 usuarios concurrentes durante 5 min | P95 menor a 1.5 s; error menor a 1% |
| `GET /busqueda?query=ga` | 50 usuarios concurrentes durante 5 min | P95 menor a 2 s; error menor a 1% |
| Alta de videojuego | 10 usuarios concurrentes durante 5 min | P95 menor a 3 s; sin pérdida de registros |
| Archivo cercano a 15 MB | 5 usuarios concurrentes | Sin caída del proceso; rechazo correcto sobre el límite |

Los objetivos son valores iniciales y deben confirmarse con el responsable del sistema antes de convertirlos en criterio contractual.

### 6.6 Pruebas de seguridad

| ID | Amenaza | Entrada | Resultado esperado |
|---|---|---|---|
| SEC-01 | Inyección SQL | `query=' OR 1=1 --` | No devuelve registros arbitrarios ni altera datos |
| SEC-02 | XSS almacenado | Título `<script>alert(1)</script>` | Se rechaza o escapa al mostrarlo |
| SEC-03 | Path traversal | Nombre de imagen `../../config.txt` | No permite escribir fuera de la carpeta de imágenes |
| SEC-04 | Tipo MIME fraudulento | TXT con MIME `image/png` | El contenido no imagen se rechaza |
| SEC-05 | Tamaño límite | Archivo exactamente igual o mayor a 15 MB | Se rechaza según la regla definida |
| SEC-06 | IDs inválidos | `/detalle/-1`, `/detalle/999999` | Respuesta controlada, sin error 500 no tratado |
| SEC-07 | Acceso no autorizado | POST de administración sin autenticación, si se incorpora seguridad | Acceso denegado |

Nota: el código actual no muestra autenticación/autorización. SEC-07 queda como requisito de seguridad pendiente o como riesgo aceptado explícitamente.

### 6.7 Pruebas de regresión y exploratorias

- La suite de regresión mínima debe incluir UT-01, UT-03, IT-03, IT-04, WEB-04, WEB-05, WEB-06, WEB-08 y AC-01 a AC-05.
- Charter exploratorio E-01: recorrer alta, edición, baja y reactivación con navegación atrás/adelante y doble envío del formulario.
- Charter exploratorio E-02: buscar con vacío, espacios, mayúsculas, acentos, caracteres especiales y texto muy largo.
- Charter exploratorio E-03: cargar PNG/JPEG, archivo sin extensión, extensión mayúscula, archivo corrupto y límite de tamaño.

## 7. Trazabilidad

| Requisito funcional | Casos que lo cubren |
|---|---|
| Mostrar catálogo activo | UT-01, IT-03, WEB-01, AC-01 |
| Buscar por título | UT-02, IT-05, WEB-02, AC-02 |
| Crear y editar datos | WEB-04, WEB-05, AC-03, AC-04 |
| Validar videojuegos | UT-05, WEB-05, WEB-06, WEB-07, AC-04 |
| Baja lógica | UT-03, IT-02, WEB-08, AC-05 |
| Proteger datos y archivos | SEC-01 a SEC-06 |
| Mantener tiempos aceptables | Escenarios de rendimiento |

## 8. Entorno y ejecución futura

- Java 17, Maven Wrapper y el perfil de pruebas de Maven.
- Base de datos exclusiva para pruebas; preferentemente H2 para repositorios o Testcontainers/MySQL para compatibilidad real.
- Directorio temporal para imágenes; nunca `C://Videojuegos/imagenes` compartido con desarrollo.
- Datos semilla versionados y repetibles.
- Ejecutar primero unitarias, luego integración/web, después sistema, rendimiento y seguridad.
- Registrar por caso: fecha, versión, ambiente, datos, resultado, evidencia y defecto asociado.

## 9. Criterios de entrada, salida y defectos

**Entrada:** código compilable, base de pruebas disponible, datos semilla cargables y directorio temporal de imágenes creado.

**Salida:** 100% de casos P0 ejecutados, 100% aprobados; ningún defecto crítico/alto abierto; los defectos medios restantes deben tener aceptación explícita.

**Clasificación:** P0 bloquea una operación principal o compromete datos/seguridad; P1 afecta una función importante; P2 afecta una función secundaria o la presentación.

**Reporte de defecto:** incluir ID de caso, pasos reproducibles, entrada, resultado esperado, resultado real, evidencia, severidad y ambiente.

## 10. Observaciones para implementación futura

- El controlador obtiene la extensión usando el nombre original antes de comprobar si el archivo está vacío; conviene agregar un caso específico para nombre nulo/vacío y corregirlo antes de automatizar el alta.
- `validarExtension` comprueba si el contenido puede ser leído como imagen, pero no valida una lista explícita de extensiones permitidas; mantener SEC-04 y E-03.
- Las consultas nativas usan parámetros para el texto de búsqueda; IT-05 y SEC-01 deben conservarse como regresión.
- La configuración contiene credenciales de base de datos en texto plano; no reutilizarlas en ambientes de prueba ni compartirlas en reportes.
