# ---- Build stage -----------------------------------------------------------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
# Cache dependencies first.
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
# Build the application (skip tests in the image build; CI runs them).
COPY src ./src
RUN mvn -B -q clean package -DskipTests

# ---- Runtime stage ---------------------------------------------------------
FROM eclipse-temurin:21-jre
WORKDIR /app
ENV LANG=C.UTF-8
# Quarkus fast-jar layout.
COPY --from=build /workspace/target/quarkus-app/lib/ ./lib/
COPY --from=build /workspace/target/quarkus-app/*.jar ./
COPY --from=build /workspace/target/quarkus-app/app/ ./app/
COPY --from=build /workspace/target/quarkus-app/quarkus/ ./quarkus/
EXPOSE 8080
USER 1001
ENTRYPOINT ["java", "-jar", "quarkus-run.jar"]
