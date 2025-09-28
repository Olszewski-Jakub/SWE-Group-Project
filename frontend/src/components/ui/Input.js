"use client";

import { forwardRef } from 'react';
import { classNames } from '../../utils/helpers';

const Input = forwardRef(function Input(
  { label, id, error, className, type = 'text', hint, ...props },
  ref
) {
  return (
    <div className={classNames('space-y-1', className)}>
      {label ? (
        <label htmlFor={id} className="block text-sm font-medium text-stone-700">
          {label}
        </label>
      ) : null}
      <input
        id={id}
        ref={ref}
        type={type}
        className={classNames(
          'block w-full rounded border px-3 py-2 text-sm focus:outline-none focus:ring-2',
          error ? 'border-red-400 focus:ring-red-500' : 'border-stone-300 focus:ring-amber-500'
        )}
        {...props}
      />
      {hint ? <p className="text-xs text-stone-500">{hint}</p> : null}
      {error ? <p className="text-xs text-red-600">{error}</p> : null}
    </div>
  );
});

export default Input;
