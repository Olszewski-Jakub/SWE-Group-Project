"use client";

import { classNames } from '@/utils/helpers';

export default function AdminLayout({ items = [], activeKey, onSelect, children }) {
  return (
    <section className="space-y-4">
      <h1 className="text-2xl font-bold text-stone-900">Admin</h1>
      <div className="flex gap-4 min-h-[calc(100vh-5rem)]">
        <aside className="w-full sm:w-56 shrink-0 border border-stone-200 rounded-lg bg-white/60 sticky top-20 h-[calc(100vh-5rem)] overflow-hidden">
          <nav className="flex sm:flex-col">
            {items.map((item) => (
              <button
                key={item.key}
                onClick={() => onSelect?.(item.key)}
                className={classNames(
                  'flex-1 text-left px-4 py-3 text-sm font-medium border-b sm:border-b-0 sm:border-r last:border-0',
                  activeKey === item.key
                    ? 'bg-amber-50 text-amber-900 border-amber-200'
                    : 'text-stone-700 hover:bg-stone-50 border-stone-200'
                )}
                aria-current={activeKey === item.key ? 'page' : undefined}
              >
                {item.label}
              </button>
            ))}
          </nav>
        </aside>
        <div className="min-w-0 flex-1 space-y-4 max-h-[calc(100vh-5rem)] overflow-y-auto pr-1">
          {children}
        </div>
      </div>
    </section>
  );
}
