Service Module (Application Layer)

Purpose
- Implements use cases and orchestrates domain logic.
- Defines ports/interfaces for infrastructure to implement (hexagonal ports).

Notable Classes
- `health.HealthCheckUseCase`: use case contract for health checks.
- `health.DatabaseProbe`: output port for DB liveness checks.
- `health.HealthCheckService`: application service using `DatabaseProbe` to return `HealthStatus`.

Working With It
- Define new use case interfaces and their services here.
- Depend only on the `domain` module and on your own service-defined ports.

Dependencies
- Depends on `:domain`.
- No dependency on delivery or infrastructure (checked by ArchUnit).

Testing
- Use plain unit tests or `spring-boot-starter-test` for light context if needed.

