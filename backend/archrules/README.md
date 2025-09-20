ArchRules Module

Purpose
- ArchUnit tests that enforce architectural constraints across modules.

Rules
- `DomainPurityTest`: `domain` must not depend on Spring/JPA/Servlet/Hibernate.
- `ArchitectureLayersTest`: `service` cannot depend on `delivery` or `infrastructure`; `infrastructure` cannot depend on `delivery`.
- `StereotypesLocationTest`: controllers in `delivery`, services in `service`/`security` only.
- `CyclesFreeTest`: packages should be free of dependency cycles.

Running
- Included in the regular test task: `./gradlew :archrules:test` or `./gradlew check`.

