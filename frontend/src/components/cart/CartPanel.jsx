"use client";

import Image from "next/image";
import { useCart } from "@/features/cart/CartContext";
import { createCheckoutSession } from "@/lib/CheckoutService";

export default function CartPanel() {
  const {
    isOpen,
    items,
    toggleCart,
    removeItem,
    clearCart,
    total,
    updateItemQuantity,
    cartId,
    refresh,
  } = useCart();

  // Format numbers as Euro currency
  const formatEuro = (amount) =>
    new Intl.NumberFormat("en-IE", {
      style: "currency",
      currency: "EUR",
    }).format(amount);

  const baseUrl = process.env.NEXT_PUBLIC_API_BASE_URL || "";

  // Function to resolve image URLs (absolute/relative/fallback)
  const resolveImageUrl = (rawUrl) => {
    if (!rawUrl) return "https://coffee.alexflipnote.dev/random";
    const isAbsolute =
      /^(?:[a-z]+:)?\/\//i.test(rawUrl) || rawUrl.startsWith("data:");
    if (isAbsolute) return rawUrl;
    const trimmedBase = baseUrl.replace(/\/$/, "");
    const path = rawUrl.startsWith("/") ? rawUrl : `/${rawUrl}`;
    return `${trimmedBase}${path}`;
  };

  return (
    <div
      className={`fixed top-16 right-0 w-96 h-[calc(100%-4rem)] bg-white 
      rounded-l-2xl border border-stone-200 
      transition-transform duration-300 ease-in-out z-50
      ${isOpen ? "translate-x-0" : "translate-x-full"}`}
    >
      {/* Header */}
      <div className="p-5 border-b bg-white shadow-sm flex justify-between items-center">
        <h2 className="text-2xl font-bold text-gray-800 tracking-wide">
          Shopping Cart
        </h2>
        <button
          onClick={toggleCart}
          className="text-gray-500 hover:text-black text-2xl"
          aria-label="Close Cart"
        >
          &times;
        </button>
      </div>

      {/* Cart Items */}
      <div className="p-5 overflow-y-auto h-[calc(100%-12rem)] space-y-4">
        {items.length === 0 ? (
          <div className="flex flex-col items-center justify-center h-full text-gray-500 pt-16">
            <p className="text-lg">Your cart is empty.</p>
            <button
              onClick={toggleCart}
              className="mt-4 text-orange-600 hover:text-orange-800 font-medium cursor-pointer"
            >
              Start Shopping
            </button>
          </div>
        ) : (
          items
            .sort((a, b) => a.productId.localeCompare(b.productId))
            .map((item) => (
              <div
                key={item.id}
                className="flex items-start p-3 bg-gray-50 hover:bg-gray-100 rounded-lg transition-colors"
              >
                {/* Image */}
                <div className="w-16 h-16 mr-4 bg-gray-200 rounded-md flex-shrink-0 overflow-hidden relative">
                  <Image
                    src={resolveImageUrl(item.imageUrl)}
                    alt={item.name}
                    fill
                    className="object-cover"
                    unoptimized
                  />
                </div>

                {/* Content */}
                <div className="flex-grow min-w-0">
                  <p className="font-semibold text-gray-800 break-words leading-tight">
                    {item.name}
                  </p>

                  <p className="text-sm text-gray-600 mt-1">
                    {formatEuro(item.price * item.quantity)} ({item.quantity} x{" "}
                    {formatEuro(item.price)})
                  </p>

                  <div className="flex items-center space-x-2 mt-2">
                    <button
                      onClick={() =>
                        updateItemQuantity(
                          item.id,
                          Math.max(0, item.quantity - 1)
                        )
                      }
                      className="px-2 py-1 border rounded text-sm cursor-pointer"
                      aria-label="Decrease quantity"
                    >
                      -
                    </button>
                    <span className="text-sm text-gray-500">Qty: {item.quantity}</span>
                    <button
                      onClick={() =>
                        updateItemQuantity(item.id, item.quantity + 1)
                      }
                      className="px-2 py-1 border rounded text-sm cursor-pointer"
                      aria-label="Increase quantity"
                    >
                      +
                    </button>
                  </div>
                </div>

                {/* Remove Button */}
                <button
                  onClick={() => removeItem(item.id)}
                  className="text-sm text-red-500 hover:text-red-700 ml-4 font-medium flex-shrink-0 cursor-pointer"
                >
                  Remove
                </button>
              </div>
            ))
        )}
      </div>

      {/* Footer */}
      <div className="absolute bottom-0 w-full border-t p-5 bg-white shadow-inner">
        {items.length > 0 && (
          <>
            <div className="flex justify-between items-center mb-4 text-lg font-bold text-gray-800">
              <span>Subtotal:</span>
              <span>{formatEuro(Number(total))}</span>
            </div>

            <button
              onClick={async () => {
                try {
                  let id = cartId;
                  if (!id) {
                    const mapped = await refresh();
                    id = mapped?.cartId;
                  }
                  if (!id) throw new Error("No cart available for checkout");
                  const { checkoutUrl } = await createCheckoutSession(id);
                  if (!checkoutUrl) throw new Error("No checkoutUrl returned");
                  window.location.assign(checkoutUrl);
                } catch (err) {
                  console.error("Failed to start checkout:", err?.message || err);
                }
              }}
              className="bg-amber-700 hover:bg-amber-800 text-white rounded-md w-full py-3 font-semibold text-lg transition-colors shadow-md cursor-pointer"
            >
              Proceed to Checkout
            </button>

            <button
              onClick={clearCart}
              className="mt-2 text-sm text-gray-500 hover:text-red-500 w-full text-center cursor-pointer"
            >
              Clear Cart
            </button>
          </>
        )}
      </div>
    </div>
  );
}
