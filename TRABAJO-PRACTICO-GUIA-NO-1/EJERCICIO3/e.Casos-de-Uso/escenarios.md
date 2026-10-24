# Escenarios de Casos de Uso — Sistema de Gestión de Stock para Minimarket

## Actores identificados

| Actor | Descripción |
|---|---|
| **Empleado** | Persona que opera el sistema en el mostrador/depósito: registra ventas, compras y consulta stock. |
| **Cliente** | Persona que adquiere productos en el minimarket (actor secundario en la venta). |
| **Proveedor** | Persona/empresa que abastece productos al minimarket (actor secundario en la compra). |
| **Administrador** | Encargado del minimarket: gestiona el catálogo de productos, clientes, proveedores y reportes. |

---

## 1. Registrar Venta

- **Actor principal:** Empleado
- **Actor secundario:** Cliente
- **Precondición:** El empleado inició sesión en el sistema; existen productos con stock disponible cargados en el sistema.
- **Flujo principal:**
  1. El empleado selecciona la opción "Registrar Venta".
  2. El sistema solicita los datos del cliente (o venta a consumidor final).
  3. El empleado busca y agrega uno o varios productos indicando cantidad, generando un `DetalleVenta` por cada ítem.
  4. El sistema valida el stock disponible de cada producto (clase `Stock`) y calcula el subtotal por línea y el total de la venta.
  5. El sistema invoca el caso de uso **Registrar Método de Pago** .
  6. El sistema invoca el caso de uso **Actualizar Stock** , descontando las cantidades vendidas.
  7. El sistema invoca el caso de uso **Generar Factura**.
  8. El sistema confirma la venta y muestra el comprobante al empleado.
- **Flujos alternativos:**
  - **A1 – Cliente no registrado:** si el cliente no existe en el sistema, el empleado puede cargarlo como "consumidor final" sin invocar `Gestionar Cliente`, o derivar al alta rápida de cliente.
  - **A2 – Modificar cantidad/eliminar producto:** el empleado puede editar la cantidad o quitar un `DetalleVenta` antes de confirmar; el sistema recalcula el total.
  - **A3 – Aplicar descuento:** el empleado ingresa un descuento sobre un producto o el total; el sistema recalcula el importe final.
- **Flujo de excepción:**
  - **E1 – Stock insuficiente:** si la cantidad solicitada supera el stock disponible de un producto, el sistema muestra un mensaje de error y no permite agregar esa cantidad; el flujo vuelve al paso 3.
  - **E2 – Falla en el método de pago:** si el pago es rechazado (ej. tarjeta), el sistema cancela la operación de venta y no descuenta stock ni genera factura.
  - **E3 – Cancelación de la operación:** el empleado cancela la venta en cualquier momento antes de la confirmación; el sistema descarta los datos ingresados sin afectar el stock.
- **Postcondición:** Se registra una `Venta` con sus `DetalleVenta` asociados, se descuenta el `Stock` de los productos vendidos y se genera la `Factura` correspondiente.

---

## 2. Registrar Compra

- **Actor principal:** Empleado
- **Actor secundario:** Proveedor
- **Precondición:** El empleado inició sesión en el sistema; el proveedor está dado de alta en el sistema.
- **Flujo principal:**
  1. El empleado selecciona la opción "Registrar Compra".
  2. El sistema solicita la selección del proveedor.
  3. El empleado agrega los productos recibidos indicando cantidad y precio de costo, generando un `DetalleCompra` por cada ítem.
  4. El sistema calcula el subtotal por línea y el total de la compra.
  5. El sistema invoca el caso de uso **Generar Factura**, registrando el comprobante recibido del proveedor.
  6. El sistema invoca el caso de uso **Actualizar Stock**, incrementando las cantidades de los productos comprados.
  7. El sistema confirma el registro de la compra.
- **Flujos alternativos:**
  - **A1 – Proveedor nuevo:** si el proveedor no existe, el empleado deriva al caso de uso **Gestionar Proveedor** para darlo de alta antes de continuar.
  - **A2 – Producto nuevo:** si alguno de los productos recibidos no existe en el catálogo, el empleado deriva al caso de uso **Gestionar Producto** para crearlo antes de continuar.
  - **A3 – Corrección de cantidades:** el empleado puede modificar o eliminar un `DetalleCompra` antes de confirmar.
- **Flujo de excepción:**
  - **E1 – Factura duplicada:** si el número de factura del proveedor ya fue registrado, el sistema rechaza la operación y solicita verificar el comprobante.
  - **E2 – Cancelación de la operación:** el empleado cancela la compra antes de confirmar; no se actualiza el stock ni se genera la factura.
- **Postcondición:** Se registra una `Compra` con sus `DetalleCompra` asociados, se incrementa el `Stock` de los productos recibidos y se genera la `Factura` de la compra.

---

## 3. Generar Factura

- **Actor:** Empleado *(caso de uso incluido por Registrar Venta y Registrar Compra)*
- **Precondición:** Existe una `Venta` o `Compra` confirmada con al menos un detalle asociado.
- **Flujo principal:**
  1. El sistema recopila los datos de la operación (venta o compra): fecha, ítems, cantidades, precios y totales.
  2. El sistema asigna un número de comprobante único a la `Factura`.
  3. El sistema asocia la `Factura` a la `Venta`/`Compra` y, según corresponda, al `Cliente` o `Proveedor`.
  4. El sistema calcula impuestos (si aplica) y el total final.
  5. El sistema guarda la `Factura` y la pone a disposición para su impresión o envío digital.
- **Flujos alternativos:**
  - **A1 – Reimpresión:** el empleado puede solicitar reimprimir una factura ya generada sin crear un nuevo registro.
  - **A2 – Envío por correo electrónico:** si el cliente/proveedor tiene un email cargado, el sistema ofrece enviar la factura digitalmente en lugar de imprimirla.
- **Flujo de excepción:**
  - **E1 – Error de datos fiscales:** si faltan datos obligatorios (ej. CUIT del proveedor), el sistema no genera la factura y notifica el dato faltante.
- **Postcondición:** Queda generada y almacenada una `Factura` válida, vinculada a la operación de origen (`Venta` o `Compra`).

---

## 4. Actualizar Stock

- **Actor:** Empleado *(caso de uso incluido por Registrar Venta y Registrar Compra; también puede invocarse manualmente)*
- **Precondición:** Existe una operación de venta, compra, o un pedido de ajuste manual de inventario.
- **Flujo principal:**
  1. El sistema identifica el/los `Producto`(s) afectados y la cantidad a modificar.
  2. Si el origen es una **Venta**, el sistema resta la cantidad vendida al `Stock` actual del producto.
  3. Si el origen es una **Compra**, el sistema suma la cantidad recibida al `Stock` actual del producto.
  4. El sistema registra el movimiento de stock (fecha, tipo de movimiento, cantidad, operación de origen).
  5. El sistema actualiza el stock disponible y lo persiste.
- **Flujos alternativos:**
  - **A1 – Ajuste manual de inventario:** el administrador o empleado autorizado ingresa manualmente un ajuste (ej. por rotura, vencimiento o conteo físico), indicando el motivo; el sistema actualiza el `Stock` en consecuencia.
- **Flujo de excepción:**
  - **E1 – Stock negativo:** si el ajuste generaría un valor de stock negativo, el sistema rechaza la operación y solicita revisión.
- **Postcondición:** El `Stock` del/los producto(s) queda actualizado y reflejado correctamente en el sistema.

---

## 5. Registrar Método de Pago

- **Actor:** Cliente / Empleado *(caso de uso incluido por Registrar Venta)*
- **Precondición:** Existe una venta en curso con al menos un producto agregado.
- **Flujo principal:**
  1. El sistema muestra las opciones de `MetodoDePago` disponibles (efectivo, tarjeta de débito/crédito, transferencia, billetera virtual).
  2. El empleado o cliente selecciona el método de pago.
  3. El sistema registra el `MetodoDePago` asociado a la `Venta`.
  4. Si corresponde, el sistema procesa el pago (ej. mediante posnet) y valida su confirmación.
- **Flujos alternativos:**
  - **A1 – Pago combinado:** el cliente paga con más de un método (ej. parte en efectivo y parte con tarjeta); el sistema registra ambos montos asociados a la misma venta.
  - **A2 – Pago en cuenta corriente:** si el cliente tiene cuenta habilitada, el sistema registra la venta como deuda pendiente.
- **Flujo de excepción:**
  - **E1 – Pago rechazado:** el medio de pago electrónico es rechazado; el sistema notifica el error y solicita reintentar o elegir otro método.
- **Postcondición:** Queda registrado el `MetodoDePago` (o combinación de métodos) utilizado en la `Venta`.

---

## 6. Consultar Stock

- **Actor:** Empleado, Administrador
- **Precondición:** El actor inició sesión en el sistema.
- **Flujo principal:**
  1. El actor selecciona la opción "Consultar Stock".
  2. El actor ingresa un criterio de búsqueda (nombre, código, categoría del `Producto`).
  3. El sistema busca el/los productos coincidentes y muestra su cantidad disponible en `Stock`.
- **Flujos alternativos:**
  - **A1 – Listado completo:** el actor solicita ver el stock de todos los productos sin aplicar filtro.
  - **A2 – Filtro por stock bajo:** el actor filtra los productos cuyo stock está por debajo de un mínimo definido, para planificar reposición.
- **Flujo de excepción:**
  - **E1 – Sin resultados:** si no existen productos que coincidan con el criterio, el sistema informa que no se encontraron resultados.
- **Postcondición:** El actor visualiza la información de stock solicitada (no se modifican datos).

---

## 7. Gestionar Producto

- **Actor:** Administrador
- **Precondición:** El administrador inició sesión con permisos correspondientes.
- **Flujo principal (Alta de producto):**
  1. El administrador selecciona "Gestionar Producto" → "Nuevo Producto".
  2. El sistema solicita los datos del `Producto` (nombre, código, categoría, precio de venta, precio de costo, stock inicial).
  3. El administrador ingresa los datos y confirma.
  4. El sistema valida que el código no esté duplicado.
  5. El sistema guarda el nuevo `Producto` y crea su registro asociado en `Stock`.
- **Flujos alternativos:**
  - **A1 – Modificar producto:** el administrador selecciona un producto existente, edita sus datos (precio, categoría, descripción) y el sistema guarda los cambios.
  - **A2 – Dar de baja producto:** el administrador marca un producto como inactivo/discontinuado; el sistema deja de ofrecerlo en nuevas ventas pero conserva el historial.
  - **A3 – Consultar producto:** el administrador busca un producto puntual y visualiza el detalle completo sin modificarlo.
- **Flujo de excepción:**
  - **E1 – Código duplicado:** si el código de producto ya existe, el sistema rechaza el alta y solicita un código distinto.
  - **E2 – Baja con movimientos pendientes:** si el producto tiene stock disponible o compras/ventas en curso, el sistema advierte antes de permitir la baja.
- **Postcondición:** El catálogo de `Producto` queda creado, modificado o dado de baja según la operación realizada.

---

## 8. Gestionar Cliente

- **Actor:** Administrador *(o Empleado, con alta rápida durante la venta)*
- **Precondición:** El actor inició sesión con permisos correspondientes.
- **Flujo principal (Alta de cliente):**
  1. El actor selecciona "Gestionar Cliente" → "Nuevo Cliente".
  2. El sistema solicita los datos del `Cliente` (nombre, documento/CUIT, dirección, contacto).
  3. El actor ingresa los datos y confirma.
  4. El sistema valida que el documento no esté duplicado y guarda el nuevo `Cliente`.
- **Flujos alternativos:**
  - **A1 – Modificar cliente:** el actor edita los datos de contacto o fiscales de un cliente existente.
  - **A2 – Dar de baja cliente:** el actor inactiva un cliente sin eliminar su historial de compras.
  - **A3 – Consultar historial de compras del cliente:** el actor visualiza las ventas asociadas a un cliente puntual.
- **Flujo de excepción:**
  - **E1 – Documento duplicado:** si el documento ya está registrado, el sistema rechaza el alta e informa el conflicto.
- **Postcondición:** El registro del `Cliente` queda creado, modificado o dado de baja según la operación.

---

## 9. Gestionar Proveedor

- **Actor:** Administrador
- **Precondición:** El administrador inició sesión con permisos correspondientes.
- **Flujo principal (Alta de proveedor):**
  1. El administrador selecciona "Gestionar Proveedor" → "Nuevo Proveedor".
  2. El sistema solicita los datos del `Proveedor` (razón social, CUIT, dirección, contacto, productos que suministra).
  3. El administrador ingresa los datos y confirma.
  4. El sistema valida que el CUIT no esté duplicado y guarda el nuevo `Proveedor`.
- **Flujos alternativos:**
  - **A1 – Modificar proveedor:** el administrador edita los datos de contacto o comerciales de un proveedor existente.
  - **A2 – Dar de baja proveedor:** el administrador inactiva un proveedor sin eliminar su historial de compras.
  - **A3 – Consultar historial de compras al proveedor:** el administrador visualiza las compras asociadas a un proveedor puntual.
- **Flujo de excepción:**
  - **E1 – CUIT duplicado:** si el CUIT ya está registrado, el sistema rechaza el alta e informa el conflicto.
- **Postcondición:** El registro del `Proveedor` queda creado, modificado o dado de baja según la operación.

---

## 10. Generar Reporte

- **Actor:** Administrador
- **Precondición:** El administrador inició sesión; existen movimientos de venta y/o compra registrados en el período consultado.
- **Flujo principal:**
  1. El administrador selecciona "Generar Reporte" y el tipo (ventas, compras, stock, productos más vendidos).
  2. El sistema solicita el rango de fechas u otros filtros (cliente, proveedor, categoría).
  3. El administrador confirma los parámetros.
  4. El sistema procesa la información de `Venta`, `Compra`, `DetalleVenta`, `DetalleCompra` y `Stock` según corresponda.
  5. El sistema muestra el reporte generado, con opción de exportarlo (PDF/Excel).
- **Flujos alternativos:**
  - **A1 – Exportar reporte:** el administrador exporta el resultado a un archivo en lugar de solo visualizarlo en pantalla.
  - **A2 – Reporte comparativo:** el administrador compara dos períodos (ej. mes actual vs. mes anterior).
- **Flujo de excepción:**
  - **E1 – Sin datos en el período:** si no hay movimientos en el rango solicitado, el sistema informa que no hay datos para mostrar.
- **Postcondición:** Se genera un reporte consolidado (no se modifican datos del sistema).

---

## Resumen de relaciones `<<include>>`

- **Registrar Venta** incluye → Generar Factura, Actualizar Stock, Registrar Método de Pago
- **Registrar Compra** incluye → Generar Factura, Actualizar Stock
