Domain Module

Purpose
- Holds core business concepts independent of frameworks.
- Contains base types like `AggregateRoot`, `DomainEvent`, and domain enums (e.g., `HealthStatus`).

Key Principles
- No Spring, JPA, servlet, or web dependencies (enforced by ArchUnit).
- Pure Java/Kotlin models, logic, and events only.

Notable Classes
- `common.AggregateRoot`: collects and exposes domain events via `pullDomainEvents()`.
- `common.DomainEvent`: interface with `occurredAt()`.
- `health.HealthStatus`: `UP`/`DOWN` enum.

Working With It
- Add entities/value objects here.
- Expose events from aggregates and let outer layers publish/handle them.

Dependencies
- None on application/web frameworks.

Testing
- Unit test pure domain logic normally; no Spring context required.

