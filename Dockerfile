# -------- Stage 1: Build the app --------
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copy pom and source code
COPY pom.xml .
COPY src ./src

# Build jar
RUN mvn clean package -DskipTests


# -------- Stage 2: Run the app --------
FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app

# Copy jar from build stage
COPY --from=build /app/target/mail-ai-0.0.1-SNAPSHOT.jar app.jar

# Render uses PORT env variable (usually 10000)
EXPOSE 10000

ENTRYPOINT ["java", "-jar", "app.jar"]