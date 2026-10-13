FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /workspace

# Cache dependencies separately from application source changes.
COPY pom.xml .
RUN mvn -B -DskipTests dependency:go-offline

COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Run the API as an unprivileged user inside the container.
RUN addgroup -S nightgrid && adduser -S nightgrid -G nightgrid

COPY --from=build /workspace/target/*.jar app.jar

USER nightgrid
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
