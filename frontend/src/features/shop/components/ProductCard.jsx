import Link from "next/link";

/**
 * ProductCard displays a single product's information in a card format.
 * It's a reusable component that receives all product data via props.
 *
 * @param {Object} props - The component's props.
 * @param {Object} props.product - The product object to display.
 */
export default function ProductCard({ product }) {
  // Safely access the first variant in the variants array using optional chaining (`?.`).
  // This prevents errors if a product has no variants.
  const firstVariant = product.variants?.[0];

  // Determine an image URL from the variants (first one with an image)
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

  console.log(imageUrl)

  // Format the price into a currency string (e.g., "€2.50").
  // The Intl.NumberFormat API is a robust way to handle international currency formatting.
  const price = firstVariant
    ? new Intl.NumberFormat('en-IE', { // Use Irish English locale for Euro formatting.
        style: 'currency',
        currency: firstVariant.currency || 'EUR', // Use currency from data, or default to EUR.
      }).format(firstVariant.priceCents/100)
    : 'Not available';

  return (
    <Link href={`/product/${product.id}`}>
      <div className="group flex flex-col overflow-hidden rounded-lg border border-gray-200 bg-white shadow-sm transition-all duration-300 hover:shadow-lg hover:-translate-y-1">
        {/* Product Image (falls back to placeholder icon) */}
        <div className="relative h-48 bg-[#FFCF71]/40">
          {imageUrl ? (
            <img
              src={imageUrl}
              alt={product.name}
              className="h-full w-full object-cover"
              loading="lazy"
            />
          ) : (
            <div className="grid h-full w-full place-items-center">
              <svg xmlns="http://www.w3.org/2000/svg" className="h-12 w-12 text-[#B6771D] opacity-50" viewBox="0 0 24 24" fill="currentColor">
                <path d="M12.4,6.6c-1.3-1-3.1-1.5-4.9-1.5C4.2,5.1,1,8.3,1,11.7c0,2.5,2.1,4.6,4.6,4.6c2.5,0,4.6-2.1,4.6-4.6c0-0.4,0-0.9-0.1-1.3c1.3,0.3,2.7,0.5,4.1,0.5c3.3,0,6.5-2.2,6.5-5.2C20.7,5.7,17.4,5,12.4,6.6z M7.5,14.3c-1.4,0-2.6-1.2-2.6-2.6c0-1.4,1.2-2.6,2.6-2.6c1.4,0,2.6,1.2,2.6,2.6C10.1,13.1,8.9,14.3,7.5,14.3z M15.2,9.3c-0.8,0-1.5-0.7-1.5-1.5s0.7-1.5,1.5-1.5c0.8,0,1.5,0.7,1.5,1.5S16,9.3,15.2,9.3z"/>
              </svg>
            </div>
          )}
          <div className="absolute inset-0 bg-black/5 opacity-0 transition-opacity duration-300 group-hover:opacity-100"></div>
        </div>

        {/* Card Content Area */}
        <div className="flex flex-grow flex-col p-5">
          <h3 className="text-lg font-semibold text-[#7B542F]">{product.name}</h3>
          <p className="mb-4 text-sm font-medium capitalize text-[#B6771D]">{product.category.toLowerCase()}</p>
          
          {/* This div uses `mt-auto` to push itself to the bottom of the card */}
          <div className="mt-auto flex items-center justify-between">
            <span className="text-2xl font-bold text-[#7B542F]">{price}</span>
            <button className="rounded-md bg-[#FF9D00] px-5 py-2.5 text-sm font-semibold text-white shadow-sm transition-all duration-200 hover:bg-[#B6771D] focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-[#FF9D00]">
              Add to Cart
            </button>
          </div>
        </div>
      </div>
    </Link>

  );
}
