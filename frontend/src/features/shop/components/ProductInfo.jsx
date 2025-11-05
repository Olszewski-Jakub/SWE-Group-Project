"use client";

import { useEffect, useMemo, useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import Image from "next/image";
import { COFFEE_CATEGORIES } from "@/constants/coffeeCategories";
import VariantInfo from "./VariantInfo";

export default function ProductInfo({ product }) {
  const router = useRouter();
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

  // Category label mapping using coffee categories constant
  const categoryLabel = useMemo(() => {
    const toLabel = (val) => {
      if (!val) return "";
      const found = COFFEE_CATEGORIES.find((c) => c.value === val);
      if (found) return found.label;
      if (typeof val === "string") {
        return val.replaceAll("_", " ").replace(/\b\w/g, (ch) => ch.toUpperCase());
      }
      return String(val);
    };
    const cat = product?.category;
    if (Array.isArray(cat)) return cat.map(toLabel).filter(Boolean).join(", ");
    return toLabel(cat);
  }, [product?.category]);

  // Build absolute image URLs; prefer selected variant image when available
  const baseUrl = process.env.NEXT_PUBLIC_API_BASE_URL || "";
  const toAbsoluteUrl = (url) => {
    if (!url) return null;
    const isAbsolute = /^(?:[a-z]+:)?\/\//i.test(url) || url.startsWith("data:");
    if (isAbsolute) return url;
    const trimmedBase = baseUrl.replace(/\/$/, "");
    const path = url.startsWith("/") ? url : `/${url}`;
    return `${trimmedBase}${path}`;
  };

  const productFallbackImage = useMemo(() => {
    const raw = product?.urlImage ?? product?.imageUrl ?? product?.image ?? product?.images?.[0]?.url;
    return toAbsoluteUrl(raw) || "https://coffee.alexflipnote.dev/random";
  }, [product]);

  const selectedVariantImage = useMemo(() => {
    const v = selectedVariant;
    const raw = v?.imageUrl || v?.images?.[0]?.url;
    return toAbsoluteUrl(raw);
  }, [selectedVariant]);

  const mainImageUrl = selectedVariantImage || productFallbackImage;

  return (
    // Page-level wrapper with responsive horizontal padding and max width
    <main className="container mx-auto px-4 sm:px-6 lg:px-8 py-8">
      {/* Top bar: back + category */}
      <header className="mb-6">
        <div className="mb-3 flex items-center gap-3">
          <button
            type="button"
            onClick={() => router.back()}
            className="inline-flex items-center gap-2 rounded-md border border-stone-300 bg-white px-3 py-1.5 text-sm text-stone-700 shadow-sm transition-colors hover:bg-stone-50 focus:outline-none focus:ring-2 focus:ring-amber-500 focus:ring-offset-2"
            aria-label="Go back"
          >
            <svg className="h-4 w-4" viewBox="0 0 20 20" fill="currentColor" aria-hidden="true"><path fillRule="evenodd" d="M12.78 15.72a.75.75 0 01-1.06 0l-4.5-4.5a.75.75 0 010-1.06l4.5-4.5a.75.75 0 111.06 1.06L8.56 10l4.22 4.22a.75.75 0 010 1.06z" clipRule="evenodd"/></svg>
            Back
          </button>
          <span className="h-5 w-px bg-stone-300" aria-hidden="true" />
          <Link href="/products" className="inline-flex items-center gap-2 text-sm tracking-wide text-[#B6771D]/90 hover:text-[#7B542F]">
            <span className="uppercase">{categoryLabel}</span>
          </Link>
        </div>
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
          {/* Main product/variant image */}
          <div className="relative overflow-hidden rounded-xl border border-stone-200 bg-gradient-to-br from-[#FFF6E6] to-white shadow-sm">
            <div className="relative w-full aspect-square">
              <Image
                src={mainImageUrl}
                alt={product?.name || "Product image"}
                fill
                sizes="(max-width: 1024px) 100vw, 50vw"
                className="object-cover transition-transform duration-300 hover:scale-[1.02]"
                priority={false}
                unoptimized
              />
            </div>
          </div>

          {/* Variant thumbnails when available */}
          {variants.some(v => v?.imageUrl || v?.images?.[0]?.url) && (
            <div className="mt-3 flex gap-2 overflow-x-auto no-scrollbar">
              {variants.map((v, i) => {
                const thumb = toAbsoluteUrl(v?.imageUrl || v?.images?.[0]?.url);
                if (!thumb) return null;
                const isActive = i === variantIndex;
                return (
                  <button
                    key={v?.sku ?? i}
                    type="button"
                    onClick={() => setVariantIndex(i)}
                    className={`relative h-16 w-16 flex-none overflow-hidden rounded-md border ${isActive ? 'border-[#B6771D] ring-2 ring-[#FF9D00]' : 'border-stone-300'}`}
                    aria-label={`Select variant ${v?.sku ?? i+1}`}
                  >
                    <Image src={thumb} alt={v?.sku || `Variant ${i+1}`} fill className="object-cover" sizes="64px" unoptimized />
                  </button>
                );
              })}
            </div>
          )}
        </div>

        {/* Details column */}
        <div className="lg:col-span-6">
          {/* Description */}
          <section className="rounded-xl border border-stone-200 bg-white/70 p-4 shadow-sm backdrop-blur">
            <h2 className="mb-2 text-sm font-semibold tracking-wide text-stone-900">Description</h2>
            <p className="text-[15px] leading-7 text-stone-700">
              {product?.description || "No description available for this product."}
            </p>
          </section>

          {/* Variant selector */}
          {variants.length > 0 && (
            <div className="mt-6 space-y-2">
              <span className="text-sm font-medium text-gray-900">Select variant:</span>
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
          {/* Quick meta under selector */}
          {selectedVariant && (
            <div className="mt-2 text-xs text-stone-600">
              <span className="font-medium text-stone-800">SKU:</span> {selectedVariant.sku || "N/A"}
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


            <button className="mt-4 inline-flex items-center gap-2 rounded-md bg-[#FF9D00] px-5 py-2.5 text-sm font-semibold text-white shadow-sm transition-all duration-200 hover:bg-[#B6771D] focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-[#FF9D00]">
                <svg className="h-4 w-4" viewBox="0 0 20 20" fill="currentColor" aria-hidden="true"><path d="M3 3.75A.75.75 0 013.75 3h1.5a.75.75 0 01.728.568L6.53 7h9.72a.75.75 0 01.73.954l-1.5 5.25a.75.75 0 01-.73.546H7.28l-.3 1.2a.75.75 0 01-.73.55h-1.5a.75.75 0 010-1.5h.93l1.5-6H4.28l-.53-2.12H3.75a.75.75 0 01-.75-.75z"/></svg>
                Add to Cart
            </button>
        </div>
      </section>
    </main>
  );
}
