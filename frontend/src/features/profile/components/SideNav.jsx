"use client";

import { useMemo, useState, useEffect, useCallback } from 'react';
import { classNames } from '@/utils/helpers';

const icons = {
  overview: (
    <svg viewBox="0 0 24 24" fill="none" className="h-4 w-4" aria-hidden>
      <path d="M4 6h16M4 12h10M4 18h16" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" />
    </svg>
  ),
  profile: (
    <svg viewBox="0 0 24 24" fill="none" className="h-4 w-4" aria-hidden>
      <path d="M12 12a4 4 0 1 0 0-8 4 4 0 0 0 0 8Zm-7 8a7 7 0 1 1 14 0" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" />
    </svg>
  ),
  orders: (
    <svg viewBox="0 0 24 24" fill="none" className="h-4 w-4" aria-hidden>
      <path d="M6 7h12M6 12h12M6 17h8" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" />
    </svg>
  ),
  addresses: (
    <svg viewBox="0 0 24 24" fill="none" className="h-4 w-4" aria-hidden>
      <path d="M12 21s7-5.686 7-11a7 7 0 1 0-14 0c0 5.314 7 11 7 11Z" stroke="currentColor" strokeWidth="1.5" />
      <circle cx="12" cy="10" r="2.5" stroke="currentColor" strokeWidth="1.5" />
    </svg>
  ),
  security: (
    <svg viewBox="0 0 24 24" fill="none" className="h-4 w-4" aria-hidden>
      <path d="M12 3 5 6v6c0 4.418 2.686 8.418 7 9 4.314-.582 7-4.582 7-9V6l-7-3Z" stroke="currentColor" strokeWidth="1.5" strokeLinejoin="round" />
      <path d="M9.5 12 11 13.5 14.5 10" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" />
    </svg>
  ),
};

export default function SideNav({ items = [], active, onChange }) {
  const [open, setOpen] = useState(false);
  const options = useMemo(() => items.map((i) => ({ value: i.key, label: i.label })), [items]);
  const activeLabel = useMemo(() => items.find((i) => i.key === active)?.label || 'Menu', [items, active]);

  const close = useCallback(() => setOpen(false), []);
  const handleSelect = useCallback((key) => {
    onChange?.(key);
    setOpen(false);
  }, [onChange]);

  // Close on Esc
  useEffect(() => {
    function onKey(e) { if (e.key === 'Escape') close(); }
    if (open) window.addEventListener('keydown', onKey);
    return () => window.removeEventListener('keydown', onKey);
  }, [open, close]);

  return (
    <nav>
      {/* Mobile: animated popup menu */}
      <div className="lg:hidden">
        <button
          type="button"
          aria-label="Open menu"
          className="inline-flex items-center gap-2 rounded-xl border border-stone-300 bg-white px-3 py-2 text-sm font-medium text-stone-900 shadow-sm focus:outline-none focus:ring-2 focus:ring-amber-500"
          onClick={() => setOpen(true)}
        >
          <svg viewBox="0 0 24 24" fill="none" className="h-4 w-4" aria-hidden>
            <path d="M4 6h16M4 12h16M4 18h16" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" />
          </svg>
          <span>{activeLabel}</span>
        </button>

        {open ? (
          <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
            {/* Backdrop */}
            <div className="absolute inset-0 bg-black/40 backdrop-blur-sm animate-fade-in" onClick={close} />
            {/* Dialog */}
            <div className="relative z-10 w-full max-w-md origin-center opacity-100 translate-y-0 scale-100 transition-all duration-200 ease-out">
              <div className="rounded-2xl border border-stone-200 bg-white p-4 shadow-xl">
                <div className="mb-2 flex items-center justify-between">
                  <h2 className="text-sm font-semibold text-stone-900">Switch section</h2>
                  <button
                    type="button"
                    aria-label="Close menu"
                    className="rounded-lg p-2 text-stone-600 hover:bg-stone-100 focus:outline-none focus:ring-2 focus:ring-amber-500"
                    onClick={close}
                  >
                    <svg viewBox="0 0 24 24" fill="none" className="h-4 w-4" aria-hidden>
                      <path d="M6 6l12 12M18 6L6 18" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" />
                    </svg>
                  </button>
                </div>
                <ul className="grid grid-cols-2 gap-2">
                  {items.map((item, idx) => {
                    const selected = active === item.key;
                    return (
                      <li key={item.key}>
                        <button
                          type="button"
                          style={{ transitionDelay: `${idx * 30}ms` }}
                          className={classNames(
                            'group w-full rounded-xl border p-3 text-left shadow-sm ring-1 ring-inset transition-all duration-200 ease-out focus:outline-none focus:ring-2 focus:ring-amber-500',
                            selected
                              ? 'border-amber-200 bg-amber-50 text-amber-900 ring-amber-200'
                              : 'border-stone-200 bg-white text-stone-800 hover:shadow-md hover:-translate-y-0.5'
                          )}
                          onClick={() => handleSelect(item.key)}
                        >
                          <div className="flex items-center gap-2">
                            <span className={classNames('rounded-lg p-2 transition-colors', selected ? 'bg-amber-100 text-amber-800' : 'bg-stone-100 text-stone-600 group-hover:bg-stone-200')}>
                              {icons[item.key] || icons.overview}
                            </span>
                            <span className="text-sm font-medium">{item.label}</span>
                          </div>
                        </button>
                      </li>
                    );
                  })}
                </ul>
              </div>
            </div>
          </div>
        ) : null}
      </div>

      {/* Desktop: vertical nav */}
      <div className="hidden lg:block sticky top-24">
        <div className="rounded-2xl border border-stone-200 bg-white p-2 shadow-sm">
          <ul className="space-y-1">
            {items.map((item) => {
              const selected = active === item.key;
              return (
                <li key={item.key}>
                  <button
                    type="button"
                    aria-current={selected ? 'page' : undefined}
                    className={classNames(
                      'w-full text-left rounded-xl px-3 py-2 text-sm transition-all flex items-center gap-2',
                      selected
                        ? 'bg-amber-100 text-amber-900 ring-1 ring-amber-300 shadow-xs'
                        : 'text-stone-700 hover:bg-stone-50 hover:text-stone-900'
                    )}
                    onClick={() => onChange?.(item.key)}
                  >
                    <span className={classNames('shrink-0', selected ? 'text-amber-800' : 'text-stone-500')}>
                      {icons[item.key] || icons.overview}
                    </span>
                    <span className="truncate">{item.label}</span>
                  </button>
                </li>
              );
            })}
          </ul>
        </div>
      </div>
    </nav>
  );
}
