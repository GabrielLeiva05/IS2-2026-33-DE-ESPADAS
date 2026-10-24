# Guía de Integración de JWT (JSON Web Token) - Tinder-REST

Este documento detalla la arquitectura, los componentes implementados, el flujo de ejecución y las instrucciones paso a paso para probar la autenticación y autorización mediante **JWT** en el proyecto **Tinder-REST** utilizando **Spring Boot 3** y **Spring Security**.

---

## 1. Arquitectura y Componentes Creados

Para mantener la separación de responsabilidades y la arquitectura en tres capas (**Controller - Service - Repository**), se implementaron y modificaron los siguientes archivos:

### A. Dependencias (`pom.xml`)
Se agregaron las bibliotecas oficiales de **JJWT (`0.11.5`)**:
* `jjwt-api`: Interfaz principal para la generación y parseo de tokens.
* `jjwt-impl` y `jjwt-jackson`: Implementación en tiempo de ejecución para serialización/deserialización JSON.

### B. Configuración de Seguridad y Beans (`sprint.tinder.config`)
Para evitar **dependencias circulares** en el contenedor de Spring, la configuración se dividió en dos clases:

1. **`ApplicationConfig.java`**: Declara los beans de bajo nivel:
   * `PasswordEncoder`: Utiliza `BCryptPasswordEncoder` para encriptar contraseñas de forma unidireccional.
   * `AuthenticationProvider`: Configura `DaoAuthenticationProvider` recibiendo `UserDetailsService` por parámetro en el método `@Bean`.
   * `AuthenticationManager`: Maneja la autenticación de credenciales en el inicio de sesión.

2. **`SecurityConfig.java`**: Define las cadenas de filtros de seguridad (`SecurityFilterChain`) utilizando `@Order`:
   * **API REST (`/api/**`)**: Configurada como `STATELESS` (sin sesiones HTTP). Aplica `JwtAuthenticationFilter` antes de `UsernamePasswordAuthenticationFilter`.
   * **Vistas Web (Thymeleaf)**: Mantiene el soporte basado en sesiones HTTP e integración con formularios HTML (`formLogin`).

3. **`JwtService.java`**: Servicio utilitario encargado de:
   * Firmar y generar tokens JWT con algoritmo `HS256`.
   * Establecer tiempo de expiración (24 horas por defecto).
   * Extraer *claims* (como el correo del usuario) y validar la autenticidad del token.

4. **`JwtAuthenticationFilter.java`**: Filtro que intercepta cada petición HTTP:
   * Extrae el encabezado `Authorization: Bearer <TOKEN>`.
   * Valida el token con `JwtService`.
   * Carga el usuario en el `SecurityContextHolder` para otorgar acceso si el token es válido.

### C. DTOs (`sprint.tinder.dtos`)
* **`RegisterRequest.java`**: Transporta `nombre`, `mail` y `clave` para la creación de cuenta.
* **`AuthRequest.java`**: Transporta `mail` y `clave` para el inicio de sesión.
* **`AuthResponse.java`**: Devuelve el token firmado y el tipo de esquema (`Bearer`).

### D. Capa de Negocio y Controladores (`sprint.tinder.services` y `controllers`)
* **`AuthService.java`**: 
  * En `/register`: Verifica duplicados por correo, encripta la clave con BCrypt, persiste la entidad en base de datos y retorna un JWT.
  * En `/login`: Valida las credenciales mediante `AuthenticationManager` y retorna el JWT.
* **`AuthApiController.java`**: Expone los endpoints REST bajo la ruta `/api/auth`.

---

## 2. Flujo de Funcionamiento

```
┌──────────┐            ┌────────────────────┐            ┌──────────────────────┐            ┌──────────────┐
│ Cliente  │            │ AuthApiController  │            │     AuthService      │            │  JwtService  │
└────┬─────┘            └─────────┬──────────┘            └──────────┬───────────┘            └──────┬───────┘
     │                            │                                  │                               │
     │ 1. POST /api/auth/register │                                  │                               │
     ├───────────────────────────>│                                  │                               │
     │                            │ 2. register(RegisterRequest)     │                               │
     │                            ├─────────────────────────────────>│                               │
     │                            │                                  │ 3. Guardar usuario en BD      │
     │                            │                                  │    y generar Token JWT        │
     │                            │                                  ├──────────────────────────────>│
     │                            │ 4. Retorna AuthResponse (Token)  │<──────────────────────────────┤
     │<───────────────────────────┼──────────────────────────────────┤                               │
     │                            │                                  │                               │
     │ 5. GET /api/mascotas       │                                  │                               │
     │    (Header: Bearer Token)  │                                  │                               │
     ├───────────────────────────>│ [JwtAuthenticationFilter evalúa token y valida SecurityContext]  │
     │                            │                                  │                               │
     │ 6. Respuesta JSON (200 OK) │                                  │                               │
     │<───────────────────────────┤                                  │                               │
```

---

## 3. Guía de Pruebas con Postman

**URL Base del Servidor:** `http://localhost:9000`

---

### Paso 1: Registrar un nuevo usuario (`/api/auth/register`)

1. En Postman, crea una petición con el verbo **`POST`**.
2. **URL:** `http://localhost:9000/api/auth/register`
3. Ve a la pestaña **Headers** y confirma:
   * **Key:** `Content-Type` | **Value:** `application/json`
4. Ve a la pestaña **Body**, marca la opción **raw** y selecciona el formato **JSON**.
5. Ingresa el JSON con los datos del usuario:
   ```json
   {
     "nombre": "Hola",
     "mail": "hola@test.com",
     "clave": "123456"
   }
   ```
6. Presiona **Send**.
7. **Respuesta esperada (`200 OK`):**
   ```json
   {
     "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJnYWJyaWVsQHRlc3QuY29tI...",
     "type": "Bearer"
   }
   ```

---

### Paso 2: Iniciar sesión (`/api/auth/login`)

1. Crea una petición **`POST`**.
2. **URL:** `http://localhost:9000/api/auth/login`
3. Configura el **Body** en modo **raw** -> **JSON**:
   ```json
   {
     "mail": "hola@test.com",
     "clave": "123456"
   }
   ```
4. Presiona **Send**.
5. **Respuesta esperada (`200 OK`):** Copia el valor del campo `token`.

---

### Paso 3: Probar Endpoints Protegidos (`/api/**`)

Para acceder a recursos protegidos (ejemplo: `/api/mascotas` o rutas REST equivalentes):

1. Crea una petición con el verbo HTTP adecuado de tu controlador (ej. **`GET`** o **`POST`**).
2. **URL:** `http://localhost:9000/api/mascotas`
3. Ve a la pestaña **Authorization**:
   * **Type:** Selecciona **Bearer Token**.
   * **Token:** Pega el token JWT obtenido en los pasos anteriores.
4. Presiona **Send**.
5. **Respuesta esperada (`200 OK`):** Retorna la información solicitada en formato JSON.

*(Alternativamente, puedes agregar manualmente en la pestaña **Headers** la clave `Authorization` con el valor `Bearer <TU_TOKEN>`).*

---

### Paso 4: Validar Rechazo de Seguridad (Prueba de Fallo)

1. En la misma petición al endpoint protegido (`/api/mascotas`), ve a **Authorization** y selecciona **No Auth** (o altera un carácter del token).
2. Presiona **Send**.
3. **Respuesta esperada (`401 Unauthorized`):** Demuestra que la API no permite acceso sin un token válido.

---

## 4. Diagnóstico de Errores Comunes

| Código HTTP | Causa Probable | Solución |
| :--- | :--- | :--- |
| **`401 Unauthorized`** | Token expirado, ausente o manipulado. | Iniciar sesión nuevamente en `/api/auth/login` para obtener un token válido. |
| **`403 Forbidden`** | El usuario no posee el rol necesario (ej. solicitar `/api/admin/**` con un usuario estándar). | Asignar el rol `ROLE_ADMIN` al usuario en la base de datos o probar con un usuario administrador. |
| **`405 Method Not Allowed`** | El método HTTP usado en Postman (`GET`, `POST`, `PUT`, `DELETE`) no coincide con la anotación (`@GetMapping`, `@PostMapping`, etc.) del controlador. | Verificar las anotaciones del controlador Spring y seleccionar el verbo correcto en Postman. |