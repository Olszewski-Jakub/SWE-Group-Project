import axiosClient, { getAccessToken } from '@/lib/axiosClient';

export async function getCart() {
  // Only call the API when the user is signed in
  const token = getAccessToken();
  if (!token) return null;
  const res = await axiosClient.get('/cart/my');
  return res.data;
}

export async function addToCart(variantId, quantity = 1) {
  if (!variantId) {
    throw new Error("variantId is required to add item to cart");
  }

  const res = await axiosClient.put("/cart/my/items", {
    variantId,
    quantity, 
  });
  return res.data;
}

export async function removeFromCart(variantId) {
  const res = await axiosClient.delete(`/cart/my/items/${variantId}`);
  return res.data;
}

export async function updateItemQuantity(cartId, variantId, quantity) {
  const res = await axiosClient.patch(`/cart/${cartId}/items/${variantId}`, {
    quantity,
  });
  return res.data;
}

export async function clearCart(cartId) {
  const res = await axiosClient.delete(`/cart/${cartId}/items`);
  return res.data;
}
