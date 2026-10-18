-- Script OPCIONAL. La aplicacion crea la base y las tablas sola (createDatabaseIfNotExist + ddl-auto=update).
-- Usalo solo si preferis crear la base manualmente o con un usuario dedicado.

CREATE DATABASE IF NOT EXISTS tienda_carrito
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;

-- Usuario dedicado (opcional, recomendado fuera del entorno de practica):
-- CREATE USER 'tienda'@'localhost' IDENTIFIED BY 'cambiar_esta_clave';
-- GRANT ALL PRIVILEGES ON tienda_carrito.* TO 'tienda'@'localhost';
-- FLUSH PRIVILEGES;
--
-- Luego ejecutar la app con:  DB_USER=tienda DB_PASSWORD=cambiar_esta_clave

-- Consultas utiles para verificar los datos luego de probar la aplicacion:
-- USE tienda_carrito;
-- SELECT id, nombre, rol, eliminado FROM usuarios;
-- SELECT id, nombre, precio, proveedor_id, eliminado FROM articulos;
-- SELECT id, nombre, mime, LENGTH(contenido) AS bytes, articulo_id, eliminado FROM imagenes;
-- SELECT id, total, usuario_id, eliminado FROM carritos;
-- SELECT id, carrito_id, articulo_id, eliminado FROM detalles;
