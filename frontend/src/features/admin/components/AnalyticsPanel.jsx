"use client";

export default function AnalyticsPanel() {
  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h2 className="text-lg font-semibold text-stone-900">Analytics</h2>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        {[1,2,3,4].map((i) => (
          <div key={i} className="rounded-lg border border-stone-200 bg-white/70 p-4">
            <div className="h-4 w-24 rounded bg-stone-200/70" />
            <div className="mt-3 h-7 w-32 rounded bg-stone-300/70" />
          </div>
        ))}
      </div>

      <div className="rounded-lg border border-stone-200 bg-white/70 p-4">
        <div className="h-5 w-36 rounded bg-stone-200/70" />
        <div className="mt-4 h-56 w-full rounded bg-stone-100" />
      </div>
    </div>
  );
}

