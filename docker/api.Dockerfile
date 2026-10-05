# syntax=docker/dockerfile:1

# Stage 1: build and unit-test the Spring Boot application.
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

# Dependencies are cached separately from the sources.
COPY apps/api/pom.xml ./pom.xml
RUN mvn -B -q dependency:go-offline

COPY apps/api/src ./src
# Integration tests need Docker, so they run in CI, not in the image build.
RUN mvn -B -q package -DskipITs
RUN java -Djarmode=tools -jar target/*.jar extract --layers --launcher --destination extracted

# Stage 2: minimal runtime with the layered jar and a non-root user.
FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

RUN addgroup -S app && adduser -S -G app app
COPY --from=build /build/extracted/dependencies/ ./
COPY --from=build /build/extracted/spring-boot-loader/ ./
COPY --from=build /build/extracted/snapshot-dependencies/ ./
COPY --from=build /build/extracted/application/ ./

USER app
EXPOSE 8080
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75"
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
