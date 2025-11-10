import { getProduct } from "@/lib/ProductService";
import ProductInfo from "../components/ProductInfo";

// Route segment page for /products/[id] (server component by default)
export default async function ProductPage({ params }) {
  // Access dynamic route param: /products/[id]
  const { id } = params;

  // Server-side data fetch (runs on the server; no client bundle impact)
  const product = await getProduct(id);

  // Pass data to a client component for interactivity (variant selection, etc.)
  return <ProductInfo product={product} />;
}
