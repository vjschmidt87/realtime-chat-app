# ---- Stage 1: Build ----
FROM maven:3.9-eclipse-temurin-21 AS build

LABEL maintainer="realtime-chat-app"
LABEL description="Real-Time Chat Application - Spring Boot Backend"
LABEL version="1.0.0"

WORKDIR /app

# Copy the POM first to leverage Docker layer caching for dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy the source code and build the application
COPY src ./src
RUN mvn clean package -DskipTests -B

# ---- Stage 2: Runtime ----
FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="realtime-chat-app"
LABEL description="Real-Time Chat Application - Spring Boot Backend (Runtime)"
LABEL version="1.0.0"

WORKDIR /app

# Add a non-root user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy the built jar from the build stage
COPY --from=build /app/target/*.jar app.jar

# Set ownership to the non-root user
RUN chown -R appuser:appgroup /app

USER appuser

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
  CMD wget -qO- http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
