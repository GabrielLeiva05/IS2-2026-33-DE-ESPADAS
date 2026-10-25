FROM eclipse-temurin:25-jdk AS build
WORKDIR /app

COPY . .
RUN chmod +x ./mvnw && ./mvnw clean package -DskipTests

FROM eclipse-temurin:25-jdk
WORKDIR /app

COPY --from=build /app/target/tiendaderopa-0.0.1-SNAPSHOT.jar tiendaderopa.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "tiendaderopa.jar"]