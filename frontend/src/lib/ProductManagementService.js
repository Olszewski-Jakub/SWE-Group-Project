/**
 * @file ProductManagementService centralizes ADMIN/MANAGER product management API calls.
 * Endpoints map to backend controller under /api/v1/management/products.
 */

import axiosClient from './axiosClient';

const BASE = '/management/products';

/**
 * Get single product (management).
 * @param {string} productId
 */
export async function getProduct(productId) {
  const res = await axiosClient.get(`${BASE}/${productId}`);
  return res.data;
}

/**
 * Create product.
 * @param {Object} body ProductRequest payload
 */
export async function createProduct(body) {
  const res = await axiosClient.post(BASE, body);
  return res.data;
}

/**
 * Create product using multipart form endpoint with variants and optional images.
 * Expected fields (repeatable arrays for variants):
 * - name, description, category, status
 * - variantSku[], variantPriceAmount[], variantPriceCurrency[], variantStockQuantity[], variantStockReserved[] (optional)
 * - images[] (optional; order must match variants)
 * @param {Object} payload
 * @param {string} payload.name
 * @param {string} [payload.description]
 * @param {string} payload.category
 * @param {string} payload.status
 * @param {Array<{sku:string, priceAmount:string|number, priceCurrency:string, stockQuantity:number|string, stockReserved?:number|string, imageFile?:File}>} payload.variants
 */
export async function createProductWithForm(payload) {
  const form = new FormData();
  form.append('name', payload.name);
  if (payload.description) form.append('description', payload.description);
  form.append('category', payload.category);
  form.append('status', payload.status);
  const variants = Array.isArray(payload.variants) ? payload.variants : [];
  variants.forEach((v) => {
    form.append('variantSku', v.sku);
    form.append('variantPriceAmount', String(v.priceAmount));
    form.append('variantPriceCurrency', String(v.priceCurrency).toUpperCase());
    form.append('variantStockQuantity', String(v.stockQuantity));
    if (v.stockReserved !== undefined && v.stockReserved !== null && `${v.stockReserved}` !== '') {
      form.append('variantStockReserved', String(v.stockReserved));
    }
  });
  // Attach images aligned by index
  variants.forEach((v) => {
    if (v.imageFile instanceof File) {
      form.append('images', v.imageFile);
    }
  });
  const res = await axiosClient.post(`${BASE}/form`, form, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
  return res.data;
}

/**
 * Update product (partial).
 * @param {string} productId
 * @param {Object} partial ProductRequest partial
 */
export async function updateProduct(productId, partial) {
  const res = await axiosClient.patch(`${BASE}/${productId}`, partial);
  return res.data;
}

/**
 * Delete product.
 * @param {string} productId
 */
export async function deleteProduct(productId) {
  await axiosClient.delete(`${BASE}/${productId}`);
}

/**
 * Add variant to a product.
 * @param {string} productId
 * @param {Object} body VariantRequest payload
 */
export async function addVariant(productId, body) {
  const res = await axiosClient.post(`${BASE}/${productId}/variants`, body);
  return res.data;
}

/**
 * Update variant.
 * @param {string} productId
 * @param {string} variantId
 * @param {Object} partial VariantRequest partial
 */
export async function updateVariant(productId, variantId, partial) {
  const res = await axiosClient.patch(`${BASE}/${productId}/variants/${variantId}`, partial);
  return res.data;
}

/**
 * Delete variant.
 * @param {string} productId
 * @param {string} variantId
 */
export async function deleteVariant(productId, variantId) {
  await axiosClient.delete(`${BASE}/${productId}/variants/${variantId}`);
}

/**
 * Upload variant image. Expects backend to accept multipart file part named "file" or "images".
 * If using ProductManagementController#createWithForm for batch creation, prefer that path.
 */
export async function uploadVariantImage(productId, variantId, file) {
  const form = new FormData();
  // Backend sample uses uploadVariantImageUseCase; depending on impl name, align to expected part name
  form.append('file', file);
  const res = await axiosClient.post(
    `${BASE}/${productId}/variants/${variantId}/image`,
    form,
    { headers: { 'Content-Type': 'multipart/form-data' } }
  );
  return res.data;
}
