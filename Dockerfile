# =========================================================
# Stage 1: Build the Spring Boot application
# =========================================================
FROM maven:3.9.11-eclipse-temurin-21 AS builder

WORKDIR /build

# Copy dependency definition first
COPY pom.xml .

# Download dependencies
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests


# =========================================================
# Stage 2: Runtime image
# =========================================================
FROM eclipse-temurin:21-jre

WORKDIR /app

# Create a non-root user
RUN useradd --system --create-home --shell /usr/sbin/nologin springuser

# Copy only the generated JAR from the build stage
COPY --from=builder /build/target/*.jar app.jar

# Give the application user ownership
RUN chown springuser:springuser app.jar

# Run the application as a non-root user
USER springuser

EXPOSE 8080

# JVM configuration suitable for containers
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-XX:InitialRAMPercentage=25.0", "-jar", "app.jar"]