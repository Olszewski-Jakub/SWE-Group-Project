"use client";

import { useState, useEffect, useCallback, useRef } from 'react';
import ProductsGrid from '../components/ProductsGrid';
import { getProducts, postSearch } from '../../../lib/ProductService';
import { CATEGORIES, SORT_OPTIONS, ATTRIBUTE_FILTERS } from '@/constants/productFilters';

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
  const [attributeFilters, setAttributeFilters] = useState({});

  // Debounces
  const [debouncedSearchTerm, setDebouncedSearchTerm] = useState('');
  const [debouncedMinPrice, setDebouncedMinPrice] = useState('');
  const [debouncedMaxPrice, setDebouncedMaxPrice] = useState('');

  // Mobile filters toggle
  const [showMobileFilters, setShowMobileFilters] = useState(false);

  // Ref to store the debounce timers
  const searchDebounceTimerRef = useRef(null);
  const priceDebounceTimerRef = useRef(null);

  // Check if any filters are active
  const hasActiveFilters = debouncedSearchTerm || selectedCategory || debouncedMinPrice || debouncedMaxPrice || sortRule || Object.keys(attributeFilters).length > 0;

  /**
   * Debounce the search term input
   * Updates debouncedSearchTerm after 500ms of no typing
   */
  useEffect(() => {
    // Clear any existing timer
    if (searchDebounceTimerRef.current) {
      clearTimeout(searchDebounceTimerRef.current);
    }

    // Set a new timer to update the debounced value
    searchDebounceTimerRef.current = setTimeout(() => {
      setDebouncedSearchTerm(searchTerm);
      // Reset to first page when search term changes
      if (searchTerm !== debouncedSearchTerm) {
        setCurrentPage(0);
      }
    }, 500); // 500ms delay

    // Cleanup function to clear timer if component unmounts or searchTerm changes
    return () => {
      if (searchDebounceTimerRef.current) {
        clearTimeout(searchDebounceTimerRef.current);
      }
    };
  }, [searchTerm]);


/**
   * Debounce the price inputs
   * Updates debouncedMinPrice and debouncedMaxPrice after 500ms of no typing
   */
  useEffect(() => {
    // Clear any existing timer
    if (priceDebounceTimerRef.current) {
      clearTimeout(priceDebounceTimerRef.current);
    }

    // Set a new timer to update the debounced values
    priceDebounceTimerRef.current = setTimeout(() => {
      setDebouncedMinPrice(minPrice);
      setDebouncedMaxPrice(maxPrice);
      // Reset to first page when prices change
      if (minPrice !== debouncedMinPrice || maxPrice !== debouncedMaxPrice) {
        setCurrentPage(0);
      }
    }, 500); // 500ms delay

    // Cleanup function to clear timer if component unmounts or prices change
    return () => {
      if (priceDebounceTimerRef.current) {
        clearTimeout(priceDebounceTimerRef.current);
      }
    };
  }, [minPrice, maxPrice]); // Depend on both price inputs

  // The useEffect hook now depends on `currentPage`
  // It will re-run whenever `currentPage` changes.
  useEffect(() => {
    const fetchProducts = async () => {
      try {
        setLoading(true); 
        
        let pageData
        if (hasActiveFilters){
            // Convert attributeFilters object to array of AttributeFilterDTO
            const attributeFiltersArray = Object.entries(attributeFilters)
              .filter(([_, values]) => values.length > 0)
              .map(([name, values]) => ({ name, values }));
          
            const searchQuery = {
              key: debouncedSearchTerm || null,
              category: selectedCategory || null,
              minPriceCents: debouncedMinPrice ? Math.round(parseFloat(debouncedMinPrice) * 100) : null,
              maxPriceCents: debouncedMaxPrice ? Math.round(parseFloat(debouncedMaxPrice) * 100) : null,
              sortRule: sortRule || null,
              attributeFilters: attributeFiltersArray.length > 0 ? attributeFiltersArray : null,
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
  }, [currentPage, debouncedSearchTerm, selectedCategory, debouncedMinPrice, debouncedMaxPrice, sortRule, attributeFilters, hasActiveFilters, size]); 

  /**
   * Toggle a value inside attributeFilters[attributeName].
   * - Adds value if not present
   * - Removes value if already present
   * - Deletes the attribute key if it ends up empty
   */
  const handleAttributeFilterChange = (attributeName, value) => {
    setAttributeFilters(prev => {
      // Current list for this attribute (default to empty array)
      const list = prev[attributeName] ?? [];

      // Toggle presence of the value
      const nextList = list.includes(value)
        ? list.filter(v => v !== value)  // remove
        : [...list, value];              // add

      // If list becomes empty, drop the key entirely
      if (nextList.length === 0) {
        const { [attributeName]: _, ...rest } = prev;
        return rest;
      }

      // Otherwise, update the key with the new list
      return { ...prev, [attributeName]: nextList };
    });

    // Always reset pagination to the first page on filter change
    setCurrentPage(0);
  };

  /**
   * Clear all filters and reset to first page
   */
  const handleClearFilters = () => {
    setDebouncedSearchTerm('');
    setSelectedCategory('');
    setDebouncedMinPrice('');
    setDebouncedMaxPrice('');
    setSortRule('');
    setAttributeFilters({});
    setCurrentPage(0);
  };

  /**
   * Filters Component - Reused for both desktop sidebar and mobile overlay
   */
  const FiltersContent = () => (
    <>
      {/* Clear Filters Button */}
      <div className="mb-6">
        <button
          onClick={handleClearFilters}
          disabled={!hasActiveFilters}
          className="w-full px-4 py-2 border border-[#B6771D] text-[#7B542F] rounded-lg hover:bg-[#F5F5DC] transition disabled:opacity-50 disabled:cursor-not-allowed"
        >
          Clear All Filters
        </button>
      </div>

      {/* Sort Filter */}
      <div className="mb-6">
        <h3 className="text-sm font-semibold text-[#7B542F] mb-3">Sort By</h3>
        <select
          value={sortRule}
          onChange={(e) => {
            setSortRule(e.target.value);
            setCurrentPage(0);
          }}
          className="w-full px-4 py-2 border border-[#B6771D] rounded-lg focus:outline-none focus:ring-2 focus:ring-[#7B542F]"
        >
          {SORT_OPTIONS.map((option) => (
            <option key={option.value} value={option.value}>
              {option.label}
            </option>
          ))}
        </select>
      </div>

      {/* Category Filter */}
      <div className="mb-6">
        <h3 className="text-sm font-semibold text-[#7B542F] mb-3">Category</h3>
        <select
          value={selectedCategory}
          onChange={(e) => {
            setSelectedCategory(e.target.value);
            setCurrentPage(0);
          }}
          className="w-full px-4 py-2 border border-[#B6771D] rounded-lg focus:outline-none focus:ring-2 focus:ring-[#7B542F]"
        >
          {CATEGORIES.map((category) => (
            <option key={category.value} value={category.value}>
              {category.label}
            </option>
          ))}
        </select>
      </div>

      {/* Price Range Filter */}
      <div className="mb-6">
        <h3 className="text-sm font-semibold text-[#7B542F] mb-3">Price Range</h3>
        <div className="space-y-2">
          <input
            type="number"
            placeholder="Min Price €"
            value={minPrice}
            onChange={(e) => setMinPrice(e.target.value)}
            className="w-full px-4 py-2 border border-[#B6771D] rounded-lg focus:outline-none focus:ring-2 focus:ring-[#7B542F]"
          />
          <input
            type="number"
            placeholder="Max Price €"
            value={maxPrice}
            onChange={(e) => setMaxPrice(e.target.value)}
            className="w-full px-4 py-2 border border-[#B6771D] rounded-lg focus:outline-none focus:ring-2 focus:ring-[#7B542F]"
          />
        </div>
      </div>
      
      {/* Attribute Filters (Roast, Origin, Size) */}
      {ATTRIBUTE_FILTERS.map((attribute) => (
        <div key={attribute.name} className="mb-6">
          <h3 className="text-sm font-semibold text-[#7B542F] mb-3">{attribute.label}</h3>
          <div className="space-y-2">
            {attribute.options.map((option) => (
              <label key={option.value} className="flex items-center cursor-pointer group">
                <input
                  type="checkbox"
                  checked={attributeFilters[attribute.name]?.includes(option.value) || false}
                  onChange={() => handleAttributeFilterChange(attribute.name, option.value)}
                  className="w-4 h-4 text-[#7B542F] border-[#B6771D] rounded focus:ring-2 focus:ring-[#7B542F] cursor-pointer"
                />
                <span className="ml-2 text-sm text-[#7B542F] group-hover:text-[#5D3E23]">
                  {option.label}
                </span>
              </label>
            ))}
          </div>
        </div>
      ))}
    </>
  );

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

        {/* Search bar with mobile filter button */}
        <div className="mb-6 lg:mb-8">
          <div className="flex gap-2">
            <input
              type="text"
              placeholder="Search products..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="flex-1 px-4 py-3 border border-[#B6771D] rounded-lg focus:outline-none focus:ring-2 focus:ring-[#7B542F]"
            />
            {/* Mobile Filters Button - Hidden on desktop (lg breakpoint) */}
            <button
              onClick={() => setShowMobileFilters(!showMobileFilters)} // Toggle filters overlay open/closed
              className="lg:hidden px-4 py-3 border border-[#B6771D] text-[#7B542F] rounded-lg hover:bg-[#F5F5DC] transition flex items-center gap-2"
            >
              {/* Icon */}
              <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path 
                  strokeLinecap="round" 
                  strokeLinejoin="round" 
                  strokeWidth={2} 
                  d="M12 6V4m0 2a2 2 0 100 4m0-4a2 2 0 110 4m-6 8a2 2 0 100-4m0 4a2 2 0 110-4m0 4v2m0-6V4m6 6v10m6-2a2 2 0 100-4m0 4a2 2 0 110-4m0 4v2m0-6V4" 
                />
              </svg>
              
              {/* Button label */}
              <span>Filters</span>
              
              {/* Active filters indicator dot - only shown when filters are applied */}
              {hasActiveFilters && (
                <span className="bg-[#7B542F] text-white text-xs w-2 h-2 rounded-full"></span>
              )}
            </button>
          </div>
        </div>
        
        {/* Mobile Filters Overlay - Full screen modal, hidden on desktop */}
        {showMobileFilters && (
          <div className="lg:hidden fixed inset-0 bg-white z-50 overflow-y-auto">
            <div className="p-6">
              {/* Header: Title and close button */}
              <div className="flex justify-between items-center mb-6">
                <h2 className="text-2xl font-bold text-[#7B542F]">Filters</h2>
                
                {/* Close button */}
                <button
                  onClick={() => setShowMobileFilters(false)}
                  className="p-2 hover:bg-gray-100 rounded-lg"
                  aria-label="Close filters"
                >
                  <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                  </svg>
                </button>
              </div>
              
              {/* Filters content (same as desktop sidebar) */}
              <FiltersContent />
            </div>
          </div>
        )}

        {/* Main content: Sidebar + Products Grid */}
        <div className="lg:grid lg:grid-cols-4 lg:gap-8">
          {/* Desktop Sidebar Filters - Hidden on mobile */}
          <aside className="hidden lg:block lg:col-span-1">
            <div className="sticky top-4 bg-[#FCFCF9] border border-[#B6771D] rounded-lg p-6 max-h-[calc(100vh-2rem)] overflow-y-auto">
              <h2 className="text-xl font-bold text-[#7B542F] mb-6">Filters</h2>
              <FiltersContent />
            </div>
          </aside>
          {/* Products Grid */}
          <main className="col-span-full lg:col-span-3">
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
          </main>
        </div>
      </div>
    </section>
  );
}