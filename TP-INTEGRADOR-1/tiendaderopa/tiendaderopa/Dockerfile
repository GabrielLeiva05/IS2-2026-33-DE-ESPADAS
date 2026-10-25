FROM eclipse-temurin:25-jdk
ARG JAR_FILE=target/tiendaderopa-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} tiendaderopa.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "tiendaderopa.jar"]