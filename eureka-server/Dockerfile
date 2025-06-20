# Multi-stage build for production optimization
FROM eclipse-temurin:17-jdk as builder

# Set working directory
WORKDIR /app

# Copy Maven wrapper and pom.xml
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Download dependencies
RUN chmod +x ./mvnw && ./mvnw dependency:go-offline -B

# Copy source code
COPY src src

# Build application
RUN ./mvnw clean package -DskipTests

# Production stage
FROM eclipse-temurin:17-jre

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Create app user
RUN groupadd -r eureka && useradd -r -g eureka eureka

# Set working directory
WORKDIR /app

# Copy jar from builder stage
COPY --from=builder /app/target/*.jar eureka-server.jar

# Create logs directory
RUN mkdir -p logs && chown -R eureka:eureka /app

# Switch to non-root user
USER eureka

# Environment variables
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:+UseContainerSupport" \
    SPRING_PROFILES_ACTIVE="docker" \
#    EUREKA_USERNAME="admin" \
#    EUREKA_PASSWORD="admin123" \
    EUREKA_HOSTNAME="eureka-server" \
    EUREKA_SELF_PRESERVATION="true"

# Expose port
EXPOSE 8761

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8761/actuator/health || exit 1

# Add labels
LABEL maintainer="ecommerce-team@company.com" \
      version="1.0.0" \
      description="Eureka Server for E-commerce Microservices Platform"

# Run application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar eureka-server.jar"]