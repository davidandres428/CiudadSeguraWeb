FROM eclipse-temurin:21-jre
WORKDIR /app

# Copia el JAR generado en tu equipo
COPY target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]