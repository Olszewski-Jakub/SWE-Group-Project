"use client";

import { useEffect, useState } from 'react';
import { classNames } from '@/utils/helpers';

export default function Alert({ type = 'error', title, message, onClose, autoCloseMs }) {
  const [show, setShow] = useState(true);
  useEffect(() => {
    if (!autoCloseMs) return;
    const id = setTimeout(() => setShow(false), autoCloseMs);
    return () => clearTimeout(id);
  }, [autoCloseMs]);
  if (!show) return null;
  const palette = type === 'success'
    ? { bg: 'bg-emerald-50', text: 'text-emerald-900', border: 'border-emerald-200' }
    : { bg: 'bg-red-50', text: 'text-red-900', border: 'border-red-200' };
  return (
    <div className={classNames('rounded-md border p-3 shadow-sm transition-all', palette.bg, palette.border, palette.text)}>
      <div className="flex items-start gap-2">
        <div className="flex-1">
          {title && <div className="font-semibold text-sm">{title}</div>}
          {message && <div className="text-sm opacity-90">{message}</div>}
        </div>
        {onClose && (
          <button className="rounded p-1 hover:bg-black/5" onClick={() => { setShow(false); onClose?.(); }} aria-label="Close alert">
            <svg className="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M18 6 6 18M6 6l12 12" /></svg>
          </button>
        )}
      </div>
    </div>
  );
}

