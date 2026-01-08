FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy the jar file
COPY target/catalog-service-1.0.0.jar app.jar

# Expose port
EXPOSE 8081

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8081/api/catalog/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]

