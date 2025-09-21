"use client";

import { Fragment } from 'react';
import { createPortal } from 'react-dom';
import { classNames } from '../../utils/helpers';

export default function Modal({ open, onClose, children }) {
  if (typeof document === 'undefined') return null;
  return createPortal(
    <Fragment>
      <div
        className={classNames(
          'fixed inset-0 bg-black/30 transition-opacity',
          open ? 'opacity-100 pointer-events-auto' : 'opacity-0 pointer-events-none'
        )}
        onClick={onClose}
      />
      <div
        className={classNames(
          'fixed inset-0 flex items-center justify-center p-4',
          open ? 'pointer-events-auto' : 'pointer-events-none'
        )}
      >
        <div className={classNames('w-full max-w-md rounded bg-white p-6 shadow transition-transform', open ? 'scale-100' : 'scale-95')}>
          {children}
        </div>
      </div>
    </Fragment>,
    document.body
  );
}

