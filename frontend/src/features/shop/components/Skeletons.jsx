export function ProductCardSkeleton() {
  return (
    <div className="animate-pulse flex flex-col overflow-hidden rounded-lg border border-stone-200 bg-white">
      <div className="h-48 w-full bg-stone-100" />
      <div className="p-5 space-y-3">
        <div className="h-5 w-2/3 bg-stone-200 rounded" />
        <div className="h-4 w-1/3 bg-stone-100 rounded" />
        <div className="pt-2 flex items-center justify-between">
          <div className="h-7 w-24 bg-stone-200 rounded" />
          <div className="h-9 w-24 bg-stone-100 rounded" />
        </div>
      </div>
    </div>
  );
}

export function ProductsGridSkeleton({ count = 9 }) {
  return (
    <div className="grid grid-cols-1 gap-8 sm:grid-cols-2 lg:grid-cols-3">
      {Array.from({ length: count }).map((_, i) => (
        <ProductCardSkeleton key={i} />
      ))}
    </div>
  );
}

export function FiltersSkeleton() {
  return (
    <div className="animate-pulse space-y-5">
      <div className="h-10 bg-stone-200 rounded" />
      <div className="space-y-3">
        {[...Array(5)].map((_, i) => (
          <div key={i} className="h-4 bg-stone-100 rounded" />
        ))}
      </div>
      <div className="space-y-3">
        {[...Array(5)].map((_, i) => (
          <div key={i} className="h-4 bg-stone-100 rounded" />
        ))}
      </div>
    </div>
  );
}
