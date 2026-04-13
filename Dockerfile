# Build stage
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /build

COPY pom.xml .
COPY mvnw .
COPY mvnw.cmd .
COPY .mvn .mvn
COPY src ./src

RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=builder /build/target/secure-project-hub-*.jar app.jar

# Expose port 8080 for HTTP, 8443 for HTTPS (PKI/mTLS), and 5005 for debugging
EXPOSE 8080 8443 5005

# Run with optimized memory settings and remote debugging enabled
# Increased heap size: 512m -> 1024m to handle PKI/certificate operations
# Debug port 5005: -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005
ENTRYPOINT ["java", "-Xms512m", "-Xmx1024m", "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005", "-jar", "app.jar"]
