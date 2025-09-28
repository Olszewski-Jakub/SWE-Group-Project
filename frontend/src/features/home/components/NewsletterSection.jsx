export default function NewsletterSection() {
  return (
    <section className="rounded-2xl border border-dashed border-stone-300 p-6">
      <div className="mx-auto grid max-w-3xl grid-cols-1 gap-4 sm:grid-cols-3">
        <div className="h-10 rounded bg-stone-100 sm:col-span-2" />
        <div className="h-10 rounded bg-stone-200" />
      </div>
    </section>
  );
}

