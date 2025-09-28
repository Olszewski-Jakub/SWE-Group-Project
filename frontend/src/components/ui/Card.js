"use client";

import { classNames } from '../../utils/helpers';

export default function Card({ className, children, title, footer }) {
  return (
    <div className={classNames('rounded-xl border border-stone-200 bg-white p-6 shadow-sm', className)}>
      {title ? <h3 className="mb-4 text-lg font-semibold text-stone-900">{title}</h3> : null}
      <div>{children}</div>
      {footer ? <div className="mt-4 border-t pt-3 text-sm text-stone-600">{footer}</div> : null}
    </div>
  );
}
