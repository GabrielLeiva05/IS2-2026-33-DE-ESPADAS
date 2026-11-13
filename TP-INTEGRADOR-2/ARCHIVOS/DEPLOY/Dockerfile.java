# Etapa 1: compilacion con Java 25
FROM eclipse-temurin:25-jdk AS build

ARG MODULE

WORKDIR /src

COPY ${MODULE}/ ./

RUN sed -i 's/\r$//' mvnw .mvn/wrapper/maven-wrapper.properties \
    && chmod +x mvnw \
    && ./mvnw -B -DskipTests package \
    && cp target/*.jar /app.jar

# Etapa 2: ejecucion con Java 25 JRE
FROM eclipse-temurin:25-jre

WORKDIR /app

RUN groupadd --system app \
    && useradd --system --gid app --no-create-home app

COPY --from=build --chown=app:app /app.jar /app/app.jar

USER app

ENTRYPOINT ["java", "-jar", "/app/app.jar"]