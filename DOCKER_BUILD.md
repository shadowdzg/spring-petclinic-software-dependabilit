# Docker Build Guide for Spring PetClinic

This document describes how to build and run the Spring PetClinic application in Docker containers with optional OpenJML verification support.

## Quick Start

### Build and Run with Docker Compose

```bash
# Build and start all services (MySQL + Application)
docker compose up --build

# Or build separately
docker compose build
docker compose up
```

The application will be available at: http://localhost:8080

### Build with OpenJML Verification

To enable OpenJML verification during the Docker build (slower but verifies JML annotations):

```bash
# Build with OpenJML enabled
docker compose build --build-arg ENABLE_OPENJML=true app

# Or using docker build directly
docker build --build-arg ENABLE_OPENJML=true -t spring-petclinic-app .
```

## Docker Architecture

### Multi-Stage Build

The Dockerfile uses a multi-stage build process:

1. **Build Stage** (`eclipse-temurin:17-jdk`):
   - Installs OpenJML (optional, controlled by `ENABLE_OPENJML` build arg)
   - Downloads Maven dependencies
   - Compiles the application
   - Runs OpenJML verification (if enabled)
   - Creates the JAR file

2. **Runtime Stage** (`eclipse-temurin:17-jre`):
   - Minimal runtime image
   - Contains only the built JAR
   - Runs as non-root user for security
   - Includes health checks

### Docker Compose Services

- **mysql**: MySQL 9.5 database
  - Port: 3306
  - Database: `petclinic`
  - User: `petclinic` / Password: `petclinic`
  - Health checks enabled

- **app**: Spring PetClinic application
  - Port: 8080
  - Depends on MySQL
  - Health checks via Spring Boot Actuator

## Build Arguments

| Argument | Default | Description |
|----------|---------|-------------|
| `ENABLE_OPENJML` | `false` | Enable OpenJML verification during build |

## JML Annotations

The project includes JML (Java Modeling Language) annotations in comment form:
- `// @ requires` - Preconditions
- `// @ ensures` - Postconditions
- `// @ pure` - Read-only methods

These annotations are present in:
- `Owner.java` (5 methods)
- `OwnerController.java` (5 methods)
- `OwnerRepository.java` (2 methods)
- `PetController.java` (2 methods)
- `Pet.java` (1 method)

## Health Checks

The application includes health checks:
- **Docker healthcheck**: Checks Spring Boot Actuator endpoint
- **Interval**: 30 seconds
- **Timeout**: 10 seconds
- **Start period**: 60 seconds

Access health endpoint: http://localhost:8080/actuator/health

## Useful Commands

```bash
# View logs
docker compose logs -f app

# Stop services
docker compose down

# Stop and remove volumes
docker compose down -v

# Rebuild without cache
docker compose build --no-cache

# Run in detached mode
docker compose up -d

# Check container status
docker compose ps
```

## Environment Variables

The application can be configured via environment variables in `docker-compose.yml`:

- `SPRING_PROFILES_ACTIVE`: Active Spring profile (default: `mysql`)
- `MYSQL_URL`: Database connection URL
- `MYSQL_USER`: Database username
- `MYSQL_PASS`: Database password

## Troubleshooting

### Build fails with OpenJML

If OpenJML verification fails, you can:
1. Build without OpenJML: `docker compose build` (default)
2. Check OpenJML version in Dockerfile
3. OpenJML verification is optional and doesn't block the build

### Application won't start

1. Check MySQL is healthy: `docker compose ps`
2. Check application logs: `docker compose logs app`
3. Verify database connection settings in `docker-compose.yml`

### Port conflicts

If port 8080 or 3306 are already in use:
- Change ports in `docker-compose.yml`
- Or stop the conflicting service

## Image Size

- **Build stage**: ~1-2 GB (includes JDK and build tools)
- **Runtime stage**: ~300-400 MB (JRE + application JAR)

## Security Features

- Runs as non-root user (`spring:spring`)
- Minimal runtime image (JRE only)
- Health checks for monitoring
- No unnecessary packages in runtime image

## Next Steps

1. **Push to DockerHub**: Tag and push the image to DockerHub
2. **Deploy to Kubernetes**: Use the provided `k8s/` manifests
3. **CI/CD Integration**: Add Docker build to your CI/CD pipeline
