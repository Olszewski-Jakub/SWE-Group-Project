"use client";

import { useEffect, useMemo, useState } from 'react';
import { useRouter } from 'next/navigation';
import RoleGuard from '@/components/common/RoleGuard';
import Alert from '@/components/common/Alert';
import { getProduct as mgmtGetProduct, addVariant as mgmtAddVariant } from '@/lib/ProductManagementService';

export default function VariantCreatePage({ productId }) {
  const router = useRouter();
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);
  const [product, setProduct] = useState(null);

  const [form, setForm] = useState({
    sku: '',
    priceAmount: '',
    priceCurrency: 'EUR',
    stockQuantity: '0',
    stockReserved: '0',
    attrs: { roast:'', origin:'', grind:'', size_ml:'', caffeine:'', weight_g:'', count:'' },
    customAttrs: []
  });
  const [fieldErrors, setFieldErrors] = useState({});

  const OPTIONS = {
    roast: [ {v:'',l:'—'}, {v:'light',l:'Light'}, {v:'medium',l:'Medium'}, {v:'dark',l:'Dark'} ],
    origin: [ {v:'',l:'—'}, 'Brazil','Colombia','Ethiopia','Kenya' ].map(x => typeof x==='string'?{v:x,l:x}:x),
    grind: [ {v:'',l:'—'}, {v:'whole_beans',l:'Whole Beans'}, {v:'espresso',l:'Espresso'}, {v:'filter',l:'Filter'}, {v:'french_press',l:'French Press'} ],
    size_ml: [ {v:'',l:'—'}, '250','350','500','1000' ].map(x => typeof x==='string'?{v:x,l:`${x} ml`}:x),
    caffeine: [ {v:'',l:'—'}, {v:'regular',l:'Regular'}, {v:'decaf',l:'Decaf'} ]
  };

  useEffect(() => {
    let mounted = true;
    (async () => {
      try {
        const data = await mgmtGetProduct(productId);
        if (!mounted) return;
        setProduct(data);
      } catch (e) {
        if (!mounted) return;
        setError(e?.message || 'Failed to load product');
      } finally {
        if (mounted) setLoading(false);
      }
    })();
    return () => { mounted = false; };
  }, [productId]);

  const buildAttributes = (attrs, custom) => {
    const pairs = Object.entries(attrs || {})
      .filter(([_, v]) => v !== undefined && v !== null && `${v}`.trim() !== '')
      .map(([name, value]) => ({ name, value: String(value).trim() }));
    (Array.isArray(custom)?custom:[]).forEach(p => {
      const n = (p?.name ?? '').trim();
      const val = (p?.value ?? '').trim();
      if (!n || !val) return;
      pairs.push({ name: n, value: val });
    });
    return pairs;
  };

  const validate = () => {
    const errs = {};
    // SKU 1-64 alnum/._-
    if (!form.sku || !/^[A-Za-z0-9._-]{1,64}$/.test(form.sku)) errs.sku = 'SKU must be 1–64 chars: letters, numbers, . _ -';
    const amt = Number.parseFloat(form.priceAmount);
    if (!(amt >= 0)) errs.priceAmount = 'Enter a valid non-negative price';
    if (!/^[A-Za-z]{3}$/.test(form.priceCurrency || '')) errs.priceCurrency = 'Currency must be a 3-letter code';
    const qty = Number.parseInt(form.stockQuantity, 10);
    const res = Number.parseInt(form.stockReserved, 10);
    if (!(qty >= 0)) errs.stockQuantity = 'Quantity must be ≥ 0';
    if (!(res >= 0)) errs.stockReserved = 'Reserved must be ≥ 0';
    if (qty >= 0 && res >= 0 && res > qty) errs.stockReserved = 'Reserved cannot exceed quantity';
    // Numeric curated attributes
    if (form.attrs.weight_g && !/^\d+$/.test(String(form.attrs.weight_g))) errs.weight_g = 'Weight must be a positive integer (g)';
    if (form.attrs.count && !/^\d+$/.test(String(form.attrs.count))) errs.count = 'Count must be a positive integer';
    setFieldErrors(errs);
    return Object.keys(errs).length === 0;
  };

  const onSubmit = async (e) => {
    e.preventDefault();
    setError('');
    if (!validate()) {
      return;
    }
    try {
      setBusy(true);
      await mgmtAddVariant(productId, {
        sku: form.sku,
        price: { amount: form.priceAmount, currency: form.priceCurrency },
        stock: { quantity: form.stockQuantity, reserved: form.stockReserved },
        attributes: buildAttributes(form.attrs, form.customAttrs)
      });
      router.push('/admin#products');
    } catch (e) {
      setError(e?.message || 'Create variant failed');
    } finally {
      setBusy(false);
    }
  };

  const addCustomRow = () => setForm(f => ({ ...f, customAttrs: [ ...(f.customAttrs||[]), { name:'', value:'' } ] }));
  const updateCustomRow = (i, patch) => setForm(f => ({ ...f, customAttrs: (f.customAttrs||[]).map((row, idx) => idx===i ? { ...row, ...patch } : row) }));
  const removeCustomRow = (i) => setForm(f => ({ ...f, customAttrs: (f.customAttrs||[]).filter((_, idx) => idx!==i) }));

  return (
    <RoleGuard requireRoles={["ADMIN", "MANAGER"]}>
      <main className="container mx-auto px-4 sm:px-6 lg:px-8 py-6">
        <div className="mb-4 flex items-center justify-between">
          <div>
            <h1 className="text-xl font-semibold text-stone-900">Create Variant</h1>
            <p className="text-sm text-stone-600">{product ? `Product: ${product.name}` : 'Loading product…'}</p>
          </div>
          <button onClick={() => router.back()} className="rounded-md border px-3 py-2 text-sm">Back</button>
        </div>

        {error && <div className="mb-4"><Alert type="error" title="Error" message={error} onClose={() => setError('')} /></div>}

        <form onSubmit={onSubmit} className="rounded-xl border border-stone-200 bg-white p-4 shadow-sm space-y-4">
          <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
            <div>
              <label className="block text-xs font-medium text-stone-700 mb-1">SKU</label>
              <input className={`w-full rounded border px-2 py-1.5 ${fieldErrors.sku ? 'border-red-400' : ''}`} value={form.sku} onChange={(e)=>setForm({...form, sku:e.target.value})} required pattern="^[A-Za-z0-9._-]{1,64}$" />
              {fieldErrors.sku && <p className="mt-1 text-xs text-red-600">{fieldErrors.sku}</p>}
            </div>
            <div>
              <label className="block text-xs font-medium text-stone-700 mb-1">Price Amount</label>
              <input type="number" step="0.01" min="0" className={`w-full rounded border px-2 py-1.5 ${fieldErrors.priceAmount ? 'border-red-400' : ''}`} value={form.priceAmount} onChange={(e)=>setForm({...form, priceAmount:e.target.value})} required />
              {fieldErrors.priceAmount && <p className="mt-1 text-xs text-red-600">{fieldErrors.priceAmount}</p>}
            </div>
            <div>
              <label className="block text-xs font-medium text-stone-700 mb-1">Currency</label>
              <input className={`w-full rounded border px-2 py-1.5 uppercase ${fieldErrors.priceCurrency ? 'border-red-400' : ''}`} value={form.priceCurrency} onChange={(e)=>setForm({...form, priceCurrency:e.target.value.toUpperCase()})} maxLength={3} />
              {fieldErrors.priceCurrency && <p className="mt-1 text-xs text-red-600">{fieldErrors.priceCurrency}</p>}
            </div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
            <div>
              <label className="block text-xs font-medium text-stone-700 mb-1">Stock Quantity</label>
              <input type="number" min="0" className={`w-full rounded border px-2 py-1.5 ${fieldErrors.stockQuantity ? 'border-red-400' : ''}`} value={form.stockQuantity} onChange={(e)=>setForm({...form, stockQuantity:e.target.value})} />
              {fieldErrors.stockQuantity && <p className="mt-1 text-xs text-red-600">{fieldErrors.stockQuantity}</p>}
            </div>
            <div>
              <label className="block text-xs font-medium text-stone-700 mb-1">Reserved</label>
              <input type="number" min="0" className={`w-full rounded border px-2 py-1.5 ${fieldErrors.stockReserved ? 'border-red-400' : ''}`} value={form.stockReserved} onChange={(e)=>setForm({...form, stockReserved:e.target.value})} />
              {fieldErrors.stockReserved && <p className="mt-1 text-xs text-red-600">{fieldErrors.stockReserved}</p>}
            </div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
            <div>
              <label className="block text-xs font-medium text-stone-700 mb-1">Roast</label>
              <select className="w-full rounded border px-2 py-1.5" value={form.attrs.roast} onChange={(e)=>setForm({...form, attrs:{...form.attrs, roast:e.target.value}})}>{OPTIONS.roast.map(o=> <option key={o.v} value={o.v}>{o.l}</option>)}</select>
            </div>
            <div>
              <label className="block text-xs font-medium text-stone-700 mb-1">Origin</label>
              <select className="w-full rounded border px-2 py-1.5" value={form.attrs.origin} onChange={(e)=>setForm({...form, attrs:{...form.attrs, origin:e.target.value}})}>{OPTIONS.origin.map(o=> <option key={o.v} value={o.v}>{o.l}</option>)}</select>
            </div>
            <div>
              <label className="block text-xs font-medium text-stone-700 mb-1">Grind</label>
              <select className="w-full rounded border px-2 py-1.5" value={form.attrs.grind} onChange={(e)=>setForm({...form, attrs:{...form.attrs, grind:e.target.value}})}>{OPTIONS.grind.map(o=> <option key={o.v} value={o.v}>{o.l}</option>)}</select>
            </div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
            <div>
              <label className="block text-xs font-medium text-stone-700 mb-1">Size (ml)</label>
              <select className="w-full rounded border px-2 py-1.5" value={form.attrs.size_ml} onChange={(e)=>setForm({...form, attrs:{...form.attrs, size_ml:e.target.value}})}>{OPTIONS.size_ml.map(o=> <option key={o.v} value={o.v}>{o.l}</option>)}</select>
            </div>
            <div>
              <label className="block text-xs font-medium text-stone-700 mb-1">Caffeine</label>
              <select className="w-full rounded border px-2 py-1.5" value={form.attrs.caffeine} onChange={(e)=>setForm({...form, attrs:{...form.attrs, caffeine:e.target.value}})}>{OPTIONS.caffeine.map(o=> <option key={o.v} value={o.v}>{o.l}</option>)}</select>
            </div>
            <div>
              <label className="block text-xs font-medium text-stone-700 mb-1">Weight (g)</label>
              <input className={`w-full rounded border px-2 py-1.5 ${fieldErrors.weight_g ? 'border-red-400' : ''}`} type="number" min="1" value={form.attrs.weight_g} onChange={(e)=>setForm({...form, attrs:{...form.attrs, weight_g:e.target.value}})} />
              {fieldErrors.weight_g && <p className="mt-1 text-xs text-red-600">{fieldErrors.weight_g}</p>}
            </div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
            <div>
              <label className="block text-xs font-medium text-stone-700 mb-1">Count</label>
              <input className={`w-full rounded border px-2 py-1.5 ${fieldErrors.count ? 'border-red-400' : ''}`} type="number" min="1" value={form.attrs.count} onChange={(e)=>setForm({...form, attrs:{...form.attrs, count:e.target.value}})} />
              {fieldErrors.count && <p className="mt-1 text-xs text-red-600">{fieldErrors.count}</p>}
            </div>
          </div>

          <div>
            <div className="text-sm font-semibold text-stone-800 mb-1">Custom Attributes</div>
            <div className="space-y-2">
              {(form.customAttrs || []).map((row, i) => (
                <div key={i} className="grid grid-cols-5 gap-2 items-center">
                  <input placeholder="name (e.g., milk)" className="col-span-2 rounded border px-2 py-1.5" value={row.name||''} onChange={(e)=>updateCustomRow(i, { name: e.target.value })} />
                  <input placeholder="value (e.g., oat)" className="col-span-2 rounded border px-2 py-1.5" value={row.value||''} onChange={(e)=>updateCustomRow(i, { value: e.target.value })} />
                  <button type="button" className="rounded-md border px-2 py-1 text-xs" onClick={()=>removeCustomRow(i)}>Remove</button>
                </div>
              ))}
              <button type="button" className="rounded-md border px-3 py-1.5 text-xs" onClick={addCustomRow}>Add custom attribute</button>
            </div>
          </div>

          <div className="flex justify-end gap-2">
            <button type="button" className="rounded-md border px-4 py-2" onClick={() => router.push('/admin#products')} disabled={busy}>Cancel</button>
            <button className="rounded-md bg-amber-600 text-white px-4 py-2 disabled:opacity-60" disabled={busy}>{busy ? 'Creating…' : 'Create Variant'}</button>
          </div>
        </form>
      </main>
    </RoleGuard>
  );
}
