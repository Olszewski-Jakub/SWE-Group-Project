export default function FeaturedGrid() {
  return (
    <section className="space-y-6">
      <div className="flex items-end justify-between">
        <h2 className="text-2xl font-bold text-stone-900">Featured</h2>
        <div className="h-8 w-28 rounded bg-stone-100" />
      </div>
      <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3">
        {Array.from({ length: 6 }).map((_, i) => (
          <div key={i} className="rounded-xl border border-dashed border-stone-300 p-4">
            <div className="mb-3 grid h-40 place-items-center rounded-lg bg-stone-100 text-stone-400">Image</div>
            <div className="mb-1 h-4 w-3/5 rounded bg-stone-200" />
            <div className="mb-2 h-3 w-2/5 rounded bg-stone-100" />
            <div className="h-9 w-full rounded bg-stone-200" />
          </div>
        ))}
      </div>
    </section>
  );
}

