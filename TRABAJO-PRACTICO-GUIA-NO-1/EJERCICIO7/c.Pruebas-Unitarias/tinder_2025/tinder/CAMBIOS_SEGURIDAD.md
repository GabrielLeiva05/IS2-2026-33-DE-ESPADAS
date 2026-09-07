# Seguridad aplicada al Tinder de Mascotas

## 1. Encriptación de contraseñas (BCrypt)

## 2. Roles (USUARIO / ADMIN)
- Todo usuario que se registra por el formulario normal entra con rol `USUARIO`.
- `config/DataInitializer.java` (nuevo): al levantar la app, si no existe, crea un usuario ADMIN de prueba:
  - **mail:** `admin@tinder.com`
  - **clave:** `admin123`

## 3. Auditoría (Hibernate Envers)

