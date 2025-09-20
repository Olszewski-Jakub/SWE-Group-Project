Build Logic (Gradle Convention Plugins)

Purpose
- Centralized Gradle configuration to keep module `build.gradle.kts` files minimal and consistent.

Highlights
- Testing conventions: JUnit 5, Mockito, test logging, platform BOM.
- Jacoco conventions: `coverage { minimum = 0.xx }` DSL with per-module thresholds and verification wired to `check`.
- Module conventions:
  - `convention-domain`: plain Java library with testing and coverage.
  - `convention-service`: depends on `:domain`, Spring basics, testing, coverage.
  - `convention-infrastructure`: depends on `:service` & `:domain`, Spring context, testing, coverage.
  - `convention-delivery`: Spring MVC-focused testing and coverage.
  - `convention-security`: Spring Security, OAuth2 resource server, validation; testing & coverage.
  - `convention-integration`: Spring Boot app with Actuator/JPA/Postgres and wiring to all modules.

Version Catalog
- `gradle/libs.versions.toml` defines versions, libraries, and plugin aliases consumed by conventions.

Usage
- Modules apply conventions via plugin aliases, e.g.: `plugins { alias(libs.plugins.convention.service) }`.

