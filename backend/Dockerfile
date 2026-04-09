FROM maven:3.9.9-eclipse-temurin-17 AS build

WORKDIR /workspace

COPY mvnw mvnw
COPY .mvn .mvn
COPY pom.xml pom.xml

RUN chmod +x mvnw
RUN ./mvnw -q -DskipTests dependency:go-offline

COPY src src
COPY database database

RUN ./mvnw -q -DskipTests package && cp target/*.jar /workspace/app.jar

FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

RUN groupadd --system spring \
    && useradd --system --gid spring --create-home spring \
    && mkdir -p /app/storage \
    && chown -R spring:spring /app

COPY --from=build /workspace/app.jar /app/app.jar
COPY --from=build /workspace/database /app/database

EXPOSE 8080

USER spring:spring

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
