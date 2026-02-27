# Use Java 17 runtime
FROM eclipse-temurin:17-jdk-jammy


# Set working directory
WORKDIR /app

# Copy jar file into container
COPY target/mail-ai-0.0.1-SNAPSHOT.jar app.jar

# Expose port
EXPOSE 8080

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]
