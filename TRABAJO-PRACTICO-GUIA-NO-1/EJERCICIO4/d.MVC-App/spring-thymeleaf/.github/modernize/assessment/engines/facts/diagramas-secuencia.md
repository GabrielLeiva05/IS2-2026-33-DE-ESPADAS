# Diagramas de Secuencia

Este documento presenta los diagramas de secuencia de análisis y de diseño del sistema de gestión de videojuegos. Los diagramas de análisis describen responsabilidades del dominio sin comprometer clases concretas; los de diseño reflejan los controladores, servicios, repositorios, vistas y recursos identificados en la implementación.

## Criterio de modelado

- **Análisis:** actor, sistema, catálogo, videojuego e imagen como responsabilidades conceptuales.
- **Diseño:** navegador, controlador MVC, servicios, repositorio JPA, base de datos, vistas Thymeleaf y sistema de archivos.
- **Alcance:** catálogo público, búsqueda, detalle, alta, modificación y baja lógica de videojuegos.
- **Regla transversal:** las consultas públicas solo consideran videojuegos activos.

## 1. Consulta del catálogo público

### 1.1 Diagrama de análisis

<!-- mermaid-checked: every participant uses `participant Id as "Label"`, no \n in aliases/messages/notes, every alt/opt/loop closed by end, no `:` inside any alias -->
```mermaid
sequenceDiagram
    participant Visitante as "Visitante"
    participant Catalogo as "Catalogo publico"
    participant Videojuego as "Videojuego"

    Visitante->>Catalogo: Solicitar catalogo
    Catalogo->>Videojuego: Obtener videojuegos activos
    Videojuego-->>Catalogo: Lista de videojuegos activos
    Catalogo-->>Visitante: Mostrar tarjetas del catalogo
```

### 1.2 Diagrama de diseño

<!-- mermaid-checked: every participant uses `participant Id as "Label"`, no \n in aliases/messages/notes, every alt/opt/loop closed by end, no `:` inside any alias -->
```mermaid
sequenceDiagram
    participant Navegador as "Navegador"
    participant Controlador as "controladorVideojuego"
    participant Servicio as "ServicioVideojuego"
    participant Repositorio as "RepositorioVideojuego"
    participant BaseDatos as "Base de datos"
    participant Vista as "inicio.html y card.html"

    Navegador->>Controlador: GET /inicio
    Controlador->>Servicio: findAllByActivo()
    Servicio->>Repositorio: findAllByActivo()
    Repositorio->>BaseDatos: Buscar videojuegos con activo verdadero
    BaseDatos-->>Repositorio: Registros activos
    Repositorio-->>Servicio: Lista de videojuegos
    Servicio-->>Controlador: Lista de videojuegos
    Controlador->>Vista: Agregar videojuegos al modelo
    Vista-->>Navegador: HTML con tarjetas y enlaces
```

## 2. Consulta del detalle

### 2.1 Diagrama de análisis

<!-- mermaid-checked: every participant uses `participant Id as "Label"`, no \n in aliases/messages/notes, every alt/opt/loop closed by end, no `:` inside any alias -->
```mermaid
sequenceDiagram
    participant Visitante as "Visitante"
    participant Catalogo as "Catalogo publico"
    participant Videojuego as "Videojuego"
    participant Imagen as "Imagen"

    Visitante->>Catalogo: Seleccionar videojuego
    Catalogo->>Videojuego: Buscar videojuego activo por identificador
    alt Videojuego encontrado
        Videojuego-->>Catalogo: Datos del videojuego
        Catalogo->>Imagen: Solicitar imagen
        Imagen-->>Catalogo: Imagen del videojuego
        Catalogo-->>Visitante: Mostrar detalle
    else Videojuego no encontrado
        Catalogo-->>Visitante: Mostrar error
    end
```

### 2.2 Diagrama de diseño

<!-- mermaid-checked: every participant uses `participant Id as "Label"`, no \n in aliases/messages/notes, every alt/opt/loop closed by end, no `:` inside any alias -->
```mermaid
sequenceDiagram
    participant Navegador as "Navegador"
    participant Controlador as "controladorVideojuego"
    participant Servicio as "ServicioVideojuego"
    participant Repositorio as "RepositorioVideojuego"
    participant BaseDatos as "Base de datos"
    participant Vista as "detalle.html"
    participant Recursos as "Servidor de imagenes"

    Navegador->>Controlador: GET /detalle/id
    Controlador->>Servicio: findByIdAndActivo(id)
    Servicio->>Repositorio: findByIdAndActivo(id)
    Repositorio->>BaseDatos: Buscar por id y activo verdadero
    BaseDatos-->>Repositorio: Resultado
    Repositorio-->>Servicio: Videojuego o ausencia
    alt Videojuego encontrado
        Servicio-->>Controlador: Videojuego activo
        Controlador->>Vista: Agregar videojuego al modelo
        Vista->>Recursos: GET /imagenes/nombre
        Recursos-->>Vista: Archivo de imagen
        Vista-->>Navegador: HTML del detalle
    else Videojuego no encontrado
        Servicio-->>Controlador: Excepcion
        Controlador-->>Navegador: Vista de error
    end
```

## 3. Busqueda por titulo

### 3.1 Diagrama de análisis

<!-- mermaid-checked: every participant uses `participant Id as "Label"`, no \n in aliases/messages/notes, every alt/opt/loop closed by end, no `:` inside any alias -->
```mermaid
sequenceDiagram
    participant Visitante as "Visitante"
    participant Buscador as "Buscador"
    participant Catalogo as "Catalogo publico"
    participant Videojuegos as "Videojuegos activos"

    Visitante->>Buscador: Ingresar titulo
    Buscador->>Catalogo: Solicitar coincidencias
    Catalogo->>Videojuegos: Buscar por titulo
    Videojuegos-->>Catalogo: Resultados activos
    alt Hay coincidencias
        Catalogo-->>Visitante: Mostrar resultados
    else No hay coincidencias
        Catalogo-->>Visitante: Mostrar ausencia de resultados
    end
```

### 3.2 Diagrama de diseño

<!-- mermaid-checked: every participant uses `participant Id as "Label"`, no \n in aliases/messages/notes, every alt/opt/loop closed by end, no `:` inside any alias -->
```mermaid
sequenceDiagram
    participant Navegador as "Navegador"
    participant Navbar as "navbar.html"
    participant Controlador as "controladorVideojuego"
    participant Servicio as "ServicioVideojuego"
    participant Repositorio as "RepositorioVideojuego"
    participant BaseDatos as "Base de datos"
    participant Vista as "busqueda.html y card.html"

    Navegador->>Navbar: Enviar texto de busqueda
    Navbar->>Controlador: GET /busqueda con query
    Controlador->>Servicio: findByTitle(query)
    Servicio->>Repositorio: findByTitle(query)
    Repositorio->>BaseDatos: Consultar titulo y activo verdadero
    BaseDatos-->>Repositorio: Coincidencias
    Repositorio-->>Servicio: Lista de videojuegos
    Servicio-->>Controlador: Resultados
    Controlador->>Vista: Agregar resultados y query al modelo
    alt Lista con resultados
        Vista-->>Navegador: HTML con tarjetas
    else Lista vacia
        Vista-->>Navegador: Mensaje sin resultados
    end
```

## 4. Alta de videojuego

### 4.1 Diagrama de análisis

<!-- mermaid-checked: every participant uses `participant Id as "Label"`, no \n in aliases/messages/notes, every alt/opt/loop closed by end, no `:` inside any alias -->
```mermaid
sequenceDiagram
    participant Administrador as "Administrador"
    participant Gestion as "Gestion de videojuegos"
    participant Catalogos as "Categorias y estudios"
    participant Imagen as "Imagen"
    participant Videojuego as "Videojuego"

    Administrador->>Gestion: Solicitar formulario de alta
    Gestion->>Catalogos: Obtener datos de referencia
    Catalogos-->>Gestion: Categorias y estudios
    Gestion-->>Administrador: Mostrar formulario vacio
    Administrador->>Gestion: Enviar datos e imagen
    Gestion->>Imagen: Validar y almacenar imagen
    alt Datos e imagen validos
        Imagen-->>Gestion: Nombre de imagen
        Gestion->>Videojuego: Crear videojuego activo
        Videojuego-->>Gestion: Alta confirmada
        Gestion-->>Administrador: Mostrar listado de gestion
    else Datos o imagen invalidos
        Gestion-->>Administrador: Mostrar errores del formulario
    end
```

### 4.2 Diagrama de diseño

<!-- mermaid-checked: every participant uses `participant Id as "Label"`, no \n in aliases/messages/notes, every alt/opt/loop closed by end, no `:` inside any alias -->
```mermaid
sequenceDiagram
    participant Navegador as "Navegador"
    participant Controlador as "controladorVideojuego"
    participant ServicioCat as "ServicioCategoria"
    participant ServicioEst as "ServicioEstudio"
    participant Servicio as "ServicioVideojuego"
    participant Repositorio as "RepositorioVideojuego"
    participant BaseDatos as "Base de datos"
    participant Archivos as "Sistema de archivos"
    participant Vista as "videojuego.html"

    Navegador->>Controlador: GET /formulario/videojuego/0
    Controlador->>ServicioCat: findAll()
    ServicioCat-->>Controlador: Categorias
    Controlador->>ServicioEst: findAll()
    ServicioEst-->>Controlador: Estudios
    Controlador->>Vista: Crear Videojuego vacio y renderizar
    Vista-->>Navegador: Formulario de alta
    Navegador->>Controlador: POST /formulario/videojuego/0 con datos y archivo
    Controlador->>Controlador: Ejecutar validacion Bean Validation
    alt Validacion de campos correcta
        Controlador->>Controlador: Validar imagen y tamano maximo
        alt Imagen valida
            Controlador->>Archivos: Escribir imagen con nombre generado
            Archivos-->>Controlador: Escritura confirmada
            Controlador->>Servicio: saveOne(videojuego)
            Servicio->>Repositorio: save(videojuego)
            Repositorio->>BaseDatos: Insertar videojuego
            BaseDatos-->>Repositorio: Videojuego persistido
            Repositorio-->>Servicio: Videojuego guardado
            Servicio-->>Controlador: Alta confirmada
            Controlador-->>Navegador: Redireccion a /crud
        else Imagen ausente o invalida
            Controlador-->>Navegador: Formulario con error de imagen
        end
    else Campos invalidos
        Controlador-->>Navegador: Formulario con errores de validacion
    end
```

## 5. Modificacion de videojuego

### 5.1 Diagrama de análisis

<!-- mermaid-checked: every participant uses `participant Id as "Label"`, no \n in aliases/messages/notes, every alt/opt/loop closed by end, no `:` inside any alias -->
```mermaid
sequenceDiagram
    participant Administrador as "Administrador"
    participant Gestion as "Gestion de videojuegos"
    participant Videojuego as "Videojuego existente"
    participant Imagen as "Imagen opcional"

    Administrador->>Gestion: Solicitar edicion
    Gestion->>Videojuego: Obtener datos actuales
    Videojuego-->>Gestion: Datos del videojuego
    Gestion-->>Administrador: Mostrar formulario precargado
    Administrador->>Gestion: Enviar cambios
    Gestion->>Gestion: Validar datos
    alt Se adjunta imagen nueva
        Gestion->>Imagen: Validar y reemplazar imagen
        Imagen-->>Gestion: Imagen actualizada
    else Se conserva imagen actual
        Gestion->>Videojuego: Mantener imagen existente
    end
    alt Datos validos
        Gestion->>Videojuego: Guardar cambios
        Videojuego-->>Gestion: Modificacion confirmada
        Gestion-->>Administrador: Mostrar listado de gestion
    else Datos invalidos
        Gestion-->>Administrador: Mostrar errores
    end
```

### 5.2 Diagrama de diseño

<!-- mermaid-checked: every participant uses `participant Id as "Label"`, no \n in aliases/messages/notes, every alt/opt/loop closed by end, no `:` inside any alias -->
```mermaid
sequenceDiagram
    participant Navegador as "Navegador"
    participant Controlador as "controladorVideojuego"
    participant ServicioCat as "ServicioCategoria"
    participant ServicioEst as "ServicioEstudio"
    participant Servicio as "ServicioVideojuego"
    participant Repositorio as "RepositorioVideojuego"
    participant BaseDatos as "Base de datos"
    participant Archivos as "Sistema de archivos"
    participant Vista as "videojuego.html"

    Navegador->>Controlador: GET /formulario/videojuego/id
    Controlador->>ServicioCat: findAll()
    ServicioCat-->>Controlador: Categorias
    Controlador->>ServicioEst: findAll()
    ServicioEst-->>Controlador: Estudios
    Controlador->>Servicio: findById(id)
    Servicio->>Repositorio: findById(id)
    Repositorio->>BaseDatos: Buscar videojuego
    BaseDatos-->>Repositorio: Registro
    Repositorio-->>Servicio: Videojuego
    Servicio-->>Controlador: Videojuego
    Controlador->>Vista: Renderizar formulario precargado
    Vista-->>Navegador: Formulario de edicion
    Navegador->>Controlador: POST /formulario/videojuego/id
    Controlador->>Controlador: Ejecutar validacion Bean Validation
    alt Campos validos
        opt Archivo nuevo informado
            Controlador->>Controlador: Validar imagen y tamano
            Controlador->>Archivos: Sobrescribir imagen existente
            Archivos-->>Controlador: Escritura confirmada
        end
        Controlador->>Servicio: updateOne(videojuego, id)
        Servicio->>Repositorio: findById(id) y save(videojuego)
        Repositorio->>BaseDatos: Actualizar videojuego
        BaseDatos-->>Repositorio: Actualizacion confirmada
        Repositorio-->>Servicio: Videojuego actualizado
        Servicio-->>Controlador: Modificacion confirmada
        Controlador-->>Navegador: Redireccion a /crud
    else Campos o imagen invalidos
        Controlador-->>Navegador: Formulario con errores
    end
```

## 6. Baja logica de videojuego

### 6.1 Diagrama de análisis

<!-- mermaid-checked: every participant uses `participant Id as "Label"`, no \n in aliases/messages/notes, every alt/opt/loop closed by end, no `:` inside any alias -->
```mermaid
sequenceDiagram
    participant Administrador as "Administrador"
    participant Gestion as "Gestion de videojuegos"
    participant Videojuego as "Videojuego"
    participant Catalogo as "Catalogo publico"

    Administrador->>Gestion: Solicitar baja
    Gestion->>Videojuego: Consultar videojuego
    Videojuego-->>Gestion: Videojuego a confirmar
    Gestion-->>Administrador: Mostrar confirmacion
    Administrador->>Gestion: Confirmar baja
    Gestion->>Videojuego: Cambiar estado activo
    Videojuego-->>Gestion: Estado actualizado
    Gestion-->>Administrador: Mostrar listado de gestion
    Administrador->>Catalogo: Consultar catalogo publico
    Catalogo->>Videojuego: Obtener solo activos
    Videojuego-->>Catalogo: Excluir videojuego inactivo
```

### 6.2 Diagrama de diseño

<!-- mermaid-checked: every participant uses `participant Id as "Label"`, no \n in aliases/messages/notes, every alt/opt/loop closed by end, no `:` inside any alias -->
```mermaid
sequenceDiagram
    participant Navegador as "Navegador"
    participant Controlador as "controladorVideojuego"
    participant Servicio as "ServicioVideojuego"
    participant Repositorio as "RepositorioVideojuego"
    participant BaseDatos as "Base de datos"
    participant VistaConf as "eliminar.html"
    participant VistaCrud as "crud.html"

    Navegador->>Controlador: GET /eliminar/videojuego/id
    Controlador->>Servicio: findById(id)
    Servicio->>Repositorio: findById(id)
    Repositorio->>BaseDatos: Buscar videojuego por id
    BaseDatos-->>Repositorio: Registro
    Repositorio-->>Servicio: Videojuego
    Servicio-->>Controlador: Videojuego
    Controlador->>VistaConf: Agregar videojuego al modelo
    VistaConf-->>Navegador: Formulario de confirmacion
    Navegador->>Controlador: POST /eliminar/videojuego/id
    Controlador->>Servicio: deleteById(id)
    Servicio->>Repositorio: findById(id)
    Repositorio->>BaseDatos: Buscar videojuego
    BaseDatos-->>Repositorio: Registro
    Servicio->>Servicio: Invertir valor de activo
    Servicio->>Repositorio: save(videojuego)
    Repositorio->>BaseDatos: Actualizar estado activo
    BaseDatos-->>Repositorio: Actualizacion confirmada
    Repositorio-->>Servicio: Baja logica confirmada
    Servicio-->>Controlador: true
    Controlador-->>Navegador: Redireccion a /crud
    Navegador->>Controlador: GET /inicio
    Controlador->>Servicio: findAllByActivo()
    Servicio->>Repositorio: findAllByActivo()
    Repositorio->>BaseDatos: Consultar activos
    BaseDatos-->>Repositorio: Lista sin videojuego inactivo
    Repositorio-->>Servicio: Lista publica
    Servicio-->>Controlador: Lista publica
    Controlador->>VistaCrud: Renderizar gestion actualizada
```

## Observaciones

- La baja implementada es lógica: `deleteById` invierte el atributo `activo`; no elimina físicamente el registro.
- Las imágenes se guardan en `C:/Videojuegos/imagenes`, fuera del classpath de la aplicación.
- Los errores de validación regresan al formulario; las excepciones del controlador se derivan a `error.html`.
- Las categorías y estudios son datos de referencia usados durante alta y modificación.
