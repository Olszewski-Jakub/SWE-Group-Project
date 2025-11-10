"use client";

import { useEffect, useMemo, useState } from "react";
import VariantInfo from "./VariantInfo";

export default function ProductInfo({ product }) {
  // Normalize variants to avoid crashes when product.variants is missing.
  const variants = (product && Array.isArray(product.variants)) ? product.variants : [];

  // Track selected variant index; default to 0 when available.
  const [variantIndex, setVariantIndex] = useState(0);

  // Ensure the selected index stays in range if the variants list changes.
  useEffect(() => {
    if (variantIndex >= variants.length) setVariantIndex(0);
  }, [variants.length, variantIndex]);

  // Compute the selected variant; undefined when no variants exist.
  const selectedVariant = useMemo(
    () => (variants.length > 0 ? variants[variantIndex] : undefined),
    [variants, variantIndex]
  );

  return (
    // Page-level wrapper with responsive horizontal padding and max width
    <main className="container mx-auto px-4 sm:px-6 lg:px-8 py-8">
      {/* Breadcrumbs or page heading area */}
      <header className="mb-6">
        <p className="text-sm uppercase tracking-wide text-[#B6771D]/90">
          {product?.category}
        </p>
        <h1 className="mt-1 text-2xl font-bold text-[#7B542F]">
          {product?.name}
        </h1>
      </header>

      {/* Responsive two-column layout:
         - Left: image/media
         - Right: description, variants, and details */}
      <section className="grid grid-cols-1 gap-8 lg:grid-cols-12">
        {/* Media column */}
        <div className="lg:col-span-6">
          {/* Image placeholder */}
          <div className="relative grid h-80 place-items-center rounded-lg bg-[#FFCF71]/40">
            <svg xmlns="http://www.w3.org/2000/svg" className="h-16 w-16 text-[#B6771D] opacity-60" viewBox="0 0 24 24" fill="currentColor" aria-hidden="true">
              <path d="M12.4,6.6c-1.3-1-3.1-1.5-4.9-1.5C4.2,5.1,1,8.3,1,11.7c0,2.5,2.1,4.6,4.6,4.6c2.5,0,4.6-2.1,4.6-4.6c0-0.4,0-0.9-0.1-1.3c1.3,0.3,2.7,0.5,4.1,0.5c3.3,0,6.5-2.2,6.5-5.2C20.7,5.7,17.4,5,12.4,6.6z M7.5,14.3c-1.4,0-2.6-1.2-2.6-2.6c0-1.4,1.2-2.6,2.6-2.6c1.4,0,2.6,1.2,2.6,2.6C10.1,13.1,8.9,14.3,7.5,14.3z M15.2,9.3c-0.8,0-1.5-0.7-1.5-1.5s0.7-1.5,1.5-1.5c0.8,0,1.5,0.7,1.5,1.5S16,9.3,15.2,9.3z" />
            </svg>
          </div>
        </div>

        {/* Details column */}
        <div className="lg:col-span-6">
          {/* Description */}
          <p className="text-base leading-relaxed text-gray-700">
            {product?.description}
          </p>

          {/* Variant selector: accessible tabs pattern */}
          {variants.length > 0 && (
            <div className="mt-6 space-y-2">
              <span className="text-sm font-medium text-gray-900">Select variant:</span>

              {/* role=tablist groups the variant "tabs" for screen readers */}
              <div
                role="tablist"
                aria-label="Product variants"
                className="flex flex-wrap gap-2"
              >
                {variants.map((variant, i) => {
                  const selected = i === variantIndex;
                  return (
                    <button
                      key={variant?.sku ?? i}
                      role="tab"
                      aria-selected={selected}
                      aria-controls={`variant-panel-${i}`}
                      id={`variant-tab-${i}`}
                      onClick={() => setVariantIndex(i)}
                      type="button"
                      className={[
                        // Base button styles
                        "rounded-md border px-3 py-1.5 text-sm transition",
                        // Focus ring for keyboard users
                        "focus:outline-none focus-visible:ring focus-visible:ring-[#FF9D00]",
                        // Hover feedback
                        "hover:border-[#B6771D] hover:bg-[#FFCF71]/20",
                        // Selected vs. unselected styles
                        selected
                          ? "border-[#B6771D] bg-[#FFCF71]/40 text-[#7B542F]"
                          : "border-gray-300 bg-white text-gray-900",
                      ].join(" ")}
                    >
                      {variant?.sku ?? `Variant ${i + 1}`}
                    </button>
                  );
                })}
              </div>
            </div>
          )}

          {/* Selected variant details */}
          {selectedVariant && (
            <div
              role="tabpanel"
              id={`variant-panel-${variantIndex}`}
              aria-labelledby={`variant-tab-${variantIndex}`}
              className="mt-6"
            >
              <VariantInfo variant={selectedVariant} />
            </div>
          )}


            <button className="mt-4 rounded-md bg-[#FF9D00] px-5 py-2.5 text-sm font-semibold text-white shadow-sm transition-all duration-200 hover:bg-[#B6771D] focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-[#FF9D00]">
                Add to Cart
            </button>
        </div>
      </section>
    </main>
  );
}
