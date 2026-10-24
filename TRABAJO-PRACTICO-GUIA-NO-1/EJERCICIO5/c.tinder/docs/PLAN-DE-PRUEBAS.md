# Plan de pruebas - C.Tinder

## 1. Objetivo

Verificar que las reglas de usuarios, mascotas, votos y navegación web funcionen de acuerdo con el comportamiento observable de la aplicación, detectando regresiones antes de cada entrega.

## 2. Alcance

- Registro, validación, autenticación y baja de usuarios.
- Alta, modificación, baja, rehabilitación y búsqueda de mascotas.
- Votación y respuesta a votos.
- Navegación pública y redirecciones de sesión.
- Persistencia JPA y carga del contexto Spring.
- Exclusiones: entrega real de correo, pruebas de carga y pruebas contra servicios externos.

## 3. Estrategia y tipos de prueba

| Tipo | Objetivo | Ejemplo aplicado | Herramienta |
|---|---|---|---|
| Unitaria | Aislar una regla de negocio y sus errores | `UsuarioService.validar` y `login` con repositorios simulados | JUnit 5 + Mockito |
| Web/controlador | Verificar rutas, vistas, estados HTTP y redirecciones | `GET /inicio` sin sesión redirige a `/login` | MockMvc standalone |
| Integración | Comprobar que los componentes Spring y JPA colaboran | Arranque completo de la aplicación y conexión a PostgreSQL | `@SpringBootTest` |
| Persistencia | Validar consultas derivadas y JPQL contra el esquema real | Consultas de `MascotaRepository` y `UsuarioRepository` | `@DataJpaTest` (fase siguiente) |
| Aceptación | Confirmar flujos completos desde la perspectiva del usuario | Registro -> login -> alta de mascota -> voto | Escenario manual o E2E (fase siguiente) |
| No funcional | Comprobar rendimiento, seguridad y compatibilidad | Tiempo de respuesta, CSRF, permisos y navegadores | JMeter, OWASP ZAP y Playwright (fase siguiente) |

## 4. Casos prioritarios

| ID | Caso | Resultado esperado | Prioridad |
|---|---|---|---|
| UT-01 | Validar usuario con nombre vacío | Lanza `ErrorService` con mensaje de nombre obligatorio | Alta |
| UT-02 | Login con correo rodeado de espacios y clave válida | Normaliza el correo y devuelve el usuario | Alta |
| UT-03 | Login de usuario dado de baja | Lanza `ErrorService` | Alta |
| WEB-01 | Acceder a `/inicio` sin sesión | Redirige a `/login` | Alta |
| WEB-02 | Cerrar sesión | Invalida la sesión y redirige a `/login` | Alta |
| INT-01 | Levantar el contexto completo | Spring crea beans, repositorios y esquema sin errores | Alta |
| INT-02 | Consultar mascotas activas por usuario | Devuelve solo registros con `baja IS NULL` | Media |
| ACC-01 | Registrar usuario con claves distintas | Se muestra el formulario con el error y no se confirma el registro | Alta |
| ACC-02 | Votar una mascota propia contra otra | Persiste el voto y envía la notificación | Alta |

## 5. Datos y condiciones

- Ejecutar las pruebas con Java 17 o superior y Maven Wrapper.
- Las pruebas de unidad y web no necesitan una base de datos.
- La prueba de contexto actual necesita PostgreSQL accesible en `localhost:5432`, la base `tinder-mascotas` y las credenciales configuradas en `application.properties` o en `.env`.
- Usar datos aislados por ejecución; el esquema de desarrollo se recrea porque `spring.jpa.hibernate.ddl-auto=create`.
- No utilizar cuentas, contraseñas ni correos reales.

## 6. Ejecución y criterios de salida

```powershell
.\mvnw.cmd test
```

La iteración se considera aprobada cuando todas las pruebas terminan sin fallos ni errores, no quedan casos críticos abiertos y los casos UT, WEB e INT prioritarios tienen evidencia en los reportes de Surefire.

## 7. Evolución propuesta

1. Completar pruebas unitarias para `MascotaService`, `VotoService` y `ZonaService`.
2. Añadir `@DataJpaTest` con una base de datos de prueba aislada para las consultas de repositorio.
3. Añadir pruebas de aceptación automatizadas para registro, login y voto.
4. Incorporar pruebas de seguridad, rendimiento y compatibilidad en el pipeline de integración continua.