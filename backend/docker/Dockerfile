# docker/Dockerfile
# Usage:
#  1) Build JAR first: ./gradlew :integration:bootJar
#  2) docker build -f docker/Dockerfile -t your/app:tag .

FROM eclipse-temurin:21 AS runtime

# Create non-root user (Debian/Ubuntu base)
RUN addgroup --system appgroup \
    && adduser --system --ingroup appgroup --disabled-login --disabled-password --no-create-home appuser

WORKDIR /app

# Copy fat jar from the Gradle build output
ARG JAR_FILE=integration/build/libs/*-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar

# Expose HTTP port
EXPOSE 8080

# Run as non-root
USER appuser:appgroup

# Use 'deploy' profile by default; DB creds are provided via env (DB_URL/DB_USER/DB_PASS)
ENTRYPOINT ["java","-jar","/app/app.jar","--spring.profiles.active=deploy"]
