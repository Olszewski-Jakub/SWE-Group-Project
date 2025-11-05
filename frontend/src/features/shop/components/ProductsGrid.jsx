import ProductCard from './ProductCard';

/**
 * ProductsGrid is a "presentational" component. Its only job is to
 * receive a list of products and display them in a grid layout.
 *
 * @param {Object} props - The component's props.
 * @param {Array} [props.products=[]] - An array of product objects to display. Defaults to an empty array.
 */
export default function ProductsGrid({ products = [] }) {
  // If the products array is empty, display a user-friendly message.
  if (products.length === 0) {
    return (
      <div className="py-16 text-center">
        <p className="text-lg text-[#B6771D]">No products found at the moment.</p>
        <p className="mt-2 text-[#7B542F]">Please check back later!</p>
      </div>
    );
  }

  // If there are products, map over the array to render a card for each one.
  return (
    <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3">
      {products.map((product) => (
        // The `key` prop is crucial for React's performance. It must be a unique
        // identifier, so we use the product's ID.
        <ProductCard key={product.id} product={product} />
      ))}
    </div>
  );
}
