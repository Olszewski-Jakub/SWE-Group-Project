Auth endpoints

- POST `/auth/login`
  Request: { "email": "user@example.com", "password": "secret" }
  Response 200:
  {
  "accessToken": "<jwt>",
  "expiresIn": 900,
  "refreshToken": "<opaque_refresh>",
  "tokenType": "Bearer"
  }
  Also sets cookie: `refreshToken=<opaque>; HttpOnly; Secure; SameSite=None; Path=/auth/refresh`.

- POST `/auth/refresh`
  Reads refresh token from cookie `refreshToken` or header `X-Refresh-Token`.
  Response 200:
  {
  "accessToken": "<jwt>",
  "expiresIn": 900,
  "refreshToken": "<new_opaque_refresh>",
  "tokenType": "Bearer"
  }
  Rotates the refresh token: sets new cookie and revokes previous session.

- POST `/auth/logout`
  Revokes the current session and clears the refresh cookie.

- POST `/auth/logout-all`
  Revokes all sessions for the current user and clears the refresh cookie.

Errors

- 401 InvalidCredentials / InvalidRefreshToken / ExpiredRefreshToken
- 403 UserNotVerified
- 423 UserLocked
- 409 RefreshReuseDetected (all sessions revoked; client must re-login)

