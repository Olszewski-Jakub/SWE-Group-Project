"use client"

import { useState, useEffect } from 'react';
import ProductCard from '../../shop/components/ProductCard';
import { getProducts } from '../../../lib/ProductService';

export default function FeaturedGrid() {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  
  useEffect(() => {
    const fetchFeatured = async () => {
      try {
        setLoading(true);
        const data = await getProducts(0, 6);
        setProducts(data?.content || [])      } catch (err) {
        console.error("Failed to load featured products:", err);
        setError(err.message);
      } finally {
        setLoading(false);
      }
    };

    fetchFeatured();
  }, []);

  if (loading) {
    return (
      <section className="space-y-6">
        <div className="flex items-end justify-between">
          <h2 className="text-2xl font-bold text-stone-900">Featured</h2>
        </div>
        <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3">
          {Array.from({ length: 6 }).map((_, i) => (
            <div key={i} className="h-[300px] animate-pulse rounded-xl border border-stone-200 bg-stone-50 p-4">
              <div className="mb-4 h-40 rounded-lg bg-stone-200" />
              <div className="mb-2 h-4 w-3/4 rounded bg-stone-200" />
              <div className="h-4 w-1/2 rounded bg-stone-200" />
            </div>
          ))}
        </div>
      </section>
    );
  }

  if (error) {
    return (
      <section className="space-y-6">
        <h2 className="text-2xl font-bold text-stone-900">Featured</h2>
        <p className="text-red-500">Unable to load featured products at this time.</p>
      </section>
    );
  }

  return (
    <section className="space-y-6">
      <div className="flex items-end justify-between">
        <h2 className="text-2xl font-bold text-stone-900">Featured</h2>
        <a href="/products" className="text-sm font-medium text-[#B6771D] hover:text-[#7B542F]">
          View all &rarr;
        </a>
      </div>
      
      {products?.length > 0 ? (
        <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3">
          {products.map((product) => (
            <ProductCard key={product.id} product={product} />
          ))}
        </div>
      ) : (
        <p className="text-stone-500">Check back soon for our featured selection.</p>
      )}
    </section>
  );
}

