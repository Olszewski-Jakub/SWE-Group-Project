"use client";

import {
  createContext,
  useContext,
  useState,
  useEffect,
  useCallback,
} from "react";
import * as cartService from "./services/CartService";

const CartContext = createContext();

function parseMoneyDto(moneyDto) {
  if (!moneyDto) return 0;
  const n =
    typeof moneyDto.amount === "string"
      ? parseFloat(moneyDto.amount)
      : Number(moneyDto.amount);
  return Number.isFinite(n) ? n : 0;
}

function mapCartDtoToState(cartDto) {
  if (!cartDto) return { items: [], total: 0, cartId: null };

  const baseUrl = process.env.NEXT_PUBLIC_API_BASE_URL || "";

  // Helper to resolve relative/absolute URLs
  const resolveImageUrl = (rawUrl) => {
    if (!rawUrl) return "https://coffee.alexflipnote.dev/random";
    const isAbsolute =
      /^(?:[a-z]+:)?\/\//i.test(rawUrl) || rawUrl.startsWith("data:");
    if (isAbsolute) return rawUrl;
    const trimmedBase = baseUrl.replace(/\/$/, "");
    const path = rawUrl.startsWith("/") ? rawUrl : `/${rawUrl}`;
    return `${trimmedBase}${path}`;
  };

  const items = (cartDto.items || []).map((it) => {
    const unit = parseMoneyDto(it.unitPrice);
    const subtotal = parseMoneyDto(it.subtotal) || unit * (it.quantity || 0);

    return {
      id: it.variantId,
      productId: it.productId,
      name: it.productName || it.productDescription || "Product",
      quantity: it.quantity || 0,
      price: unit,
      subtotal,
      // Add imageUrl here
      imageUrl: resolveImageUrl(it.imageUrl),
    };
  });

  const total =
    parseMoneyDto(cartDto.totalPrice) ||
    items.reduce((acc, i) => acc + (i.subtotal || i.price * i.quantity), 0);

  return {
    items,
    total,
    cartId: cartDto.id || null,
    raw: cartDto,
  };
}

export const CartProvider = ({ children }) => {
  const [isOpen, setIsOpen] = useState(false);
  const [items, setItems] = useState([]);
  const [total, setTotal] = useState(0);
  const [cartId, setCartId] = useState(null);
  const [loading, setLoading] = useState(false);

  const toggleCart = () => setIsOpen((prev) => !prev);

  const fetchCart = useCallback(async () => {
    try {
      setLoading(true);
      const data = await cartService.getCart();
      const mapped = mapCartDtoToState(data);
      setItems(mapped.items);
      setTotal(mapped.total);
      setCartId(mapped.cartId);
      return mapped;
    } catch (err) {
      console.error(
        "Failed to load cart:",
        err?.response?.status,
        err?.message || err
      );
      setItems([]);
      setTotal(0);
      setCartId(null);
      return { items: [], total: 0, cartId: null };
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchCart();
  }, [fetchCart]);

  const addItem = async (variantId, quantity = 1) => {
    if (!variantId) throw new Error("variantId is required");
    if (!Number.isInteger(quantity) || quantity < 1) quantity = 1;

    try {
      setLoading(true);
      const updatedDto = await cartService.addToCart(variantId, quantity);
      const mapped = mapCartDtoToState(updatedDto);
      setItems(mapped.items);
      setTotal(mapped.total);
      setCartId(mapped.cartId);
      return mapped;
    } catch (err) {
      console.error(
        "Failed to add item:",
        err?.response?.status,
        err?.message || err
      );
      throw err;
    } finally {
      setLoading(false);
    }
  };

  const removeItem = async (variantId) => {
    if (!variantId) throw new Error("variantId is required");
    try {
      setLoading(true);
      const updatedDto = await cartService.removeFromCart(variantId);
      const mapped = mapCartDtoToState(updatedDto);
      setItems(mapped.items);
      setTotal(mapped.total);
      setCartId(mapped.cartId);
      return mapped;
    } catch (err) {
      console.error(
        "Failed to remove item:",
        err?.response?.status,
        err?.message || err
      );
      throw err;
    } finally {
      setLoading(false);
    }
  };

  const updateItemQuantity = async (variantId, quantity) => {
    if (!variantId) throw new Error("variantId is required");
    if (!Number.isInteger(quantity) || quantity < 0)
      throw new Error("quantity must be >= 0");

    if (!cartId) {
      await fetchCart();
      if (!cartId) throw new Error("No cart available");
    }

    try {
      setLoading(true);
      const updatedDto = await cartService.updateItemQuantity(
        cartId,
        variantId,
        quantity
      );
      const mapped = mapCartDtoToState(updatedDto);
      setItems(mapped.items);
      setTotal(mapped.total);
      setCartId(mapped.cartId);
      return mapped;
    } catch (err) {
      console.error(
        "Failed to update quantity:",
        err?.response?.status,
        err?.message || err
      );
      throw err;
    } finally {
      setLoading(false);
    }
  };

  const clearCart = async () => {
    if (!cartId) {
      setItems([]);
      setTotal(0);
      return { items: [], total: 0 };
    }

    try {
      setLoading(true);
      const updatedDto = await cartService.clearCart(cartId);
      const mapped = mapCartDtoToState(updatedDto);
      setItems(mapped.items);
      setTotal(mapped.total);
      setCartId(mapped.cartId);
      return mapped;
    } catch (err) {
      console.error(
        "Failed to clear cart:",
        err?.response?.status,
        err?.message || err
      );
      throw err;
    } finally {
      setLoading(false);
    }
  };

  return (
    <CartContext.Provider
      value={{
        isOpen,
        items,
        total,
        cartId,
        loading,
        addItem,
        removeItem,
        clearCart,
        updateItemQuantity,
        toggleCart,
        refresh: fetchCart,
      }}
    >
      {children}
    </CartContext.Provider>
  );
};

export const useCart = () => useContext(CartContext);
