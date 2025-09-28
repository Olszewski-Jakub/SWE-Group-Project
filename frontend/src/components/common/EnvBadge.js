"use client";

export default function EnvBadge() {
  const env = process.env.NEXT_PUBLIC_ENVIRONMENT || (process.env.NODE_ENV === 'production' ? 'prod' : 'dev');
  if (!env) return null;
  const color = env === 'prod' ? 'bg-red-600' : env === 'dev' ? 'bg-amber-700' : 'bg-emerald-600';
  const label = env.toUpperCase();
  return (
    <div className="pointer-events-none fixed bottom-4 right-4 z-[60] opacity-90">
      <span className={`pointer-events-auto inline-block rounded-full ${color} px-3 py-1 text-xs font-semibold text-white shadow`}>{label}</span>
    </div>
  );
}
