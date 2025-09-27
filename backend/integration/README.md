Integration Module (Composition Root)

Purpose

- Spring Boot application that wires all modules and runs the service.
- Scans packages `ie.universityofgalway.groupnine.*` and entities under `infrastructure`.

Entry Point

- `IntegrationApplication` (main class)

Run

- `./gradlew :integration:bootRun`
- Or build and run JAR: `./gradlew :integration:bootJar` then `java -jar integration/build/libs/*-SNAPSHOT.jar`

Configuration

- Default app config: `src/main/resources/application.yml` (port 8080, Postgres).
- For container/deploy, prefer standard Spring env vars: `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`,
  `SPRING_DATASOURCE_PASSWORD`, etc.

Testing

- `IntegrationSmokeTest` ensures the Spring context starts.
- `IntegrationApplicationAllTests` sanity tests for annotations and main method.

