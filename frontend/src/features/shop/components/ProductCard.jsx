export default function ProductCard({ index }) {
  return (
    <div className="rounded-xl border border-dashed border-stone-300 p-4">
      <div className="mb-3 grid h-40 place-items-center rounded-lg bg-stone-100 text-stone-400">Image</div>
      <div className="mb-1 h-4 w-3/5 rounded bg-stone-200" />
      <div className="mb-2 h-3 w-2/5 rounded bg-stone-100" />
      <div className="h-9 w-full rounded bg-stone-200" />
    </div>
  );
}

