# ----------- Stage 1: Build -----------
FROM maven:3.9.8-eclipse-temurin-21 AS builder

WORKDIR /app

# Cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code, ui and build
COPY src ./src
COPY ui ./ui
RUN mvn clean package -DskipTests

# ----------- Stage 2: Runtime -----------
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Add a non-root user
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy only the final jar
COPY --from=builder /app/target/*.jar app.jar

# Expose port
EXPOSE 8080

# JVM optimizations for containers
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
