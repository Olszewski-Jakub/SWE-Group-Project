"use client";

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import RoleGuard from '@/components/common/RoleGuard';
import Alert from '@/components/common/Alert';
import Modal from '@/components/common/Modal';
import { COFFEE_CATEGORIES } from '@/constants/coffeeCategories';
import {
  getProduct as mgmtGetProduct,
  createProductWithForm,
  updateProduct as mgmtUpdateProduct,
  deleteVariant as mgmtDeleteVariant,
  updateVariant as mgmtUpdateVariant,
} from '@/lib/ProductManagementService';

export default function ProductEditorPage({ mode = 'create', productId }) {
  const router = useRouter();
  const editing = mode === 'edit';
  const [loading, setLoading] = useState(editing);
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);
  const [product, setProduct] = useState(null);
  const [fieldErrors, setFieldErrors] = useState({});

  const [form, setForm] = useState({
    name: '', description: '', category: '', status: 'ACTIVE',
    variants: [ { sku:'', priceAmount:'', priceCurrency:'EUR', stockQuantity:'0', stockReserved:'0', imageFile:null, attrs: { roast:'', origin:'', grind:'', size_ml:'', caffeine:'', weight_g:'', count:'' }, customAttrs: [] } ]
  });

  const [attrModal, setAttrModal] = useState({ open: false, productId: null, variantId: null, attrs: { roast:'', origin:'', grind:'', size_ml:'', caffeine:'', weight_g:'', count:'' }, custom: [] });

  useEffect(() => {
    if (!editing) return;
    let mounted = true;
    (async () => {
      try {
        const data = await mgmtGetProduct(productId);
        if (!mounted) return;
        setProduct(data);
        setForm({
          name: data.name || '',
          description: data.description || '',
          category: data.category || '',
          status: (data.status || 'ACTIVE').toUpperCase(),
          variants: [ ...Array.isArray(data.variants) ? data.variants : [] ]
        });
      } catch (e) {
        if (!mounted) return;
        setError(e?.message || 'Failed to load product');
      } finally {
        if (mounted) setLoading(false);
      }
    })();
    return () => { mounted = false; };
  }, [editing, productId]);

  const validateProductCreate = () => {
    const errs = {};
    if (!form.name || form.name.trim().length === 0 || form.name.length > 120) errs.name = 'Name is required (max 120)';
    if (!form.category) errs.category = 'Category is required';
    if (!['DRAFT','ACTIVE','ARCHIVED'].includes(form.status)) errs.status = 'Invalid status';
    form.variants.forEach((v, idx) => {
      if (!v.sku || !/^[A-Za-z0-9._-]{1,64}$/.test(v.sku)) errs[`v${idx}_sku`] = 'SKU 1–64 chars: letters, numbers, . _ -';
      const amt = Number.parseFloat(v.priceAmount);
      if (!(amt >= 0)) errs[`v${idx}_price`] = 'Price must be a non-negative number';
      if (!/^[A-Za-z]{3}$/.test(v.priceCurrency || '')) errs[`v${idx}_cur`] = 'Currency must be a 3-letter code';
      const qty = Number.parseInt(v.stockQuantity, 10);
      const res = Number.parseInt(v.stockReserved, 10);
      if (!(qty >= 0)) errs[`v${idx}_qty`] = 'Quantity must be ≥ 0';
      if (!(res >= 0)) errs[`v${idx}_res`] = 'Reserved must be ≥ 0';
      if (qty >= 0 && res >= 0 && res > qty) errs[`v${idx}_res`] = 'Reserved cannot exceed quantity';
      if (v.attrs?.weight_g && !/^\d+$/.test(String(v.attrs.weight_g))) errs[`v${idx}_wg`] = 'Weight must be a positive integer (g)';
      if (v.attrs?.count && !/^\d+$/.test(String(v.attrs.count))) errs[`v${idx}_cnt`] = 'Count must be a positive integer';
    });
    setFieldErrors(errs);
    return Object.keys(errs).length === 0;
  };

  const submitCreate = async (e) => {
    e.preventDefault(); setError('');
    if (!validateProductCreate()) return;
    try {
      setBusy(true);
      await createProductWithForm(form);
      router.push('/admin#products');
    } catch (e) {
      setError(e?.message || 'Create failed');
    } finally { setBusy(false); }
  };

  const submitUpdateProduct = async (e) => {
    e.preventDefault(); setError('');
    try {
      setBusy(true);
      await mgmtUpdateProduct(productId, { name: form.name, description: form.description, category: form.category, status: form.status });
      router.push('/admin#products');
    } catch (e) {
      setError(e?.message || 'Update failed');
    } finally { setBusy(false); }
  };

  const buildAttrsObj = (list) => {
    const base = { roast:'', origin:'', grind:'', size_ml:'', caffeine:'', weight_g:'', count:'' };
    const custom = [];
    (Array.isArray(list)?list:[]).forEach(a => {
      if (!a?.name) return; const k = a.name; const v = String(a.value ?? '');
      if (Object.hasOwn(base, k)) base[k] = v; else custom.push({ name: k, value: v });
    });
    return { base, custom };
  };

  const VARIANT_OPTIONS = {
    roast: [ {v:'',l:'—'}, {v:'light',l:'Light'}, {v:'medium',l:'Medium'}, {v:'dark',l:'Dark'} ],
    origin: [ {v:'',l:'—'}, 'Brazil','Colombia','Ethiopia','Kenya' ].map(x => typeof x==='string'?{v:x,l:x}:x),
    grind: [ {v:'',l:'—'}, {v:'whole_beans',l:'Whole Beans'}, {v:'espresso',l:'Espresso'}, {v:'filter',l:'Filter'}, {v:'french_press',l:'French Press'} ],
    size_ml: [ {v:'',l:'—'}, '250','350','500','1000' ].map(x => typeof x==='string'?{v:x,l:`${x} ml`}:x),
    caffeine: [ {v:'',l:'—'}, {v:'regular',l:'Regular'}, {v:'decaf',l:'Decaf'} ]
  };

  const updateVariantField = (idx, key, value) => setForm(f => ({ ...f, variants: f.variants.map((v, i) => i===idx ? { ...v, [key]: value } : v) }));
  const updateVariantAttr = (idx, key, value) => setForm(f => ({ ...f, variants: f.variants.map((v, i) => i===idx ? { ...v, attrs: { ...(v.attrs||{}), [key]: value } } : v) }));
  const updateVariantCustomRow = (idx, rowIdx, patch) => setForm(f => ({ ...f, variants: f.variants.map((v, i) => {
    if (i !== idx) return v; const list = Array.isArray(v.customAttrs) ? [...v.customAttrs] : []; list[rowIdx] = { ...(list[rowIdx]||{ name:'', value:'' }), ...patch }; return { ...v, customAttrs: list };
  }) }));
  const addVariantCustomRow = (idx) => setForm(f => ({ ...f, variants: f.variants.map((v, i) => i===idx ? { ...v, customAttrs: [ ...(v.customAttrs||[]), { name:'', value:'' } ] } : v) }));
  const removeVariantCustomRow = (idx, rowIdx) => setForm(f => ({ ...f, variants: f.variants.map((v, i) => i===idx ? { ...v, customAttrs: (v.customAttrs||[]).filter((_, j) => j !== rowIdx) } : v) }));
  const addVariantRow = () => setForm(f => ({ ...f, variants: [...f.variants, { sku:'', priceAmount:'', priceCurrency:'EUR', stockQuantity:'0', stockReserved:'0', imageFile:null, attrs: { roast:'', origin:'', grind:'', size_ml:'', caffeine:'', weight_g:'', count:'' }, customAttrs: [] }] }));
  const removeVariantRow = (idx) => setForm(f => ({ ...f, variants: f.variants.filter((_, i) => i!==idx) }));

  const openEditAttrs = (v) => {
    const split = buildAttrsObj(v.attributes);
    setAttrModal({ open: true, productId, variantId: v.id, attrs: split.base, custom: split.custom });
  };
  const saveEditAttrs = async () => {
    const attrs = [ ...Object.entries(attrModal.attrs).filter(([_, val]) => (val ?? '').toString().trim() !== '').map(([name, value]) => ({ name, value: String(value) })),
      ...(attrModal.custom||[]).filter(r => (r?.name||'').trim() && (r?.value||'').trim())
    ];
    await mgmtUpdateVariant(productId, attrModal.variantId, { attributes: attrs });
    const updated = await mgmtGetProduct(productId);
    setProduct(updated);
    setAttrModal({ ...attrModal, open: false });
  };

  return (
    <RoleGuard requireRoles={["ADMIN", "MANAGER"]}>
      <main className="container mx-auto px-4 sm:px-6 lg:px-8 py-6">
        <div className="mb-4 flex items-center justify-between">
          <div>
            <h1 className="text-xl font-semibold text-stone-900">{editing ? 'Edit Product' : 'Create Product'}</h1>
            {editing && <p className="text-sm text-stone-600">ID: <span className="font-mono">{productId}</span></p>}
          </div>
          <button onClick={() => router.back()} className="rounded-md border px-3 py-2 text-sm">Back</button>
        </div>

        {error && <div className="mb-4"><Alert type="error" title="Error" message={error} onClose={() => setError('')} /></div>}

        {!editing && (
          <form onSubmit={submitCreate} className="space-y-4 rounded-xl border border-stone-200 bg-white p-4 shadow-sm">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
              <div><label className="block text-xs font-medium text-stone-700 mb-1">Name</label><input className={`w-full rounded border px-3 py-2 ${fieldErrors.name ? 'border-red-400' : ''}`} value={form.name} onChange={(e)=>setForm({...form, name:e.target.value})} required maxLength={120} />{fieldErrors.name && <p className="mt-1 text-xs text-red-600">{fieldErrors.name}</p>}</div>
              <div><label className="block text-xs font-medium text-stone-700 mb-1">Category</label><select className={`w-full rounded border px-3 py-2 ${fieldErrors.category ? 'border-red-400' : ''}`} value={form.category} onChange={(e)=>setForm({...form, category:e.target.value})} required><option value="" disabled>Select category</option>{COFFEE_CATEGORIES.map(c => <option key={c.value} value={c.value}>{c.label}</option>)}</select>{fieldErrors.category && <p className="mt-1 text-xs text-red-600">{fieldErrors.category}</p>}</div>
              <div><label className="block text-xs font-medium text-stone-700 mb-1">Status</label><select className={`w-full rounded border px-3 py-2 ${fieldErrors.status ? 'border-red-400' : ''}`} value={form.status} onChange={(e)=>setForm({...form, status:e.target.value})}><option value="DRAFT">DRAFT</option><option value="ACTIVE">ACTIVE</option><option value="ARCHIVED">ARCHIVED</option></select>{fieldErrors.status && <p className="mt-1 text-xs text-red-600">{fieldErrors.status}</p>}</div>
              <div><label className="block text-xs font-medium text-stone-700 mb-1">Description</label><textarea className="w-full rounded border px-3 py-2 min-h-[80px]" value={form.description} onChange={(e)=>setForm({...form, description:e.target.value})} /></div>
            </div>
            <div className="space-y-3">
              <div className="flex items-center justify-between"><div className="font-medium">Variants</div><button type="button" className="rounded-md border border-stone-300 px-3 py-1.5 text-sm hover:bg-stone-50" onClick={addVariantRow}>Add Variant</button></div>
              <div className="rounded-md border border-stone-200 p-2 bg-white/60 max-h-[60vh] overflow-y-auto">
                <div className="space-y-3">
                  {form.variants.map((v, idx) => (
                    <div key={idx} className="rounded-lg border border-stone-200 bg-white p-3 shadow-sm">
                      <div className="mb-2 text-sm font-semibold text-stone-800">Variant {idx + 1}</div>
                      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3">
                        <div><label className="block text-xs font-medium text-stone-700 mb-1">SKU</label><input className={`w-full rounded border px-2 py-1.5 ${fieldErrors[`v${idx}_sku`] ? 'border-red-400' : ''}`} value={v.sku} onChange={(e)=>updateVariantField(idx,'sku',e.target.value)} required pattern="^[A-Za-z0-9._-]{1,64}$" />{fieldErrors[`v${idx}_sku`] && <p className="mt-1 text-xs text-red-600">{fieldErrors[`v${idx}_sku`]}</p>}</div>
                        <div><label className="block text-xs font-medium text-stone-700 mb-1">Price Amount</label><input type="number" step="0.01" min="0" className={`w-full rounded border px-2 py-1.5 ${fieldErrors[`v${idx}_price`] ? 'border-red-400' : ''}`} value={v.priceAmount} onChange={(e)=>updateVariantField(idx,'priceAmount',e.target.value)} required />{fieldErrors[`v${idx}_price`] && <p className="mt-1 text-xs text-red-600">{fieldErrors[`v${idx}_price`]}</p>}</div>
                        <div><label className="block text-xs font-medium text-stone-700 mb-1">Currency</label><input className={`w-full rounded border px-2 py-1.5 uppercase ${fieldErrors[`v${idx}_cur`] ? 'border-red-400' : ''}`} value={v.priceCurrency} onChange={(e)=>updateVariantField(idx,'priceCurrency',e.target.value.toUpperCase())} maxLength={3} />{fieldErrors[`v${idx}_cur`] && <p className="mt-1 text-xs text-red-600">{fieldErrors[`v${idx}_cur`]}</p>}</div>
                      </div>
                      <div className="mt-3 grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3 items-start">
                        <div><label className="block text-xs font-medium text-stone-700 mb-1">Stock Quantity</label><input type="number" min="0" className={`w-full rounded border px-2 py-1.5 ${fieldErrors[`v${idx}_qty`] ? 'border-red-400' : ''}`} value={v.stockQuantity} onChange={(e)=>updateVariantField(idx,'stockQuantity',e.target.value)} />{fieldErrors[`v${idx}_qty`] && <p className="mt-1 text-xs text-red-600">{fieldErrors[`v${idx}_qty`]}</p>}</div>
                        <div><label className="block text-xs font-medium text-stone-700 mb-1">Reserved</label><input type="number" min="0" className={`w-full rounded border px-2 py-1.5 ${fieldErrors[`v${idx}_res`] ? 'border-red-400' : ''}`} value={v.stockReserved} onChange={(e)=>updateVariantField(idx,'stockReserved',e.target.value)} />{fieldErrors[`v${idx}_res`] && <p className="mt-1 text-xs text-red-600">{fieldErrors[`v${idx}_res`]}</p>}</div>
                        <div><label className="block text-xs font-medium text-stone-700 mb-1">Image</label><div className="flex items-center gap-3"><input className="rounded border px-2 py-1.5 w-full" type="file" accept="image/*" onChange={(e)=>updateVariantField(idx,'imageFile', e.target.files?.[0] || null)} />{v.imageFile && <img alt="preview" className="h-12 w-12 rounded object-cover ring-1 ring-black/5" src={URL.createObjectURL(v.imageFile)} />}</div></div>
                      </div>
                      <div className="mt-3 grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3">
                        <div>
                          <label className="block text-xs font-medium text-stone-700 mb-1">Roast</label>
                          <select className="w-full rounded border px-2 py-1.5" value={v.attrs?.roast||''} onChange={(e)=>updateVariantAttr(idx,'roast',e.target.value)}>{VARIANT_OPTIONS.roast.map(o=> <option key={o.v} value={o.v}>{o.l}</option>)}</select>
                        </div>
                        <div>
                          <label className="block text-xs font-medium text-stone-700 mb-1">Origin</label>
                          <select className="w-full rounded border px-2 py-1.5" value={v.attrs?.origin||''} onChange={(e)=>updateVariantAttr(idx,'origin',e.target.value)}>{VARIANT_OPTIONS.origin.map(o=> <option key={o.v} value={o.v}>{o.l}</option>)}</select>
                        </div>
                        <div>
                          <label className="block text-xs font-medium text-stone-700 mb-1">Grind</label>
                          <select className="w-full rounded border px-2 py-1.5" value={v.attrs?.grind||''} onChange={(e)=>updateVariantAttr(idx,'grind',e.target.value)}>{VARIANT_OPTIONS.grind.map(o=> <option key={o.v} value={o.v}>{o.l}</option>)}</select>
                        </div>
                      </div>
                      <div className="mt-3 grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3">
                        <div>
                          <label className="block text-xs font-medium text-stone-700 mb-1">Size (ml)</label>
                          <select className="w-full rounded border px-2 py-1.5" value={v.attrs?.size_ml||''} onChange={(e)=>updateVariantAttr(idx,'size_ml',e.target.value)}>{VARIANT_OPTIONS.size_ml.map(o=> <option key={o.v} value={o.v}>{o.l}</option>)}</select>
                        </div>
                        <div>
                          <label className="block text-xs font-medium text-stone-700 mb-1">Caffeine</label>
                          <select className="w-full rounded border px-2 py-1.5" value={v.attrs?.caffeine||''} onChange={(e)=>updateVariantAttr(idx,'caffeine',e.target.value)}>{VARIANT_OPTIONS.caffeine.map(o=> <option key={o.v} value={o.v}>{o.l}</option>)}</select>
                        </div>
                        <div>
                          <label className="block text-xs font-medium text-stone-700 mb-1">Weight (g)</label>
                          <input className={`w-full rounded border px-2 py-1.5 ${fieldErrors[`v${idx}_wg`] ? 'border-red-400' : ''}`} type="number" min="1" value={v.attrs?.weight_g||''} onChange={(e)=>updateVariantAttr(idx,'weight_g',e.target.value)} />{fieldErrors[`v${idx}_wg`] && <p className="mt-1 text-xs text-red-600">{fieldErrors[`v${idx}_wg`]}</p>}
                        </div>
                      </div>
                      <div className="mt-3 grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3">
                        <div>
                          <label className="block text-xs font-medium text-stone-700 mb-1">Count</label>
                          <input className={`w-full rounded border px-2 py-1.5 ${fieldErrors[`v${idx}_cnt`] ? 'border-red-400' : ''}`} type="number" min="1" value={v.attrs?.count||''} onChange={(e)=>updateVariantAttr(idx,'count',e.target.value)} />{fieldErrors[`v${idx}_cnt`] && <p className="mt-1 text-xs text-red-600">{fieldErrors[`v${idx}_cnt`]}</p>}
                        </div>
                      </div>
                      <div className="mt-3">
                        <div className="text-sm font-semibold text-stone-800">Custom Attributes</div>
                        <div className="space-y-2 mt-1">
                          {(v.customAttrs || []).map((row, rIdx) => (
                            <div key={rIdx} className="grid grid-cols-5 gap-2 items-center">
                              <input placeholder="name" className="col-span-2 rounded border px-2 py-1.5" value={row.name||''} onChange={(e)=>updateVariantCustomRow(idx, rIdx, { name: e.target.value })} />
                              <input placeholder="value" className="col-span-2 rounded border px-2 py-1.5" value={row.value||''} onChange={(e)=>updateVariantCustomRow(idx, rIdx, { value: e.target.value })} />
                              <button type="button" className="rounded-md border px-2 py-1 text-xs" onClick={()=>removeVariantCustomRow(idx, rIdx)}>Remove</button>
                            </div>
                          ))}
                          <button type="button" className="rounded-md border px-3 py-1.5 text-xs" onClick={()=>addVariantCustomRow(idx)}>Add custom attribute</button>
                        </div>
                      </div>
                      <div className="mt-3 flex justify-end"><button type="button" className="rounded-md border border-red-300 text-red-700 px-3 py-1.5 text-sm hover:bg-red-50" onClick={()=>removeVariantRow(idx)}>Remove</button></div>
                    </div>
                  ))}
                </div>
              </div>
            </div>
            <div className="flex justify-end gap-2"><button type="button" className="rounded-md border px-4 py-2" onClick={()=>router.push('/admin#products')} disabled={busy}>Cancel</button><button className="rounded-md bg-amber-600 text-white px-4 py-2 disabled:opacity-60" disabled={busy}>{busy ? 'Creating…' : 'Create'}</button></div>
          </form>
        )}

        {editing && !loading && product && (
          <form onSubmit={submitUpdateProduct} className="space-y-4 rounded-xl border border-stone-200 bg-white p-4 shadow-sm">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
              <div><label className="block text-xs font-medium text-stone-700 mb-1">Name</label><input className="w-full rounded border px-3 py-2" value={form.name} onChange={(e)=>setForm({...form, name:e.target.value})} /></div>
              <div><label className="block text-xs font-medium text-stone-700 mb-1">Category</label><select className="w-full rounded border px-3 py-2" value={form.category} onChange={(e)=>setForm({...form, category:e.target.value})}><option value="" disabled>Select category</option>{COFFEE_CATEGORIES.map(c => <option key={c.value} value={c.value}>{c.label}</option>)}</select></div>
              <div><label className="block text-xs font-medium text-stone-700 mb-1">Status</label><select className="w-full rounded border px-3 py-2" value={form.status} onChange={(e)=>setForm({...form, status:e.target.value})}><option value="DRAFT">DRAFT</option><option value="ACTIVE">ACTIVE</option><option value="ARCHIVED">ARCHIVED</option></select></div>
              <div><label className="block text-xs font-medium text-stone-700 mb-1">Description</label><textarea className="w-full rounded border px-3 py-2 min-h-[80px]" value={form.description} onChange={(e)=>setForm({...form, description:e.target.value})} /></div>
            </div>

            <div className="space-y-3">
              <div className="flex items-center justify-between"><div className="font-medium">Variants ({Array.isArray(product.variants) ? product.variants.length : 0})</div><button type="button" className="rounded-md border px-3 py-1.5 text-sm" onClick={()=>router.push(`/admin/products/${productId}/variants/new`)}>Create Variant</button></div>
              <div className="rounded-md border border-stone-200 bg-white/70">
                <table className="min-w-full text-sm">
                  <thead className="bg-stone-50"><tr>
                    <th className="px-3 py-2 text-left">SKU</th>
                    <th className="px-3 py-2 text-left">Price</th>
                    <th className="px-3 py-2 text-left">Stock</th>
                    <th className="px-3 py-2 text-left">Actions</th>
                  </tr></thead>
                  <tbody>
                    {(product.variants || []).map(v => (
                      <tr key={v.id} className="border-t">
                        <td className="px-3 py-2">{v.sku}</td>
                        <td className="px-3 py-2">{v.price?.amount} {v.price?.currency}</td>
                        <td className="px-3 py-2">{v.stock?.quantity} ({v.stock?.reserved} reserved)</td>
                        <td className="px-3 py-2 space-x-2">
                          <button type="button" className="rounded-md border px-2 py-1 text-xs" onClick={()=>openEditAttrs(v)}>Edit Attrs</button>
                          <button type="button" className="rounded-md border border-red-300 text-red-700 px-2 py-1 text-xs" onClick={async ()=>{ if (!confirm('Delete variant?')) return; await mgmtDeleteVariant(productId, v.id); const updated = await mgmtGetProduct(productId); setProduct(updated); }}>Delete</button>
                        </td>
                      </tr>
                    ))}
                    {(product.variants || []).length === 0 && (<tr><td colSpan={4} className="px-3 py-6 text-center text-stone-600">No variants.</td></tr>)}
                  </tbody>
                </table>
              </div>
            </div>

            <div className="flex justify-end gap-2"><button type="button" className="rounded-md border px-4 py-2" onClick={()=>router.push('/admin#products')} disabled={busy}>Cancel</button><button className="rounded-md bg-amber-600 text-white px-4 py-2 disabled:opacity-60" disabled={busy}>{busy ? 'Saving…' : 'Save'}</button></div>
          </form>
        )}

        <Modal open={attrModal.open} onClose={()=>setAttrModal({...attrModal, open:false})} size="3xl" tall>
          <div className="space-y-3">
            <div><h4 className="text-base font-semibold">Edit Variant Attributes</h4></div>
            <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
              <div>
                <label className="block text-xs font-medium text-stone-700 mb-1">Roast</label>
                <select className="w-full rounded border px-2 py-1.5" value={attrModal.attrs.roast} onChange={(e)=>setAttrModal(m => ({ ...m, attrs: { ...m.attrs, roast: e.target.value } }))}>{VARIANT_OPTIONS.roast.map(o=> <option key={o.v} value={o.v}>{o.l}</option>)}</select>
              </div>
              <div>
                <label className="block text-xs font-medium text-stone-700 mb-1">Origin</label>
                <select className="w-full rounded border px-2 py-1.5" value={attrModal.attrs.origin} onChange={(e)=>setAttrModal(m => ({ ...m, attrs: { ...m.attrs, origin: e.target.value } }))}>{VARIANT_OPTIONS.origin.map(o=> <option key={o.v} value={o.v}>{o.l}</option>)}</select>
              </div>
              <div>
                <label className="block text-xs font-medium text-stone-700 mb-1">Grind</label>
                <select className="w-full rounded border px-2 py-1.5" value={attrModal.attrs.grind} onChange={(e)=>setAttrModal(m => ({ ...m, attrs: { ...m.attrs, grind: e.target.value } }))}>{VARIANT_OPTIONS.grind.map(o=> <option key={o.v} value={o.v}>{o.l}</option>)}</select>
              </div>
            </div>
            <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
              <div>
                <label className="block text-xs font-medium text-stone-700 mb-1">Size (ml)</label>
                <select className="w-full rounded border px-2 py-1.5" value={attrModal.attrs.size_ml} onChange={(e)=>setAttrModal(m => ({ ...m, attrs: { ...m.attrs, size_ml: e.target.value } }))}>{VARIANT_OPTIONS.size_ml.map(o=> <option key={o.v} value={o.v}>{o.l}</option>)}</select>
              </div>
              <div>
                <label className="block text-xs font-medium text-stone-700 mb-1">Caffeine</label>
                <select className="w-full rounded border px-2 py-1.5" value={attrModal.attrs.caffeine} onChange={(e)=>setAttrModal(m => ({ ...m, attrs: { ...m.attrs, caffeine: e.target.value } }))}>{VARIANT_OPTIONS.caffeine.map(o=> <option key={o.v} value={o.v}>{o.l}</option>)}</select>
              </div>
              <div>
                <label className="block text-xs font-medium text-stone-700 mb-1">Weight (g)</label>
                <input className="w-full rounded border px-2 py-1.5" type="number" min="1" value={attrModal.attrs.weight_g} onChange={(e)=>setAttrModal(m => ({ ...m, attrs: { ...m.attrs, weight_g: e.target.value } }))} />
              </div>
            </div>
            <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
              <div>
                <label className="block text-xs font-medium text-stone-700 mb-1">Count</label>
                <input className="w-full rounded border px-2 py-1.5" type="number" min="1" value={attrModal.attrs.count} onChange={(e)=>setAttrModal(m => ({ ...m, attrs: { ...m.attrs, count: e.target.value } }))} />
              </div>
            </div>
            <div>
              <div className="mt-1 text-sm font-semibold text-stone-800">Custom Attributes</div>
              <div className="space-y-2">
                {(attrModal.custom || []).map((row, i) => (
                  <div key={i} className="grid grid-cols-5 gap-2 items-center">
                    <input placeholder="name" className="col-span-2 rounded border px-2 py-1.5" value={row.name||''} onChange={(e)=>{
                      const list = [...(attrModal.custom||[])]; list[i] = { ...list[i], name: e.target.value }; setAttrModal(m => ({ ...m, custom: list }));
                    }} />
                    <input placeholder="value" className="col-span-2 rounded border px-2 py-1.5" value={row.value||''} onChange={(e)=>{
                      const list = [...(attrModal.custom||[])]; list[i] = { ...list[i], value: e.target.value }; setAttrModal(m => ({ ...m, custom: list }));
                    }} />
                    <button type="button" className="rounded-md border px-2 py-1 text-xs" onClick={()=>{
                      const list = (attrModal.custom||[]).filter((_, idx) => idx !== i); setAttrModal(m => ({ ...m, custom: list }));
                    }}>Remove</button>
                  </div>
                ))}
                <button type="button" className="rounded-md border px-3 py-1.5 text-xs" onClick={()=> setAttrModal(m => ({ ...m, custom: [ ...(m.custom||[]), { name: '', value: '' } ] }))}>Add custom attribute</button>
              </div>
            </div>
            <div className="flex justify-end gap-2">
              <button type="button" className="rounded-md border px-3 py-2" onClick={()=>setAttrModal({...attrModal, open:false})}>Cancel</button>
              <button className="rounded-md bg-amber-600 text-white px-3 py-2" onClick={saveEditAttrs}>Save</button>
            </div>
          </div>
        </Modal>
      </main>
    </RoleGuard>
  );
}
