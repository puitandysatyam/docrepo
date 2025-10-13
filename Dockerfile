# ───── Stage 1: Build with Maven 4 and Java 21 ─────
FROM maven:4.0.0-eclipse-temurin-21 AS build
WORKDIR /app

# Copy pom first 
COPY pom.xml .
# Optional: include Maven wrapper if present
COPY mvnw* ./
COPY .mvn .mvn

# Pre-download dependencies to speed up rebuilds
RUN mvn -B -f pom.xml -q dependency:go-offline

# Copy source and build the JAR
COPY src ./src
RUN mvn -B -f pom.xml -DskipTests package


# ───── Stage 2: Run the app on a slim JRE 21 image ─────
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Copy the built JAR from previous stage
COPY --from=build /app/target/*.jar app.jar

# Expose the port used by your Spring Boot app
EXPOSE 8080

# Run the application
ENTRYPOINT ["java","-jar","/app.jar"]
