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
    // Use the axiosClient to make a GET request.
    // The base URL (http://... or /api) is already handled by the client.
    const response = await axiosClient.get(`/api/v1/products?page=${page}&size=${size}`);
    
    // With axios, the data is directly on the `data` property
    return response.data;
  } catch (error) {
    console.error('Failed to fetch products:', error);
    throw new Error('Sorry, we could not fetch the products right now.');
  }
};

