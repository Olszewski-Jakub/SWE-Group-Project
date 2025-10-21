# docker/Dockerfile (multi-stage)
# Builds the app with Gradle and produces a slim runtime image.

# -------- Build stage --------
FROM eclipse-temurin:17 AS build
WORKDIR /workspace

# Pre-copy Gradle files to leverage layer caching
COPY gradle gradle
COPY gradlew gradlew
COPY settings.gradle.kts settings.gradle.kts
COPY build.gradle.kts build.gradle.kts
COPY gradle.properties gradle.properties
COPY build-logic build-logic

RUN chmod +x gradlew

# Warm Gradle caches (no source yet for better caching)
RUN --mount=type=cache,target=/root/.gradle/caches \
    --mount=type=cache,target=/root/.gradle/wrapper \
    ./gradlew --no-daemon help

# Copy the rest of the source
COPY . .

# Build the fat JAR for the :integration module
RUN --mount=type=cache,target=/root/.gradle/caches \
    --mount=type=cache,target=/root/.gradle/wrapper \
    ./gradlew :integration:bootJar -x test --no-daemon

# -------- Runtime stage --------
FROM eclipse-temurin:17 AS runtime

# Create non-root user (Debian/Ubuntu base)
RUN addgroup --system appgroup \
    && adduser --system --ingroup appgroup --disabled-login --disabled-password --no-create-home appuser

WORKDIR /app

# Copy built JAR from build stage (accept both release and SNAPSHOT)
COPY --from=build /workspace/integration/build/libs/*.jar /app/app.jar

# Expose HTTP port
EXPOSE 8888 8889 8080

# Run as non-root
USER appuser:appgroup

# Use 'deploy' profile by default; DB creds are provided via env (DB_URL/DB_USER/DB_PASS)
ENTRYPOINT ["java","-jar","/app/app.jar","--spring.profiles.active=deploy"]
