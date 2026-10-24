# API REST

La API de mascotas usa JSON y autenticación HTTP Basic. Las rutas web Thymeleaf existentes siguen disponibles.

Base URL predeterminada: `http://localhost:9000/api`. Se puede cambiar con `tinder.api.base-url` y `server.port`.

## Rutas

| Método | Ruta | Respuesta |
| --- | --- | --- |
| GET | `/mascotas/mias` | Mascotas activas del usuario autenticado |
| GET | `/mascotas/baja` | Mascotas dadas de baja |
| GET | `/mascotas/explorar?tipo=PERRO` | Mascotas de otros usuarios; `tipo` es opcional (`PERRO` o `GATO`) |
| GET | `/mascotas/{id}` | Detalle de una mascota propia |
| POST | `/mascotas` | Crea una mascota; responde `201 Created` |
| PUT | `/mascotas/{id}` | Modifica nombre, sexo y tipo; conserva su foto actual |
| DELETE | `/mascotas/{id}` | Da de baja una mascota; responde `204 No Content` |
| PATCH | `/mascotas/{id}/restaurar` | Restaura una mascota; responde `204 No Content` |

Para crear o modificar se envía:

```json
{
  "nombre": "Luna",
  "sexo": "HEMBRA",
  "tipo": "GATO"
}
```

Ejemplo:

```bash
curl -u usuario@ejemplo.com:clave http://localhost:9000/api/mascotas/mias
```

Las operaciones requieren un usuario autenticado. Los errores de validación se responden como JSON con `mensaje`; los recursos no autorizados devuelven `403` y una API sin credenciales devuelve `401`. La carga de imágenes continúa disponible mediante los formularios multipart actuales.

`MascotaApiClient` es el cliente Java basado en `RestTemplate`; permite consultar las mascotas propias con las credenciales del usuario. `MascotaMapper` convierte entidades a DTOs sin exponer contraseñas, relaciones JPA ni el contenido binario de las fotos.
