-- ============================================================================
-- MIGRACION A LA VERSION 2 (con roles) - SOLO si ya tenias la base de la version 1 con datos.
-- ============================================================================
-- Si NO te importan los datos de prueba, la forma mas simple es borrar la base y dejar que la app la recree:
--     DROP DATABASE tienda_carrito;
--
-- Si queres conservar tus datos:
-- 1) Al iniciar, Hibernate (ddl-auto=update) agrega solo la columna usuarios.rol con DEFAULT 'CLIENTE':
--    todos los usuarios existentes pasan a ser CLIENTE.
-- 2) Al iniciar, la aplicacion promueve a ADMIN al usuario llamado "admin" si no existe ningun administrador
--    (DataLoader / UsuarioService.asegurarAdministrador). Si preferis hacerlo vos a mano:

USE tienda_carrito;

-- Ver como quedaron los roles:
SELECT id, nombre, rol, eliminado FROM usuarios;

-- Convertir en administrador a un usuario puntual:
UPDATE usuarios SET rol = 'ADMIN' WHERE nombre = 'admin' AND eliminado = 0;

-- Convertir en cliente a un usuario puntual:
-- UPDATE usuarios SET rol = 'CLIENTE' WHERE nombre = 'otro_usuario';
