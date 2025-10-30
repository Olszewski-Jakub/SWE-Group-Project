# Payments + Stripe Checkout Implementation Plan (Tailored)

This refines the end-to-end design for Stripe Checkout, webhooks, async processing via RabbitMQ, PostgreSQL persistence, auditing, and notifications, aligned to the current domain and modules.

## Relevant existing modules
- `domain`, `service`, `infrastructure`, `delivery/rest`, `security`, `integration`, `delivery/worker` (email worker present), `docker`.
- RabbitMQ is already used for email via `email.exchange` with routing keys `email.{type}` and a DLX `email.dlx`.
- Auditing port/adapters exist: `service.audit.port.AuditEventPort` with JPA adapter `JpaAuditEventAdapter`.
- No existing transactional outbox or processed-events tables found.

## New/updated application ports
- `CartQueryPort`: Use existing `ShoppingCartPort` for loading carts by `CartId` and `UserId`.
- `OrderRepository`: CRUD for the new `Order` aggregate.
- `InventoryReservationRepository`: CRUD for `InventoryReservation` records.
- `PaymentGatewayPort`: outbound Stripe adapter to create Checkout Sessions (and refunds later).
- `MessagePublisherPort`: generic publisher to RabbitMQ exchanges/routing keys (email already uses Rabbit; we add a general-purpose publisher + transactional outbox).
- `AuditorPort`: reuse existing `AuditEventPort`.

## Immutable Order Snapshot (from cart)
- Items: for each `CartItem`
  - `variant_id`: `VariantId` (UUID)
  - `sku`: `Variant.sku`
  - `unit_amount_minor`: from `Money.amount` scaled to minor units (EUR → 2dp)
  - `quantity`: `CartItem.quantity`
  - `currency`: cart currency (enforced single-currency by `CartItems`)
- Order-level:
  - `order_id` (UUID)
  - `user_id` (UUID)
  - `cart_id` (UUID)
  - `total_minor`, `currency`
  - optional `attributes` summary per variant for display/debug
- This snapshot is persisted with the order in `orders.snapshot_json` and drives Stripe params and payment verification.

## Messaging topology (RabbitMQ)
Exchanges (topic):
- `payments.events` — normalized payment events produced by Stripe webhooks.
- `orders.commands` — optional, for order-scoped commands (TBD).
- `inventory.commands` — reserve/confirm/release requests.
- `inventory.events` — reservation results/expirations.
- `notifications.events` — optional bridge to email worker if needed; however, we can publish email jobs directly to `email.exchange` using existing infra.

Queues & bindings:
- `q.orders.payment-handler` ← `payments.events` with `payment.event.#`
- `q.inventory.reservation` ← `inventory.commands` with `inventory.reserve.request`
- `q.inventory.confirm` ← `inventory.commands` with `inventory.confirm.request`
- `q.inventory.release` ← `inventory.commands` with `inventory.release.request`
- `q.inventory.scheduler` (delayed/DLQ-based) that re-publishes `inventory.release.request(EXPIRED)` after TTL
- Existing email queues remain unchanged (from `EmailWorkerConfig`). Payments code will publish to `email.exchange` with routing key `email.{type}` using new email job types if added.
- Configure DLQs for each consumer queue with dead-lettering and redrive.

## Database tables (Flyway)
- `orders(id, user_id, cart_id, total_minor, currency, status, created_at, updated_at, snapshot_json)`
- `inventory(variant_id, available, reserved, sold, updated_at)` or adapt to existing stock if present
- `inventory_reservations(id, order_id, items_json, status, expires_at, created_at, updated_at)`
- `processed_events(source, key, processed_at)` for idempotency (webhooks and consumers)
- `outbox_messages(id, exchange, routing_key, headers_json, payload_json, created_at, published_at, attempts)` for the Transactional Outbox

## Idempotency strategy
- Inbound (webhooks): dedupe on `stripe_event_id` in `processed_events` with `source='stripe'`.
- Inbound (consumers): dedupe on message IDs/correlation (e.g., `source='amqp'`, `key=<exchange>:<routingKey>:<messageId>`).
- Outbound (Stripe): pass idempotency keys derived from `orderId + snapshotHash` when creating Checkout Sessions.
- Outbound (AMQP): publish via Transactional Outbox; headers include `correlation_id`, `order_id`, `reservation_id`.

## Email notifications (reuse existing worker)
- Preferred: add new `EmailType` values `ORDER_PAID`, `PAYMENT_FAILED`, `ORDER_REFUNDED` and corresponding mailers/templates, routed to `email.exchange` with `email.{type}`.
- If adding new types is out of scope, provide a minimal bridge event to `notifications.events` and a small worker to translate into existing email jobs; preferred approach is extending `EmailType`.

## Use cases and flows
1) Start Checkout (REST → use case only, no framework logic):
- Validate cart invariants: `ACTIVE`, not empty, single currency, ownership.
- Create order snapshot and `Order(PENDING_PAYMENT)` with computed total.
- Call `PaymentGatewayPort.createCheckoutSession(snapshot, successUrl, cancelUrl, idempotencyKey)`.
- Write two outbox messages in the same DB transaction:
  - `order.checkout.started` (metadata + session id)
  - `inventory.reserve.request` (items, TTL = `RESERVATION_TTL_SECONDS`)
- Audit: `checkout_started` with orderId, amount, currency, userId.

2) Stripe Webhook (delivery:webhook module):
- Verify signature using `STRIPE_WEBHOOK_SECRET`.
- Support `checkout.session.completed`, `payment_intent.succeeded`, `payment_intent.payment_failed`, `charge.refunded`.
- Normalize to `payment.event` structure:
  `{ stripe_event_id, type, order_id, payment_intent_id, amount_minor?, reason?, occurred_at, idempotency_key }`.
- Idempotency: insert into `processed_events`; if exists → 200 OK (no-op).
- Publish normalized event via outbox to `payments.events`.
- Audit: `webhook_received` + type + orderId.

3) Payment Handler (delivery:worker module):
- Consume `payment.event` from `q.orders.payment-handler`.
- Load `Order` and transition:
  - success/completed → `PAID`
  - failed → `PAYMENT_FAILED`
  - refunded → `REFUNDED`
- On `PAID`:
  - Publish `inventory.confirm.request` with `reservation_id`.
  - Publish email job `ORDER_PAID` to `email.exchange` (with order snapshot, totals, user info if available).
  - Lock cart (`ShoppingCart` → `CHECKED_OUT`).
  - Audit: `payment_succeeded`.
- On `FAILED`:
  - Publish `inventory.release.request (PAYMENT_FAILED)`
  - Audit: `payment_failed` + reason
- On `REFUNDED`:
  - Optionally publish `inventory.release.request (REFUNDED)` depending on restore-stock policy
  - Audit: `payment_refunded`
- Ensure idempotency using `processed_events`.

4) Inventory Reservation Consumers (delivery:worker module):
- `inventory.reserve.request` → attempt soft reserve; create `InventoryReservation(RESERVED)` with `expiresAt = now + TTL`; publish `inventory.events.reserved` or `inventory.events.rejected`.
- `inventory.confirm.request` → reserved → sold; set `InventoryReservation(CONFIRMED)`.
- `inventory.release.request` → reserved → available; set `InventoryReservation(RELEASED)`.
- Expiration: TTL or delayed exchange to `q.inventory.scheduler` which republishes `inventory.release.request(EXPIRED)`.
- Audit each action.

## Config and secrets
- `STRIPE_SECRET`, `STRIPE_WEBHOOK_SECRET`, `APP_BASE_URL`, `CHECKOUT_SUCCESS_PATH`, `CHECKOUT_CANCEL_PATH`, `RESERVATION_TTL_SECONDS` (default 1800), `RABBITMQ_URL`.
- Wire base RabbitMQ `ConnectionFactory`/`RabbitTemplate` and topic/direct exchanges as above.

## Transactional Outbox
- Add JPA/Jdbc repository and a background publisher in `integration` that polls `outbox_messages`, publishes to RabbitMQ, marks `published_at`, retries with backoff.
- Include correlation headers: `correlation_id`, `order_id`, `reservation_id`.

## Payment verification
- On success, verify PaymentIntent status is `succeeded`, amount and currency match order snapshot, and metadata matches (`order_id`, `cart_id`, `user_id`).
- On mismatch, mark `PAYMENT_FAILED` and audit `payment_verification_failed`.

## Tests (high-level)
- Unit tests for mapping, use cases, webhook normalization, payment handler transitions, reservation flows, outbox publisher idempotency.
- E2E: happy path, reserve rejected, payment failed, timeout (expire), duplicate webhook (ignored), amount mismatch (invalid).

