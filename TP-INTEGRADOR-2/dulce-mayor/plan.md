# Plan de acción — Dulce Mayor

**Estado:** Fases 4 a 9 implementadas y verificadas. Backend: `mvnw clean verify` → BUILD SUCCESS, **158 pruebas**. admin-web: 13 pruebas (incluye render de cada pantalla nueva); institucional-web: 3 pruebas; ambas apps móviles con `lint` y `build` en verde. Quedan fuera de alcance, por decisión del usuario, el chatbot y el pipeline de Jenkins.

## 1. Propósito y alcance

Completar el sistema descrito en el enunciado considerando sus cuatro aplicaciones: dashboard administrativo, sitio institucional para minoristas, app móvil del preventista y app móvil del distribuidor, además de la API compartida. El trabajo cubrirá todo el alcance, pero se ordenará para que primero exista un flujo mínimo ejecutable de punta a punta y luego se incorporen capacidades adicionales.

El plan está pensado para **una persona**. Las fases se ejecutan principalmente en secuencia por sus dependencias; algunas tareas de documentación/configuración pueden anticiparse, pero no se presupone un equipo paralelo.

### Resultado prioritario (primer corte funcional)
Un preventista registra al responsable y sus puntos de venta, carga en la visita un pedido para un punto elegido y puede quitar o reducir renglones sin disponibilidad; presenta el pedido ajustado al cliente antes de confirmarlo. El sistema aplica un único código promocional, congela precios/importes y reserva stock. Se gestiona el pago según el medio, se consume stock y se emite comprobante interno al acreditarse el pago total; administración consulta y resuelve excepciones; distribución consulta la ruta y registra entrega o intento fallido. El minorista verifica su correo y usa su cuenta solo para consultar pedidos y comprobantes. Los pedidos web directos quedan fuera de esta primera versión; la primera entrega no excluye las demás funcionalidades, que se ubican en fases posteriores.

## 2. Estado actual observado y puntos de partida

- **Backend:** hay módulos de catálogo/stock, categorías, precios, usuarios, personas, clientes, empleados y ubicaciones; JWT y permisos cerrados por defecto. Faltan los módulos de pedidos/estados, puntos de venta como entidad comercial independiente, promociones, pagos/comprobantes, reservas auditables, rutas/entregas y notificaciones.
- **Dashboard administrador:** existen login, cambio obligatorio de clave, indicadores, catálogo, clientes y empleados. Faltan bandeja y ciclo de pedidos, conciliación de pagos, promociones, excepciones/reprogramación, sucursales, reportes exportables y estadísticas del negocio.
- **Sitio institucional:** existen acceso/registro, cuenta y catálogo. Falta completar contenido/sucursales; el contrato acordado para el cliente es consulta propia de pedidos/estados y comprobantes, sin alta de pedidos web. También faltan BCRA, newsletter y chatbot orientativo sin capacidad de pedido.
- **App preventista:** scaffolding Vite/React. Debe convertirse en herramienta de trabajo en campo: autenticación de empleado, cartera, alta de responsable y múltiples puntos de venta, consulta de catálogo/stock, captura y ajuste de pedido presencial, promoción/pago, y luego geolocalización/ruta.
- **App distribuidor:** scaffolding Vite/React. Debe convertirse en hoja de ruta con asignación, mapa/paradas, detalle, entrega o intento fallido, cobro en efectivo y reprogramación controlada.
- **Transversal:** PostgreSQL es el objetivo pero configuración/documentación parte de MySQL; faltan migraciones gestionadas, Compose local, Jenkins de build/pruebas, estrategia común de email/auditoría y pruebas E2E entre apps. El backend no se ha arrancado con DB real.
- **Validación disponible (5 nov. 2026):** dashboard admin 5 pruebas e institucional 1 pasan; ambas apps móviles pasan instalación desde lockfile, build Vite y ESLint, npm reportó 4 vulnerabilidades moderadas por app. No fue posible establecer el baseline del backend: su compilación se detiene en `ServicioStock.java` por `RepositorioReservaStock` no importado (cambio local sin seguimiento previo a esta fase). No se atribuye el fallo a trabajo de Fase 3.
- **Git:** se preservaron sin modificación los cambios locales preexistentes en `backend/.env.example`, `backend/pom.xml`, `backend/src/main/resources/application.properties`, `../ARCHIVOS/Enunciado.txt` y `.vscode/`.

Esta matriz es una lectura de alcance existente, no una afirmación de auditoría exhaustiva de cada pantalla ni de validación en runtime. Antes de implementar, revisar cambios locales en los archivos anteriores; no restaurar ni mezclar cambios ajenos.

## 3. Principios de diseño y patrones

### Resultados de preflight y revalidación autorizada (5 nov. 2026)
- Git: el árbol de trabajo ya contenía múltiples cambios modificados y no rastreados en backend y otras áreas; se preservaron, modificando solo archivos de código/test autorizados por el usuario.
- Revalidación backend: se corrigió el import faltante de `RepositorioReservaStock`; luego se corrigió el método derivado de consulta a `OrderByIdAsc`, ya que `Pedido` no tiene una propiedad `fechaCreacion`. El test de descuento ahora mockea lectura de stock y reservas activas para reflejar la regla de disponibilidad. Revalidación final: `BUILD SUCCESS`, 82 tests, 0 fallas/errores/omitidos.
- Dashboard admin: test offline pasa, 5/5. Institucional: 1/1.
- Móviles: `npm ci --ignore-scripts`, Vite build y ESLint pasan en ambas; no hay script test. npm reportó 4 vulnerabilidades moderadas por app; no se ejecutó `audit fix`.
- No se inició backend ni se probó DB/API. Los `node_modules` locales son ignorados; no se cambiaron manifests.
- Fase 3 funcional sigue sin iniciar. Baseline validado; queda por definir si iniciar el primer subcorte funcional.

### Evidencia de análisis de patrones
El README declara explícitamente DTO y Template Method como patrones ya usados. Mantener la arquitectura de tres capas MVC; Template Method no debe sustituirse por abstracciones nuevas sin demostrar que el caso lo requiere. No se encontraron todavía funcionalidades de pago/pedido ni eventos de dominio en el código inspeccionado, así que Strategy y Observer se incorporarán al diseñar esos módulos, no como cambios aislados en esta Fase 0.

- Para cada funcionalidad, evaluar los patrones de diseño convenientes y aplicarlos solo cuando mejoren claridad, extensibilidad o separación de responsabilidades; no introducir abstracciones ceremoniales.
- Preservar la arquitectura y convenciones ya existentes en cada módulo (MVC, capas controller/service/repository, DTOs, servicios de dominio y seguridad cerrada por defecto). Antes de alterar clases existentes, seguir sus patrones y evitar duplicar lógica entre clientes web/móviles y backend. El README del proyecto establece que hasta ahora se usaron **Template Method y DTO**; mantenerlos donde están y verificar sus instancias concretas antes de añadir/alterar comportamiento.
- Documentar en el diseño de cada fase qué patrón se usa, qué problema resuelve y qué alternativas se descartaron; cubrirlo con pruebas y mantener consistencia de nombres/paquetes.
- **Strategy para medios de pago (investigar y aplicar si el contrato de cada medio lo permite):** definir una política común para operaciones realmente compartidas (p. ej. iniciar pago, validar transición y procesar confirmación), con estrategias separadas para Mercado Pago (preferencia/QR y webhook), transferencia (conciliación manual) y efectivo (registro al entregar). Un resolver/fábrica selecciona la estrategia según el medio del pedido; persistir el estado y los datos de cada proveedor por separado. No fingir que los tres medios ofrecen las mismas operaciones: transferencias manuales y efectivo no deben exponer métodos irrelevantes de QR/webhook. Evaluar además idempotencia, límites transaccionales, errores y pruebas por estrategia antes de fijar la abstracción.
- **Observer para cambios de estado del pedido (candidato recomendado):** publicar eventos de dominio cuando el pedido confirma pago, se cancela, pasa a preparación, se asigna/reprograma ruta o se entrega. Observadores desacoplados pueden enviar correo, actualizar auditoría/proyecciones y alimentar métricas; la entrega de notificaciones se realiza luego del commit transaccional, con idempotencia/reintentos y manejo de fallos. Preferir eventos de aplicación de Spring / `@TransactionalEventListener(AFTER_COMMIT)` sobre una implementación Observer manual, salvo que las convenciones actuales indiquen lo contrario. Mantener la transición y el guardado del pedido en el servicio de dominio, y no usar Observer para ocultar decisiones de negocio críticas ni para reservar/descontar stock.
- Revisar otros patrones de forma contextual (p. ej. State para ciclo de pedidos solo si reduce condicionales inconsistentes, Adapter para proveedores externos, Repository/DTO ya adoptados); no aplicar patrones solo por cumplir una lista.

## 4. Reglas y decisiones de negocio acordadas

### Clientes y seguridad
- Un responsable comercial (`Cliente`) puede administrar varios puntos de venta. Cada punto tiene identidad, dirección/geolocalización y contacto propios; pedido y entregas se asocian a un punto específico, no solo al responsable.
- En la primera versión, el preventista registra al responsable y uno o más comercios/puntos en campo, asociándolos al preventista que los gestiona. El alta del responsable requiere correo para crear/habilitar su cuenta de consulta; al verificarlo, puede consultar sus propios pedidos/estados y descargar comprobantes. No realiza pedidos desde el portal.
- El preventista es el único rol que crea pedidos en esta etapa. Puede crear pedidos para los puntos que tiene asignados; el cliente no crea ni edita pedidos. Mantener acceso a datos del cliente aislado por cuenta y del preventista por cartera; Jefe tiene alcance global conforme al rol.
- Empleados ingresan con correo y contraseña local. OAuth social se mantiene limitado a clientes, según la configuración existente.
- Los pedidos creados por preventista se confirman desde la app tras presentar al cliente el contenido y total; el ingreso en la visita representa conformidad presencial. El backend registra autor, hora y líneas finales, sin requerir un segundo acto de aceptación en portal. Administración gestiona excepciones posteriores.
- Jefe tiene acceso global a todos los módulos, sin suplantar usuarios. Toda acción relevante registra actor, rol y marca temporal.

### Pedido, precios y stock
- El preventista consulta disponibilidad en visita y antes de confirmar. Si una línea no está disponible, puede quitarla o reducir su cantidad; no se sustituyen productos automáticamente. Presenta el pedido ajustado y el total al cliente en el lugar, y registra esas líneas finales como el pedido confirmado. La aprobación presencial es implícita en el flujo acordado; no se pide una aceptación posterior del cliente en el portal ni se permite confirmar desde la app una línea que excede la disponibilidad vigente.
- El precio vigente, cantidad, descuento aplicado y total quedan copiados al pedido al confirmarlo (snapshot); cambios posteriores de catálogo/precio/promoción no alteran pedidos confirmados.
- La confirmación reserva stock inmediatamente. Al acreditarse y registrarse el pago total se consume definitivamente la reserva y se descuenta inventario; no se emite comprobante antes del pago.
- Transferencia y Mercado Pago sin pago total a las 24 horas desde la confirmación cancelan el pedido automáticamente, liberan la reserva y dejan aviso a preventista/cliente. El proceso debe ser idempotente y no cancelar si el pago fue acreditado concurrentemente con el vencimiento.
- En efectivo, la reserva se mantiene durante preparación/ruta. En intento fallido, distribuidor registra motivo y administración reprograma. Conservar reserva como máximo 7 días desde la primera asignación; luego administración cancela/libera o revalida stock antes de una nueva asignación.
- Cancelar antes del pago libera reservas. Tras acreditación no se borra ni muta la transacción histórica: devoluciones/notas de crédito quedan fuera del primer corte y requerirán un flujo administrativo separado.

### Pago, promociones y comprobantes
- Se debe pagar el total; no hay pagos parciales. Medios: efectivo (se registra cobrado en la entrega), transferencia (alias configurado y conciliación manual de administración) y Mercado Pago (QR/preferencia, confirmación validada por webhook). La selección se gestiona en el pedido; una pantalla de pago no es autoridad para marcar acreditación.
- Transferencia y Mercado Pago: plazo de 24 horas desde confirmación del pedido; efectivo queda reservado durante la ruta, sujeto al límite de reprogramación de 7 días acordado.
- Al pago total acreditado se consume el stock reservado y se emite un comprobante **interno, no fiscal**, descargable por el cliente verificado y remitido por correo según la fase de notificaciones. No se integra facturación fiscal ARCA.
- En el primer corte se acepta como máximo un código promocional por pedido, no acumulable. Tipos: porcentaje o monto fijo; aplica al subtotal de líneas elegibles, el descuento nunca supera ese subtotal. Código y valores calculados se congelan en el pedido. La administración define vigencia/productos elegibles; el backend calcula de forma autoritativa.
- Usar `BigDecimal`, almacenar importes en ARS y fijar escala/redondeo monetario uniforme antes de implementar cálculo/persistencia; la cotización de USD es solo informativa, no habilita pagos en dólares.

### Institucional, rutas y mensajes
- Cotización informativa desde fuente BCRA y mostrar fecha/hora de última actualización.
- Chatbot público consulta solamente catálogo/precios y arma presupuesto indicativo; no crea ni confirma pedidos. Comparar proveedores, costo, privacidad y límites antes de seleccionar uno.
- Administración gestiona sucursales, direcciones, datos de contacto e imágenes.
- Mapas OpenStreetMap con Leaflet. Geolocalización del punto de venta mediante pin manual. Para primera versión, la ruta presenta todas las paradas del día y sugiere el próximo punto por cercanía en línea recta; preventista/distribuidor puede reordenar manualmente. Sin optimización vial, tráfico, ventanas horarias ni capacidad de carga.
- Al confirmar entrega se avisa por correo al siguiente comercio. Si no hay correo válido, omitir el aviso y registrar motivo. Correo es obligatorio para las cuentas de clientes.
- Newsletter el día 5 de cada mes solo para suscriptores opt-in, con mecanismo claro y sencillo de baja.

### Plataforma y pruebas
- PostgreSQL es base principal; almacenar imágenes en la base de datos.
- Docker Compose para ejecución local; Jenkins para compilar y ejecutar pruebas, sin despliegue cloud automatizado en esta etapa.
- Validación: build y arranque de cada proyecto, pruebas API/integración y E2E del flujo completo; pruebas unitarias enfocadas en reglas críticas.

## 5. Requisitos trazables

IDs asignados para este plan a los requisitos del enunciado; son trazabilidad interna, no IDs preexistentes:

| ID | Resultado requerido |
|---|---|
| REQ-01 | Gestión de catálogo, imágenes, presentaciones, precios y stock disponible. |
| REQ-02 | Registro/gestión del cliente-comercio, ubicación y coordenadas. |
| REQ-03 | Captura del pedido por preventista, precio snapshot, promociones y faltantes. |
| REQ-04 | Trazabilidad de pedido desde carga hasta facturación, preparación y entrega. |
| REQ-05 | Reserva/liberación/descuento de stock transaccional y expiración según medio. |
| REQ-06 | Pago total: efectivo, transferencia y Mercado Pago; comprobante interno descargable. |
| REQ-07 | Roles, aislamiento de datos, OAuth cliente y auditoría de acciones. |
| REQ-08 | App de preventista responsive con clientes, pedidos, catálogo y mapa/ruta. |
| REQ-09 | App de distribuidor con hoja de ruta, mapa y resultados de entrega. |
| REQ-10 | Administración de pedidos, promociones, pagos/excepciones y sucursales. |
| REQ-11 | Sitio institucional, catálogo, cuenta cliente, seguimiento y comprobantes. |
| REQ-12 | Correo de confirmación/entrega y newsletter mensual opt-in. |
| REQ-13 | Reportes PDF/Excel y estadísticas requeridas por período, departamento y preventista. |
| REQ-14 | Cotización de dólar informativa desde BCRA, con actualización visible. |
| REQ-15 | Chatbot público de presupuesto orientativo con catálogo/precios, sin creación de pedidos. |
| REQ-16 | OpenStreetMap/Leaflet, pin manual y sugerencia de siguiente punto por cercanía. |
| REQ-17 | PostgreSQL primario, Docker Compose local y pipeline Jenkins de build/pruebas. |
| REQ-18 | Verificación de build, APIs, integración, E2E y preservación de cambios locales preexistentes. |

## 6. Secuencia de implementación

### Fase 0 — Preparación y línea base — **completada**
**Objetivo:** identificar estado de compilación y preservar cambios existentes.

1. Consultar `git status`; no revertir cambios preexistentes.
2. Verificar toolchains/dependencias.
3. Ejecutar test/build de backend y ambas apps web; lint/build de ambos móviles.
4. Anotar resultados, fallas y límites ambientales.

**Cierre:** línea base capturada. El arranque con base real se ejecutará en la fase de validación integral.

### Fase 1 — Reglas detalladas, modelo y contratos — **cerrada en diseño**
**Objetivo:** fijar los detalles imprescindibles que no pueden deducirse del enunciado antes de codificar flujos sensibles. Las reglas listadas arriba son decisiones aprobadas por el usuario; precisiones técnicas abiertas se quedan como tareas de diseño antes del código.

1. **Estados/transiciones:** pedido: borrador interno del cliente app → confirmado/reservado → pendiente de pago (transferencia/MP) o pendiente de entrega/cobro (efectivo) → pagado → preparación → listo → asignado → en ruta → entregado; ramales de cancelación (antes de acreditación), intento fallido/reprogramación y expiraciones. Definir en tabla origen, acción/rol, precondición, efectos, evento/auditoría y destino. Separar estado del pedido de estado del pago y entrega; administrar la matriz antes de migraciones.
2. **Promoción única:** cerrar esquema de campaña/código, vigencia, elegibilidad por líneas/productos/punto o cliente, importe fijo/porcentual, subtotal base, límite/descuento máximo, uso/reutilización, escala/redondeo y comportamiento de código inválido. No hay acumulación. Persistir snapshot del cálculo y versión/regla aplicada en pedido.
3. **Cliente/puntos de venta:** diseño relacional: responsable Cliente puede tener varios PuntosVenta; cada punto contiene dirección/contacto/coordenadas y referencia de responsable. Cerrar datos obligatorios y duplicados; el preventista crea ambos en la visita y se asocia como gestor. Crear cuenta de consulta para el responsable con verificación de correo; hasta verificar, no hay acceso autenticado a pedidos/comprobantes. No hay pedidos desde portal en este corte; preventista es creador de pedidos. El plan no debe exigir una segunda confirmación del cliente en web.
4. **Mercado Pago:** definir creación idempotente de operación, asociación con pago/pedido, validación de firma/origen del webhook, consulta server-to-server, reconciliación, estados fallidos/reintentos y expiración de 24 h desde confirmación. No confiar en el retorno del navegador.
5. **Dinero:** `BigDecimal`; elegir escala y redondeo (propuesta técnica: ARS con 2 decimales, `HALF_UP`) y hacerlo uniforme en persistencia/API/visualización. Evitar `double` en los módulos nuevos y planificar si se migra el `VigenciaPrecio.precio` existente.
6. **Modelo/API PostgreSQL:** proponer `PuntoVenta`, `Pedido`, `DetallePedido`, `ReservaStock`/`MovimientoStock`, `Promocion`, `Pago`, `Comprobante`, `Entrega`/`Ruta`, historial/auditoría de estado, y configuración/newsletter/cotización. Revisar el modelo actual para no duplicar `Cliente`, aprovechar stock/precio existente con migración de `double`, y diseñar ownership. Redactar DTOs, servicios, endpoints, estados/códigos HTTP, idempotencia, errores y paginación/filtros.
7. **Ruta v1:** registrar paradas diarias asociadas a cartera del preventista o pedidos de distribuidor; sugerir cercanía en línea recta y permitir secuencia manual. Verificar fuente de coordenadas y comportamiento sin geolocalización.
8. **Aceptación/escenarios:** redactar Given/When/Then para falta de stock, ajuste en visita y presentación del total, confirmación, reserva concurrente, un código elegible/no elegible, pago total/expiración race, aislamiento cliente/preventista, descarga de comprobante y reintento de entrega.

**Decisiones confirmadas por el usuario:** reservar al confirmar; consumo y comprobante solo tras pago total; faltantes se resuelven durante visita quitando o reduciendo líneas con aprobación presencial implícita y sin sustitución automática; un cliente puede tener múltiples puntos de venta; preventista único creador de pedidos en primera versión; portal minorista de solo consulta de pedidos/estados/comprobantes tras verificación; apps móviles requieren conexión para acciones confirmadas (sin garantía offline); un solo código promocional por pedido, porcentaje o monto fijo sobre subtotal elegible; transferencia/MP vencen a las 24 h desde confirmación; ruta inicial por cercanía simple, reordenable.

**Cierre:** reglas de ciclo, pagos, promociones y cartera; ER y contratos de DTO/API revisados; precisión y escenarios de aceptación listos. Se evita implementar código fuera del alcance de esta fase.

### Fase 2 — Base PostgreSQL y cimientos transversales
**Objetivo:** hacer reproducible el stack y establecer infraestructura común.

**Estado (5 nov. 2026):** avances parciales implementados; backend verificado en H2 y apps cliente con build/tests verdes. La validación PostgreSQL real sigue bloqueada por Docker daemon no disponible.

1. [x] Configurar PostgreSQL como DB de desarrollo por defecto (`application.properties`), conservar compatibilidad de dependencias con MySQL y pruebas existentes en H2. [x] Documentar defaults locales y opciones en el README. No se afirma compatibilidad certificada de MySQL.
2. [ ] Agregar migraciones versionadas y semillas idempotentes. Se posterga el cambio de `ddl-auto` hasta elegir la estrategia y probar contra base PostgreSQL real, para evitar pérdida/alteración accidental de datos en entornos existentes.
3. [ ] Correo configurable; requiere elegir proveedor/transportes y parámetros seguros. Auditoría/correlación requiere modelo de retención y alcance. No implementado en este corte.
4. [x] Añadir `docker-compose.yml` solo para PostgreSQL 17, con volumen y healthcheck. La validación sintáctica `docker compose config --quiet` pasa. [ ] No se pudo arrancar/validar el contenedor: Docker CLI instalado pero Docker Desktop daemon no estaba disponible.
5. [x] Añadir Jenkinsfile para `verify` backend y dos sitios Java, `npm ci`, lint y build de ambas apps móviles; documentar requisitos: agente Windows con JDK 25 y Node/npm. La ejecución real requiere Jenkins.
6. [x] Crear `.gitignore` local para `.env`, datos DB y artefactos comunes. Verificado con `git check-ignore`; artefactos/credenciales existentes ya están ignorados por sus `.gitignore` propios.
7. [x] Validación Maven en línea de backend (`mvnw.cmd -B -ntp verify`): BUILD SUCCESS, 83 tests, 0 failures/errors/skips (verificación repetida el 2026-11-05).
8. [x] Ambas apps web: tests de integración/contexto (admin 5, institucional 1) pasan; `verify` genera jars ejecutables (BUILD SUCCESS).
9. [x] Ambas apps React: `npm run lint` y `npm run build` pasan.

**Pendientes explícitos:** iniciar Docker Desktop/Compose y backend contra PostgreSQL; verificar aplicación real y esquema; diseñar/implantar migraciones revisadas; acordar el alcance de email/auditoría antes de implementarlos. La sintaxis Compose y `.gitignore` pasan validación estática; no se verificó un pipeline Jenkins real. Las credenciales configuradas son solo de desarrollo local, no producción. Se preservaron todos los cambios locales preexistentes.

### Fase 3 — MVP de clientes, pedidos, stock y pago

**Objetivo:** implementar la primera ruta vertical end-to-end según las reglas confirmadas: el preventista registra y ajusta el pedido presencialmente; el portal del cliente es de solo consulta.

**Alcance de la fase (por componente):**

1. Backend: alta del responsable minorista y uno o varios puntos de venta, con unicidad/manejo de duplicados definida, asociación al preventista gestor, contacto y ubicación. Cuenta cliente con verificación; endpoint solo de consulta para propiedad propia.
2. Backend: catálogo y disponibilidad para el rol preventista, creación de pedido para punto de venta gestionado, líneas/cantidades, snapshot de precio/promoción, total y trazabilidad. Cliente autenticado no puede crear/modificar pedidos.
3. Backend: al confirmar, verificar nuevamente stock y reservarlo atómicamente (bloqueo o actualización condicional). Registrar movimientos/reservas; no permitir sobreventa ni confirmar líneas sin stock.
4. App preventista: presentar catálogo, disponibilidad y carrito; si hay faltante, permitir quitar o reducir cantidad y recalcular; mostrar totales finales al cliente y, tras aprobación en la visita, confirmar el pedido. No exige una aceptación digital posterior ni registra texto/evidencia de consentimiento adicional; el evento de confirmación conserva el preventista actor y hora.
5. Backend/admin: operación de pago total por transferencia con conciliación manual y Mercado Pago con QR/preferencia/webhook; efectivo se registra como cobrado solo en entrega. Strategy solo para lo común entre operaciones, con adaptador específico por proveedor; idempotencia y reglas de expiración desde confirmación.
6. Backend: pedido no pagado de transferencia/MP a 24 h → cancelación idempotente y liberación de reserva; coordinar race con confirmación de pago. Efectivo mantiene reserva durante ruta hasta máximo de 7 días desde primera asignación.
7. Backend: pago total confirmado consume reserva y stock en transacción coherente; generar comprobante interno no fiscal (descargable autenticado). Notificar por correo en fase 8 si aún no hay plataforma de correo.
8. Dashboard: bandeja, detalle, estados independientes, stock/reserva, historial, conciliación de transferencia y excepciones autorizadas.
9. Portal institucional: cliente verificado consulta pedidos/estados/comprobantes propios, sin crear/editar pedido en esta versión.
10. Pruebas: ajuste presencial y sin stock, precio congelado, reserva concurrente, propiedad punto/cliente/preventista, una promoción, cancelación/liberación, vencimiento pago, webhook repetido/incorrecto, transición a entrega/efectivo y comprobante solo tras pago total.

**Cierre vertical esperado:** completar un recorrido de alta de punto → pedido → confirmación/reserva → cancelación/liberación o finalización/consumo; comprobar aislamiento entre preventistas y coherencia de stock antes/después. Pago y comprobante pertenecen a un subcorte posterior y no bloquean el cierre de este alcance.

#### Primer subcorte ejecutado

**Prioridad acordada:** avanzar en funcionalidad de negocio primero. Docker, Jenkins y otras tareas de CI/despliegue quedaron pospuestas hasta cerrar las funcionalidades.

**Alcance del subcorte:** alta de comercio y pedido presencial hasta confirmar y reservar stock. La confirmación reserva stock; la cancelación lo libera; la finalización autorizada lo consume. Pagos y comprobante quedaron para el subcorte siguiente, que ya se ejecutó (ver sección 15); no se finge acreditación en ningún caso.

**Decisiones confirmadas por el usuario:**
- ARS se calcula con dos decimales y `HALF_UP`; el servidor conserva precios/cantidades/importes del pedido como snapshot.
- Si el documento del responsable ya existe, rechazar el alta y requerir revisión administrativa; no fusionar ni reasignar automáticamente.
- En esta etapa el alta por preventista no crea una cuenta cliente utilizable: no hay verificación email activa. OAuth/registro existente no se cambia.

**Especificidades acordadas antes de codificar:**
- Autorización por ownership en todos los endpoints del preventista: cada cuenta opera solo su cartera, sus pedidos y sus cobros.
- App del preventista con estados vacío/error/cargando y sin repetir confirmaciones; el carrito permite quitar o reducir cantidades y recalcula antes de confirmar.
- Pruebas de servicio, persistencia/API y seguridad: documento duplicado, visibilidad por preventista, precio congelado, cantidad/total/escala, reserva concurrente y acceso denegado por otros roles.
- No se modifica Docker/Jenkins, no se activa el registro de cliente por email, no se incluye pago/promoción/ruta ni checkout web, y no se rompe el contrato de inventario: las reservas se registran aparte y la disponibilidad resta reservas activas.

**Riesgos y preservación:** `ddl-auto=update` crea y actualiza tablas en desarrollo pero no sustituye migraciones versionadas; el runtime PostgreSQL no está disponible (Docker daemon ausente) y las pruebas corren con H2, brecha que se advierte en cada checkpoint y que impide declarar el sistema listo para producción. Tampoco se deben convertir `VigenciaPrecio.precio` ni los contratos de catálogo existentes a `BigDecimal` de forma incompatible: el snapshot monetario nuevo usa `BigDecimal` de escala 2 sin cambiar las respuestas heredadas.

**Pausa de alcance:** al completar el subcorte (o ante cualquier bloqueo ambiental/funcional) se reportan endpoints, pruebas y brechas; se pausa antes de avanzar a la fase siguiente.

**Historial:** el detalle de lo ejecutado en cada etapa (cambios, verificaciones y límites conocidos) está reunido en la **sección 15, «Registro de ejecución»**.

### Fase 4 — Promociones y catálogo comercial completo
1. Backend de promociones: vigencia, código, elegibilidad, importe fijo/porcentaje, límites y auditoría. Un código por pedido, sin acumulación.
2. Rechazar códigos inválidos/inactivos/vencidos, subtotal elegible nulo, descuento superior al subtotal y reutilización no permitida; no aceptar monto calculado desde cliente.
3. Admin para crear/editar/desactivar promociones, indicar aplicabilidad y previsualizar el resultado.
4. Aplicación en app preventista; servidor calcula y devuelve desglose claro por línea/subtotal.
5. Probar precisión/redondeo, topes, elegibilidad, duplicación/reintento y snapshot al editar reglas.

**Cierre:** descuento reproducible y congelado en pedido; ninguna regla de acumulación habilitada sin cambio explícito de alcance.

### Fase 5 — Rutas, entregas y app del distribuidor
1. Backend: asignar pedidos listos a ruta/vehículo/distribuidor; secuencia editable, estados y prevención de asignaciones duplicadas.
2. Definir jornadas de reparto y cómo se combinan pedidos de varios clientes/puntos; crear endpoints para hoja de ruta propia, detalle, siguiente parada sugerida por distancia en línea recta y reordenamiento.
3. App distribuidor: login responsive, lista de paradas, detalle, Leaflet/OSM, avance, entrega o intento fallido con motivo; acciones confirmadas en servidor, sin modo offline.
4. Al entregar: registrar fecha/hora, usuario, cantidades/resultado, cobro de efectivo según contrato, estado del pedido y siguiente parada; notificación de siguiente punto cuando se habilite correo. Si falta correo, no bloquear el despacho.
5. Limitar reserva de pedidos en efectivo a 7 días desde primera asignación; vencimiento con alerta/acción administrativa idempotente.
6. Pruebas: no marcar entrega doble, permisos del distribuidor asignado, reordenación, intento fallido/reprogramación, efectivo, expiración/liberación y ausencia de conectividad.

**Cierre:** una entrega queda trazada y el siguiente destino recibe email o un motivo auditado de omisión.

### Fase 6 — Sitio institucional y funcionalidades comerciales
1. Completar contenido de inicio, información institucional, contactos y páginas de sucursales.
2. Administra sucursales con dirección, coordenadas, contacto e imágenes; aplicar autorización y almacenamiento de imágenes en DB según decisión.
3. Mostrar catálogo y disponibilidad acorde a stock publicable; retirar/ocultar automáticamente productos agotados sin romper enlaces ni acceso administrativo.
4. Cotización BCRA en servicio con timeout, caché, fecha de consulta, manejo de indisponibilidad y valor anterior identificado como no actualizado. No bloquear catálogo/pedido si el proveedor falla.
5. Completar estado del cliente, historial, seguimiento de pedido, perfil, consentimiento y comprobantes.
6. Probar responsive/accessibilidad básica, sesiones/cookies seguras y errores API; las apps web mantienen sesión de servidor y nunca exponen credenciales/secrets al browser.

**Cierre:** contenido administrativo visible públicamente de forma segura; caída del proveedor de cambio no impide operación.

### Fase 7 — Mapas y app del preventista
1. Añadir selector Leaflet/OSM para geolocalización manual de punto de venta; guardar coordenadas validadas y dirección descriptiva; contemplar precisión/ubicación inexistente.
2. App preventista: mapa de todos sus clientes y pedidos previstos para el día; ruta de visitas ordenada por cercanía con reordenamiento manual.
3. Mostrar ubicación, datos mínimos del comercio y estado de visita; asegurar que preventista no consulte puntos de otros usuarios, salvo acceso global del jefe.
4. Evitar servicios públicos de geocodificación sin políticas de uso; en esta definición el pin es manual, no se depende de geocodificación automatizada.
5. Documentar atribución/licencia de OSM, atribución visible, configuración de tiles y límites de uso.

**Cierre:** clientes geolocalizados aparecen en mapa; orden sugerido se entiende, es editable y queda guardado para la jornada.

### Fase 8 — Correo, tareas programadas y chatbot — **implementada salvo chatbot**
1. Correo transaccional de alta/verificación, confirmación de pago/facturación, cancelación por vencimiento, próxima entrega y eventos acordados; plantillas, reintentos, idempotencia y registro de fallos.
2. Newsletter programado el día 5 para opt-in; dar de baja con mecanismo sencillo, respetar baja en la misma ejecución, evitar duplicados y manejar zona horaria local.
3. Definir y aprobar proveedor de chatbot comparando costo, privacidad, disponibilidad y límites antes de habilitar credenciales. Nunca adjuntar claves al frontend.
4. Chatbot público limitado a consulta del catálogo/precios y presupuesto indicativo; el modelo no es autoridad para precio ni disponibilidad: backend calcula desde información vigente. Limitar prompts a los datos permitidos, uso, tamaño de entrada y exposición de datos personales. Degradar con respuesta controlada si proveedor no está disponible.
5. Añadir aviso claro de carácter orientativo y mecanismo para que el usuario revise el presupuesto; no crea pedido ni promete stock.

**Cierre:** correos repetidos no duplican efectos, baja del newsletter se respeta y chatbot no puede confirmar pedido ni inventar precio autoritativo.

### Fase 9 — Dashboard, reportes y estadísticas — **implementada**
1. Gestión de sucursales, pedidos y promociones; vistas de excepciones, pagos por validar, reservas próximas a vencer, entregas fallidas y reprogramaciones.
2. Reportes con filtros y autorización global: clientes, pedidos, estados de envío y montos facturados; descarga PDF.
3. Exportación Excel histórica de ventas por departamento y preventor, en período especificado.
4. Reportes gerenciales descargables PDF o Excel; proteger grandes rangos mediante filtros/paginación/límites razonables y generación segura.
5. Gráficos: pedidos por preventista por mes/día según criterio temporal definido, y cantidades en stock por producto. Alinear rótulos y agregación a los datos reales; no simular datos.
6. Evitar exposición de datos personales en reportes descargados; registrar exportaciones sensibles según política del sistema.
7. Pruebas de totales, zona horaria, filtro de fechas inclusivo/exclusivo, departamentos/preventistas, PDF y Excel reproducibles.

**Cierre:** filtros concuerdan con consultas; exportaciones reflejan datos comprobados y el usuario sin rol no accede.

### Fase 10 — Endurecimiento e integración final — **parcial: chatbot y Jenkins pendientes**
1. Actualizar README con puertos, variables, perfiles, servicios, datos de prueba, pasos de arranque y recuperación; quitar afirmaciones obsoletas de estado.
2. Componer servicios con Docker Compose local, readiness/healthchecks, almacenamiento persistente y datos no secretos en `.env.example`.
3. Jenkins: etapas checkout/build/test frontend y backend, reportes de pruebas, fallar ante regresiones y mantener credenciales externas como secretos del sistema CI. No incluir deploy cloud.
4. Pruebas E2E por rol para un recorrido común: login empleado → alta cliente → pedido → parcial opcional → pago → comprobante → ruta → entrega/seguimiento.
5. Pruebas de seguridad por rol y ownership, CSRF/sesión web, autorización API, webhook no autorizado/repetido, fuga de secretos, errores y rate limits de servicios externos.
6. Verificación de accesibilidad/responsive móvil; manejo de API offline/errores; respaldos/restauración de DB e imágenes.
7. Corrida final limpia del build/test/arranque y checklist de requisitos. Dejar fuera explícitamente integraciones no incluidas (facturación fiscal y despliegue cloud) para evitar interpretar el comprobante como factura legal.

**Cierre final:** todo requisito REQ-01..REQ-18 tiene evidencia funcional y prueba, o una excepción/limitación explícita aprobada; no quedan módulos con pantallas de scaffold presentadas como funcionalidad.

## 7. Dependencias y orden crítico

`Fase 0 → Fase 1 → Fase 2 → Fase 3 → Fase 5 → Fase 6/7 → Fase 8/9 → Fase 10`.

- Fase 4 depende de cálculo monetario/snapshot resuelto en fase 1 y pedido base de fase 3; se puede implementar antes de rutas.
- Fase 5 depende de pedido/pago/stock de fase 3 y coordenadas; puede avanzar una vez las APIs estén estabilizadas.
- Fase 6 puede avanzar parcialmente después de fases 2/3: sucursales y BCRA no bloquean el MVP vertical.
- Chatbot (fase 8) depende de catálogo estable y proveedor aprobado; debe quedar al final, después del flujo transaccional.
- Reportes (fase 9) dependen de datos y estados estabilizados de pedidos, pagos y entregas.
- Jenkins/Compose empiezan en fase 2 y se completan al integrar fase 10; no se posponen todos los cimientos al cierre.

## 8. Criterios de “terminado” por tarea/fase

- Código integrado en el proyecto correcto y estilo/estructura compatible con MVC, servicios y DTOs actuales.
- La regla de negocio se valida en servidor, no solo en frontend; permisos denegados por defecto y ownership probado.
- Persistencia/migración actualizada de forma versionada y segura; operación monetaria usa precisión decimal.
- Build/test del módulo pasan; pruebas relevantes de reglas límite incluidas. E2E valida rutas entre aplicaciones cuando completa un flujo.
- UI responsive presenta carga, éxito, vacío, error y reintento sin confirmar operación dos veces.
- Variables/credenciales documentadas sin valores sensibles; actualizada documentación relacionada.
- Auditoría preserva quién/cuándo/qué cambió para pedido, pago, inventario y acciones del jefe.

## 9. Riesgos y tratamientos

| Riesgo | Tratamiento en el plan |
|---|---|
| Reservas concurrentes y sobreventa | Operación atómica/transaccional, movimientos trazables y pruebas de concurrencia. |
| Webhook duplicado o falso | Validación server-side, autenticidad, idempotencia y conciliación del estado en Mercado Pago. |
| Efectivo/reprogramaciones mantienen stock bloqueado | Límite máximo 7 días, alertas, liberación/cancelación y política de re-asignación. |
| Reglas de promoción y moneda | Un código no acumulable acordado; falta cerrar precisión/escala, elegibilidad y reutilización por código antes de implementar. |
| Alta en campo sin email verificado | Cuenta pendiente con estado explícito; limitar inicio de sesión/acciones según regla definida; flujo de verificación seguro. |
| BCRA/proveedor IA/SMTP indisponible | Timeout, caché/estado de actualización, reintentos selectivos y degradación controlada. |
| Imágenes en DB incrementan tamaño/backup | Medir volumen; limitar formato/tamaño, servir contenido con cache/validaciones y probar backup/restore. |
| Diferencias actuales entre MySQL y PostgreSQL | Alinear configuración a PostgreSQL y probar migraciones/consultas del flujo nuevo en ese motor. |
| Reportes o datos sensibles mal autorizados | Consultas con scope de rol, pruebas de acceso, controles de exportación y auditoría. |
| Falta de configuración de email/OAuth/Mercado Pago/IA | Entornos sandbox y variables externas documentadas; ningún secreto en Git. |
| Alcance amplio para una persona | Priorizar el corte vertical primero; las fases 4 y 6–9 vienen después sin abandonar requisitos. |

## 10. Requerimientos abiertos de implementación (no bloquean el plan)
Antes de sus fases correspondientes se documentarán las siguientes precisiones; no contradicen las decisiones aprobadas:
1. Campos obligatorios de responsable/punto de venta, unicidad y resolución de duplicados.
2. Precisión/escala y redondeo de ARS; topes, elegibilidad y política de reutilización de códigos promocionales.
3. Credenciales sandbox/producción de Mercado Pago, catálogo de estados del proveedor, conciliación y tratamiento de reembolsos (estos últimos fuera de MVP).
4. Datos comerciales y leyenda visible de comprobante interno “no válido como factura fiscal”.
5. Consentimiento opt-in/newsletter, baja, registro y tratamiento de emails rebotados.
6. Fuente/tipo de cotización BCRA y frecuencia de actualización.
7. Criterio calendario y filtros de reportes/gráficos.
8. Proveedor/costos/límites del chatbot, aprobado antes de integrar.
9. Formatos, tamaño máximo, procesamiento y límite de imágenes almacenadas como BLOB.
10. Proveedor de correo transaccional y política de reintentos/alertas.
11. Modalidad de asignación de una jornada de reparto y acciones del distribuidor sin coordenadas; disponibilidad/datos sandbox y observabilidad para validar backend con PostgreSQL.

## 11. Trazabilidad de requisitos a fases

| Requisito | Fases y evidencia de implementación |
|---|---|
| REQ-01 Catálogo, precios e inventario | 0, 1, 3, 4, 6 — snapshots, visibilidad agotado y pruebas de stock. |
| REQ-02 Cliente/comercio y geolocalización | 1, 3, 7 — alta, cuenta/verificación, pin y ownership. |
| REQ-03 Captura de pedidos/promociones | 1, 3, 4 — pedido desde app preventista, confirmaciones y cálculo de servidor. |
| REQ-04 Trazabilidad completa | 1, 3, 5 — eventos auditables de pedido y fechas de ciclo. |
| REQ-05 Reservas/expiración | 1, 3, 5 — atomicidad, pago-online 24 h y efectivo/reprogramación 7 días. |
| REQ-06 Pagos/comprobante | 1, 3, 5 — transfer, webhook MP, efectivo, pago total, comprobante interno. |
| REQ-07 Roles/seguridad | 0, 1, 3, 5, 6, 9, 10 — pruebas de autorización/ownership y auditoría. |
| REQ-08 App preventista | 3, 7, 10 — app conectada, pedidos propios y ruta OSM. |
| REQ-09 App distribuidor | 5, 10 — entregas, mapa, intentos y estados. |
| REQ-10 Administración | 3, 4, 5, 6, 9 — pedidos, promociones, sucursales y excepciones. |
| REQ-11 Sitio cliente | 3, 6, 10 — catálogo, cuenta, seguimiento y descarga. |
| REQ-12 Notificaciones/newsletter | 3, 5, 8, 10 — templates, avisos, opt-in/baja y tareas. |
| REQ-13 Reportes y gráficos | 9, 10 — PDF, XLSX, agregaciones y permisos. |
| REQ-14 Dólar BCRA | 6, 10 — consulta/caché/última actualización y fallback. |
| REQ-15 Chatbot de presupuesto | 8, 10 — proveedor aprobado, presupuesto orientativo sin pedido. |
| REQ-16 OSM/Leaflet | 5, 7, 10 — mapa distribuidor/preventista, pin y cercanía editable. |
| REQ-17 PostgreSQL/Docker/Jenkins | 2, 10 — PostgreSQL, Compose local, CI de build/tests. |
| REQ-18 Validación y preservación | 0–10 — baseline, test gates, E2E, README y preservación de cambios locales. |

## 12. Orden sugerido de ejecución resumido

1. Verificar rama/cambios y builds (F0).
2. Cerrar estado del pedido, reglas de promoción, alta/verificación y contrato de pagos (F1).
3. Migraciones, PostgreSQL, correo básico, auditoría, Compose y Jenkins base (F2).
4. Backend vertical de cliente, pedido, reservas y pagos; probar reglas críticas (F3 backend).
5. Admin para pedidos/excepciones y app preventista para alta y carga (F3 UI).
6. Seguimiento de cliente y comprobante descargable; prueba vertical (F3 cierre).
7. Promociones detalladas (F4).
8. Rutas/entregas, distribución, efectivo, reprogramación y emails de siguiente punto (F5).
9. Sucursales, contenido institucional, BCRA y mapas preventista (F6–F7).
10. Notificaciones completas, newsletter y chatbot (F8).
11. Reportes/estadísticas (F9).
12. E2E de cuatro apps, hardening, documentación y pipeline completo (F10).

## 13. Exclusiones explícitas

- Facturación fiscal ARCA/AFIP: fuera del alcance actual; solo comprobante interno no fiscal.
- Pago en USD: fuera; el dólar es informativo.
- Optimización avanzada de rutas con ventanas horarias/capacidad: fuera de primera versión; sugerencia simple por cercanía.
- Offline-first en móviles: no definido ni comprometido; las acciones críticas requieren confirmación del backend.
- Despliegue cloud automatizado: fuera; Docker local y CI de Jenkins sí incluidos.
- Chatbot creando pedidos: expresamente fuera.

## 14. Notas de ejecución

- Referencias de estado actual: el README del proyecto en `dulce-mayor/README.md`, fuentes de backend en `dulce-mayor/backend`, administración en `dulce-mayor/admin-web`, institucional en `dulce-mayor/institucional-web` y apps en `dulce-mayor/movil`.
- El documento vive fuera del repositorio, en el directorio persistente de esta sesión. La elaboración del plan no modificó los proyectos; la ejecución posterior, con sus verificaciones, se documenta en la sección 15.
- Cambios locales preexistentes observados en el backend y artefactos ajenos al alcance se preservarán; se inspeccionarán solo al iniciar ejecución.

## 15. Registro de ejecución

Historial de lo ejecutado, en orden cronológico. Cada entrada resume cambios, verificaciones y límites conocidos; el diseño por fases se mantiene en la sección 6 y las notas generales del documento en la sección 14.

### Registro de ejecución (2026-11-04)

- `exit_plan_mode` no estuvo disponible en el modo actual. `ask_user` confirmó iniciar implementación y luego amplió alcance para incluir el ciclo mínimo de reservas (liberación al cancelar, consumo al completar); no se implementarán pagos ni despacho UI en este subcorte.
- Seguimiento SQL: API/reglas (`in_progress`), app móvil (depende de API), verificación/checkpoint (depende de ambas).
- **Continuación 2026-11-05:** la app preventista pasa lint/build; backend `clean verify` pasa con 83 pruebas (0 fallos) y `ServicioStockTest` con 9/9. Se eliminó la inyección de campo de `RepositorioReservaStock` en `ServicioStock` y se dejó como dependencia explícita de constructor (sin cambio de reglas); también se incorporó fecha de creación persistida para que el DTO de pedido exponga una fecha real. El flujo móvil ahora refleja el borrador pendiente de la respuesta API al recargar y evita ofrecer ajuste cuando el pedido ya no está en borrador. Falta agregar/ejecutar pruebas de integración específicas para alta→pedido→reserva→cancelación/consumo y revisar el contrato móvil/API; Docker daemon no disponible, así que PostgreSQL real no se verificó. No iniciar pagos.
- Se preservará el estado local preexistente en `README.md`, `.gitignore`, `Jenkinsfile`, `docker-compose.yml`, `backend/.env.example`, `backend/pom.xml`, `backend/src/main/resources/application.properties` y `../ARCHIVOS/Enunciado.txt`; no modificar sin inspeccionar y distinguir cambios ajenos.
- Límite técnico: `VigenciaPrecio.precio` y `ProductoCatalogoDTO.precio` son `double`; bajo la restricción de no romper el contrato actual, el pedido puede almacenar un snapshot `BigDecimal` de escala 2/HALF_UP, pero toma el valor vigente desde la representación `double`. Recomendar contrato de precio decimal versionado para precisión end-to-end.

### Registro de ejecución (2026-11-06)

Verificación de la afirmación “la implementación está en Fase 3”: **confirmada pero incompleta**. Existían el modelo (`PuntoVenta`, `Pedido`, `LineaPedido`, `ReservaStock`), los servicios `ServicioPuntoVenta`/`ServicioPedido`, los endpoints `/api/preventista/**` y la pantalla de la app preventista, pero el ciclo de reserva no se podía cerrar: `completarAdministrativo`/`cancelarAdministrativo` estaban en el servicio sin ningún endpoint que los invocara.

Etapas completadas en esta sesión:

1. **Ciclo administrativo expuesto.** Nuevo `ControladorPedidoAdministrativo` (`POST /api/pedidos/{id}/completar`, `POST /api/pedidos/{id}/cancelar`) y regla `.requestMatchers("/api/pedidos/**").hasRole(ADMINISTRATIVO)` en `SecurityConfig` (el JEFE la hereda; el preventista recibe 403). Con esto la finalización consume la reserva y la cancelación la libera por una vía autenticada y autorizada.
2. **Bug corregido en la cartera.** `ServicioPuntoVenta.listarCartera`/`listarPuntosCliente` hacían `preventista.getId()` con `preventista == null`, que es el caso del JEFE (permitido por seguridad en `/api/preventista/puntos-venta/**`): producía `NullPointerException` → 500. Ahora el JEFE consulta la cartera completa ordenada por nombre comercial y no registra puntos (no tiene cartera propia).
3. **Pruebas de servicio nuevas.** `ServicioPedidoTest` (13) cubre snapshot de precio con escala 2/`HALF_UP`, producto repetido, producto sin precio vigente, solicitud inválida, reserva de unidades convertidas, sobreventa sin reserva parcial, confirmación sin líneas, consumo total al completar, exigencia de todas las reservas activas, completar no confirmado, liberación al cancelar, cancelar completado y cuenta sin perfil de preventista. `ServicioPuntoVentaTest` (8) cubre alta con asociación al preventista, documento duplicado, rol sin perfil, preventista sin empleado, datos mínimos, punto ajeno y cartera global del JEFE.
4. **Pruebas de integración nuevas/reescritas.** La prueba `elPedidoConfirmadoSoloConsumeStockAlCompletarseAdministrativamente` estaba mal: cancelaba en lugar de completar y no probaba el consumo. Ahora completa por la vía administrativa (10 → 7 unidades, reserva consumida con fecha, sin liberación, sin doble consumo). Se agregaron: cierre administrativo cerrado al preventista y carga de pedidos cerrada al administrativo (con herencia del JEFE), sobreventa sin reservas parciales, aislamiento entre preventistas (cartera y pedidos ajenos) y precio congelado en el pedido confirmado aunque cambie la vigencia.

Verificación final: `mvnw.cmd -B -ntp verify` → **BUILD SUCCESS, 110 pruebas, 0 fallas/errores/omitidos** (baseline previo: 85). Contrato móvil/API revisado: la app preventista solo usa logins, `/api/preventista/puntos-venta`, `/api/preventista/pedidos` (+`/lineas`, `/confirmar`), `/api/catalogo` y ubicaciones; todos existen y no se modificaron DTOs, por lo que no hubo cambios en `movil/`.

Pendiente de este subcorte: bandeja/detalle de pedidos en el dashboard (hoy solo hay transiciones por id), migraciones versionadas y PostgreSQL real (Docker daemon no disponible), y las pruebas E2E entre apps. Pagos, comprobante y UI de despacho seguían fuera de ese corte; el subcorte siguiente los cubre.


### Registro de ejecución — subcorte de pagos (2026-11-06)

Alcance aprobado por el usuario antes de codificar: adaptador real de Mercado Pago por API (sin credenciales responde «no configurado», no simula acreditación), comprobante HTML sin dependencias nuevas, bandeja/conciliación en admin-web y registro manual de efectivo como paso intermedio hasta la Fase 5.

1. **Modelo y estados.** `Pago` (medio, estado, importe escala 2, `referenciaOperacion` como clave de idempotencia, `referenciaExterna`, vencimiento, `@Version`) y `Comprobante` (uno por pedido, único). El estado del pago (`EstadoPago`) se lleva separado del estado del pedido, como pedía la Fase 1.
2. **Strategy por medio** (`pagos/`): interfaz común `EstrategiaPago` (iniciar, vigencia de reserva, acreditar) y capacidad separada `EstrategiaNotificable` solo para Mercado Pago, para no fingir que transferencia y efectivo tienen QR/webhook. `ResolvedorEstrategiaPago` selecciona por medio.
3. **Mercado Pago real.** `ClienteMercadoPago` (JDK `HttpClient`, sin dependencias nuevas): crea la preferencia con `external_reference` = clave de idempotencia, valida `x-signature` (HMAC-SHA256 del manifiesto `id:…;request-id:…;ts:…;`) y verifica el pago con `GET /v1/payments/{id}`. El webhook es el único endpoint abierto y responde 401 ante firma inválida.
4. **Servicio de pagos.** Inicio de cobro (preventista dueño o administrativo) con validación de estado y de intento pendiente único; acreditación con **bloqueo pesimista** del pago, consumo de reservas y emisión del comprobante en una sola transacción; rechazo; expiración idempotente con cancelación del pedido y liberación de la reserva.
5. **Cambio de regla en el pedido.** `POST /api/pedidos/{id}/completar` ya no consume stock por sí solo: exige un pago acreditado. El consumo real lo dispara la acreditación (Fase 3, ítem 7).
6. **Tarea programada** (`ProgramadorExpiracionPagos`): cada minuto expira los pagos vencidos; cada pago se procesa en su propia transacción y, si uno falla, el resto continúa. Desactivable con `app.pagos.expiracion-habilitada=false` (así corren las pruebas).
7. **Comprobante interno no fiscal** en HTML imprimible, descargable con autenticación tanto por administración como por el preventista dueño; el HTML escapa los datos del pedido.
8. **Dashboard (admin-web):** `ControladorPedidos` + `ServicioPedidosAdmin` + plantillas `pedidos/lista.html` y `pedidos/detalle.html` (bandeja con filtros por estado y búsqueda, detalle con productos, intentos de pago, acreditación de transferencia con número de operación, rechazo, inicio de cobro y cancelación). El menú dejó de mostrar «Pedidos» como módulo pendiente.
9. **App del preventista:** panel de cobro por pedido (iniciar transferencia/Mercado Pago/efectivo, ver intentos y enlace de pago, descargar comprobante).

Pruebas agregadas: `ServicioPagoTest` (15), `ClienteMercadoPagoTest` (4, incluida la firma HMAC), `EstrategiaPagoMercadoPagoTest` (6), dos pruebas nuevas de `ServicioPedidoTest`, tres de integración end-to-end (transferencia iniciada por preventista y acreditada por administración con consumo y comprobante; vencimiento que libera y cancela; Mercado Pago sin configurar y webhook sin firma) y `PlantillasPedidosTest` en admin-web, que renderiza las pantallas nuevas con el motor real.

Verificación: backend `verify` → BUILD SUCCESS, **139 pruebas** (antes 110). admin-web `verify` → BUILD SUCCESS, **8 pruebas** (antes 5). App preventista: `npm run lint` y `npm run build` OK.

Límites conocidos: Mercado Pago no se pudo probar contra el proveedor real (no hay credenciales ni sandbox configurado); la integración queda validada con dobles de prueba y responde «no configurado» mientras falte el token. El cobro en efectivo se registra desde administración porque la entrega pertenece a la Fase 5. El comprobante es interno y no fiscal. Sigue pendiente PostgreSQL real y migraciones versionadas.


### Registro de ejecución — Fases 4 a 7 (2026-11-07)

**Fase 4 — Promociones.** Entidad `Promocion` (código único, tipo porcentaje/monto fijo, vigencia, compra mínima, uso único) y snapshot en el pedido (`subtotal`, `descuento`, `promocionCodigo`). Reglas: un solo código por pedido, sin acumulación, descuento calculado siempre en el servidor con `HALF_UP`, sin superar el subtotal, y control de reutilización para códigos de uso único. API administrativa con vista previa, endpoints del preventista para aplicar/quitar sobre el borrador, UI en admin-web y campo de código en la app.

**Fase 5 — Rutas y entregas.** Entidades `Ruta` y `Parada`; asignación administrativa de pedidos confirmados o pagos, prevención de duplicados y de rutas cerradas. La app del distribuidor lista su jornada, sugiere la siguiente parada por distancia en línea recta, permite reordenar y registrar entrega o intento fallido con motivo. Al entregar un pedido con cobro pendiente en efectivo se acredita el pago, se consume el stock, se emite el comprobante y el pedido pasa a `ENTREGADO`. Un intento fallido deja el pedido reservado para reprogramarlo. admin-web incorpora la pantalla de hojas de ruta.

**Fase 6 — Sitio institucional.** Sucursales (`Sucursal` + API pública/admin + página pública con enlace a OpenStreetMap), cotización del BCRA con timeout, caché de 30 minutos y degradación controlada (nunca bloquea catálogo ni pedidos) y portal del cliente (`/api/cliente/**` + `/mis-pedidos`) que muestra estado, historial y comprobante de los pedidos de sus comercios, identificados por el correo de contacto del punto de venta.

**Fase 7 — Mapas y visitas.** Planificación diaria de visitas (`Visita`) generada desde la cartera con orden sugerido por cercanía (vecino más cercano en línea recta) y reordenamiento manual. La app del preventista suma un mapa Leaflet/OSM con los comercios y el plan del día, y el alta de comercio permite marcar el pin a mano sobre el mapa (sin geocodificación automática), con atribución de OpenStreetMap.

Verificación: backend `mvnw clean verify` → BUILD SUCCESS, **155 pruebas** (18 de integración end-to-end, incluidas promoción→pago→entrega con efectivo, intento fallido y reprogramación, portal del cliente, sucursales y plan de visitas). admin-web: BUILD SUCCESS con render de las pantallas nuevas. institucional-web: BUILD SUCCESS con prueba de render de la página pública. Apps móviles: `npm run lint` y `npm run build` sin errores.

Límites conocidos: la cotización depende del BCRA (si no hay red se muestra el último valor con aviso), los mapas usan mosaicos de OpenStreetMap (requieren internet) y no hay geocodificación inversa ni ruteo por calles: la sugerencia es en línea recta, como indica el plan.

### Registro de ejecución — Fases 8 y 9, configuración y datos (2026-11-07)

**Fase 8 sin chatbot — correo, eventos y newsletter.** `ServicioCorreo` envía por SMTP cuando el `.env` define
`MAIL_HOST` y, si no, registra el envío como `SIMULADO` en `envios_correo` y en el log, de modo que las
notificaciones se pueden probar sin proveedor. Los servicios de pedido, pago y ruta publican `EventoPedido` y el
observador `EscuchaEventosPedido` arma y entrega el correo (Observer con eventos de Spring, como proponía la
sección 3). `ServicioNewsletter` implementa opt-in público, baja por token y envío mensual programado el día
configurable (5 por defecto) vía `ProgramadorNewsletter`. Consulta y disparo manual en `/api/notificaciones/**`.

**Fase 9 — reportes y estadísticas.** `ServicioReporte` agrega ventas por departamento, preventista, comercio o
estado sobre el período inclusivo, con totales, desglose por estado, serie de pedidos por preventista y stock por
producto. `ControladorReporte` expone JSON, exportación **CSV compatible con Excel** (UTF-8 con BOM) y las series.
En admin-web se agregaron las pantallas de Reportes, Notificaciones y Puntos de venta, y el inicio dejó de mostrar
módulos «en desarrollo»: ahora enlaza a cada sección operativa.

**Configuración por `.env`.** El panel y el sitio incorporaron `spring.config.import` (carpeta propia y raíz del
proyecto), así que toda la configuración —base de datos, JWT, OAuth2, Mercado Pago, correo, newsletter y datos
demo— puede definirse en archivos `.env` sin variables de entorno del sistema. Se crearon los cinco
`.env.example` (API, panel, sitio y ambas apps móviles).

**OAuth2 del cliente por configuración.** `SeguridadOAuth2Config` dejó de depender de perfiles de Spring: se
activa con `app.oauth2.habilitado` (`APP_OAUTH2_HABILITADO` en el `.env`) y construye los registros de Google y
Facebook en código, incluyendo solo los proveedores con credenciales completas. Se eliminaron los dos
`application-oauth-*.properties`, que quedaron obsoletos. El plan ya contemplaba este ingreso desde la Fase 1
(REQ-07); ahora queda operativo y configurable sin tocar el sistema operativo.

**Datos semilla ampliados.** El dataset demo pasó a 12 comercios con coordenadas en seis departamentos, 12
productos, 12 pedidos repartidos entre confirmado, completado (transferencia acreditada), entregado y cancelado,
una hoja de ruta con paradas mixtas y el plan de visitas del día. Con eso los mapas del preventista y del
distribuidor muestran un recorrido realista y los reportes tienen volumen para agrupar.

Verificación: backend `clean verify` → 158 pruebas; admin-web 13; institucional-web 3; apps móviles con lint y
build. Se corrigieron además los constructores de dos pruebas unitarias afectados por la inyección del publicador
de eventos y se hizo null-safe la notificación del comprobante.
