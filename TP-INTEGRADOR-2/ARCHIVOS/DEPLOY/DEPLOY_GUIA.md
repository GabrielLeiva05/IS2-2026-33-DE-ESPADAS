# Guía Definitiva de Despliegue en DonWeb (Java 25 + React Vite + Docker + GitHub + PuTTY)

Como experto en infraestructura y deploys en DonWeb con más de diez años de experiencia, he actualizado esta guía técnica paso a paso. Está adaptada para servidores Cloud VPS de DonWeb utilizando **Windows** como estación de trabajo local, **PuTTY** para la conexión SSH, **Docker** para la contenerización, **GitHub** como repositorio de código, **Java 25** para el backend y **React con Vite** para las 4 vistas del frontend.

## Arquitectura del Escenario

* **Entorno Local:** Windows 10 / 11.
* **Herramienta SSH:** PuTTY y PuTTYgen.
* **Servidor Remoto:** VPS Linux (Ubuntu Server en DonWeb).
* **Backend:** Java 25 (ej. Spring Boot) corriendo en el puerto `8080`.
* **Frontend:** 4 aplicaciones independientes de **React con Vite** corriendo cada una en su propio puerto (`3001`, `3002`, `3003`, `3004`), comunicándose con el backend mediante API REST.
* **Orquestación:** Docker y Docker Compose.

---

## Fase 1: Preparación del Acceso SSH desde Windows con PuTTY

1. **Conversión de Claves SSH:**
   * Abre **PuTTYgen** en tu PC con Windows.
   * Haz clic en **Load** y selecciona tu archivo `.pem` provisto por DonWeb.
   * Haz clic en **Save private key** para generar el archivo compatible `.ppk` (ej. `donweb_key.ppk`).

2. **Configuración de PuTTY:**
   * Abre **PuTTY**.
   * En **Session**, ingresa la IP de tu VPS de DonWeb en *Host Name (or IP address)* y puerto `22`.
   * Navega a **Connection** > **SSH** > **Auth** > **Credentials**.
   * En *Private key file for authentication*, haz clic en **Browse** y selecciona tu archivo `.ppk`.
   * Haz clic en **Open** para iniciar la sesión con tu usuario (ej. `root`).

---

## Fase 2: Instalación de Dependencias en el VPS de DonWeb

Conéctate a tu VPS y ejecuta los comandos para instalar Docker y Git:

```bash
# Actualizar el sistema
sudo apt update && sudo apt upgrade -y

# Instalar prerrequisitos
sudo apt install apt-transport-https ca-certificates curl software-properties-common git -y

# Instalar Docker y Docker Compose v2
sudo apt install docker-ce docker-ce-cli containerd.io docker-compose-plugin -y

# Verificar versiones
sudo docker --version
sudo docker compose version
```

---

## Fase 3: Clonación del Repositorio de GitHub

1. Crea el directorio de trabajo en tu servidor:
   ```bash
   mkdir -p /var/www/mi-proyecto
   cd /var/www/mi-proyecto
   ```

2. Clona tu repositorio usando tu token de GitHub o llave SSH:
   ```bash
   git clone https://<tu-token>@github.com/tu-usuario/tu-repositorio.git .
   ```

---

## Fase 4: Configuración de Dockerfiles (Java 25 y React Vite)

### 1. Dockerfile del Backend (Java 25)
Utilizaremos la imagen oficial de Eclipse Temurin 25 para Java:

```dockerfile
# Dockerfile.backend
FROM eclipse-temurin:25-jdk-alpine
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 2. Dockerfile para las 4 Vistas de React con Vite (Multi-stage con Nginx)
Dado que usas **Vite**, el comando de compilación genera la carpeta `dist` por defecto:

```dockerfile
# Dockerfile.frontend (usar en cada una de las 4 carpetas de vistas)
# Etapa 1: Build con Node
FROM node:18-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

# Etapa 2: Servidor Web Nginx para producción
FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

---

## Fase 5: Orquestación con Docker Compose

Crea el archivo `docker-compose.yml` en la raíz del proyecto para desplegar el backend en Java 25 y las 4 vistas de React con Vite en sus puertos independientes:

```yaml
version: '3.8'

services:
  backend-java:
    build:
      context: .
      dockerfile: Dockerfile.backend
    container_name: backend-java-25
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
    restart: always

  frontend-vista1:
    build:
      context: ./front-vista1
      dockerfile: Dockerfile.frontend
    container_name: react-vite-vista-1
    ports:
      - "3001:80"
    depends_on:
      - backend-java
    restart: always

  frontend-vista2:
    build:
      context: ./front-vista2
      dockerfile: Dockerfile.frontend
    container_name: react-vite-vista-2
    ports:
      - "3002:80"
    depends_on:
      - backend-java
    restart: always

  frontend-vista3:
    build:
      context: ./front-vista3
      dockerfile: Dockerfile.frontend
    container_name: react-vite-vista-3
    ports:
      - "3003:80"
    depends_on:
      - backend-java
    restart: always

  frontend-vista4:
    build:
      context: ./front-vista4
      dockerfile: Dockerfile.frontend
    container_name: react-vite-vista-4
    ports:
      - "3004:80"
    depends_on:
      - backend-java
    restart: always
```

---

## Fase 6: Configuración de la Comunicación API REST

1. **Variables de Entorno en React (Vite):**
   En cada aplicación Vite, las variables de entorno comienzan con `VITE_`. Crea un archivo `.env.production` en cada vista:
   ```env
   VITE_API_BASE_URL=http://<IP_DE_TU_VPS_DONWEB>:8080/api
   ```
   *(Nota: En código, accederás a ella mediante `import.meta.env.VITE_API_BASE_URL`)*.

2. **CORS en Backend (Java 25):**
   Asegúrate de permitir conexiones desde los puertos del front en tu configuración de Spring Boot:
   ```java
   @Configuration
   public class CorsConfig implements WebMvcConfigurer {
       @Override
       public void addCorsMappings(CorsRegistry registry) {
           registry.addMapping("/api/**")
                   .allowedOrigins(
                       "http://localhost:3001", "http://localhost:3002", 
                       "http://localhost:3003", "http://localhost:3004",
                       "http://<IP_DE_TU_VPS_DONWEB>:3001", "http://<IP_DE_TU_VPS_DONWEB>:3002",
                       "http://<IP_DE_TU_VPS_DONWEB>:3003", "http://<IP_DE_TU_VPS_DONWEB>:3004"
                   )
                   .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS");
       }
   }
   ```

---

## Fase 7: Ejecución y Despliegue en DonWeb

1. **Levantar contenedores en el VPS:**
   ```bash
   sudo docker compose up --build -d
   ```

2. **Abrir Puertos en el Firewall (UFW):**
   ```bash
   sudo ufw allow 8080/tcp
   sudo ufw allow 3001/tcp
   sudo ufw allow 3002/tcp
   sudo ufw allow 3003/tcp
   sudo ufw allow 3004/tcp
   sudo ufw reload
   ```

---

## Fase 8: Actualizaciones Continuas

Para actualizar tu sistema tras realizar cambios en GitHub desde Windows:
```bash
cd /var/www/mi-proyecto
git pull origin main
sudo docker compose up --build -d
```