# Dev-oriented Dockerfile for Next.js (runs `next dev`)

FROM node:22.17.0-bookworm-slim AS deps
ENV NODE_ENV=development \
    NEXT_TELEMETRY_DISABLED=1
WORKDIR /app

RUN apt-get update && apt-get install -y --no-install-recommends ca-certificates && rm -rf /var/lib/apt/lists/*

COPY package.json package-lock.json ./
# Install all dependencies including devDependencies for `next dev`
RUN npm ci --no-audit --no-fund

FROM node:22.17.0-bookworm-slim AS dev
ENV NODE_ENV=development \
    NEXT_TELEMETRY_DISABLED=1 \
    # Prefer WASM implementation to avoid native binding issues
    LIGHTNINGCSS_IGNORE_NATIVE=1 \
    LIGHTNINGCSS_FORCE_WASM=1
WORKDIR /app

# Bring in installed node_modules and the app source
COPY --from=deps /app/node_modules ./node_modules
COPY . .

# These envs can still be overridden at runtime via docker-compose
ARG NEXT_PUBLIC_API_BASE_URL
ARG NEXT_PUBLIC_ENVIRONMENT
ENV NEXT_PUBLIC_API_BASE_URL=${NEXT_PUBLIC_API_BASE_URL} \
    NEXT_PUBLIC_ENVIRONMENT=${NEXT_PUBLIC_ENVIRONMENT}

EXPOSE 3000

# Run the Next.js dev server and listen on all interfaces
CMD ["npm", "run", "dev", "--", "-H", "0.0.0.0"]
