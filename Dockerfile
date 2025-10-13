# Stage 1: build with Maven (Java 17)
FROM maven:3.9.8-eclipse-temurin-17 AS build
WORKDIR /app

# copy only pom first to leverage cache
COPY pom.xml .
RUN mvn -B -f pom.xml -q dependency:go-offline

# copy source and build
COPY src ./src
RUN mvn -B -f pom.xml -DskipTests package

# Stage 2: run the jar
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar"]
