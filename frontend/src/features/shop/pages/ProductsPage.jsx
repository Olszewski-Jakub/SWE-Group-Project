"use client";

import { useState, useEffect, useCallback, useRef } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import ProductsGrid from '../components/ProductsGrid';
import { ProductsGridSkeleton, FiltersSkeleton } from '../components/Skeletons';
import { getProducts, postSearch } from '../../../lib/ProductService';
import { CATEGORIES, SORT_OPTIONS, ATTRIBUTE_FILTERS } from '@/constants/productFilters';
import {COFFEE_CATEGORIES} from "@/constants/coffeeCategories";

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
  const [totalElements, setTotalElements] = useState(null);
  const [facets, setFacets] = useState(null);

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
  const [showMobileSort, setShowMobileSort] = useState(false);
  const filterButtonRef = useRef(null);
  const sortButtonRef = useRef(null);
  const filterOverlayRef = useRef(null);
  const sortOverlayRef = useRef(null);

  // Accordion state for filters (expanded sections)
  const [openSections, setOpenSections] = useState({
    sort: true,
    category: true,
    price: true,
  });

  // Ref to store the debounce timers
  const searchDebounceTimerRef = useRef(null);
  const priceDebounceTimerRef = useRef(null);

  // Check if any filters are active
  const hasActiveFilters = debouncedSearchTerm || selectedCategory || debouncedMinPrice || debouncedMaxPrice || sortRule || Object.keys(attributeFilters).length > 0;

  // URL routing helpers
  const router = useRouter();
  const searchParams = useSearchParams();
  // const hydratedFromUrlRef is declared later for URL hydration

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

  // Hydrate state from URL params on initial mount
  const hydratedFromUrlRef = useRef(false);
  useEffect(() => {
    if (hydratedFromUrlRef.current) return;
    try {
      const params = new URLSearchParams(searchParams?.toString?.() || '');
      const key = params.get('key') || '';
      const category = params.get('category') || '';
      const minP = params.get('minPrice') || '';
      const maxP = params.get('maxPrice') || '';
      const sort = params.get('sort') || '';
      const pageParam = parseInt(params.get('page') || '1', 10);
      const newAttrs = {};
      params.forEach((value, k) => {
        if (k.startsWith('attr_')) {
          const name = k.replace(/^attr_/, '');
          const values = value.split(',').filter(Boolean);
          if (values.length) newAttrs[name] = values;
        }
      });

      setSearchTerm(key);
      setDebouncedSearchTerm(key);
      setSelectedCategory(category);
      setMinPrice(minP);
      setDebouncedMinPrice(minP);
      setMaxPrice(maxP);
      setDebouncedMaxPrice(maxP);
      setSortRule(sort);
      setAttributeFilters(newAttrs);
      setCurrentPage(Number.isFinite(pageParam) && pageParam > 0 ? pageParam - 1 : 0);
    } catch {}
    hydratedFromUrlRef.current = true;
  }, [searchParams]);

  // Sync state to URL params
  useEffect(() => {
    if (!hydratedFromUrlRef.current) return;
    const q = new URLSearchParams();
    if (debouncedSearchTerm) q.set('key', debouncedSearchTerm);
    if (selectedCategory) q.set('category', selectedCategory);
    if (debouncedMinPrice) q.set('minPrice', String(debouncedMinPrice));
    if (debouncedMaxPrice) q.set('maxPrice', String(debouncedMaxPrice));
    if (sortRule) q.set('sort', sortRule);
    q.set('page', String(currentPage + 1));
    Object.entries(attributeFilters).forEach(([name, values]) => {
      if (values?.length) q.set(`attr_${name}`, values.join(','));
    });

    const newQs = q.toString();
    const currentQs = searchParams?.toString?.() || '';
    if (newQs !== currentQs) {
      const path = typeof window !== 'undefined' ? window.location.pathname : '/products';
      // Replace without scroll jump
      router.replace(`${path}${newQs ? `?${newQs}` : ''}`);
    }
  }, [debouncedSearchTerm, selectedCategory, debouncedMinPrice, debouncedMaxPrice, sortRule, attributeFilters, currentPage]);

  // Basic focus trap for mobile overlays
  useEffect(() => {
    const trap = (container, onClose, returnRef) => {
      if (!container) return () => {};
      const selectors = [
        'a[href]', 'button:not([disabled])', 'input:not([disabled])', 'select:not([disabled])', 'textarea:not([disabled])', '[tabindex]:not([tabindex="-1"])'
      ];
      const getFocusable = () => Array.from(container.querySelectorAll(selectors.join(',')));
      const focusables = getFocusable();
      if (focusables[0]) focusables[0].focus();
      const handler = (e) => {
        if (e.key !== 'Tab') return;
        const f = getFocusable();
        if (!f.length) return;
        const first = f[0];
        const last = f[f.length - 1];
        if (e.shiftKey && document.activeElement === first) {
          e.preventDefault();
          last.focus();
        } else if (!e.shiftKey && document.activeElement === last) {
          e.preventDefault();
          first.focus();
        }
      };
      document.addEventListener('keydown', handler);
      return () => {
        document.removeEventListener('keydown', handler);
        if (returnRef?.current) returnRef.current.focus();
        if (onClose) onClose();
      };
    };

    let cleanup = () => {};
    if (showMobileFilters) {
      cleanup = trap(filterOverlayRef.current, null, filterButtonRef);
    } else if (showMobileSort) {
      cleanup = trap(sortOverlayRef.current, null, sortButtonRef);
    }
    return cleanup;
  }, [showMobileFilters, showMobileSort]);

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
        setTotalElements(pageData.totalElements ?? null);
        setFacets(pageData.facets || pageData.aggregations || null);

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
          className="w-full px-4 py-2 border border-stone-300 text-stone-700 rounded-lg hover:bg-stone-100 transition disabled:opacity-50 disabled:cursor-not-allowed"
        >
          Clear All Filters
        </button>
      </div>

      {/* Sort Filter */}
      <div className="mb-4 border-b border-stone-200 pb-4">
        <button
          onClick={() => setOpenSections((s) => ({ ...s, sort: !s.sort }))}
          className="w-full flex items-center justify-between text-left"
          aria-expanded={openSections.sort}
          aria-controls="filter-sort"
        >
          <h3 className="text-sm font-semibold text-stone-900">Sort By</h3>
          <svg className={`w-4 h-4 text-stone-700 transition-transform ${openSections.sort ? 'rotate-180' : ''}`} viewBox="0 0 20 20" fill="currentColor"><path fillRule="evenodd" d="M5.23 7.21a.75.75 0 011.06.02L10 10.94l3.71-3.71a.75.75 0 011.08 1.04l-4.25 4.25a.75.75 0 01-1.06 0L5.21 8.27a.75.75 0 01.02-1.06z" clipRule="evenodd"/></svg>
        </button>
        {openSections.sort && (
          <div className="mt-3 space-y-3" id="filter-sort">
            <select
              value={sortRule}
              onChange={(e) => {
                setSortRule(e.target.value);
                setCurrentPage(0);
              }}
              className="w-full px-4 py-2 border border-stone-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-amber-500"
            >
              {SORT_OPTIONS.map((option) => (
                <option key={option.value} value={option.value}>
                  {option.label}
                </option>
              ))}
            </select>
            <div className="flex justify-end">
              <button onClick={() => { setSortRule(''); setCurrentPage(0); }} className="text-xs text-stone-600 hover:underline">Clear</button>
            </div>
          </div>
        )}
      </div>

      {/* Category Filter */}
      <div className="mb-4 border-b border-stone-200 pb-4">
        <button
          onClick={() => setOpenSections((s) => ({ ...s, category: !s.category }))}
          className="w-full flex items-center justify-between text-left"
          aria-expanded={openSections.category}
          aria-controls="filter-category"
        >
          <h3 className="text-sm font-semibold text-stone-900">Category</h3>
          <svg className={`w-4 h-4 text-stone-700 transition-transform ${openSections.category ? 'rotate-180' : ''}`} viewBox="0 0 20 20" fill="currentColor"><path fillRule="evenodd" d="M5.23 7.21a.75.75 0 011.06.02L10 10.94l3.71-3.71a.75.75 0 011.08 1.04l-4.25 4.25a.75.75 0 01-1.06 0L5.21 8.27a.75.75 0 01.02-1.06z" clipRule="evenodd"/></svg>
        </button>
        {openSections.category && (
          <div className="mt-3 space-y-3" id="filter-category">
            <select
              value={selectedCategory}
              onChange={(e) => {
                setSelectedCategory(e.target.value);
                setCurrentPage(0);
              }}
              className="w-full px-4 py-2 border border-stone-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-amber-500"
            >
              {COFFEE_CATEGORIES.map((category) => (
                <option key={category.value} value={category.value}>
                  {category.label}
                </option>
              ))}
            </select>
            <div className="flex justify-end">
              <button onClick={() => { setSelectedCategory(''); setCurrentPage(0); }} className="text-xs text-stone-600 hover:underline">Clear</button>
            </div>
          </div>
        )}
      </div>

      {/* Price Range Filter */}
      <div className="mb-4 border-b border-stone-200 pb-4">
        <button
          onClick={() => setOpenSections((s) => ({ ...s, price: !s.price }))}
          className="w-full flex items-center justify-between text-left"
          aria-expanded={openSections.price}
          aria-controls="filter-price"
        >
          <h3 className="text-sm font-semibold text-stone-900">Price Range</h3>
          <svg className={`w-4 h-4 text-stone-700 transition-transform ${openSections.price ? 'rotate-180' : ''}`} viewBox="0 0 20 20" fill="currentColor"><path fillRule="evenodd" d="M5.23 7.21a.75.75 0 011.06.02L10 10.94l3.71-3.71a.75.75 0 011.08 1.04l-4.25 4.25a.75.75 0 01-1.06 0L5.21 8.27a.75.75 0 01.02-1.06z" clipRule="evenodd"/></svg>
        </button>
        {openSections.price && (
          <div id="filter-price" className="mt-3 space-y-2">
            <input
              type="number"
              placeholder="Min Price €"
              value={minPrice}
              onChange={(e) => setMinPrice(e.target.value)}
              className="w-full px-4 py-2 border border-stone-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-amber-500"
            />
            <input
              type="number"
              placeholder="Max Price €"
              value={maxPrice}
              onChange={(e) => setMaxPrice(e.target.value)}
              className="w-full px-4 py-2 border border-stone-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-amber-500"
            />
            <div className="flex justify-end">
              <button onClick={() => { setMinPrice(''); setMaxPrice(''); setCurrentPage(0); }} className="text-xs text-stone-600 hover:underline">Clear</button>
            </div>
          </div>
        )}
      </div>
      
      {/* Attribute Filters (Roast, Origin, Size) */}
      {ATTRIBUTE_FILTERS.map((attribute) => (
        <div key={attribute.name} className="mb-4 border-b border-stone-200 pb-4">
          <details className="group" open>
            <summary className="cursor-pointer list-none flex items-center justify-between">
              <h3 className="text-sm font-semibold text-stone-900">{attribute.label}</h3>
              <svg className="w-4 h-4 text-stone-700 transition-transform group-open:rotate-180" viewBox="0 0 20 20" fill="currentColor"><path fillRule="evenodd" d="M5.23 7.21a.75.75 0 011.06.02L10 10.94l3.71-3.71a.75.75 0 011.08 1.04l-4.25 4.25a.75.75 0 01-1.06 0L5.21 8.27a.75.75 0 01.02-1.06z" clipRule="evenodd"/></svg>
            </summary>
          <div className="mt-3 space-y-2">
            {attribute.options.map((option) => {
              const count = (facets && facets[attribute.name] && facets[attribute.name][option.value] != null)
                ? facets[attribute.name][option.value]
                : null;
              return (
                <label key={option.value} className="flex items-center cursor-pointer group">
                  <input
                    type="checkbox"
                    checked={attributeFilters[attribute.name]?.includes(option.value) || false}
                    onChange={() => handleAttributeFilterChange(attribute.name, option.value)}
                    className="w-4 h-4 text-amber-700 border-stone-300 rounded focus:ring-2 focus:ring-amber-500 cursor-pointer"
                  />
                  <span className="ml-2 text-sm text-stone-700 group-hover:text-stone-900">
                    {option.label}{typeof count === 'number' ? ` (${count})` : ''}
                  </span>
                </label>
              );
            })}
            <div className="flex justify-end">
              <button onClick={() => { setAttributeFilters((prev) => { const { [attribute.name]: _omit, ...rest } = prev; return rest; }); setCurrentPage(0); }} className="text-xs text-stone-600 hover:underline">Clear</button>
            </div>
          </div>
          </details>
        </div>
      ))}
    </>
  );

  return (
    <div className="space-y-12">
      {/* Header + Search card */}
      <section className="relative overflow-hidden rounded-2xl bg-gradient-to-b from-amber-50 to-white p-6 sm:p-8 shadow-sm">
        <header className="mb-4 sm:mb-6">
          <h1 className="text-3xl sm:text-4xl font-extrabold tracking-tight text-stone-900">Our Collection</h1>
          <p className="mt-2 text-stone-700">Hand-crafted coffee and freshly baked pastries, just for you.</p>
          <div className="mt-2 text-sm text-stone-600" aria-live="polite">
            {typeof totalElements === 'number' ? `${totalElements} results` : `${products.length} results`}
          </div>
        </header>

        {/* Search bar with mobile filter button */}
        <div className="mb-2 lg:mb-0">
          <div className="flex gap-2 items-center">
            <div className="relative flex-1">
              <span className="pointer-events-none absolute inset-y-0 left-3 flex items-center text-stone-400">
                <svg className="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-4.35-4.35M10 18a8 8 0 100-16 8 8 0 000 16z"/></svg>
              </span>
              <input
                type="text"
                inputMode="search"
                aria-label="Search products"
                placeholder="Search products..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="w-full rounded-full border border-stone-300 bg-white pl-10 pr-10 py-3 shadow-sm focus:outline-none focus:ring-2 focus:ring-amber-500"
              />
              {searchTerm && (
                <button
                  type="button"
                  onClick={() => setSearchTerm('')}
                  aria-label="Clear search"
                  className="absolute inset-y-0 right-2 my-1 px-2 rounded-full text-stone-600 hover:bg-stone-100"
                >
                  <svg className="h-5 w-5" viewBox="0 0 24 24" fill="none" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12"/></svg>
                </button>
              )}
            </div>
            {/* Mobile Filters Button - Hidden on desktop (lg breakpoint) */}
            <button
              onClick={() => setShowMobileFilters(!showMobileFilters)} // Toggle filters overlay open/closed
              className="lg:hidden px-4 py-3 border border-stone-300 text-stone-700 rounded-full hover:bg-stone-100 transition flex items-center gap-2"
              aria-haspopup="dialog"
              aria-expanded={showMobileFilters}
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
                <span className="bg-amber-700 text-white text-xs w-2 h-2 rounded-full"></span>
              )}
            </button>
          </div>
          {/* Active Filter Chips */}
          {hasActiveFilters && (
            <div className="mt-3 flex flex-wrap gap-2" aria-label="Active filters">
              {debouncedSearchTerm ? (
                <button onClick={() => setSearchTerm('')} className="px-3 py-1 rounded-full bg-stone-100 text-stone-700 text-sm inline-flex items-center gap-1">
                  <span>Search: {debouncedSearchTerm}</span>
                  <svg className="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12"/></svg>
                </button>
              ) : null}
              {selectedCategory ? (
                <button onClick={() => setSelectedCategory('')} className="px-3 py-1 rounded-full bg-stone-100 text-stone-700 text-sm inline-flex items-center gap-1">
                  <span>Category: {selectedCategory}</span>
                  <svg className="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12"/></svg>
                </button>
              ) : null}
              {debouncedMinPrice ? (
                <button onClick={() => setMinPrice('')} className="px-3 py-1 rounded-full bg-stone-100 text-stone-700 text-sm inline-flex items-center gap-1">
                  <span>Min: €{debouncedMinPrice}</span>
                  <svg className="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12"/></svg>
                </button>
              ) : null}
              {debouncedMaxPrice ? (
                <button onClick={() => setMaxPrice('')} className="px-3 py-1 rounded-full bg-stone-100 text-stone-700 text-sm inline-flex items-center gap-1">
                  <span>Max: €{debouncedMaxPrice}</span>
                  <svg className="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12"/></svg>
                </button>
              ) : null}
              {Object.entries(attributeFilters).flatMap(([name, values]) =>
                values.map((v) => (
                  <button key={`${name}:${v}`} onClick={() => handleAttributeFilterChange(name, v)} className="px-3 py-1 rounded-full bg-stone-100 text-stone-700 text-sm inline-flex items-center gap-1">
                    <span>{name}: {v}</span>
                    <svg className="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12"/></svg>
                  </button>
                ))
              )}
            </div>
          )}
        </div>
      </section>

      {/* Mobile Filters Overlay - Full screen modal, hidden on desktop */}
      {showMobileFilters && (
        <div className="lg:hidden fixed inset-0 z-50" role="dialog" aria-modal="true">
          <div className="absolute inset-0 bg-black/30" onClick={() => setShowMobileFilters(false)} aria-hidden="true" />
          <div ref={filterOverlayRef} className="absolute inset-y-0 right-0 w-full max-w-md bg-white shadow-xl overflow-y-auto transform transition-transform duration-300 ease-out translate-x-0">
            <div className="p-6">
              {/* Header: Title and close button */}
              <div className="flex justify-between items-center mb-6">
                <h2 className="text-2xl font-bold text-stone-900">Filters</h2>
                
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
              {loading ? <FiltersSkeleton /> : <FiltersContent />}
              </div>
            </div>
          </div>
        )}

      {/* Mobile Sort Sheet */}
      {showMobileSort && (
        <div className="lg:hidden fixed inset-0 z-50" role="dialog" aria-modal="true">
          <div className="absolute inset-0 bg-black/30" onClick={() => setShowMobileSort(false)} aria-hidden="true" />
          <div ref={sortOverlayRef} className="absolute bottom-0 w-full bg-white rounded-t-2xl shadow-xl transform transition-transform duration-300 ease-out">
            <div className="p-6">
              <div className="mb-4 flex items-center justify-between">
                <h3 className="text-lg font-semibold text-stone-900">Sort</h3>
                <button onClick={() => setShowMobileSort(false)} className="p-2 hover:bg-gray-100 rounded-lg" aria-label="Close sort">
                  <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12"/></svg>
                </button>
              </div>
              <div className="space-y-2">
                {SORT_OPTIONS.map((option) => (
                  <label key={option.value} className="flex items-center gap-3">
                    <input
                      type="radio"
                      name="mobile-sort"
                      value={option.value}
                      checked={sortRule === option.value}
                      onChange={(e) => { setSortRule(e.target.value); setCurrentPage(0); setShowMobileSort(false); }}
                      className="text-amber-700 focus:ring-amber-500"
                    />
                    <span className="text-stone-800">{option.label}</span>
                  </label>
                ))}
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Main content: Sidebar + Products Grid */}
      <section className="lg:grid lg:grid-cols-4 lg:gap-8">
          {/* Desktop Sidebar Filters - Hidden on mobile */}
          <aside className="hidden lg:block lg:col-span-1">
            <div className="bg-white border border-dashed border-stone-300 rounded-2xl p-6">
              <h2 className="text-xl font-bold text-stone-900 mb-6">Filters</h2>
              {loading ? <FiltersSkeleton /> : <FiltersContent />}
            </div>
          </aside>
          {/* Products Grid */}
          <main className="col-span-full lg:col-span-3">
            {/* Conditional rendering based on the fetch state */}
            {loading && <ProductsGridSkeleton />}
            {error && <p className="text-center text-red-500">{error}</p>}
            
            {/* Render grid and new pagination controls */}
            {!loading && !error && (
              <>
                <ProductsGrid products={products} />
                
                {/* Simple Pagination UI */}
                <div className="mt-10 flex justify-center items-center gap-4">
                  <button
                    onClick={() => setCurrentPage(currentPage - 1)}
                    disabled={currentPage <= 0}
                    className="px-4 py-2 rounded-md bg-amber-700 text-white hover:bg-amber-800 focus:outline-none focus:ring-2 focus:ring-amber-500 focus:ring-offset-2 disabled:bg-stone-300"
                  >
                    Previous
                  </button>
                  <span className="text-[#B6771D]">
                    Page {currentPage + 1} of {totalPages}
                  </span>
                  <button
                    onClick={() => setCurrentPage(currentPage + 1)}
                    disabled={currentPage + 1 >= totalPages}
                    className="px-4 py-2 rounded-md bg-amber-700 text-white hover:bg-amber-800 focus:outline-none focus:ring-2 focus:ring-amber-500 focus:ring-offset-2 disabled:bg-stone-300"
                  >
                    Next
                  </button>
                </div>
              </>
            )}
          </main>
        </section>

        {/* Mobile sticky actions */}
        <div className="lg:hidden fixed bottom-4 inset-x-0 px-4">
          <div className="mx-auto max-w-3xl rounded-full bg-white shadow-lg border border-stone-200 px-4 py-2 flex items-center justify-between">
            <span className="text-sm text-stone-700">{typeof totalElements === 'number' ? `${totalElements} results` : `${products.length} results`}</span>
            <div className="flex items-center gap-2">
              <button ref={sortButtonRef} onClick={() => setShowMobileSort(true)} className="px-4 py-2 rounded-full border border-stone-300 text-stone-700 hover:bg-stone-100">Sort</button>
              <button ref={filterButtonRef} onClick={() => setShowMobileFilters(true)} className="px-4 py-2 rounded-full bg-amber-700 text-white hover:bg-amber-800">Filter</button>
            </div>
          </div>
        </div>
    </div>
  );
}
