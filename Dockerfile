# Private Nexus Docker registry 
ARG DOCKER_PRIVATE_REPO=16-52-79-103.sslip.io/myapp-docker-group

# ---- Build stage ----
FROM ${DOCKER_PRIVATE_REPO}/maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /build

# Copy the Maven descriptor first (better caching)
COPY pom.xml .

# Copy the rest of the project
COPY . .

# Build the application
RUN mvn clean package

# ---- Runtime stage ----
FROM ${DOCKER_PRIVATE_REPO}/eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy the built JAR from the build stage
COPY --from=build /build/target/catalog-service-*.jar app.jar

# Expose port
EXPOSE 8081

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8081/api/catalog/health || exit 1

# Run the application
ENTRYPOINT ["java","-jar","app.jar"]
