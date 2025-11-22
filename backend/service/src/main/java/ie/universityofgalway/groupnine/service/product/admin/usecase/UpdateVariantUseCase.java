package ie.universityofgalway.groupnine.service.product.admin.usecase;

import ie.universityofgalway.groupnine.domain.product.*;
import ie.universityofgalway.groupnine.service.product.port.ProductPort;
import ie.universityofgalway.groupnine.service.product.admin.UpdateVariantCommand;

import java.math.BigDecimal;

public class UpdateVariantUseCase {
    private final ProductPort port;
    public UpdateVariantUseCase(ProductPort port) { this.port = port; }

    public Variant execute(ProductId productId, UpdateVariantCommand cmd) {
        port.findById(productId).orElseThrow(() -> new java.util.NoSuchElementException("Product not found"));
        Variant existing = port.findVariantById(cmd.getId()).orElseThrow(() -> new java.util.NoSuchElementException("Variant not found"));

        String sku = cmd.getSku() != null ? cmd.getSku().trim() : existing.getSku().getValue();
        BigDecimal amount = cmd.getPriceAmount() != null ? cmd.getPriceAmount() : existing.getPrice().getAmount();
        String currencyCode = cmd.getCurrency() != null ? cmd.getCurrency() : existing.getPrice().getCurrency().getCurrencyCode();
        Integer qty = cmd.getStockQuantity() != null ? cmd.getStockQuantity() : existing.getStock().getQuantity();
        Integer res = cmd.getReservedQuantity() != null ? cmd.getReservedQuantity() : existing.getStock().getReserved();

        validateSku(sku);
        validateMoney(amount, currencyCode);
        validateStock(qty, res);

        String imageUrl = cmd.getImageUrl() != null ? cmd.getImageUrl() : existing.getImageUrl();
        java.util.List<Attribute> attrs = existing.getAttributes();
        if (cmd.getAttributes() != null) {
            attrs = cmd.getAttributes().stream()
                    .filter(p -> p != null && p.getName() != null && p.getValue() != null)
                    .map(p -> {
                        String raw = p.getName().trim();
                        String norm = raw.toLowerCase(java.util.Locale.ROOT).replaceAll("[^a-z0-9_]", "_");
                        norm = norm.replaceAll("_+", "_");
                        norm = norm.replaceAll("^_+|_+$", "");
                        if (norm.isEmpty()) norm = raw; // fallback to original if normalization empties it
                        return new Attribute(norm, p.getValue());
                    })
                    .toList();
        }

        Variant updated = new Variant(
                existing.getId(),
                new Sku(sku),
                new Money(amount, java.util.Currency.getInstance(currencyCode.toUpperCase())),
                new Stock(qty, res),
                attrs,
                imageUrl
        );
        return port.saveVariant(productId, updated);
    }

    private void validateSku(String sku) { if (sku == null || sku.isBlank()) throw new IllegalArgumentException("sku must not be blank"); }
    private void validateMoney(BigDecimal amount, String currency) {
        if (amount == null || amount.signum() < 0) throw new IllegalArgumentException("amount must be >= 0");
        if (currency == null || currency.isBlank()) throw new IllegalArgumentException("currency is required");
        try { java.util.Currency.getInstance(currency.toUpperCase()); } catch (Exception e) { throw new IllegalArgumentException("invalid currency"); }
    }
    private void validateStock(int quantity, int reserved) {
        if (quantity < 0 || reserved < 0) throw new IllegalArgumentException("stock must be >= 0");
        if (reserved > quantity) throw new IllegalArgumentException("reserved cannot exceed quantity");
    }
}
