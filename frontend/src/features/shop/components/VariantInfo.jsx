export default function VariantInfo({ variant }) {
  // Normalize attributes to an array to avoid runtime errors if missing.
  const attributes = Array.isArray(variant?.attributes) ? variant.attributes : [];

  return (
    <div className="rounded-md border border-gray-200 bg-white p-4">
      {/* Attributes list */}
      <div className="text-sm">
        <div className="mb-2 font-medium text-[#7B542F]">Attributes</div>

        {/* Render a simple list only when there are attributes */}
        {attributes.length > 0 ? (
          <ul className="list-disc space-y-1 pl-5 text-gray-700 marker:text-[#B6771D]">
            {attributes.map((attribute, i) => (
              <li key={attribute?.name ?? i}>
                {/* Use <strong> for semantic emphasis */}
                <strong>{attribute?.name}</strong>: {attribute?.value}
              </li>
            ))}
          </ul>
        ) : (
          <p className="text-gray-500">No attributes available.</p>
        )}
      </div>

      {/* Price block */}
      <div className="mt-4">
        {/* Format cents to currency; en-IE locale with dynamic currency */}
        <span className="text-base font-semibold text-[#7B542F]">
          {new Intl.NumberFormat("en-IE", {
            style: "currency",
            currency: variant?.currency || "EUR",
          }).format((variant?.priceCents ?? 0) / 100)}
        </span>
      </div>
    </div>
  );
}
