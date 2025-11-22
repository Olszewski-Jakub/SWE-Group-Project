"use client";

import { Fragment } from 'react';
import { createPortal } from 'react-dom';
import { classNames } from '../../utils/helpers';

export default function Modal({ open, onClose, children }) {
  if (typeof document === 'undefined') return null;
  if (!open) return null; // Unmount when closed to avoid visibility/pointer issues
  return createPortal(
    <Fragment>
      <div
        className={classNames('fixed inset-0 bg-black/30 opacity-100 pointer-events-auto transition-opacity z-40')}
        onClick={onClose}
      />
      <div className={classNames('fixed inset-0 z-50 flex items-center justify-center p-4 pointer-events-auto')}>
        <div
          role="dialog"
          aria-modal="true"
          className={classNames('w-full max-w-2xl rounded-lg bg-white p-6 shadow-xl ring-1 ring-black/5 transform transition-all duration-150 ease-out scale-100 max-h-[85vh] overflow-auto')}
        >
          {children}
        </div>
      </div>
    </Fragment>,
    document.body
  );
}
