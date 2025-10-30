# Cart -> Stripe Line Item Mapping

This document summarizes how the current domain models map to Stripe Checkout line items. It is based on the existing classes under `ie.universityofgalway.groupnine.domain.cart.*` and `ie.universityofgalway.groupnine.domain.product.*`.

Confirmed domain shapes:
- Cart aggregate: `ShoppingCart` with `CartItems` enforcing a single currency per cart.
- Line item: `CartItem` holding a `Variant` and a `quantity`.
- Variant: `Variant` provides `id` (`VariantId`), `sku` (`Sku`), `price` (`Money`), `stock`, `attributes`.
- Money: `Money` exposes `amount: BigDecimal` and `currency: java.util.Currency`.

Notes:
- `Variant` does not expose a display `name`. The closest identifier is `sku`. Product display names exist on `Product`, but `CartItem` only references `Variant`. For Stripe product_data name we will use a safe, deterministic label based on the variant (e.g., the SKU and/or a compact attribute summary).

Mapping rules to Stripe Checkout Session line items:
- Name: use `variant.sku` (string value) or `"SKU " + variant.sku` as the product display name. If needed later, enrich with attributes, e.g., `"<SKU> (size=M, color=Black)"`.
- Unit amount (minor): take `Money.amount` (BigDecimal) and convert to minor units per currency. For EUR we use 2 decimal places → `amount_minor = amount.scaleByPowerOfTen(2).longValueExact()`.
- Quantity: `CartItem.quantity`.
- Currency: the cart currency enforced by `CartItems` (`CartItems.getCartCurrency()`); all items share the same currency.

Server-side totals:
- The cart total is computed as the sum of line item subtotals: `CartItem.subtotal() -> Money` and reduced by `Money::add` within `ShoppingCart.total()`.
- Never trust client-provided totals; the server-computed totals and snapshot are the source of truth for Stripe parameters and subsequent payment verification.

Idempotency and invariants:
- Cart mutability is restricted to `CartStatus.ACTIVE`. The checkout flow will lock the cart after successful payment.
- Single-currency invariant is enforced by `CartItems` and is assumed by the payment mapping.

