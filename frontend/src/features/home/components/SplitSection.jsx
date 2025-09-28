export default function SplitSection() {
  return (
    <section className="grid grid-cols-1 gap-6 lg:grid-cols-2">
      <div className="h-56 rounded-xl border border-dashed border-stone-300" />
      <div className="space-y-3">
        <div className="h-7 w-48 rounded bg-stone-200" />
        <div className="h-4 w-5/6 rounded bg-stone-100" />
        <div className="h-4 w-4/6 rounded bg-stone-100" />
        <div className="h-4 w-3/6 rounded bg-stone-100" />
      </div>
    </section>
  );
}

