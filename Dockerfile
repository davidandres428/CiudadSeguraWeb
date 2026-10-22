# Etapa 1: Compilar el proyecto con Maven y Java 21
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copiar primero el archivo de dependencias y luego el código fuente
COPY pom.xml .
COPY src ./src

# Empaquetar la aplicación ignorando las pruebas
RUN mvn clean package -DskipTests

# Etapa 2: Ejecutar la aplicación con una imagen ligera de Java 21 (JRE)
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copiar el archivo jar generado desde la primera etapa
COPY --from=build /app/target/*.jar app.jar

# Exponer el puerto y ejecutar la aplicación
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
