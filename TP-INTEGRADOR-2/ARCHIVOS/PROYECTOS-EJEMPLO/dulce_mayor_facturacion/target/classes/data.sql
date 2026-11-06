INSERT INTO cliente (id, razon_social, cuit, domicilio, localidad, condicion_iva) 
VALUES (1, 'Kiosco Don Juan S.R.L.', '30-71234567-9', 'San Martin 450', 'Mendoza', 'Responsable Inscripto');

INSERT INTO pedido (id, cliente_id, fecha, numero_factura, subtotal, iva, total, estado, medio_pago) 
VALUES (1, 1, CURRENT_TIMESTAMP(), 'FC-0001-00000142', 25000.00, 5250.00, 30250.00, 'FACTURADO', 'Transferencia');

INSERT INTO detalle_pedido (id, pedido_id, producto, cantidad, precio_unitario, subtotal) 
VALUES (1, 1, 'Chupetines Pico Dulce (Display x 48u)', 5, 3000.00, 15000.00);

INSERT INTO detalle_pedido (id, pedido_id, producto, cantidad, precio_unitario, subtotal) 
VALUES (2, 1, 'Alfajores Jorgito Dulce de Leche (Caja x 36u)', 2, 5000.00, 10000.00);
