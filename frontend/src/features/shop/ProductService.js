/**
 * @file This service module centralizes all API calls related to products.
 * Keeping API logic here makes components cleaner and easier to manage.
 */

/**
 * Fetches a paginated list of products from the API.
 * The Next.js proxy (in next.config.js) forwards any requests to /api/*
 * to the backend server running on http://localhost:8080.
 *
 * @param {number} [page=0] - The page number to fetch.
 * @param {number} [size=9] - The number of products per page.
 * @returns {Promise<{
 * content: Array<any>,
 * page: number,
 * size: number,
 * totalElements: number,
 * totalPages: number
 * }>} A promise that resolves to the paginated data object from the backend.
 * @throws {Error} If the network response is not OK (e.g., server error).
 */
export const getProducts = async (page = 0, size = 9) => {
  // Make a GET request to our backend's product endpoint.
  const response = await fetch(`/api/v1/products?page=${page}&size=${size}`);

  // If the server responds with an error status (like 500), throw an error
  // which can be caught by the component calling this function.
  if (!response.ok) {
    throw new Error('Sorry, we could not fetch the products right now.');
  }

  // Parse the JSON response body and return it.
  return response.json();
};