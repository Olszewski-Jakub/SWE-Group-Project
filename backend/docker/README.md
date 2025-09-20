Docker & Compose

Files
- `Dockerfile`: runs the prebuilt `:integration` fat JAR in a minimal JRE image.
- `Dockerfile.dev`: builds the app inside Docker using the Gradle wrapper, then runs it.
- `compose.local.yml`: local development stack: Postgres + app.
- `compose.deploy.yml`: template for deployment (replace placeholders or switch to Spring env vars).

Local Dev
- `docker compose -f docker/compose.local.yml up --build`
- App: `http://localhost:8080/api/health` | DB: `localhost:5432` (demo/demo)

Build Image From Prebuilt Jar
- `./gradlew :integration:bootJar`
- `docker build -f docker/Dockerfile -t backend-app:local .`

Notes
- For deploy, prefer Spring env var names: `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`, and `SPRING_PROFILES_ACTIVE`.
- Set JWT secret via `APP_SECURITY_JWT_HMAC-SECRET` or map your secret to that key.

