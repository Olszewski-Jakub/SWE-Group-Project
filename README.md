## SWE Group Project

Monorepo containing a Next.js frontend and a Spring Boot backend packaged with Gradle. A local Docker Compose setup runs a full stack (API, DB, cache, MQ, frontend) with minimal friction.

### Purpose & Overview

An online coffee shop where customers can browse products, build a cart, and place orders with secure payments and delivery options. The system sends transactional emails and supports OAuth sign‑in for a smooth checkout.

- Frontend: Next.js app (Turbopack in dev) at `http://localhost:3000`.
- Backend: Spring Boot app (module `integration`) exposing REST API at `http://localhost:8080/api/v1`.
- Infrastructure: Postgres (catalog/orders), Redis (caching, rate limits), RabbitMQ (async jobs/email).

#### Key Features

- Product browsing: view coffee products with details; supports a modern, responsive UI.
- Cart and reservations: items reserved briefly during checkout (configurable TTL).
- Checkout and payments: integrates with Stripe for payments; success/cancel flows configurable.
- Delivery options: shipping rate IDs are configurable for supported destinations.
- Email notifications: Mailjet integration for order confirmations and other emails.
- Authentication: JWT‑based auth with refresh tokens; Google OAuth optional.
- Security: brute‑force protection and sane cookie defaults for local/dev.

---

## Quick Start (Docker)

Requirements: Docker and Docker Compose

1) Copy env file

```bash
cp .env.example .env
# Edit .env and set real secrets for JWT/Stripe/Google/Mailjet as needed
```

2) Build and start

```bash
docker compose up -d --build
```

3) Open the apps

- Frontend: http://localhost:3000
- Backend health: http://localhost:8080/api/v1/health

Notes

- Only API (8080) and Frontend (3000) are exposed to the host. Postgres, Redis, and RabbitMQ are available only on the internal Docker network `swe_app_net`.
- The backend runs with the `docker` Spring profile and connects to services by container DNS names: `postgresql-group9`, `redis-group9`, `rabbitmq-group9`.
- The frontend image runs `next dev` for a fast dev experience inside Docker.

Useful commands

```bash
docker compose logs -f app       # Tail backend logs
docker compose logs -f frontend  # Tail frontend logs
docker compose down              # Stop stack
```

---

## Local Development (without full Docker)

You can run infra via Docker and the apps locally for faster iteration.

1) Start infra only

```bash
docker compose up -d postgresql redis rabbitmq
```

2) Run backend locally

```bash
cd backend
./gradlew :integration:bootRun
# Uses 'local' profile by default; points to localhost unless overridden via env
```

3) Run frontend locally

```bash
cd frontend
npm ci
npm run dev:local
# or: NEXT_PUBLIC_API_BASE_URL=http://localhost:8080/api/v1 npm run dev
```

---

## Project Structure

```
.
├── backend/                     # Spring Boot (Gradle multi-module)
│   ├── integration/             # API application module
│   │   └── src/main/resources/
│   │       ├── application-local.yml
│   │       ├── application-docker.yml
│   │       └── application-deploy.yml
│   └── docker/
│       ├── Dockerfile           # Production image (profile=deploy)
│       └── Dockerfile.dev       # Dev image (profile configurable)
├── frontend/                    # Next.js 15 app
│   ├── Dockerfile               # Dev-oriented (runs next dev)
│   ├── package.json
│   └── src/
├── docker-compose.yml           # Local stack (DB/cache/MQ/API/frontend)
├── .env.example                 # Copy to .env and customize
└── README.md
```

---

## Configuration & Environment Variables

Set variables in `.env` (see `.env.example`). Key values:

- Frontend
  - `NEXT_PUBLIC_API_BASE_URL` – Browser calls to the API (default `http://localhost:8080/api/v1`).
  - `NEXT_PUBLIC_ENVIRONMENT` – Display/env indicator for the UI.
- Database
  - `POSTGRES_USER`, `POSTGRES_PASSWORD`, `POSTGRES_DB` – Postgres credentials.
- RabbitMQ
  - `RABBITMQ_USERNAME`, `RABBITMQ_PASSWORD` – Credentials.
- Email (Mailjet)
  - `MAILJET_API_KEY`, `MAILJET_API_SECRET`, `MAIL_FROM_EMAIL`, `MAIL_FROM_NAME`.
- Backend secrets
  - `JWT_SECRET` – HMAC secret for signing JWTs.
  - `STRIPE_API_KEY`, `STRIPE_WEBHOOK_SECRET` – Payments.
  - `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`, `GOOGLE_REDIRECT_URI` – OAuth.

Runtime profiles

- Backend dev image defaults to `SPRING_PROFILES_ACTIVE=docker` (configured in `backend/docker/Dockerfile.dev`).
- You can override by setting `SPRING_PROFILES_ACTIVE` on the `app` service in `docker-compose.yml`.

---

## Deployment

### CI/CD (GitHub Actions)

- Umbrella:
  - `.github/workflows/Deploy-Umbrella.yml` orchestrates deployments, calling backend and frontend deploy workflows when triggered.
- Backend deploy (`deploy-backend.yml`):
  - Builds and tests with Gradle, then builds and pushes a Docker image to GitHub Container Registry `ghcr.io/<org>/StackOverFlowedCup-API`.
  - Signs the image with cosign (keyless via OIDC).
  - SSHes into a remote host and performs blue/green deploy via Docker Compose and Kong for gradual traffic shift.
  - Required repository SECRETS:
    - `SSH_PRIVATE_KEY` – private key for the deploy user.
    - `SERVER_USER`, `SERVER_IP` – SSH target.
  - Required repository VARS:
    - `APP_DIR` – absolute path to the app’s folder on the server (git repo is pulled here).
    - `DOCKER_COMPOSE_FILE` – absolute path to the compose file on server (e.g. `backend/docker/compose.bluegreen.yml`).
    - `KONG_ADMIN_URL` – Kong Admin API upstream targets base URL.
    - `BLUE_PORT`, `GREEN_PORT` – ports used by blue/green services (match compose file).
- Frontend deploy (`deploy-frontend.yml`):
  - Builds the Next.js app with Node 20, then uses Vercel CLI to deploy production build.
  - Required repository SECRETS: `VERCEL_ORG_ID`, `VERCEL_PROJECT_ID`, `VERCEL_TOKEN`.
- Pull request workflows:
  - Backend: compile, test, coverage report, dependency updates, CodeQL (`pull-request-backend.yml`).
  - Frontend: install, build, and Vercel Preview deploy (`pull-request-frontend.yml`).

#### Server prerequisites for backend blue/green

- Install Docker and Docker Compose; ensure the deploy user can run `sudo docker`.
- Clone this repo into `APP_DIR` and keep `origin` pointing to GitHub (workflow runs `git pull origin main`).
- Ensure the external Docker network used in `backend/docker/compose.bluegreen.yml` exists (e.g., `docker network create bushive_network`).
- Create `backend/.env` on the server with production values referenced by the compose file (`env_file: ../.env`).
- Kong configured with two upstream targets, e.g., `swe-backend-blue` and `swe-backend-green`, reachable on `BLUE_PORT`/`GREEN_PORT`.

Triggering deployments

- From GitHub → Actions → run “Deploy Umbrella”, or trigger individual backend/frontend deploy workflows with “Run workflow”.
- Image tags: the backend workflow tags/pushes to GHCR using the standard Docker metadata-action strategy based on the branch/ref.

### Backend (Docker)

Build a production image using the provided Dockerfile:

```bash
docker build -f backend/docker/Dockerfile -t app-backend:prod ./backend
docker run -p 8080:8080 \
  -e DB_URL="jdbc:postgresql://<db-host>:5432/<db-name>" \
  -e DB_USERNAME=<user> -e DB_PASSWORD=<pass> \
  -e JWT_SECRET=<secret> \
  app-backend:prod
```

The prod image uses the `deploy` Spring profile; configure DB/Redis/RabbitMQ and other secrets via env variables or your orchestrator’s secret manager.

### Frontend

Option A — Node process (e.g., behind Nginx/ingress):

```bash
cd frontend
npm ci
NEXT_PUBLIC_API_BASE_URL=https://api.example.com/api/v1 \
NEXT_PUBLIC_ENVIRONMENT=prod \
npm run build
npm run start -- -p 3000
```

Option B — Containerize a prod image (recommended): create a multi-stage Dockerfile that runs `next build` and `next start`, or adapt the existing dev Dockerfile to a production target. Ensure `NEXT_PUBLIC_*` variables are provided at build-time.

### Compose/Orchestrator

- Place Postgres/Redis/RabbitMQ on internal networks; expose only the API and frontend.
- Provide secrets via your platform (Docker secrets, Kubernetes Secrets, etc.).

---

## Troubleshooting

- DB connection to `localhost:5432` in Docker: ensure the backend runs with the `docker` profile and that `application-docker.yml` points to your DB service hostname.
- Frontend can’t reach API: verify `NEXT_PUBLIC_API_BASE_URL` matches your API base URL and the container/host port mapping.
- LightningCSS native binding errors in frontend: the dev image sets `LIGHTNINGCSS_*` to prefer WASM; keep these if your platform lacks native bindings.
