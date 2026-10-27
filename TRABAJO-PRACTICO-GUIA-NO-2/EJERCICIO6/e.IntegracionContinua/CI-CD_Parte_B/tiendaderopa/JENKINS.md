# Integración continua con Jenkins

El `Jenkinsfile` de este directorio ejecuta las pruebas Maven, publica sus resultados en Jenkins y construye `tiendaderopa:<número-de-build>` y `tiendaderopa:latest` en el daemon Docker local. No publica la imagen en un registro ni inicia la aplicación.

## Requisitos del agente

- Una instalación de JDK 25 configurada en Jenkins con el nombre exacto `JDK25`.
- Docker CLI instalado y operativo en ese mismo agente (`docker version`).
- Acceso de ese CLI al daemon Docker local.

Si Jenkins corre dentro de un contenedor, el contenedor (o el agente que ejecuta el pipeline) necesita el cliente Docker y acceso al socket del host, normalmente montado como `/var/run/docker.sock`. El socket concede al contenedor amplios privilegios sobre el host; úselo únicamente en un entorno local de confianza. Si Jenkins ya está corriendo sin ese montaje, será necesario recrear el contenedor/agente con el montaje habilitado. Un agente remoto construirá la imagen en su propio daemon, no en el Docker del host.

## Crear y cargar el pipeline en Jenkins

1. En **Manage Jenkins → Tools → JDK installations**, agregue o seleccione una instalación de Java 25 y asígnele el nombre `JDK25`. Puede apuntar a un JDK 25 ya instalado en el agente o configurar su instalación automática. Verifique que incluya `javac`; un JRE no alcanza para compilar.
2. Confirme que los archivos `Jenkinsfile`, `Dockerfile` y `.dockerignore` estén guardados en el repositorio y accesibles desde la rama que Jenkins va a construir.
3. En Jenkins, seleccione **New Item**, asigne un nombre y elija **Pipeline**.
4. En **Pipeline**, seleccione **Pipeline script from SCM**, elija **Git** e indique la URL del repositorio. Configure credenciales si el repositorio es privado y seleccione la rama a construir.
5. Configure **Script Path** con la ruta del `Jenkinsfile` desde la raíz del repositorio:
   `TRABAJO-PRACTICO-GUIA-NO-2/EJERCICIO6/e.IntegracionContinua/CI-CD_Parte_B/tiendaderopa/Jenkinsfile`
6. Guarde la configuración y seleccione **Build Now**. El pipeline solicita la herramienta `JDK25`, clona el repositorio, ejecuta `./mvnw -B clean verify` desde el directorio del proyecto y construye la imagen si las pruebas terminan correctamente.
7. Revise **Console Output** y la sección de resultados de pruebas. Para comprobar la imagen desde el host/daemon compartido, ejecute `docker images tiendaderopa`; el tag numérico corresponde al número del build.

El wrapper descarga Maven según `.mvn/wrapper/maven-wrapper.properties`; no es necesario instalar Maven manualmente. El nombre `JDK25` de Jenkins debe coincidir exactamente con el solicitado en el `Jenkinsfile`.

## Construcción y ejecución manual

Desde el directorio del proyecto:

```shell
# Windows
.\mvnw.cmd -B clean verify
docker build -t tiendaderopa:local .
docker run --rm -p 8080:8080 tiendaderopa:local
```

La aplicación necesita una base de datos MySQL o PostgreSQL para ejecutarse. En Docker, `localhost` apunta al contenedor de la aplicación, no al host ni a otro contenedor; configure `DB_URL`, `DB_USERNAME` y las demás variables necesarias para el entorno antes de iniciar la imagen. Las pruebas del pipeline usan H2 y no requieren una base externa.
