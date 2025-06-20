# Dockerfile
FROM openjdk:17-jdk-slim

LABEL maintainer="ecommerce-team@company.com"
LABEL description="E-commerce Config Server"

# Create app directory
WORKDIR /app

# Create logs directory
RUN mkdir -p /var/log

# Copy Maven wrapper and pom.xml
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Download dependencies
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src/ src/

# Build application
RUN ./mvnw package -DskipTests

# Create non-root user
RUN groupadd -r configserver && useradd -r -g configserver configserver

# Change ownership of app directory
RUN chown -R configserver:configserver /app /var/log

# Switch to non-root user
USER configserver

# Expose port
EXPOSE 8888

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8888/actuator/health || exit 1

# Run application
ENTRYPOINT ["java", "-jar", "target/config-server-1.0.0.jar", "--spring.profiles.active=docker"]