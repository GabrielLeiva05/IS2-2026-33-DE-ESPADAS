# Refactor del ejercicio 3 "c" con patrones de diseño

Resumen de qué patrón se aplicó, dónde está y por qué. Los paquetes son del servidor
(`biblioteca-server/src/main/java/ar/com/biblioteca/server/`), salvo que se indique lo contrario.

| Patrón | Tipo | Dónde | Para qué (enunciado) |
|---|---|---|---|
| Builder | Creacional | `builder/LibroBuilder` | Crear objetos Libro |
| Fábrica (Factory Method) | Creacional | `factory/LibroCreator`, `LibroFisicoCreator`, `LibroDigitalCreator` | Crear Libros Físicos y Digitales |
| Prototype | Creacional | `prototype/Prototipo`, `entities/Autor.clonar()` | Clonar un autor |
| Adapter | Estructural | `dto/*`, `dto/adapter/*` | DTO ⇄ Entidad |
| Strategy | Comportamiento | `strategy/*`, contexto en `services/LibroService` | Buscar libros por título, género y autor |
| Iterator | Comportamiento | `iterator/*`, usado en `services/AutorService.librosDelAutor` | Recorrer libros y obtener los de un autor |

---

## 1. Builder — `LibroBuilder`

**Problema.** Un libro tiene muchos datos: título, año, género, páginas, autor, autores, fecha de devolución y el
dato propio de su tipo. Con un constructor hay que pasarlos todos y en orden (`new Libro("Ficciones", 1944, "Cuentos", 174)`):
es fácil confundirse y no hay forma cómoda de omitir los opcionales.

**Solución.** El builder junta las partes paso a paso, con un método con nombre por cada dato, y recién en `build()`
valida lo obligatorio y entrega el objeto terminado:

```java
fisicoCreator.builder()
        .titulo("Rayuela").fecha(1963).genero("Novela").paginas(635)
        .autores(cortazar)
        .datosPropios(libro -> libro.setUbicacion("Estante B3"))
        .build();
```

`Libro` ya no tiene constructor con parámetros: se arma siempre con el builder (lo usan el `DataLoader` y el `LibroCreator`).

## 2. Fábrica (Factory Method) — `LibroCreator`

**Cambio en el modelo.** `Libro` pasa a ser **abstracta**, y aparecen `LibroFisico` (dato propio: `ubicacion`) y
`LibroDigital` (dato propio: `formato`). En JPA es herencia `SINGLE_TABLE`: una sola tabla `libro` con una columna
`tipo` que dice si cada fila es FISICO o DIGITAL.

**Piezas del patrón (GoF):**
- Product: `Libro`. ConcreteProducts: `LibroFisico`, `LibroDigital`.
- Creator: `LibroCreator`, con el *factory method* abstracto `nuevoLibro()`.
- ConcreteCreators: `LibroFisicoCreator` (`new LibroFisico()`) y `LibroDigitalCreator` (`new LibroDigital()`).

**Por qué.** El resto del sistema (adapter, DataLoader) trabaja con "un Libro" y "un LibroCreator", y nunca hace
`new LibroFisico()` ni `new LibroDigital()`. El `LibroAdapter` recibe todos los creators (Spring los inyecta en una lista)
y elige el que corresponde al `"tipo"` del JSON. Si no viene tipo se crea FISICO, así los pedidos de antes siguen funcionando.
Agregar un tipo nuevo, por ejemplo AudioLibro, es agregar una entidad y un creator, sin tocar el adapter ni el service.

**Cómo se combina con el Builder.** El builder recibe el factory method (`this::nuevoLibro`) y lo llama dentro de `build()`.
La Fábrica decide **qué clase** se crea y el Builder decide **cómo se arma**.

Reglas nuevas:
- Un libro existente **no puede cambiar de tipo** (JPA no convierte un LibroFisico en LibroDigital): da 400.
- Tipo desconocido: da 400.

## 3. Prototype — `Autor.clonar()`

**Piezas:** `Prototipo<T>` (interfaz Prototype) y `Autor` (ConcretePrototype).

`Autor.clonar()` devuelve un autor **nuevo** con los mismos datos y sin id. El service lo guarda con el método plantilla
`create()`. Quien pide la copia no copia atributo por atributo: se la pide al original.

Endpoint: `POST /api/v1/autores/{id}/clonar` responde 201 con la copia. En el cliente, el botón **Clonar** de la lista de
autores crea la copia y abre su formulario para modificarla.

## 4. Adapter — `BaseAdapter`, `LibroAdapter`, `AutorAdapter`, `LocalidadAdapter`

Mismo molde que en la Clínica. La entidad (JPA) y el DTO (JSON) tienen "interfaces" distintas, y el adapter traduce
entre las dos en el controller:

```
JSON → DTO --toEntity()--> Entidad → service → Entidad --toDTO()--> DTO → JSON
```

- `ControladorBase` pasa a ser `ControladorBase<Entidad, DTO, Service>`. Autor y Localidad usan DTO sin repetir el CRUD.
- Los libros de `/personas/{id}/libros` entran y salen como `LibroDTO`. `LibroAdapter.toEntity` usa la Fábrica y el Builder.
- Los services **no cambian de forma**: siguen trabajando con entidades.
- Los DTO tienen los mismos nombres de campos que las entidades, así que el cliente no se rompe.
- Persona sigue viajando como entidad. Aplicar el adapter también a Persona, Domicilio y Localidad anidada no agrega nada
  nuevo al patrón.

## 5. Strategy — `CriterioBusquedaLibro`

**Piezas:**
- Strategy: `CriterioBusquedaLibro`.
- ConcreteStrategies: `CriterioPorTitulo` (`"titulo"`), `CriterioPorGenero` (`"genero"`) y `CriterioPorAutor` (`"autor"`).
- Context: `LibroService.buscar(criterio, valor)`.

Spring arma un `Map<nombreDelBean, estrategia>`. El context elige la estrategia por la clave y la aplica, sin un `if`
por cada criterio. La comparación ignora mayúsculas y tildes ("garcia" encuentra "García Márquez").

Endpoint: `GET /api/v1/libros?criterio=autor&valor=borges`. En el cliente se usa desde el buscador de la pantalla **Libros**.

## 6. Iterator — `ColeccionLibros` + `IteradorLibrosPorAutor`

**Piezas:**
- Iterator: `Iterador<T>`, con `tieneSiguiente()` y `siguiente()`.
- Aggregate: `ColeccionLibros`.
- ConcreteIterator: `IteradorLibrosPorAutor`, que en cada paso **saltea** los libros que no son del autor.

`AutorService.librosDelAutor` hace solo `while (it.tieneSiguiente()) it.siguiente()`: no conoce la lista interna ni el filtro.

Endpoint: `GET /api/v1/autores/{id}/libros`. En el cliente se usa desde el botón **Libros** de la lista de autores.

---

## Clases nuevas que no son "del patrón"

- `LibroFisico` y `LibroDigital` (entidades): son los productos que pide la Fábrica.
- `LibroService` + `LibroController` (`/api/v1/libros`): hacía falta un lugar para buscar en **todos** los libros, porque
  hasta ahora solo se accedía a ellos dentro de cada persona.

## Cambios en `biblioteca-client`

| Patrón | Cambio en el cliente |
|---|---|
| Fábrica | `LibroDTO` suma `tipo`, `ubicacion` y `formato`. El formulario de libro tiene selector de tipo (bloqueado al editar). Se agregó la columna "Tipo" en la ficha de la persona y en el catálogo. |
| Strategy | Buscador (criterio + texto) en la pantalla Libros: `CatalogoController` y `PersonaService.buscarLibros`. |
| Iterator | Botón "Libros" en Autores, `AutorService.librosDelAutor` y la vista nueva `autores/libros.html`. |
| Prototype | Botón "Clonar" en Autores y `AutorService.clonar`. |
| Builder, Adapter | Nada: son internos del servidor. |

## Otras correcciones

- `AutorService`: ordenaba por `"Id"` (la propiedad se llama `id`).
- `AutorService`: el mensaje de "en uso" hablaba de localidad y domicilios.

## Ojo con MySQL

Con H2 (lo de siempre) no hay que hacer nada. Si se usa el perfil `mysql` con una base que ya tenía libros, la columna
nueva `tipo` queda vacía en esas filas y Hibernate no puede saber de qué clase son. En ese caso hay que borrar la base
`biblioteca` (o la tabla `libro`) una vez para que se regenere.
