import ProductEditorPage from '@/features/admin/pages/ProductEditorPage';

export default function Page({ params }) {
  const { productId } = params;
  return <ProductEditorPage mode="edit" productId={productId} />;
}

