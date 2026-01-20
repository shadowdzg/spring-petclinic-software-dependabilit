# Multi-stage build for Spring PetClinic with OpenJML support
FROM eclipse-temurin:17-jdk AS build

WORKDIR /app

# Install OpenJML (optional, for JML verification)
# Set ENABLE_OPENJML=true to enable OpenJML verification during build
ARG ENABLE_OPENJML=false
ENV OPENJML_VERSION=0.24.0-2024.01.20
ENV OPENJML_HOME=/opt/openjml

# Download and install OpenJML if enabled
RUN if [ "$ENABLE_OPENJML" = "true" ]; then \
    apt-get update && \
    apt-get install -y wget unzip && \
    mkdir -p ${OPENJML_HOME} && \
    wget -q https://github.com/OpenJML/OpenJML/releases/download/${OPENJML_VERSION}/openjml-${OPENJML_VERSION}-linux.zip -O /tmp/openjml.zip && \
    unzip -q /tmp/openjml.zip -d ${OPENJML_HOME} && \
    chmod +x ${OPENJML_HOME}/openjml && \
    rm /tmp/openjml.zip && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*; \
    fi

# Add OpenJML to PATH if enabled
ENV PATH=${PATH}:${OPENJML_HOME}

# Copy Maven wrapper and pom.xml
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Make mvnw executable
RUN chmod +x mvnw

# Download dependencies (this layer will be cached if pom.xml doesn't change)
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application (skip tests and checkstyle for faster builds)
RUN ./mvnw clean package -DskipTests -Dcheckstyle.skip=true -B

# Optional: Run OpenJML verification on annotated files if enabled
# Note: This is a basic verification - for full verification, run OpenJML separately
RUN if [ "$ENABLE_OPENJML" = "true" ]; then \
    echo "Running OpenJML verification on JML-annotated files..." && \
    find src/main/java -name "*.java" -type f -exec grep -l "// @" {} \; | head -5 | \
    while read file; do \
        echo "Checking JML annotations in: $file" && \
        ${OPENJML_HOME}/openjml -check "$file" 2>&1 | head -20 || echo "Note: Full verification may require additional setup"; \
    done; \
    echo "OpenJML check completed (basic syntax check)"; \
    fi

# Runtime stage
FROM eclipse-temurin:17-jre

WORKDIR /app

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Create a non-root user for security
RUN groupadd -r spring && useradd -r -g spring spring

# Copy the built JAR from build stage
COPY --from=build /app/target/*.jar app.jar

# Change ownership to non-root user
RUN chown spring:spring app.jar

# Switch to non-root user
USER spring:spring

# Expose port
EXPOSE 8080

# Health check using Spring Boot Actuator
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8081/actuator/health || exit 1

# Run the application with JVM optimizations
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", "app.jar"]

