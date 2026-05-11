# Build stage
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /app

# Copy parent pom
COPY pom.xml .
COPY common/pom.xml common/
COPY api-gateway/pom.xml api-gateway/
COPY collaborator-service/pom.xml collaborator-service/
COPY contract-service/pom.xml contract-service/
COPY notification-service/pom.xml notification-service/

# Download dependencies
RUN mvn dependency:go-offline -B -pl api-gateway -am

# Copy source
COPY common/src common/src
COPY api-gateway/src api-gateway/src

# Build
RUN mvn clean package -DskipTests -pl api-gateway -am

# Runtime stage
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Add non-root user
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=builder /app/api-gateway/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
