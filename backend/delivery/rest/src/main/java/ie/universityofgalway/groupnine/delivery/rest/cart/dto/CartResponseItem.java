package ie.universityofgalway.groupnine.delivery.rest.cart.dto;

import java.util.UUID;

/**
 * Represents an item in the cart response, enriched with product information
 * derived from the variant ID.
 */
public class CartResponseItem {
    private UUID variantId;
    private int quantity;

    private UUID productId;
    private String productName;
    private String productDescription;
    private ie.universityofgalway.groupnine.delivery.rest.product.dto.MoneyDto unitPrice;
    private ie.universityofgalway.groupnine.delivery.rest.product.dto.MoneyDto subtotal;

    public CartResponseItem() {}

    public CartResponseItem(UUID variantId, int quantity, UUID productId, String productName, String productDescription,
                            ie.universityofgalway.groupnine.delivery.rest.product.dto.MoneyDto unitPrice,
                            ie.universityofgalway.groupnine.delivery.rest.product.dto.MoneyDto subtotal) {
        this.variantId = variantId;
        this.quantity = quantity;
        this.productId = productId;
        this.productName = productName;
        this.productDescription = productDescription;
        this.unitPrice = unitPrice;
        this.subtotal = subtotal;
    }

    public UUID getVariantId() { return variantId; }
    public void setVariantId(UUID variantId) { this.variantId = variantId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getProductDescription() { return productDescription; }
    public void setProductDescription(String productDescription) { this.productDescription = productDescription; }

    public ie.universityofgalway.groupnine.delivery.rest.product.dto.MoneyDto getUnitPrice() { return unitPrice; }
    public void setUnitPrice(ie.universityofgalway.groupnine.delivery.rest.product.dto.MoneyDto unitPrice) { this.unitPrice = unitPrice; }

    public ie.universityofgalway.groupnine.delivery.rest.product.dto.MoneyDto getSubtotal() { return subtotal; }
    public void setSubtotal(ie.universityofgalway.groupnine.delivery.rest.product.dto.MoneyDto subtotal) { this.subtotal = subtotal; }
}
