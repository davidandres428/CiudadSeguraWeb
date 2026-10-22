FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /workspace

COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B package -DskipTests

FROM eclipse-temurin:25-jre

WORKDIR /app
ENV PORT=8081
EXPOSE 8081

COPY --from=build --chown=10001:10001 /workspace/target/ciudad-segura-0.0.1-SNAPSHOT.jar /app/app.jar

USER 10001:10001
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
