# Un solo Dockerfile para las tres apps Spring Boot.
# Se construye desde la carpeta dulce-mayor/ pasando MODULE=backend | admin-web | institucional-web
FROM eclipse-temurin:25-jdk AS build
ARG MODULE
WORKDIR /src
COPY ${MODULE}/ ./
# mvnw y su .properties pueden venir con saltos de línea de Windows (CRLF) y fallar en Linux
RUN sed -i 's/\r$//' mvnw .mvn/wrapper/maven-wrapper.properties && chmod +x mvnw \
 && ./mvnw -B -q -DskipTests package \
 && cp target/*.jar /app.jar

FROM eclipse-temurin:25-jre
WORKDIR /app
RUN useradd --system --no-create-home appuser
COPY --from=build /app.jar app.jar
USER appuser
ENTRYPOINT ["java", "-jar", "app.jar"]
