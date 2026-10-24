# Weather OpenWeather - Spring Boot MVC

Aplicación web académica que consume la API externa de **OpenWeather** desde el BackEnd y muestra la información en una vista Thymeleaf basada en la plantilla Bootstrap **Grayscale**.

## Arquitectura

La aplicación respeta MVC:

- `controller/`: recibe la petición HTTP y prepara el `Model` para la vista.
- `service/`: concentra las reglas de negocio y realiza la consulta al servicio externo.
- `dto/`: objetos para transportar y adaptar la respuesta externa.
- `config/`: configuración técnica, incluyendo `RestTemplate`.
- `templates/`: Vista Thymeleaf.
- `static/`: CSS, JavaScript e imágenes de la plantilla.

No existe repositorio porque este ejercicio no necesita persistencia propia. Por lo tanto, no se crea un `Repository` artificial.

### Regla de comunicación entre capas

El `WeatherController` se comunica únicamente con `WeatherService`.

El `WeatherService` no accede a ningún repositorio ajeno. En este ejercicio no existe repositorio: la fuente de datos es la API externa OpenWeather.

La vista no consume OpenWeather directamente. El recorrido es:

`Navegador -> Controller -> Service -> OpenWeather API -> Service -> Controller -> Thymeleaf -> Navegador`

## Requisitos

- Java 21 o superior
- Maven 3.9+
- Una API key de OpenWeather

## Configurar la API key

### Windows CMD

```cmd
set OPENWEATHER_API_KEY=TU_API_KEY
mvn spring-boot:run
```

### Windows PowerShell

```powershell
$env:OPENWEATHER_API_KEY="TU_API_KEY"
mvn spring-boot:run
```

### IntelliJ / Eclipse

Crear una variable de entorno:

`OPENWEATHER_API_KEY=TU_API_KEY`

También se puede configurar desde la configuración de ejecución.

## Ejecutar

```bash
mvn clean spring-boot:run
```

Luego abrir:

`http://localhost:8080/`

La pantalla inicia consultando **Mendoza**. Desde el buscador se puede consultar otra ciudad de Argentina.

## Qué demuestra el ejercicio

1. El BackEnd consume una API REST externa mediante `RestTemplate`.
2. El FrontEnd no se comunica directamente con OpenWeather.
3. La API key permanece en una variable de entorno.
4. El Service contiene las reglas de negocio y adaptación de datos.
5. El Controller no contiene reglas de negocio.
6. La plantilla Bootstrap se reutiliza como interfaz visual.
7. La respuesta externa se transforma a un DTO propio antes de llegar a la vista.

## API utilizada

OpenWeather Current Weather Data:

`https://api.openweathermap.org/data/2.5/weather`

Parámetros utilizados:

- `q`: ciudad
- `appid`: API key
- `units=metric`: temperatura en Celsius
- `lang=es`: descripción en español

La documentación oficial indica que la API de clima actual admite `units=metric` y `lang`, y requiere una API key. Para búsquedas por nombre de ciudad, OpenWeather mantiene el parámetro `q`; su documentación actual recomienda el Geocoding API para una conversión más precisa de nombres a coordenadas.

## Importante sobre la API key

El proyecto viene preparado para funcionar, pero **no incluye una API key real**. Debes colocar la tuya en `OPENWEATHER_API_KEY`.

Si acabás de crear la clave, OpenWeather puede tardar un tiempo en activarla.
