# Pagos con Mercado Pago: configuración y prueba paso a paso

Esta guía explica cómo obtener cada credencial y cómo probar de punta a punta los dos cobros con
**Checkout API** (sin redirigir al comprador):

1. **QR dinámico** generado por Mercado Pago, enviado por correo al cliente cuando el preventista
   confirma que va a pagar de esa forma, y también visible en la app del preventista.
2. **Tarjeta** tokenizada en el navegador (Bricks), cobrada en la misma pantalla.

> Todo se configura en `backend/.env` (y en `movil/preventista/.env` para la public key). No hace
> falta definir variables de entorno del sistema operativo.

---

## 0. Cómo funciona el flujo

```
Preventista confirma el pedido y elige MERCADO_PAGO
        │
        ├─► La API crea una orden con QR en la caja (POS) del vendedor   → qr_data
        ├─► Genera la imagen PNG del QR (ZXing) y la incrusta en el correo (CID)
        └─► Responde con qrContenido + qrImagenBase64 (la app lo muestra)
                │
Cliente escanea / paga ──► Mercado Pago ──► webhook firmado a /api/pagos/mercadopago/notificacion
                                                │
                                    La API consulta GET /v1/payments/{id}
                                    → si está "approved" y el external_reference coincide:
                                      acredita el pago, consume el stock y emite el comprobante
```

La tarjeta sigue el mismo cierre: el navegador tokeniza, la API crea `POST /v1/payments` y si el
pago queda `approved` se acredita en la misma transacción.

---

## 1. Crear la aplicación en Mercado Pago

1. Entrá a <https://www.mercadopago.com.ar/developers/panel/app> con tu cuenta.
2. **Creá una aplicación** (por ejemplo `Dulce Mayor - TP`).
3. Elegí el producto **Checkout API** (antes "Checkout Transparente"). *No* elijas Checkout Pro:
   este proyecto no usa preferencias ni redirecciones.
4. En la aplicación, abrí **Credenciales**.

Vas a necesitar cuatro datos:

| Dato | Dónde está | Variable del `.env` |
|---|---|---|
| Access Token **de prueba** | Credenciales → *Credenciales de prueba* | `MERCADOPAGO_ACCESS_TOKEN` |
| Public Key **de prueba** | Credenciales → *Public Key* (prueba) | `VITE_MP_PUBLIC_KEY` (app preventista) |
| User ID (Collector ID) | Credenciales → *User ID* | `MERCADOPAGO_USER_ID` |
| Clave secreta del webhook | Webhooks → *Clave secreta* | `MERCADOPAGO_WEBHOOK_SECRET` |

> Para producción repetís lo mismo con *Credenciales de producción*. **Nunca** subas el `.env` al
> repositorio: ya está en `.gitignore`.

### Verificar que el token y el User ID son correctos

```bash
curl -s https://api.mercadopago.com/users/me \
  -H "Authorization: Bearer TU_ACCESS_TOKEN"
```

La respuesta trae `id` (ese es tu `MERCADOPAGO_USER_ID`) y el `site_id` (`MLA` para Argentina).

---

## 2. Crear la caja (POS) donde se genera el QR

El QR dinámico necesita una **caja** de Mercado Pago. El `external_id` que elijas es el valor que
va en `MERCADOPAGO_POS_ID`.

```bash
curl -s -X POST https://api.mercadopago.com/pos \
  -H "Authorization: Bearer TU_ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
        "name": "Caja Dulce Mayor",
        "external_id": "CAJA01",
        "fixed_amount": false,
        "category": "621102",
        "notification_url": "https://TU-DOMINIO-PUBLICO/api/pagos/mercadopago/notificacion"
      }'
```

- `external_id: "CAJA01"` → `MERCADOPAGO_POS_ID=CAJA01` (podés usar otro nombre, debe coincidir).
- La respuesta incluye el `id` interno de la caja; **no** se usa en la configuración, pero sirve
  para verificarla con `GET /pos/{id}`.
- `fixed_amount: false` permite QR de monto variable (cada pedido tiene su importe).

Si ya existe una caja con ese `external_id`, la API de Mercado Pago responde con error de
duplicado: cambiá el `external_id` o reutilizá la existente.

---

## 3. Configurar el webhook y la clave secreta

1. En la aplicación → **Webhooks** → *Configurar notificaciones*.
2. **URL de producción**: `https://TU-DOMINIO-PUBLICO/api/pagos/mercadopago/notificacion`
3. **Eventos**: marcá **Pagos** (`payment`).
4. Guardá y copiá la **Clave secreta**: es `MERCADOPAGO_WEBHOOK_SECRET`.

Cómo se valida la firma (ya implementado en `ClienteMercadoPago.firmaValida`):

```
manifiesto = "id:" + data.id + ";request-id:" + x-request-id + ";ts:" + ts + ";"
v1         = HMAC_SHA256(manifiesto, MERCADOPAGO_WEBHOOK_SECRET) en hexadecimal
```

El endpoint compara `v1` con el encabezado `x-signature` (`ts=...,v1=...`) en tiempo constante y
**responde 401** si no coincide.

### Exponer tu localhost para el webhook

Mercado Pago no puede llamar a `localhost`. Usá un túnel mientras probás:

```bash
# Opción A: ngrok
ngrok http 8080
# Copiá la URL https://xxxx.ngrok-free.app

# Opción B: cloudflared
cloudflared tunnel --url http://localhost:8080
```

Después completá en `backend/.env`:

```properties
MERCADOPAGO_NOTIFICACION_URL=https://xxxx.ngrok-free.app/api/pagos/mercadopago/notificacion
```

y usá la misma URL en la **caja** y en el **webhook** de Mercado Pago.

---

## 4. Completar los `.env`

### `backend/.env`

```properties
# --- Credenciales de prueba ---
MERCADOPAGO_ACCESS_TOKEN=TEST-0000000000000000-000000-aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa
MERCADOPAGO_USER_ID=123456789
MERCADOPAGO_WEBHOOK_SECRET=bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb

# --- Caja y rutas (los valores por defecto ya sirven) ---
MERCADOPAGO_POS_ID=CAJA01
MERCADOPAGO_BASE_URL=https://api.mercadopago.com
MERCADOPAGO_RUTA_PAGOS=/v1/payments
MERCADOPAGO_RUTA_ORDENES_QR=/instore/orders/qr/seller/collectors/{userId}/pos/{posId}/qrs

# --- Webhook público (ngrok/cloudflared o dominio real) ---
MERCADOPAGO_NOTIFICACION_URL=https://xxxx.ngrok-free.app/api/pagos/mercadopago/notificacion

# --- Vigencia de la reserva de stock para Mercado Pago ---
PAGOS_EXPIRACION_HABILITADA=true
```

### `movil/preventista/.env`

```properties
# Public Key de prueba: permite tokenizar tarjetas en el navegador
VITE_MP_PUBLIC_KEY=TEST-00000000-0000-0000-0000-000000000000
```

> Si falta `VITE_MP_PUBLIC_KEY`, la app muestra el QR pero avisa que no puede tokenizar tarjetas.
> El cobro por QR sigue funcionando.

### Correo (para ver el QR en el buzón)

Sin SMTP el correo queda como `SIMULADO` (visible en **Panel → Notificaciones**), pero **la imagen
del QR se genera igual**. Para envío real agregá:

```properties
MAIL_HOST=smtp.tu-proveedor.com
MAIL_PORT=587
MAIL_USERNAME=tu-usuario
MAIL_PASSWORD=tu-clave
MAIL_REMITENTE=no-responder@tudominio.com
```

---

## 5. Reiniciar y verificar la configuración

```bash
cd backend && mvnw.cmd spring-boot:run
cd movil/preventista && npm run dev
```

Comprobaciones rápidas:

- La API arranca sin avisos de Mercado Pago.
- Con el preventista, al iniciar un cobro **MERCADO_PAGO** el pedido **no** se abre en otra
  pestaña: aparece el QR en la pantalla de cobro.
- El correo al cliente (real o simulado) contiene la imagen del QR.

Errores de configuración y su mensaje:

| Mensaje | Causa |
|---|---|
| `Mercado Pago no está configurado en el servidor: falta el access token` | `MERCADOPAGO_ACCESS_TOKEN` vacío |
| `no está configurado para QR: faltan MERCADOPAGO_USER_ID o MERCADOPAGO_POS_ID` | Falta el vendedor o la caja |
| `La firma de la notificación de Mercado Pago es inválida` | `MERCADOPAGO_WEBHOOK_SECRET` distinto del panel |
| `Mercado Pago rechazó la operación (código 401)` | Token vencido o de otro ambiente |
| `Mercado Pago rechazó la operación (código 404)` | La caja `MERCADOPAGO_POS_ID` no existe |

---

## 6. Probar el pago con QR

### 6.1 Generar el QR desde la aplicación

1. Ingresá a la app del preventista con una cuenta cuyo `correo` sea el **email de contacto del
   comercio** (así el cliente recibe el correo).
2. Cargá un pedido y **Confirmar pedido**.
3. En **Pedidos → Cobrar → Mercado Pago**: se crea el pago, aparece el QR y se envía el correo.

Podés ver el contenido y la imagen del QR en la API:

```bash
curl -s http://localhost:8080/api/preventista/pedidos/{idPedido}/pagos \
  -H "Authorization: Bearer TOKEN_DEL_PREVENTISTA"
```

Campos relevantes: `qrContenido` (EMVCo), `qrImagenBase64` (`data:image/png;base64,...`) y
`referenciaOperacion` (el `external_reference` que usa Mercado Pago para conciliar).

### 6.2 Pagar el QR (dos formas)

**A. Con la app de Mercado Pago y una cuenta de prueba**

1. En el panel de Mercado Pago: **Cuentas de prueba → Crear cuenta de prueba** (país Argentina).
   Vas a obtener un usuario de prueba con su correo y contraseña.
2. Abrí la app de Mercado Pago en el celular e iniciá sesión con esa cuenta.
3. Escaneá el QR (desde la pantalla o impreso desde el correo) y confirmá el pago con **dinero en
   cuenta**.

> Un QR generado con credenciales de **prueba** solo se puede pagar con cuentas de prueba.

**B. Simular el pago por API (sin celular)**

Con el **access token del comprador de prueba** (credenciales de esa cuenta de prueba), creá un
pago usando la misma `referenciaOperacion` del pago interno:

```bash
curl -s -X POST https://api.mercadopago.com/v1/payments \
  -H "Authorization: Bearer ACCESS_TOKEN_DEL_COMPRADOR_DE_PRUEBA" \
  -H "Content-Type: application/json" \
  -H "X-Idempotency-Key: demo-qr-1" \
  -d '{
        "transaction_amount": 270.00,
        "payment_method_id": "account_money",
        "payer": { "email": "comprador.prueba@testuser.com" },
        "external_reference": "REFERENCIA_OPERACION_DEL_PAGO",
        "description": "Pedido demo Dulce Mayor"
      }'
```

- `transaction_amount` debe coincidir con el importe del pedido.
- `external_reference` **debe** ser exactamente el `referenciaOperacion` del pago interno.
- La respuesta trae `id` y `status` (`approved` si salió bien). Guardá el `id`: es el que usa el
  webhook.

### 6.3 Verificar la acreditación

**Con webhook (recomendado).** Si `MERCADOPAGO_NOTIFICACION_URL` es público, Mercado Pago envía la
notificación sola. En el log de la API vas a ver:

```
Notificación de Mercado Pago sin pago asociado   ← external_reference no coincide
Pago {id} del pedido {id} expirado                ← llegó después del vencimiento
```

Y en el pedido: estado `COMPLETADO`, stock consumido y **comprobante emitido**. En el panel:
**Pedidos → detalle** muestra el pago como `ACREDITADO`.

**Sin webhook (acreditación manual).** Si no podés exponer localhost, tomá el `id` del pago que
devolvió el paso anterior y acreditalo desde el panel:

1. **Panel (8081) → Pedidos → abrí el pedido.**
2. En el intento de Mercado Pago pendiente, escribí el **ID del pago** en *Referencia* y presioná
   **Acreditar**.

El sistema **no confía** en ese dato: vuelve a consultar `GET /v1/payments/{id}`, verifica que esté
`approved` y que el `external_reference` coincida. Si no, rechaza la acreditación.

### 6.4 Probar el vencimiento

El pago vence a las 24 h (configurable con `PAGOS_TRANSFERENCIA_VIGENCIA_HORAS` /
`app.pagos.mercadopago.vigencia-horas`). Para no esperar, en una prueba con H2 podés forzar la fecha
en la base, o esperar a que la tarea programada (`PAGOS_EXPIRACION_INTERVALO_MS`) expire el pago:
el pedido se cancela y la reserva de stock se libera.

---

## 7. Probar el pago con tarjeta (Checkout API, sin redirección)

1. En la app del preventista, iniciá el cobro con **Mercado Pago**.
2. En el panel **Pagar con tarjeta** completá los datos con una **tarjeta de prueba**:

| Tarjeta | Número | CVV | Vencimiento | Resultado |
|---|---|---|---|---|
| Mastercard | `5031 7557 3453 0604` | `123` | `11/30` | Aprobado |
| Visa | `4509 9535 6623 3704` | `123` | `11/30` | Aprobado |
| Mastercard (rechazo) | `5031 4332 1540 6351` | `123` | `11/30` | Rechazado por fondos insuficientes |

- Titular: cualquier nombre. DNI: `12345678` (o `123456789` según el largo que acepte el formulario).
- En **producción** estas tarjetas no funcionan: usá una tarjeta real.

Qué hace la app:

1. Tokeniza la tarjeta con `@mercadopago/sdk-js` (el número **nunca** llega al backend).
2. Envía el token a `POST /api/preventista/pedidos/{idPedido}/pagos/{idPago}/tarjeta`.
3. La API crea `POST /v1/payments` con `X-Idempotency-Key`.
4. Si el pago queda `approved`, acredita: consume stock, emite comprobante y el pedido pasa a
   `COMPLETADO`. Si queda `rejected`, el pago interno sigue `PENDIENTE` y se registra el motivo en
   `observacion`.

---

## 8. Verificar la firma del webhook a mano

Útil para comprobar que la clave secreta está bien copiada:

```bash
DATA_ID=123456789
REQUEST_ID=req-abc-123
TS=1704908010
SECRET=TU_CLAVE_SECRETA

MANIFIESTO="id:${DATA_ID};request-id:${REQUEST_ID};ts:${TS};"
V1=$(printf '%s' "$MANIFIESTO" | openssl dgst -sha256 -hmac "$SECRET" -hex | sed 's/^.* //')

curl -i -X POST "http://localhost:8080/api/pagos/mercadopago/notificacion?data.id=${DATA_ID}&type=payment" \
  -H "x-request-id: ${REQUEST_ID}" \
  -H "x-signature: ts=${TS},v1=${V1}"
```

- **200**: la firma es válida (si el pago no existe, simplemente no hace nada).
- **401**: firma inválida → revisá `MERCADOPAGO_WEBHOOK_SECRET` y que `ts` sea el mismo del cálculo.

---

## 9. Checklist rápido

- [ ] Aplicación creada con producto **Checkout API**.
- [ ] `MERCADOPAGO_ACCESS_TOKEN`, `MERCADOPAGO_USER_ID`, `MERCADOPAGO_WEBHOOK_SECRET` en `backend/.env`.
- [ ] Caja creada con `external_id` igual a `MERCADOPAGO_POS_ID`.
- [ ] `MERCADOPAGO_NOTIFICACION_URL` público (ngrok/cloudflared) y cargado en el webhook de MP.
- [ ] `VITE_MP_PUBLIC_KEY` en `movil/preventista/.env` (solo para tarjeta).
- [ ] Cuenta de prueba de comprador creada para pagar el QR.
- [ ] Pedido confirmado con medio **Mercado Pago** → QR visible en la app y en el correo.
- [ ] Pago aprobado → pedido `COMPLETADO`, stock consumido, comprobante disponible.

---

## 10. Notas y límites

- **Sin credenciales** la API responde «no configurado» y **no simula** acreditaciones.
- Los QR generados con credenciales de prueba **solo** se pagan con cuentas de prueba.
- El enlace de Checkout Pro (`init_point`) ya no se usa: cualquier referencia a "abrir el pago" en
  la app fue reemplazada por el QR y el formulario de tarjeta.
- La notificación se valida con firma HMAC **y** con una consulta server-to-server; nunca se
  acredita por el cuerpo del webhook.
- Si usás un túnel gratuito, la URL cambia cada vez: actualizala en el `.env`, en la caja y en el
  webhook de Mercado Pago.
