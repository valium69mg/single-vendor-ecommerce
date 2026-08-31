# ───────────────────────────────────────
# Build stage (Maven / JDK 21)
# ───────────────────────────────────────
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /build

# Cache dependencies on pom.xml alone
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

# Build the jar
COPY src ./src
RUN mvn -B -q clean package -DskipTests

# ───────────────────────────────────────
# Runtime stage (JRE 21)
# ───────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

# Jar produced by the build stage
COPY --from=builder /build/target/single-vendor-ecommerce.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
