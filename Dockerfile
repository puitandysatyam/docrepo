# ───── Stage 1: Build with Maven 4 RC 4 + Java 21 ─────
FROM maven:4.0.0-rc-4-eclipse-temurin-21 AS build
WORKDIR /app

# Copy pom.xml first for caching
COPY pom.xml .
COPY mvnw* ./
COPY .mvn .mvn

# Download dependencies
RUN mvn -B -f pom.xml dependency:go-offline

# Copy source code and build
COPY src ./src
RUN mvn -B -f pom.xml -DskipTests package

# ───── Stage 2: Run with JRE 21 ─────
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Copy the Spring Boot JAR from the build stage
# Replace the filename with your actual JAR name if needed
COPY --from=build /app/target/docrepo-0.0.1-SNAPSHOT.jar app.jar

# Expose Spring Boot port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
