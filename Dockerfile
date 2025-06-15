# Build stage
FROM gradle:8.5-jdk17 AS builder

# Set working directory
WORKDIR /app

# Copy gradle files first to cache dependencies
COPY build.gradle.kts settings.gradle.kts ./
COPY gradle ./gradle

# Download dependencies
RUN gradle dependencies --no-daemon

# Copy source code
COPY src ./src

# Build the application
RUN gradle build --no-daemon -x test

# Runtime stage
FROM eclipse-temurin:17-jre-jammy

# Set working directory
WORKDIR /app

# Add a non-root user
RUN useradd -r -u 1001 -g root springuser
USER springuser

# Set environment variables
ENV SERVER_PORT=8080
ENV SPRING_PROFILES_ACTIVE=prod

# Copy the built artifact from builder stage
COPY --from=builder /app/build/libs/*.jar app.jar

# Expose the application port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", \
            "-XX:+UseContainerSupport", \
            "-XX:MaxRAMPercentage=75.0", \
            "-Djava.security.egd=file:/dev/./urandom", \
            "-jar", "app.jar"]