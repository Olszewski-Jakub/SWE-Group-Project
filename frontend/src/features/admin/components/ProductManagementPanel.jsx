"use client";

import { useEffect, useMemo, useState, Fragment } from 'react';
import { getProducts } from '@/lib/ProductService';
import {
  getProduct as mgmtGetProduct,
  createProductWithForm,
  updateProduct as mgmtUpdateProduct,
  deleteProduct as mgmtDeleteProduct,
  addVariant as mgmtAddVariant,
  updateVariant as mgmtUpdateVariant,
  deleteVariant as mgmtDeleteVariant,
} from '@/lib/ProductManagementService';
import Modal from '@/components/common/Modal';
import Alert from '@/components/common/Alert';
import { classNames } from '@/utils/helpers';
import { COFFEE_CATEGORIES } from '@/constants/coffeeCategories';

function ProductsTable({ page = 0, size = 20, onPageChange, refreshTick = 0 }) {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [expandedId, setExpandedId] = useState(null);
  const [detail, setDetail] = useState(null);
  const [detailLoading, setDetailLoading] = useState(false);
  const [editProductId, setEditProductId] = useState(null);
  const [editProductFields, setEditProductFields] = useState({ name: '', description: '', category: '', status: '' });
  const [actionError, setActionError] = useState('');
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    let mounted = true;
    setLoading(true);
    setError(null);
    getProducts(page, size)
      .then((res) => { if (mounted) setData(res); })
      .catch((e) => { if (mounted) setError(e?.message || 'Failed to load'); })
      .finally(() => { if (mounted) setLoading(false); });
    return () => { mounted = false; };
  }, [page, size, refreshTick]);

  const items = useMemo(() => data?.content || [], [data]);
  const totalPages = data?.totalPages ?? 0;

  const statusClasses = (status) => {
    const s = (status || '').toUpperCase();
    if (s === 'ACTIVE') return 'bg-emerald-50 text-emerald-700 ring-emerald-200';
    if (s === 'DRAFT') return 'bg-amber-50 text-amber-800 ring-amber-200';
    if (s === 'ARCHIVED') return 'bg-stone-100 text-stone-700 ring-stone-300';
    return 'bg-stone-50 text-stone-700 ring-stone-200';
  };

  if (loading) return <div className="rounded-lg border border-stone-200 bg-white/70 p-6">Loading products…</div>;
  if (error) return <div className="rounded-lg border border-red-200 bg-red-50 p-6 text-red-900">{error}</div>;

  const refreshPage = async () => {
    setLoading(true);
    setError(null);
    try { setData(await (await getProducts(page, size))); } catch (e) { setError(e?.message || 'Failed to load'); } finally { setLoading(false); }
  };

  const toggleExpand = async (id) => {
    setDetail(null);
    if (expandedId === id) { setExpandedId(null); return; }
    setExpandedId(id);
    setDetailLoading(true);
    try { setDetail(await mgmtGetProduct(id)); } catch (_) { setDetail(null); } finally { setDetailLoading(false); }
  };

  const handleDeleteProduct = async (id) => {
    if (!confirm('Delete this product?')) return;
    try { setBusy(true); await mgmtDeleteProduct(id); if (expandedId === id) setExpandedId(null); await refreshPage(); }
    catch (e) { setActionError(e?.message || 'Delete failed'); }
    finally { setBusy(false); }
  };

  const startEditProduct = (p) => {
    setEditProductId(p.id);
    setEditProductFields({ name: p.name || '', description: p.description || '', category: p.category || '', status: (p.status || '').toUpperCase() });
  };

  const submitEditProduct = async (e) => {
    e.preventDefault();
    const id = editProductId; if (!id) return;
    try { setBusy(true); await mgmtUpdateProduct(id, editProductFields); setEditProductId(null); await refreshPage(); if (expandedId === id) try { setDetail(await mgmtGetProduct(id)); } catch (_) {} }
    catch (e) { setActionError(e?.message || 'Update failed'); }
    finally { setBusy(false); }
  };

  const VariantManager = () => {
    const [adding, setAdding] = useState(false);
    const [form, setForm] = useState({ sku: '', priceAmount: '', priceCurrency: 'EUR', stockQuantity: '0', stockReserved: '0' });
    const onAdd = async (e) => {
      e.preventDefault(); if (!detail?.id) return;
      await mgmtAddVariant(detail.id, { sku: form.sku, price: { amount: form.priceAmount, currency: form.priceCurrency }, stock: { quantity: form.stockQuantity, reserved: form.stockReserved }, attributes: [] });
      setAdding(false); setForm({ sku: '', priceAmount: '', priceCurrency: 'EUR', stockQuantity: '0', stockReserved: '0' });
      try { setDetail(await mgmtGetProduct(detail.id)); } catch (_) {}
    };
    const onUpdate = async (variantId, patch) => { if (!detail?.id) return; await mgmtUpdateVariant(detail.id, variantId, patch); try { setDetail(await mgmtGetProduct(detail.id)); } catch (_) {} };
    const onDelete = async (variantId) => { if (!detail?.id) return; if (!confirm('Delete this variant?')) return; await mgmtDeleteVariant(detail.id, variantId); try { setDetail(await mgmtGetProduct(detail.id)); } catch (_) {} };
    const buildImageUrl = (u) => { if (!u) return null; const isAbs = /^(?:[a-z]+:)?\/\//i.test(u) || u.startsWith('data:'); if (isAbs) return u; const base = (process.env.NEXT_PUBLIC_API_BASE_URL || '').replace(/\/$/, ''); const path = u.startsWith('/') ? u : `/${u}`; return `${base}${path}`; };
    return (
      <div className="space-y-3">
        <div className="flex items-center justify-between">
          <h3 className="text-base font-semibold">Variants</h3>
          <button className="rounded-md border border-stone-300 px-3 py-1.5 text-sm hover:bg-stone-50" onClick={() => setAdding((v) => !v)}>{adding ? 'Cancel' : 'Add Variant'}</button>
        </div>
        {adding && (
          <form onSubmit={onAdd} className="rounded-lg border border-stone-200 bg-white/70 p-3 space-y-3">
            <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
              <div><label className="block text-xs font-medium text-stone-700 mb-1">SKU</label><input className="w-full rounded border px-2 py-1.5" value={form.sku} onChange={(e)=>setForm({...form, sku:e.target.value})} required /></div>
              <div><label className="block text-xs font-medium text-stone-700 mb-1">Price Amount</label><input className="w-full rounded border px-2 py-1.5" value={form.priceAmount} onChange={(e)=>setForm({...form, priceAmount:e.target.value})} required /></div>
              <div><label className="block text-xs font-medium text-stone-700 mb-1">Currency</label><input className="w-full rounded border px-2 py-1.5" value={form.priceCurrency} onChange={(e)=>setForm({...form, priceCurrency:e.target.value})} /></div>
            </div>
            <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
              <div><label className="block text-xs font-medium text-stone-700 mb-1">Stock Quantity</label><input className="w-full rounded border px-2 py-1.5" value={form.stockQuantity} onChange={(e)=>setForm({...form, stockQuantity:e.target.value})} /></div>
              <div><label className="block text-xs font-medium text-stone-700 mb-1">Reserved</label><input className="w-full rounded border px-2 py-1.5" value={form.stockReserved} onChange={(e)=>setForm({...form, stockReserved:e.target.value})} /></div>
              <div className="flex items-end"><button className="w-full rounded-md bg-amber-600 text-white px-3 py-2">Add Variant</button></div>
            </div>
          </form>
        )}
        <div className="overflow-x-auto rounded border">
          <table className="min-w-full text-sm">
            <thead className="bg-stone-50"><tr>
              <th className="px-3 py-2 text-left">Image</th>
              <th className="px-3 py-2 text-left">SKU</th>
              <th className="px-3 py-2 text-left">Price</th>
              <th className="px-3 py-2 text-left">Stock</th>
              <th className="px-3 py-2 text-left">Actions</th>
            </tr></thead>
            <tbody>
              {(detail?.variants || []).map((v) => (
                <tr key={v.id} className="border-t">
                  <td className="px-3 py-2">{v.imageUrl ? (<img src={buildImageUrl(v.imageUrl)} alt={v.sku} className="h-10 w-10 rounded object-cover ring-1 ring-black/5" />) : (<div className="h-10 w-10 rounded bg-stone-100 ring-1 ring-black/5 grid place-items-center text-stone-400 text-xs">—</div>)}</td>
                  <td className="px-3 py-2">{v.sku}</td>
                  <td className="px-3 py-2">{v.price?.amount} {v.price?.currency}</td>
                  <td className="px-3 py-2">{v.stock?.quantity} ({v.stock?.reserved} reserved)</td>
                  <td className="px-3 py-2 space-x-2">
                    <button className="rounded-md border border-stone-300 px-2 py-1 text-xs hover:bg-stone-50" onClick={() => onUpdate(v.id, { sku: v.sku })}>Save</button>
                    <button className="rounded-md border border-red-300 text-red-700 px-2 py-1 text-xs hover:bg-red-50" onClick={() => onDelete(v.id)}>Delete</button>
                  </td>
                </tr>
              ))}
              {(detail?.variants || []).length === 0 && (<tr><td className="px-3 py-6 text-center text-stone-600" colSpan={5}>No variants.</td></tr>)}
            </tbody>
          </table>
        </div>
      </div>
    );
  };

  return (
    <div className="space-y-3">
      {actionError && <Alert type="error" title="Action failed" message={actionError} onClose={() => setActionError('')} />}
      <div className="overflow-x-auto rounded-lg border border-stone-200 bg-white/70">
        <table className="min-w-full text-sm">
          <thead className="bg-stone-50/60 text-stone-700"><tr>
            <th className="px-4 py-3 text-left font-semibold">Name</th>
            <th className="px-4 py-3 text-left font-semibold">Category</th>
            <th className="px-4 py-3 text-left font-semibold">Status</th>
            <th className="px-4 py-3 text-left font-semibold">Variants</th>
            <th className="px-4 py-3 text-left font-semibold">Created</th>
            <th className="px-4 py-3 text-left font-semibold">Actions</th>
          </tr></thead>
          <tbody>
            {items.map((p) => (
              <Fragment key={p.id}>
                <tr className="border-t border-stone-200 text-stone-800">
                  <td className="px-4 py-3">{p.name}</td>
                  <td className="px-4 py-3 capitalize">{p.category}</td>
                  <td className="px-4 py-3"><span className={classNames('inline-flex items-center rounded-full px-2 py-0.5 text-xs font-semibold ring-1', statusClasses(p.status))}>{(p.status || '').toUpperCase()}</span></td>
                  <td className="px-4 py-3">{Array.isArray(p.variants) ? p.variants.length : 0}</td>
                  <td className="px-4 py-3 text-stone-600">{p.createdAt ? new Date(p.createdAt).toLocaleString() : '-'}</td>
                  <td className="px-4 py-3 space-x-2 whitespace-nowrap">
                    <button className="rounded-md border border-stone-300 px-2 py-1 text-xs hover:bg-stone-50" onClick={() => toggleExpand(p.id)}>{expandedId === p.id ? 'Hide' : 'Manage'}</button>
                    <button className="rounded-md border border-amber-300 text-amber-800 px-2 py-1 text-xs hover:bg-amber-50" onClick={() => startEditProduct(p)}>Edit</button>
                    <button className="rounded-md border border-red-300 text-red-700 px-2 py-1 text-xs hover:bg-red-50" onClick={() => handleDeleteProduct(p.id)}>Delete</button>
                  </td>
                </tr>
                {expandedId === p.id && (
                  <tr>
                    <td colSpan={6} className="bg-stone-50/50 px-4 py-4">
                      {detailLoading ? (
                        <div>Loading…</div>
                      ) : detail && detail.id === p.id ? (
                        <div className="space-y-4">
                          <div className="rounded-lg border border-stone-200 bg-white p-4 shadow-sm">
                            <div className="mb-2 flex items-center justify-between">
                              <h3 className="text-base font-semibold text-stone-900">Product Info</h3>
                            </div>
                            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                              <div><label className="block text-xs font-medium text-stone-600 mb-1">Name</label><div className="text-sm text-stone-900">{detail.name || '-'}</div></div>
                              <div><label className="block text-xs font-medium text-stone-600 mb-1">Category</label><div className="text-sm capitalize text-stone-900">{detail.category || '-'}</div></div>
                              <div><label className="block text-xs font-medium text-stone-600 mb-1">Status</label><span className={classNames('inline-flex items-center rounded-full px-2 py-0.5 text-xs font-semibold ring-1', statusClasses(detail.status))}>{(detail.status || '').toUpperCase() || '-'}</span></div>
                              <div><label className="block text-xs font-medium text-stone-600 mb-1">Variants</label><div className="text-sm">{Array.isArray(detail.variants) ? detail.variants.length : 0}</div></div>
                              <div className="md:col-span-2"><label className="block text-xs font-medium text-stone-600 mb-1">Description</label><div className="text-sm text-stone-900 whitespace-pre-wrap">{detail.description || '-'}</div></div>
                              <div><label className="block text-xs font-medium text-stone-600 mb-1">Created</label><div className="text-sm text-stone-700">{detail.createdAt ? new Date(detail.createdAt).toLocaleString() : '-'}</div></div>
                              <div><label className="block text-xs font-medium text-stone-600 mb-1">Updated</label><div className="text-sm text-stone-700">{detail.updatedAt ? new Date(detail.updatedAt).toLocaleString() : '-'}</div></div>
                              <div className="md:col-span-2"><label className="block text-xs font-medium text-stone-600 mb-1">ID</label><div className="font-mono text-xs break-all text-stone-700">{detail.id}</div></div>
                            </div>
                          </div>
                          <VariantManager />
                        </div>
                      ) : (
                        <div className="text-stone-600">No details.</div>
                      )}
                    </td>
                  </tr>
                )}
              </Fragment>
            ))}
            {items.length === 0 && (<tr><td colSpan={6} className="px-4 py-8 text-center text-stone-600">No products found.</td></tr>)}
          </tbody>
        </table>
      </div>

      <Modal open={Boolean(editProductId)} onClose={() => setEditProductId(null)}>
        <form onSubmit={submitEditProduct} className="space-y-4">
          <div><h3 className="text-lg font-semibold">Edit Product</h3><p className="text-sm text-stone-600">Update product details.</p></div>
          {actionError && <Alert type="error" title="Update failed" message={actionError} onClose={() => setActionError('')} />}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
            <div><label className="block text-xs font-medium text-stone-700 mb-1">Name</label><input className="w-full rounded border px-3 py-2" value={editProductFields.name} onChange={(e)=>setEditProductFields({...editProductFields, name:e.target.value})} /></div>
            <div><label className="block text-xs font-medium text-stone-700 mb-1">Category</label><select className="w-full rounded border px-3 py-2" value={editProductFields.category} onChange={(e)=>setEditProductFields({...editProductFields, category:e.target.value})}><option value="" disabled>Select category</option>{COFFEE_CATEGORIES.map(c => <option key={c.value} value={c.value}>{c.label}</option>)}</select></div>
            <div><label className="block text-xs font-medium text-stone-700 mb-1">Status</label><select className="w-full rounded border px-3 py-2" value={editProductFields.status} onChange={(e)=>setEditProductFields({...editProductFields, status:e.target.value})}><option value="DRAFT">DRAFT</option><option value="ACTIVE">ACTIVE</option><option value="ARCHIVED">ARCHIVED</option></select></div>
            <div><label className="block text-xs font-medium text-stone-700 mb-1">Description</label><textarea className="w-full rounded border px-3 py-2 min-h-[80px]" value={editProductFields.description} onChange={(e)=>setEditProductFields({...editProductFields, description:e.target.value})} /></div>
          </div>
          <div className="flex justify-end gap-2"><button type="button" className="rounded-md border px-4 py-2" onClick={()=>setEditProductId(null)} disabled={busy}>Cancel</button><button className="rounded-md bg-amber-600 text-white px-4 py-2 disabled:opacity-60" disabled={busy}>{busy ? 'Saving…' : 'Save'}</button></div>
        </form>
      </Modal>
    </div>
  );
}

export default function ProductManagementPanel() {
  const [page, setPage] = useState(0);
  const [showCreate, setShowCreate] = useState(false);
  const [refreshTick, setRefreshTick] = useState(0);
  const [createForm, setCreateForm] = useState({ name: '', description: '', category: '', status: 'ACTIVE', variants: [ { sku: '', priceAmount: '', priceCurrency: 'EUR', stockQuantity: '0', stockReserved: '0', imageFile: null } ] });
  const [createError, setCreateError] = useState('');
  const [createBusy, setCreateBusy] = useState(false);

  const addVariantRow = () => setCreateForm((f) => ({ ...f, variants: [...f.variants, { sku:'', priceAmount:'', priceCurrency:'EUR', stockQuantity:'0', stockReserved:'0', imageFile: null }] }));
  const removeVariantRow = (idx) => setCreateForm((f) => ({ ...f, variants: f.variants.filter((_, i) => i !== idx) }));
  const updateVariantField = (idx, key, value) => setCreateForm((f) => ({ ...f, variants: f.variants.map((v, i) => i === idx ? { ...v, [key]: value } : v) }));

  const submitCreate = async (e) => {
    e.preventDefault();
    try { setCreateBusy(true); await createProductWithForm(createForm); setShowCreate(false); setRefreshTick((t)=>t+1); setCreateForm({ name: '', description: '', category: '', status: 'ACTIVE', variants: [ { sku: '', priceAmount: '', priceCurrency: 'EUR', stockQuantity: '0', stockReserved: '0', imageFile: null } ] }); setCreateError(''); }
    catch (e) { setCreateError(e?.message || 'Create failed'); }
    finally { setCreateBusy(false); }
  };

  return (
    <div className="space-y-3">
      <div className="flex items-center justify-between">
        <h2 className="text-lg font-semibold text-stone-900">Product Management</h2>
        <button className="rounded-md bg-amber-600 px-3 py-2 text-sm font-semibold text-white hover:bg-amber-700" onClick={() => setShowCreate((v) => !v)}>{showCreate ? 'Close' : 'Create Product'}</button>
      </div>

      <Modal open={showCreate} onClose={() => setShowCreate(false)}>
        <form onSubmit={submitCreate} className="space-y-4">
          <div><h3 className="text-lg font-semibold">Create Product</h3><p className="text-sm text-stone-600">Add a new product with one or more variants.</p></div>
          {createError && <Alert type="error" title="Create failed" message={createError} onClose={() => setCreateError('')} />}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
            <div><label className="block text-xs font-medium text-stone-700 mb-1">Name</label><input className="w-full rounded border px-3 py-2" value={createForm.name} onChange={(e)=>setCreateForm({...createForm, name:e.target.value})} required /></div>
            <div><label className="block text-xs font-medium text-stone-700 mb-1">Category</label><select className="w-full rounded border px-3 py-2" value={createForm.category} onChange={(e)=>setCreateForm({...createForm, category:e.target.value})} required><option value="" disabled>Select category</option>{COFFEE_CATEGORIES.map(c => <option key={c.value} value={c.value}>{c.label}</option>)}</select></div>
            <div><label className="block text-xs font-medium text-stone-700 mb-1">Status</label><select className="w-full rounded border px-3 py-2" value={createForm.status} onChange={(e)=>setCreateForm({...createForm, status:e.target.value})}><option value="DRAFT">DRAFT</option><option value="ACTIVE">ACTIVE</option><option value="ARCHIVED">ARCHIVED</option></select></div>
            <div><label className="block text-xs font-medium text-stone-700 mb-1">Description</label><textarea className="w-full rounded border px-3 py-2 min-h-[80px]" value={createForm.description} onChange={(e)=>setCreateForm({...createForm, description:e.target.value})} /></div>
          </div>
          <div className="space-y-3">
            <div className="flex items-center justify-between"><div className="font-medium">Variants</div><button type="button" className="rounded-md border border-stone-300 px-3 py-1.5 text-sm hover:bg-stone-50" onClick={addVariantRow}>Add Variant</button></div>
            <div className="max-h-96 overflow-y-auto rounded-md border border-stone-200 p-2 bg-white/60">
              <div className="space-y-3">
                {createForm.variants.map((v, idx) => (
                  <div key={idx} className="rounded-lg border border-stone-200 bg-white p-3 shadow-sm">
                    <div className="mb-2 text-sm font-semibold text-stone-800">Variant {idx + 1}</div>
                    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3">
                      <div><label className="block text-xs font-medium text-stone-700 mb-1">SKU</label><input className="w-full rounded border px-2 py-1.5" value={v.sku} onChange={(e)=>updateVariantField(idx,'sku',e.target.value)} required /></div>
                      <div><label className="block text-xs font-medium text-stone-700 mb-1">Price Amount</label><input className="w-full rounded border px-2 py-1.5" value={v.priceAmount} onChange={(e)=>updateVariantField(idx,'priceAmount',e.target.value)} required /></div>
                      <div><label className="block text-xs font-medium text-stone-700 mb-1">Currency</label><input className="w-full rounded border px-2 py-1.5" value={v.priceCurrency} onChange={(e)=>updateVariantField(idx,'priceCurrency',e.target.value)} /></div>
                    </div>
                    <div className="mt-3 grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3 items-start">
                      <div><label className="block text-xs font-medium text-stone-700 mb-1">Stock Quantity</label><input className="w-full rounded border px-2 py-1.5" value={v.stockQuantity} onChange={(e)=>updateVariantField(idx,'stockQuantity',e.target.value)} /></div>
                      <div><label className="block text-xs font-medium text-stone-700 mb-1">Reserved</label><input className="w-full rounded border px-2 py-1.5" value={v.stockReserved} onChange={(e)=>updateVariantField(idx,'stockReserved',e.target.value)} /></div>
                      <div><label className="block text-xs font-medium text-stone-700 mb-1">Image</label><div className="flex items-center gap-3"><input className="rounded border px-2 py-1.5 w-full" type="file" accept="image/*" onChange={(e)=>updateVariantField(idx,'imageFile', e.target.files?.[0] || null)} />{v.imageFile && <img alt="preview" className="h-12 w-12 rounded object-cover ring-1 ring-black/5" src={URL.createObjectURL(v.imageFile)} />}</div></div>
                    </div>
                    <div className="mt-3 flex justify-end"><button type="button" className="rounded-md border border-red-300 text-red-700 px-3 py-1.5 text-sm hover:bg-red-50" onClick={()=>removeVariantRow(idx)}>Remove</button></div>
                  </div>
                ))}
              </div>
            </div>
          </div>
          <div className="flex justify-end gap-2"><button type="button" className="rounded-md border px-4 py-2" onClick={()=>setShowCreate(false)} disabled={createBusy}>Cancel</button><button className="rounded-md bg-amber-600 text-white px-4 py-2 disabled:opacity-60" disabled={createBusy}>{createBusy ? 'Creating…' : 'Create'}</button></div>
        </form>
      </Modal>

      <ProductsTable page={page} size={20} onPageChange={setPage} refreshTick={refreshTick} />
    </div>
  );
}

