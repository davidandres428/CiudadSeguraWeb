# Etapa 1: Compilar la aplicación usando Maven global y Java 21
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copiar el archivo de dependencias y el código fuente
COPY pom.xml .
COPY src ./src

# Compilar el proyecto ignorando las pruebas
RUN mvn clean package -DskipTests

# Etapa 2: Ejecutar la aplicación con una imagen ligera de Java 21 (JRE)
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copiar el JAR generado desde la primera etapa de compilación
COPY --from=build /app/target/*.jar app.jar

# Exponer el puerto y arrancar la app
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]