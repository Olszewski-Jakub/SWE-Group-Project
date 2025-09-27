Auth endpoints

How to test locally

- Start the app from the `integration` module (it runs Flyway and exposes HTTP on `:8080`). Ensure Postgres and
  `application.yml` creds are valid.
- Register: `POST http://localhost:8080/auth/register` with JSON body: { "email": "user@example.com", "password": "
  0123456789X", "firstName": "Jane", "lastName": "Doe" }. Expect 201.
- Verify email: read the opaque token from logs (LoggingEmailSenderAdapter logs the verify URL), then call
  `POST http://localhost:8080/auth/verify-email` with body: { "token": "<opaque-from-logs>" }. Expect 200.
- Errors map to HTTP: duplicate email → 409, invalid token → 400, expired → 410, already used → 409.

Notes

- Only a hash of the token is stored in DB; the opaque token appears only once in logs.
- Endpoints are public as configured in `application.yml`.

