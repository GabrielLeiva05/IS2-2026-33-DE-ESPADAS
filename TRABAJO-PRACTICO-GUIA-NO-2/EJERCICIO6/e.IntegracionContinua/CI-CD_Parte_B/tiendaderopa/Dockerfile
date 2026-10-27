FROM eclipse-temurin:25-jre

WORKDIR /app

COPY --chown=10001:0 target/*.jar app.jar

USER 10001

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
