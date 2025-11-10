/**
 * @file This service module centralizes all API calls related to products.
 * It uses the central 'axiosClient' to make requests.
 */

// Import the central axios client
import axiosClient from './axiosClient';

/**
 * Fetches a paginated list of products from the API.
 *
 * @param {number} [page=0] - The page number to fetch.
 * @param {number} [size=9] - The number of products per page.
 * @returns {Promise<any>} A promise that resolves to the paginated product data.
 * @throws {Error} If the network response is not OK.
 */
export const getProducts = async (page = 0, size = 9) => {
  try {
    const response = await axiosClient.get(`/products?page=${page}&size=${size}`);
    
    // With axios, the data is directly on the `data` property
    return response.data;
  } catch (error) {
    console.error('Failed to fetch products:', error);
    throw new Error('Sorry, we could not fetch the products right now.');
  }
};

/**
 * Search products with filters and pagination
 * @param {Object} searchQuery - Search criteria
 * @param {string|null} searchQuery.key - Search term for product name/description
 * @param {string|null} searchQuery.category - Category filter
 * @param {number|null} searchQuery.minPriceCents - Minimum price in cents
 * @param {number|null} searchQuery.maxPriceCents - Maximum price in cents
 * @param {string|null} searchQuery.sortRule - Sort rule
 * @param {Object|null} searchQuery.attributeFilters - Additional attribute filters
 * @param {number} [page=0] - The page number to fetch.
 * @param {number} [size=9] - The number of products per page.
 * @returns {Promise<any>} A promise that resolves to the paginated product data.
 * @throws {Error} If the network response is not OK.
 */
export const postSearch = async (page = 0, size = 9, searchQuery = {}) => {
  try {
    // Build request body from searchQuery parameter
    const requestBody = {
      key: searchQuery.key || null,
      category: searchQuery.category || null,
      minPriceCents: searchQuery.minPriceCents || null,
      maxPriceCents: searchQuery.maxPriceCents || null,
      sortRule: searchQuery.sortRule || null,
      attributeFilters: searchQuery.attributeFilters || null
    };

    const response = await axiosClient.post(
      `/products/search?page=${page}&size=${size}`,
      requestBody
    );
    
    return response.data;
  } catch (error) {
    console.error('Failed to search products', error);
    throw new Error('Sorry, something went wrong during search');
  }
};

/**
 * Fetch a product by ID.
 * @param {string} id - product id
 */
export const getProduct = async (id) => {
  try {
    // GET /products/:id
    const res = await axiosClient.get(`/products/${id}`);
    return res.data;
  } catch (err) {
    console.error("Failed to fetch product", err);
    throw new Error("Sorry, we could not fetch this product right now.");
  }
};


