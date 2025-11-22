import VariantCreatePage from '@/features/admin/pages/VariantCreatePage';

export default function Page({ params }) {
  const { productId } = params;
  return <VariantCreatePage productId={productId} />;
}

