"use client";

import { useState, useEffect } from 'react';
import ProductsGrid from '../components/ProductsGrid';
import { getProducts, postSearch } from '../../../lib/ProductService';

/**
 * ProductsPage is the main component for the product catalog.
 * It is responsible for:
 * 1. Managing the state of the page (product list, loading status, errors).
 * 2. Fetching product data from the API when the component mounts.
 * 3. Conditionally rendering the UI based on the current state.
 */
export default function ProductsPage() {
  // State to store the array of products fetched from the API.
  const [products, setProducts] = useState([]);
  
  // New state for pagination
  const [currentPage, setCurrentPage] = useState(0); // Track the current page, starting at 0
  const [totalPages, setTotalPages] = useState(0);   // Store the total pages from the API response
  const [size] = useState(9); // Number of items per page

  // State to track whether the data is currently being fetched.
  const [loading, setLoading] = useState(true);
  // State to store any error messages if the fetch fails.
  const [error, setError] = useState(null);

  // Search & Filter state
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('');
  const [minPrice, setMinPrice] = useState('');
  const [maxPrice, setMaxPrice] = useState('');
  const [sortRule, setSortRule] = useState('');

  // Check if any filters are active
  const hasActiveFilters = searchTerm || selectedCategory || minPrice || maxPrice || sortRule;

  // The useEffect hook now depends on `currentPage`
  // It will re-run whenever `currentPage` changes.
  useEffect(() => {
    const fetchProducts = async () => {
      try {
        setLoading(true); 
        
        let pageData
        if (hasActiveFilters){
            const searchQuery = {
              key: searchTerm || null,
              category: selectedCategory || null,
              minPriceCents: minPrice ? minPrice : null,
              maxPriceCents: maxPrice ? maxPrice : null,
              sortRule: sortRule || null,
          };
          pageData = await postSearch(currentPage, size, searchQuery)
        } else {
          // Pass the current page to the service function
          pageData = await getProducts(currentPage, size); 
        }
        
        // Update both products and total pages from the response
        setProducts(pageData.content); 
        setTotalPages(pageData.totalPages);

      } catch (err) {
        setError(err.message); 
      } finally {
        setLoading(false); 
      }
    };

    fetchProducts();
  }, [currentPage]); // Dependency array now includes currentPage

  return (
    <section className="bg-white p-8 sm:p-12">
      <div className="mx-auto max-w-7xl">
        {/* Page header section */}
        <header className="mb-12 text-center">
          <h1 className="text-4xl font-bold tracking-tight text-[#7B542F] sm:text-5xl">
            Our Collection
          </h1>
          <p className="mt-4 text-lg text-[#B6771D]">
            Hand-crafted coffee and freshly baked pastries, just for you.
          </p>
        </header>

        {/* Conditional rendering based on the fetch state */}
        {loading && <p className="text-center text-[#B6771D]">Loading our delicious treats...</p>}
        {error && <p className="text-center text-red-500">{error}</p>}
        
        {/* Render grid and new pagination controls */}
        {!loading && !error && (
          <>
            <ProductsGrid products={products} />
            
            {/* Simple Pagination UI */}
            <div className="mt-12 flex justify-center items-center gap-4">
              <button
                onClick={() => setCurrentPage(currentPage - 1)}
                disabled={currentPage <= 0}
                className="px-4 py-2 bg-[#7B542F] text-white rounded disabled:bg-gray-300"
              >
                Previous
              </button>
              <span className="text-[#B6771D]">
                Page {currentPage + 1} of {totalPages}
              </span>
              <button
                onClick={() => setCurrentPage(currentPage + 1)}
                disabled={currentPage + 1 >= totalPages}
                className="px-4 py-2 bg-[#7B542F] text-white rounded disabled:bg-gray-300"
              >
                Next
              </button>
            </div>
          </>
        )}
      </div>
    </section>
  );
}