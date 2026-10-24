# App Clima

Aplicación del clima con **Spring Boot** y **React**. El backend consulta OpenWeather y conserva la clave fuera del navegador; React permite buscar una ciudad y muestra temperatura, sensación térmica, humedad, viento y presión.

## Requisitos

- Java 21 o superior
- Maven 3.9+
- Node.js 20.19+ o 22.12+
- Una API key de [OpenWeather](https://openweathermap.org/api)

## Configurar y ejecutar

1. Crea el archivo `.env` en la raíz del proyecto, junto a `pom.xml`, copiando `.env.example`:

   ```powershell
   Copy-Item .env.example .env
   ```

   Abre `.env` y reemplaza el valor de `OPENWEATHER_API_KEY` por tu clave de OpenWeather. Spring Boot importa ese archivo al iniciar; no hace falta definir la variable en la consola.

2. Inicia el backend desde la raíz del proyecto, donde se encuentra `.env`:

   ```powershell
   mvn spring-boot:run
   ```

3. En otra terminal, inicia React:

   ```powershell
   cd frontend
   npm install
   npm run dev
   ```

4. Abre la URL que muestra Vite (por defecto `http://localhost:5173`).

El servidor expone `GET /api/weather?city=Buenos%20Aires`. Durante el desarrollo, Vite redirige `/api` a Spring Boot en `http://localhost:8080`.

## Pruebas y compilación

Backend:

```powershell
mvn test
```

Frontend:

```powershell
cd frontend
npm run build
```

La clave de OpenWeather nunca debe guardarse en el repositorio. `.env` está excluido por `.gitignore`; `.env.example` es solo una plantilla y no contiene una clave real.
