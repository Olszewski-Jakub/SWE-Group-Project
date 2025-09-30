"use client";

import ProductsGrid from '@/features/shop/components/ProductsGrid';

export default function ProductsPage() {
  return (
    <section className="space-y-8">
      <header className="space-y-3">
        <h1 className="text-2xl font-bold text-stone-900">Products</h1>
        <p className="text-stone-600">Wireframe — product listing layout with filters and grid.</p>
      </header>
      <ProductsGrid />
    </section>
  );
}
