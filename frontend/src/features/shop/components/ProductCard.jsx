/**
 * ProductCard displays a single product's information in a card format.
 * It's a reusable component that receives all product data via props.
 *
 * @param {Object} props - The component's props.
 * @param {Object} props.product - The product object to display.
 */
import Image from 'next/image';
import { useState, useMemo } from 'react';
import Link from 'next/link';
import { COFFEE_CATEGORIES } from '@/constants/coffeeCategories';

export default function ProductCard({ product }) {
  const variants = product.variants || [];
  const firstVariant = variants[0];
  const formatPrice = (priceCents, currency = 'EUR') =>
    new Intl.NumberFormat('en-IE', { style: 'currency', currency }).format((priceCents || 0) / 100);

  const variantWithImage = product.variants?.find(v => v?.imageUrl);
  const rawImageUrl = variantWithImage?.imageUrl || null;

  // Build a full URL when the API returns a relative path (e.g., "/api/v1/.../image")
  const baseUrl = process.env.NEXT_PUBLIC_API_BASE_URL || '';
  const imageUrl = (() => {
      if (!rawImageUrl) return null;
      const isAbsolute = /^(?:[a-z]+:)?\/\//i.test(rawImageUrl) || rawImageUrl.startsWith('data:');
      if (isAbsolute) return rawImageUrl;
      const trimmedBase = baseUrl.replace(/\/$/, '');
      const path = rawImageUrl.startsWith('/') ? rawImageUrl : `/${rawImageUrl}`;
      return `${trimmedBase}${path}`;
  })();
  const finalImage = imageUrl || 'https://coffee.alexflipnote.dev/random';

  // Rating values (optional)
  const rating = typeof product.rating === 'number' ? product.rating : product.averageRating;
  const reviewCount = product.reviewCount || product.reviewsCount || product.numReviews;

  // Map category value(s) to human labels using COFFEE_CATEGORIES
  const categoryLabel = (() => {
    const toLabel = (val) => {
      if (!val) return '';
      const found = COFFEE_CATEGORIES.find((c) => c.value === val);
      if (found) return found.label;
      if (typeof val === 'string') {
        return val
          .replaceAll('_', ' ')
          .replace(/\b\w/g, (ch) => ch.toUpperCase());
      }
      return String(val);
    };
    const cat = product.category;
    if (Array.isArray(cat)) return cat.map(toLabel).filter(Boolean).join(', ');
    return toLabel(cat);
  })();

  // Variant swatches (optional)
  const { sizes, roasts } = useMemo(() => {
    const result = { sizes: new Set(), roasts: new Set() };
    (variants).forEach((v) => {
      const attrs = v.attributes || v.attrs || {};
      const size = attrs.size_ml || attrs.size || attrs['size_ml'];
      const roast = attrs.roast || attrs['roast'];
      if (size) result.sizes.add(String(size));
      if (roast) result.roasts.add(String(roast));
    });
    return { sizes: Array.from(result.sizes), roasts: Array.from(result.roasts) };
  }, [variants]);

  const [selectedSize, setSelectedSize] = useState(null);
  const [selectedRoast, setSelectedRoast] = useState(null);
  const [selectedVariantIndex, setSelectedVariantIndex] = useState(null);

  const hasExplicitAttrs = (sizes.length > 0 || roasts.length > 0);
  const showGenericVariantSwatches = (variants.length > 1 && !hasExplicitAttrs);

  const requiresSelection = (sizes.length > 1 || roasts.length > 1 || showGenericVariantSwatches);
  const selectionSatisfied = (
    (!sizes.length || selectedSize) && (!roasts.length || selectedRoast) && (!showGenericVariantSwatches || selectedVariantIndex !== null)
  );

  // Display price: if generic variants and none selected, show a range; else show selected or first variant price
  const displayPriceText = (() => {
    if (showGenericVariantSwatches) {
      if (selectedVariantIndex !== null) {
        const v = variants[selectedVariantIndex];
        return formatPrice(v?.priceCents, v?.currency || firstVariant?.currency || 'EUR');
      }
      const prices = variants.map(v => v.priceCents).filter(Boolean).sort((a,b)=>a-b);
      if (prices.length >= 2) {
        const min = prices[0];
        const max = prices[prices.length - 1];
        const cur = variants[0]?.currency || 'EUR';
        return `${formatPrice(min, cur)} – ${formatPrice(max, cur)}`;
      }
    }
    if (firstVariant) return formatPrice(firstVariant.priceCents, firstVariant.currency || 'EUR');
    return 'Not available';
  })();

  const handleAdd = () => {
    // Placeholder quick-add; no cart system present yet
    // eslint-disable-next-line no-console
    console.log('Quick add', {
      productId: product.id,
      selectedSize,
      selectedRoast,
      selectedVariant: selectedVariantIndex !== null ? variants[selectedVariantIndex]?.sku : undefined,
    });
  };

  // Dynamic text sizing for chips to avoid wrapping and layout shift
  const chipTextClass = (label) => {
    const len = (label || '').length;
    if (len > 14) return 'text-[10px]';
    if (len > 10) return 'text-[11px]';
    return 'text-xs';
  };

  // Reverted dynamic price font shrinking per request

  

  return (
    <Link href={`/product/${product?.id}`} className="group flex flex-col overflow-hidden rounded-xl border border-stone-200 bg-white shadow-sm transition-all duration-300 hover:shadow-md hover:-translate-y-0.5">
      {/* Image */}
      <div className="relative h-48 bg-stone-100">
        <Image
          src={finalImage}
          alt={product.name}
          fill
          sizes="(max-width: 1024px) 50vw, 33vw"
          className="object-cover"
          priority={false}
          unoptimized
        />
        <div className="absolute inset-0 bg-black/0 transition-opacity duration-300 group-hover:bg-black/5" />
      </div>

      {/* Card Content */}
      <div className="flex flex-grow flex-col p-5">
        <h3 className="text-lg font-semibold text-stone-900">{product.name}</h3>
        <p className="text-sm font-medium text-stone-500">{categoryLabel}</p>

        {/* Rating */}
        {typeof rating === 'number' && (
          <div className="mt-2 flex items-center gap-2" aria-label={`Rating ${rating} out of 5`}>
            <div className="relative inline-block align-middle" aria-hidden="true">
              <div className="flex text-stone-300">
                {[...Array(5)].map((_, i) => (
                  <svg key={i} className="h-4 w-4" viewBox="0 0 20 20" fill="currentColor"><path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.801 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81H7.03a1 1 0 00.95-.69l1.07-3.292z"/></svg>
                ))}
              </div>
              <div className="absolute inset-0 overflow-hidden" style={{ width: `${Math.max(0, Math.min(5, rating)) / 5 * 100}%` }}>
                <div className="flex text-amber-500">
                  {[...Array(5)].map((_, i) => (
                    <svg key={i} className="h-4 w-4" viewBox="0 0 20 20" fill="currentColor"><path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.801 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81H7.03a1 1 0 00.95-.69l1.07-3.292z"/></svg>
                  ))}
                </div>
              </div>
            </div>
            {reviewCount ? <span className="text-xs text-stone-500">({reviewCount})</span> : null}
          </div>
        )}

        {/* Swatches */}
        {(sizes.length > 0 || roasts.length > 0) && (
          <div className="mt-3 space-y-2">
            {sizes.length > 0 && (
              <div className="flex flex-nowrap overflow-x-auto no-scrollbar items-center gap-2 h-8" aria-label="Select size">
                {sizes.map((s) => (
                  <button
                    key={s}
                    type="button"
                    onClick={(e) => { e.preventDefault(); e.stopPropagation(); setSelectedSize(s === selectedSize ? null : s); }}
                    className={`px-3 h-8 inline-flex items-center rounded-full border whitespace-nowrap leading-none ${selectedSize === s ? 'border-amber-500 bg-amber-50 text-amber-800' : 'border-stone-300 text-stone-700 hover:bg-stone-100'} ${chipTextClass(String(s))}`}
                    aria-pressed={selectedSize === s}
                  >
                    {s}ml
                  </button>
                ))}
              </div>
            )}
            {roasts.length > 0 && (
              <div className="flex flex-nowrap overflow-x-auto no-scrollbar items-center gap-2 h-8" aria-label="Select roast">
                {roasts.map((r) => (
                  <button
                    key={r}
                    type="button"
                    onClick={(e) => { e.preventDefault(); e.stopPropagation(); setSelectedRoast(r === selectedRoast ? null : r); }}
                    className={`px-3 h-8 inline-flex items-center rounded-full border capitalize whitespace-nowrap leading-none ${selectedRoast === r ? 'border-amber-500 bg-amber-50 text-amber-800' : 'border-stone-300 text-stone-700 hover:bg-stone-100'} ${chipTextClass(String(r))}`}
                    aria-pressed={selectedRoast === r}
                  >
                    {r}
                  </button>
                ))}
              </div>
            )}
          </div>
        )}

        {/* Fallback generic variant swatches when attributes are missing */}
        {showGenericVariantSwatches && (
          <div className="mt-3 space-y-2" aria-label="Select variant">
            <div className="flex flex-nowrap overflow-x-auto no-scrollbar items-center gap-2 h-8">
              {variants.map((v, idx) => (
                <button
                  key={v.sku || idx}
                  type="button"
                  onClick={(e) => { e.preventDefault(); e.stopPropagation(); setSelectedVariantIndex(selectedVariantIndex === idx ? null : idx); }}
                  className={`px-3 h-8 inline-flex items-center rounded-full border whitespace-nowrap leading-none ${selectedVariantIndex === idx ? 'border-amber-500 bg-amber-50 text-amber-800' : 'border-stone-300 text-stone-700 hover:bg-stone-100'} ${chipTextClass(v.sku || `Variant ${idx+1}`)}`}
                  aria-pressed={selectedVariantIndex === idx}
                  title={v.sku || `Variant ${idx+1}`}
                >
                  {v.sku || `Variant ${idx+1}`}
                </button>
              ))}
            </div>
          </div>
        )}

        <div className="mt-auto grid grid-cols-[1fr_auto] items-center gap-3 pt-4 min-h-[48px]">
          <div className="min-w-0">
            <span className="text-2xl leading-tight font-bold text-stone-900 truncate">{displayPriceText}</span>
          </div>
          {requiresSelection && !selectionSatisfied ? (
            <button
              disabled
              onClick={(e) => { e.preventDefault(); e.stopPropagation(); }}
              className="rounded-md bg-stone-200 px-5 py-2.5 text-sm font-medium text-stone-600 shadow-sm cursor-not-allowed whitespace-nowrap"
            >
              Select options
            </button>
          ) : (
            <button
              onClick={(e) => { e.preventDefault(); e.stopPropagation(); handleAdd(); }}
              className="rounded-md bg-amber-700 px-5 py-2.5 text-sm font-medium text-white shadow-sm transition-colors hover:bg-amber-800 focus:outline-none focus:ring-2 focus:ring-amber-500 focus:ring-offset-2 whitespace-nowrap"
            >
              Add to Cart
            </button>
          )}
        </div>
      </div>
    </Link>
  );
}
