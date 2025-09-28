import ProductCard from './ProductCard';

export default function ProductsGrid({ count = 6 }) {
  return (
    <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3">
      {Array.from({ length: count }).map((_, i) => (
        <ProductCard key={i} index={i} />
      ))}
    </div>
  );
}

