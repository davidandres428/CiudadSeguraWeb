FROM eclipse-temurin:25-jre
WORKDIR /app

COPY *.jar app.jar

ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]