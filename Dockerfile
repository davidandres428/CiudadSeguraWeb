# Etapa 1: Compilar el proyecto con Maven Wrapper y Java 21
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Copiar archivos de configuración y dependencias de Maven Wrapper
COPY .mvn .mvn
COPY mvnw pom.xml ./
COPY src ./src

# Dar permisos de ejecución al wrapper y compilar el proyecto
RUN chmod +x mvnw
RUN ./mvnw clean package -DskipTests

# Etapa 2: Ejecutar la aplicación con una imagen ligera de Java 21 (JRE)
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copiar el archivo jar generado desde la primera etapa
COPY --from=build /app/target/*.jar app.jar

# Exponer el puerto y ejecutar la aplicación
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]