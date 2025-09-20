Infrastructure Module (Adapters)

Purpose
- Implements adapters for external systems (DB, messaging, files, etc.).
- Concrete implementations of ports defined in `service`.

Notable Classes
- `health.DatabaseProbeImpl`: JDBC/Hikari-based DB ping using `DataSource` (`SELECT 1`).

Working With It
- Implement the interfaces (ports) from `service` and annotate as Spring components.
- Configure `DataSource` via Spring Boot properties (e.g., `spring.datasource.*`).

Dependencies
- Depends on `:service` and `:domain`.
- Must not depend on `delivery` (enforced by ArchUnit).

Configuration
- In local dev, `integration/application.yml` configures Postgres URL/credentials.

Testing
- Write unit tests using mocks, or slice/integration tests from the `integration` module.

