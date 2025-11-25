"use client";

import {
  CATEGORIES,
  SORT_OPTIONS,
  ATTRIBUTE_FILTERS,
} from "@/constants/productFilters";
import { COFFEE_CATEGORIES } from "@/constants/coffeeCategories";
import { memo } from "react";

/**
 * FiltersComponent
 * Responsible for rendering the Sidebar and Mobile Filter UI.
 * It is memoized to prevent unnecessary re-renders if props haven't changed.
 */
const FiltersComponent = ({
  // State values
  hasActiveFilters,
  sortRule,
  selectedCategory,
  minPrice,
  maxPrice,
  attributeFilters,
  facets,
  openSections,
  
  // State setters / Handlers
  setOpenSections,
  setSortRule,
  setSelectedCategory,
  setMinPrice,
  setMaxPrice,
  setAttributeFilters,
  handleAttributeFilterChange,
  handleClearFilters,
  resetPage, // Helper to set page to 0
}) => {

  return (
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
          <svg
            className={`w-4 h-4 text-stone-700 transition-transform ${
              openSections.sort ? "rotate-180" : ""
            }`}
            viewBox="0 0 20 20"
            fill="currentColor"
          >
            <path
              fillRule="evenodd"
              d="M5.23 7.21a.75.75 0 011.06.02L10 10.94l3.71-3.71a.75.75 0 011.08 1.04l-4.25 4.25a.75.75 0 01-1.06 0L5.21 8.27a.75.75 0 01.02-1.06z"
              clipRule="evenodd"
            />
          </svg>
        </button>
        {openSections.sort && (
          <div className="mt-3 space-y-3" id="filter-sort">
            <select
              value={sortRule}
              onChange={(e) => {
                setSortRule(e.target.value);
                resetPage();
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
              <button
                onClick={() => {
                  setSortRule("");
                  resetPage();
                }}
                className="text-xs text-stone-600 hover:underline"
              >
                Clear
              </button>
            </div>
          </div>
        )}
      </div>

      {/* Category Filter */}
      <div className="mb-4 border-b border-stone-200 pb-4">
        <button
          onClick={() =>
            setOpenSections((s) => ({ ...s, category: !s.category }))
          }
          className="w-full flex items-center justify-between text-left"
          aria-expanded={openSections.category}
          aria-controls="filter-category"
        >
          <h3 className="text-sm font-semibold text-stone-900">Category</h3>
          <svg
            className={`w-4 h-4 text-stone-700 transition-transform ${
              openSections.category ? "rotate-180" : ""
            }`}
            viewBox="0 0 20 20"
            fill="currentColor"
          >
            <path
              fillRule="evenodd"
              d="M5.23 7.21a.75.75 0 011.06.02L10 10.94l3.71-3.71a.75.75 0 011.08 1.04l-4.25 4.25a.75.75 0 01-1.06 0L5.21 8.27a.75.75 0 01.02-1.06z"
              clipRule="evenodd"
            />
          </svg>
        </button>
        {openSections.category && (
          <div className="mt-3 space-y-3" id="filter-category">
            <select
              value={selectedCategory}
              onChange={(e) => {
                setSelectedCategory(e.target.value);
                resetPage();
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
              <button
                onClick={() => {
                  setSelectedCategory("");
                  resetPage();
                }}
                className="text-xs text-stone-600 hover:underline"
              >
                Clear
              </button>
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
          <svg
            className={`w-4 h-4 text-stone-700 transition-transform ${
              openSections.price ? "rotate-180" : ""
            }`}
            viewBox="0 0 20 20"
            fill="currentColor"
          >
            <path
              fillRule="evenodd"
              d="M5.23 7.21a.75.75 0 011.06.02L10 10.94l3.71-3.71a.75.75 0 011.08 1.04l-4.25 4.25a.75.75 0 01-1.06 0L5.21 8.27a.75.75 0 01.02-1.06z"
              clipRule="evenodd"
            />
          </svg>
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
              <button
                onClick={() => {
                  setMinPrice("");
                  setMaxPrice("");
                  resetPage();
                }}
                className="text-xs text-stone-600 hover:underline"
              >
                Clear
              </button>
            </div>
          </div>
        )}
      </div>

      {/* Attribute Filters (Roast, Origin, Size) */}
      {ATTRIBUTE_FILTERS.map((attribute) => (
        <div
          key={attribute.name}
          className="mb-4 border-b border-stone-200 pb-4"
        >
          <details className="group">
            <summary className="cursor-pointer list-none flex items-center justify-between">
              <h3 className="text-sm font-semibold text-stone-900">
                {attribute.label}
              </h3>
              <svg
                className="w-4 h-4 text-stone-700 transition-transform group-open:rotate-180"
                viewBox="0 0 20 20"
                fill="currentColor"
              >
                <path
                  fillRule="evenodd"
                  d="M5.23 7.21a.75.75 0 011.06.02L10 10.94l3.71-3.71a.75.75 0 011.08 1.04l-4.25 4.25a.75.75 0 01-1.06 0L5.21 8.27a.75.75 0 01.02-1.06z"
                  clipRule="evenodd"
                />
              </svg>
            </summary>
            <div className="mt-3 space-y-2">
              {attribute.options.map((option) => {
                const count =
                  facets &&
                  facets[attribute.name] &&
                  facets[attribute.name][option.value] != null
                    ? facets[attribute.name][option.value]
                    : null;
                return (
                  <label
                    key={option.value}
                    className="flex items-center cursor-pointer group"
                  >
                    <input
                      type="checkbox"
                      checked={
                        attributeFilters[attribute.name]?.includes(
                          option.value
                        ) || false
                      }
                      onChange={() =>
                        handleAttributeFilterChange(attribute.name, option.value)
                      }
                      className="w-4 h-4 text-amber-700 border-stone-300 rounded focus:ring-2 focus:ring-amber-500 cursor-pointer"
                    />
                    <span className="ml-2 text-sm text-stone-700 group-hover:text-stone-900">
                      {option.label}
                      {typeof count === "number" ? ` (${count})` : ""}
                    </span>
                  </label>
                );
              })}
              <div className="flex justify-end">
                                <button
                  onClick={() => {
                    setAttributeFilters((prev) => {
                      const { [attribute.name]: _omit, ...rest } = prev;
                      return rest;
                    });
                    
                    resetPage();
                  }}
                  className="text-xs text-stone-600 hover:underline"
                >
                  Clear
                </button>
              </div>
            </div>
          </details>
        </div>
      ))}
    </>
  );
};

// Use memo so this component only re-renders when its direct props change,
// not when the parent re-renders for other reasons (like just fetching data)
export default memo(FiltersComponent);
