Backend (Clean Architecture) — Monorepo Modules

Overview

- Multi-module Spring Boot backend using Clean Architecture:
    - `domain`: business types and rules (framework-agnostic)
    - `service`: use cases/application services (depends on `domain`)
    - `infrastructure`: adapters to external systems (e.g., database), implements service ports
    - `delivery/rest`: HTTP controllers and API error handling
    - `security`: JWT-based security and route configuration
    - `integration`: composition root (Spring Boot app) wiring everything together
    - `test-support`: common test utilities/annotations
    - `archrules`: ArchUnit tests enforcing architecture
    - `build-logic`: Gradle convention plugins and shared build config
    - `docker`: Dockerfiles and Compose for local/dev

Quickstart

- Requirements
    - Java 17 (toolchain configured via Gradle)
    - Docker optional (for Postgres and containerized app)
    - No global Gradle needed (wrapper included)

- Run locally (without Docker)
    1) Start a Postgres instance and update `integration/src/main/resources/application.yml` if needed
    2) Run the app: `./gradlew :integration:bootRun`
    3) Health endpoint: `GET http://localhost:8080/api/health`

- Run with Docker Compose (local dev)
    - `docker compose -f docker/compose.local.yml up --build`
    - Exposes: app on `8080`, Postgres on `5432`

- Build runnable JAR
    - `./gradlew :integration:bootJar`
    - JAR: `integration/build/libs/*-SNAPSHOT.jar`

- Run tests and coverage
    - All tests: `./gradlew check`
    - Per-module coverage thresholds enforced via JaCoCo
    - Aggregate report: `./gradlew jacocoRootReport` (output in each module’s `build/reports/jacoco/test/html`)

Security

- JWT resource-server with HMAC (HS256). Defaults from `RouteProperties`:
    - Permit: `/api/health`, Actuator `/actuator/info`
    - Authenticated: `/api/**` (unless explicitly permitted)
    - Role mapping: claim `roles` mapped to `ROLE_*`
- Provide secrets via env or config (see `security/README.md`)

Project Structure and Responsibilities

- Domain: Entities, value objects, events; no Spring/JPA/web dependencies
- Service: Orchestrates use cases; depends only on Domain; defines ports (e.g., `DatabaseProbe`)
- Infrastructure: Adapters implementing ports (e.g., JDBC `DatabaseProbeImpl`); depends on Service & Domain
- Delivery/REST: Controllers, DTOs, and global exception handler; depends on Service & Domain
- Security: Spring Security config, route properties, JWT decoder, JSON error handlers
- Integration: Spring Boot application (`IntegrationApplication`) scanning all modules; includes application config
- Test-Support: MVC slice test helpers and annotations
- Archrules: ArchUnit tests enforcing layers and purity
- Build-Logic: Convention plugins applying common dependencies, testing, and coverage settings

Endpoints

- `GET /api/health` — returns `{ "status": "UP|DOWN" }` (503 when DOWN)

Configuration

- Default app config: `integration/src/main/resources/application.yml` (port 8080, Postgres URL/creds)
- Security config properties bean: `app.security.*` (see `security/RouteProperties`)
- For deployments, use standard Spring env vars, e.g.:
    - `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`
    - `APP_SECURITY_JWT_HMAC-SECRET`, `APP_SECURITY_JWT_ISSUER`, `APP_SECURITY_JWT_AUTHORITIES-CLAIM`
    - Note: The sample `docker/compose.deploy.yml` uses `DB_URL/DB_USER/DB_PASS` placeholders — replace with Spring’s
      `SPRING_DATASOURCE_*` or add a deploy profile config.

Build & Conventions

- Gradle Kotlin DSL with a version catalog (`gradle/libs.versions.toml`)
- Custom convention plugins under `build-logic/` standardize testing, coverage, and dependencies per module type
- Coverage thresholds per module via `coverage { minimum = ... }` in each `build.gradle.kts`

Common Tasks

- `./gradlew clean build` — compile + test all modules
- `./gradlew :delivery:rest:test` — run delivery layer tests
- `./gradlew verifyCoverageAll` — run checks across all modules with coverage gates

Troubleshooting

- 401/403 responses: ensure JWT secret/issuer and roles claim match your tokens
- DB health DOWN: verify Postgres connectivity and credentials in `application.yml`
- Coverage failures: open the HTML report under each module `build/reports/jacoco/test/html`

