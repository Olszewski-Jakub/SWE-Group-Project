Security Module

Purpose

- Centralizes Spring Security configuration for the app.
- Configures JWT resource-server, route authorization, and JSON error handling.

Key Components

- `SecurityConfig`: builds `SecurityFilterChain`; sets CSRF/CORS/stateless; applies route rules; configures JWT.
- `RouteProperties`: `app.security.*` configuration (permit-all, authenticated, role-based patterns; JWT settings).
- `JsonAuthHandlers`: consistent JSON 401/403 responses (`ErrorResponse`).
- `JwtConverters`: maps JWT claim (default `roles`) -> `ROLE_*` authorities.
- `Roles`: shared constants (`ADMIN`, `USER`, ...).

Configuration

- Example properties (flatten to Spring-style env names in deploy):
    - `APP_SECURITY_ROUTES_PERMIT-ALL[0]=/api/health`
    - `APP_SECURITY_ROUTES_AUTHENTICATED[0]=/api/**`
    - `APP_SECURITY_ROUTES_ROLES.ADMIN[0]=/api/admin/**`
    - `APP_SECURITY_JWT_HMAC-SECRET=${JWT_SECRET}`
    - `APP_SECURITY_JWT_AUTHORITIES-CLAIM=roles`
    - `APP_SECURITY_JWT_ISSUER=https://issuer` (optional)

Notes

- Default rules allow `/api/health` and deny-all otherwise unless configured.
- Provide a valid HS256 secret in non-dev environments.

Testing

- `security/src/test` covers config, handlers, roles, and converters.

