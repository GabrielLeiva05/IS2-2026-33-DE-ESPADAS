# Biblioteca · Sistema cliente–servidor con Spring Boot, REST y RestTemplate

Implementación completa del diagrama de clases de diseño solicitado (`docs/diagrama-de-clases.png`) como
**dos aplicaciones Spring Boot independientes** que se comunican únicamente mediante una **API REST**:

| Aplicación | Carpeta | Puerto | Rol |
|---|---|---|---|
| **Servidor** | `biblioteca-server` | `8080` | API REST + persistencia (Spring Web, Spring Data JPA, H2/MySQL) |
| **Cliente**  | `biblioteca-client` | `8081` | Sitio HTML (Thymeleaf) que consume la API con **RestTemplate** y maneja la vista con **DTO's** |

```
┌───────────────┐   HTML   ┌─────────────────────────┐  HTTP + JSON   ┌──────────────────────────┐   JPA   ┌────────┐
│   Navegador   │ ───────► │  biblioteca-client :8081│ ─────────────► │ biblioteca-server :8080  │ ──────► │ H2 /   │
│               │ ◄─────── │  Controller → Service   │ ◄───────────── │ Controller → Service →   │ ◄────── │ MySQL  │
└───────────────┘          │  → ApiClient(RestTemplate)  DTO's       │ Repository → Entity      │         └────────┘
                           └─────────────────────────┘   /api/v1/...  └──────────────────────────┘
```

---

## 1. Requisitos y ejecución rápida

* **JDK 17 o superior** (el proyecto compila con `java.version=17`; funciona también en JDK 21).
* **Conexión a Internet** la primera vez (Maven descarga dependencias) y para que el navegador cargue Bootstrap y las
  tipografías desde CDN. No hace falta instalar Maven: cada proyecto incluye el **Maven Wrapper** (`mvnw` / `mvnw.cmd`).
* No hace falta instalar ninguna base de datos: por defecto se usa **H2 en memoria**.

Abra **dos terminales**.

**Terminal 1 – Servidor (primero):**
```bash
cd biblioteca-server
./mvnw spring-boot:run          # Windows: mvnw.cmd spring-boot:run
```

**Terminal 2 – Cliente:**
```bash
cd biblioteca-client
./mvnw spring-boot:run          # Windows: mvnw.cmd spring-boot:run
```

Luego abra **http://localhost:8081** en el navegador.

Otras URL útiles:

| URL | Descripción |
|---|---|
| http://localhost:8081 | Sitio web (cliente) |
| http://localhost:8080/api/v1/personas | La API cruda (JSON) |
| http://localhost:8080/h2-console | Consola de la base H2 (JDBC URL `jdbc:h2:mem:bibliotecadb`, usuario `sa`, sin contraseña) |

> Si el servidor no está en ejecución, el cliente lo detecta y muestra un mensaje claro
> («No se pudo conectar con el servidor…») en lugar de un error técnico.

**Compilar sin ejecutar tests:** `./mvnw clean package -DskipTests` (genera `target/*.jar`; se ejecuta con `java -jar target/<nombre>.jar`).
**Ejecutar tests:** `./mvnw test` (el servidor incluye una prueba de integración del recorrido completo de la API).

En IntelliJ IDEA / Eclipse / STS: *Open → pom.xml* de cada carpeta y ejecutar `BibliotecaServerApplication` y `BibliotecaClientApplication`.

---

## 2. Del diagrama de clases al código

Diagrama original: [`docs/diagrama-de-clases.png`](docs/diagrama-de-clases.png)

### 2.1 Clases y atributos (respetados tal cual el diagrama)

| Clase | Atributos |
|---|---|
| `Domicilio` | `id: Long`, `calle: String`, `numero: int` |
| `Persona` | `id: Long`, `nombre: String`, `apellido: String`, `dni: int` |
| `Localidad` | `id: Long`, `denominacion: String` |
| `Libro` | `id: Long`, `titulo: String`, `fecha: int`, `genero: String`, `paginas: int`, `autor: String` |
| `Autor` | `id: Long`, `nombre: String`, `apellido: String`, `biografia: String` |

### 2.2 Relaciones y su traducción a JPA

| Relación del diagrama | Multiplicidad | Implementación en el servidor |
|---|---|---|
| **Persona → Domicilio** | 1 a 1 (unidireccional) | En `Persona`: `@OneToOne(cascade = ALL, orphanRemoval = true)` + `@JoinColumn(name="domicilio_id")` |
| **Domicilio → Localidad** | muchos a 1 (unidireccional) | En `Domicilio`: `@ManyToOne(optional = false)` + `@JoinColumn(name="localidad_id")` |
| **Persona ◆ Libro** (composición) | 1 a * | En `Persona`: `@OneToMany(cascade = ALL, orphanRemoval = true)` + `@JoinColumn(name="persona_id")`. Los libros **no existen sin su persona**: si se elimina la persona, se eliminan sus libros. |
| **Libro → Autor** | * a * (unidireccional) | En `Libro`: `@ManyToMany` + `@JoinTable(name="libro_autor")` |

### 2.3 Decisiones de interpretación (léalas: son los únicos puntos donde el diagrama admite más de una lectura)

1. **Domicilio → Localidad como *muchos a uno***: la notación `1 … *` del diagrama se interpretó en su sentido natural
   del dominio: *muchos domicilios pertenecen a una misma localidad* (una localidad no "posee" domicilios;
   un domicilio "referencia" a una localidad). Si su cátedra lo espera al revés, el cambio es de una línea en `Domicilio`.
2. **Atributo `autor: String` de `Libro`**: se conservó exactamente como figura en el diagrama y **además** se implementó la
   relación real `Libro *–* Autor` con la colección `autores`. Si el campo de texto `autor` llega vacío, el servidor lo completa
   con los nombres de los autores asociados.
3. **`fecha: int` de `Libro`** se interpreta como **año de publicación** (validado entre 1 y 2100).
4. **Tipos `int`**: en el servidor se usan primitivos `int` como en el diagrama; en los DTO del cliente se usa `Integer`
   para que un campo vacío del formulario produzca un error de validación amigable en vez de guardarse como `0`.
5. **DNI único**: dos personas no pueden tener el mismo DNI (regla de negocio adicional → HTTP 409).

---

## 3. Servidor (`biblioteca-server`)

Arquitectura en capas clásica:

```
ar.com.biblioteca.server
├── BibliotecaServerApplication
├── entities/       Persona, Domicilio, Localidad, Libro, Autor           ← modelo del diagrama (JPA + Bean Validation)
├── repositories/   PersonaRepository, LibroRepository, ...               ← Spring Data JPA
├── services/       PersonaService, LocalidadService, AutorService        ← reglas de negocio y transacciones
├── controllers/    PersonaController, LocalidadController, AutorController  ← API REST (@RestController)
├── exceptions/     GlobalExceptionHandler, ApiError, *Exception          ← errores JSON uniformes
└── config/         DataLoader                                            ← datos de ejemplo
```

### 3.1 API REST (base: `http://localhost:8080/api/v1`)

| Método | Ruta | Descripción | Respuestas |
|---|---|---|---|
| GET | `/localidades` | Lista localidades (ordenadas) | 200 |
| GET | `/localidades/{id}` | Obtiene una localidad | 200 / 404 |
| POST | `/localidades` | Crea una localidad | 201 / 400 |
| PUT | `/localidades/{id}` | Modifica una localidad | 200 / 400 / 404 |
| DELETE | `/localidades/{id}` | Elimina (si no está en uso) | 204 / 404 / **409** si hay domicilios que la usan |
| GET | `/autores` · `/autores/{id}` | Lista / obtiene autores | 200 / 404 |
| POST | `/autores` | Crea un autor | 201 / 400 |
| PUT | `/autores/{id}` | Modifica un autor | 200 / 400 / 404 |
| DELETE | `/autores/{id}` | Elimina (si no está en uso) | 204 / 404 / **409** si hay libros que lo usan |
| GET | `/personas?filtro=` | Lista personas (filtro opcional por nombre/apellido) | 200 |
| GET | `/personas/{id}` | Persona con su domicilio, localidad y libros | 200 / 404 |
| POST | `/personas` | Crea persona **con su domicilio** (la localidad se referencia por `id`) | 201 / 400 / **409** DNI repetido |
| PUT | `/personas/{id}` | Modifica persona y domicilio (no toca los libros) | 200 / 400 / 404 / 409 |
| DELETE | `/personas/{id}` | Elimina persona, **su domicilio y sus libros** (composición) | 204 / 404 |
| GET | `/personas/{personaId}/libros` | Libros de la persona | 200 / 404 |
| GET | `/personas/{personaId}/libros/{libroId}` | Un libro de la persona | 200 / 404 |
| POST | `/personas/{personaId}/libros` | Agrega un libro (autores por `id`) | 201 / 400 / 404 |
| PUT | `/personas/{personaId}/libros/{libroId}` | Modifica un libro y sus autores | 200 / 400 / 404 |
| DELETE | `/personas/{personaId}/libros/{libroId}` | Elimina el libro | 204 / 404 |

Los libros se administran **anidados bajo su persona** porque el diagrama los define como composición.

Ejemplo de alta de persona:

```bash
curl -X POST http://localhost:8080/api/v1/personas -H "Content-Type: application/json" -d '{
  "nombre": "María", "apellido": "López", "dni": 33444555,
  "domicilio": { "calle": "Av. Las Heras", "numero": 250, "localidad": { "id": 1 } }
}'
```

Hay más ejemplos listos para ejecutar en [`docs/requests.http`](docs/requests.http).

### 3.2 Errores uniformes

Toda falla devuelve el mismo JSON, que el cliente interpreta y muestra al usuario:

```json
{
  "timestamp": "2026-09-20T20:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Los datos enviados no son válidos",
  "path": "/api/v1/personas",
  "fieldErrors": { "nombre": "El nombre es obligatorio" }
}
```

| Situación | HTTP |
|---|---|
| Recurso inexistente | 404 |
| Validación fallida (`@NotBlank`, `@Positive`, …) o referencia inexistente (localidad/autor) | 400 |
| DNI repetido, localidad o autor en uso | 409 |
| Error inesperado | 500 |

### 3.3 Persistencia y datos de ejemplo

* **H2 en memoria** por defecto (`application.properties`). Al iniciar, `DataLoader` carga 6 localidades, 4 autores y 2 personas
  con sus libros (solo si la base está vacía). Con H2 en memoria los datos se pierden al detener el servidor.
* **MySQL (opcional):** cree/tenga un MySQL local, ajuste usuario y clave en `application-mysql.properties` y ejecute:
  ```bash
  ./mvnw spring-boot:run -Dspring-boot.run.profiles=mysql
  ```
  La base `biblioteca` se crea sola y las tablas se generan con `ddl-auto=update`.

---

## 4. Cliente (`biblioteca-client`)

```
ar.com.biblioteca.client
├── BibliotecaClientApplication
├── config/       RestTemplateConfig            ← bean RestTemplate (timeouts de conexión y lectura)
├── api/          ApiClient                     ← ÚNICO lugar que usa RestTemplate (GET/POST/PUT/DELETE + traducción de errores)
├── dto/          PersonaDTO, DomicilioDTO, LocalidadDTO, LibroDTO, AutorDTO, LibroConPropietarioDTO, ApiErrorDTO
├── service/      PersonaService, LocalidadService, AutorService   ← consumen la API a través de ApiClient
├── controller/   Home, Persona, Libro, Catalogo, Localidad, Autor ← controladores MVC (devuelven vistas Thymeleaf)
└── exception/    ApiException, GlobalExceptionHandler
resources/templates/   index, error, fragments/layout, personas/*, libros/*, autores/*, localidades/*
resources/static/css/app.css
```

### 4.1 Cómo cumple la consigna

* **Usa `RestTemplate`**: definido en `RestTemplateConfig` y utilizado en `ApiClient` con `getForObject`, `postForObject`,
  `put` y `delete`. La URL del servidor se configura en `application.properties` (`biblioteca.api.base-url`).
* **Usa DTO's para manipular la vista**: la aplicación cliente **no tiene entidades ni base de datos**. Los controladores nunca
  ven JSON ni objetos del servidor: reciben/entregan DTO's (`PersonaDTO`, `LibroDTO`, …) que además llevan las **validaciones de los formularios**.
  Ejemplos de DTO's pensados para la vista: `LibroDTO.autoresIds` (casillas tildadas del formulario, marcado `@JsonIgnore` para que no viaje
  a la API) y `LibroConPropietarioDTO` (fila del listado general de libros).
* **Muestra los datos en un sitio HTML**: plantillas Thymeleaf con Bootstrap y estilos propios.

### 4.2 Pantallas y rutas

| Ruta | Pantalla |
|---|---|
| `/` | Portada con un «estante» de accesos y cantidades por sección |
| `/personas` | Listado de personas con buscador por nombre/apellido |
| `/personas/nueva` · `/personas/{id}/editar` | Formulario de persona **y su domicilio** (la localidad se elige de una lista) |
| `/personas/{id}` | Ficha de la persona: domicilio y **sus libros** (agregar / editar / eliminar) |
| `/personas/{id}/libros/nuevo` · `/…/libros/{libroId}/editar` | Formulario de libro con selección de **autores** (muchos a muchos) |
| `/libros` | Listado general de libros con su propietario (solo lectura) |
| `/autores`, `/autores/nuevo`, `/autores/{id}/editar` | ABM de autores |
| `/localidades`, `/localidades/nueva`, `/localidades/{id}/editar` | ABM de localidades |

Comportamientos a destacar: validación de formularios con mensajes junto a cada campo, confirmación antes de eliminar,
mensajes de éxito/error tras cada operación (por ejemplo, al intentar borrar una localidad en uso se muestra el aviso del servidor),
y página de error amigable si la API no responde.

---

## 5. Flujo de una petición (ejemplo: «Agregar libro»)

1. El navegador envía el formulario `POST /personas/1/libros` al **cliente** (`LibroController`).
2. Spring enlaza el formulario con `LibroDTO` y lo valida (`@Valid`).
3. `PersonaService` (cliente) convierte los `autoresIds` en la lista de autores y llama a `ApiClient.post(...)`.
4. `RestTemplate` envía `POST http://localhost:8080/api/v1/personas/1/libros` con el JSON del libro.
5. El **servidor** (`PersonaController` → `PersonaService`) valida, resuelve los autores por id, agrega el libro a la persona
   (cascade) y responde `201` con el libro creado.
6. El cliente redirige a `/personas/1` y muestra el mensaje «El libro se agregó correctamente».
   Si el servidor hubiera respondido un error, `ApiClient` lo traduce a `ApiException` y se muestra en el formulario.

---

## 6. Solución de problemas

| Síntoma | Causa / solución |
|---|---|
| El cliente muestra «No se pudo conectar con el servidor» | Inicie primero `biblioteca-server` (puerto 8080) o corrija `biblioteca.api.base-url` en `biblioteca-client/src/main/resources/application.properties`. |
| `Port 8080/8081 was already in use` | Cambie `server.port` en el `application.properties` correspondiente (y la URL base del cliente si cambia la del servidor). |
| La página se ve sin estilos ni tipografías | Bootstrap y Google Fonts se cargan por CDN: se requiere Internet en el navegador. |
| `mvnw: Permission denied` (Linux/macOS) | `chmod +x mvnw` |
| `release version 17 not supported` | Use JDK 17 o superior (`java -version`). |

---

## 7. Tecnologías

Java 17 · Spring Boot 3.5 (Spring Web MVC, Spring Data JPA/Hibernate, Bean Validation, Thymeleaf) · H2 / MySQL ·
Jackson · Maven (con wrapper) · JUnit 5 + MockMvc · Bootstrap 5 (CDN).

## 8. Posibles extensiones

Paginación en los listados, DTO's también en el servidor (para no exponer entidades), documentación OpenAPI/Swagger,
autenticación con Spring Security, y un `docker-compose` con MySQL.
