Delivery Module (REST)

Purpose
- HTTP API layer: controllers, DTOs, and error handling.

Notable Endpoints
- `GET /api/health` (`HealthController`): returns `{ status: "UP|DOWN" }`; 200 when `UP`, 503 when `DOWN`.

Supporting Classes
- `support.ApiResponse<T>` and `support.ApiError`: standard response envelope and error body.
- `support.GlobalExceptionHandler`: maps common exceptions to JSON errors.

Working With It
- Keep controllers thin; call `service` use cases.
- Map domain/application errors to consistent API responses.

Testing
- MVC slice tests via `@DeliveryWebMvcTest` and `CommonWebMvcTest` from `test-support`.

Dependencies
- Depends on `:service` and `:domain`.

