// src/lib/CheckoutService.js
// Centralized service for creating Stripe Checkout sessions
// Uses the shared axios client and follows the same conventions

import axiosClient from './axiosClient';

/**
 * Create a Stripe Checkout session for the given cart.
 * @param {string} cartId - The ID of the user's cart
 * @returns {Promise<{ orderId: string, sessionId: string, checkoutUrl: string }>} The checkout session payload
 */
export async function createCheckoutSession(cartId) {
  if (!cartId) throw new Error('cartId is required');
  const res = await axiosClient.post('/checkout/sessions', { cartId });
  return res.data;
}

export default {
  createCheckoutSession,
};

