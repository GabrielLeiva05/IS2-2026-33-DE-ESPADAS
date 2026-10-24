# Guía de Prueba del Sistema de Transferencias

Este documento describe el procedimiento para realizar una prueba de transferencia entre cuentas y verificar la actualización de los saldos en la base de datos H2.

---

## 1. Ejecutar la Transferencia en Postman

Abre **Postman** (o tu navegador) y envía una solicitud HTTP a la siguiente ruta:

```http
http://localhost:8080/accounts/transfer/1/2/5
```

### Significado de los parámetros:
* **`1` (Cuenta origen):** ID del usuario/cuenta que envía el dinero.
* **`2` (Cuenta destino):** ID del usuario/cuenta que recibe el dinero.
* **`5` (Monto):** Cantidad de dinero (pesos) a transferir.

En este ejemplo, el **Usuario 1** le transfiere **5 pesos** al **Usuario 2**.

---

## 2. Verificar el Resultado en la Base de Datos (H2)

1. Abre tu navegador e ingresa a la consola de H2:  
   [http://localhost:8080/h2-console](http://localhost:8080/h2-console)

2. Completa los datos de conexión por defecto:
   * **JDBC URL:** `jdbc:h2:mem:testdb`
   * **User Name:** `a`
   * **Password:** `a`

3. Una vez conectado, ejecuta la siguiente consulta SQL para verificar los saldos actualizados:

```sql
SELECT * FROM account;
```

Deberías observar el saldo de la cuenta con `id = 1` descontado en 5 pesos y el de la cuenta con `id = 2` incrementado en 5 pesos.