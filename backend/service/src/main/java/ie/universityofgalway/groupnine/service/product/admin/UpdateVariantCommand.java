package ie.universityofgalway.groupnine.service.product.admin;

import ie.universityofgalway.groupnine.domain.product.VariantId;

import java.math.BigDecimal;
import java.util.List;

public class UpdateVariantCommand {
    private final VariantId id;
    private final String sku;
    private final BigDecimal priceAmount;
    private final String currency;
    private final Integer stockQuantity;
    private final Integer reservedQuantity;
    private final String imageUrl;
    private final List<AttributePair> attributes;

    public UpdateVariantCommand(VariantId id, String sku, BigDecimal priceAmount, String currency,
                                Integer stockQuantity, Integer reservedQuantity, String imageUrl,
                                List<AttributePair> attributes) {
        this.id = id;
        this.sku = sku;
        this.priceAmount = priceAmount;
        this.currency = currency;
        this.stockQuantity = stockQuantity;
        this.reservedQuantity = reservedQuantity;
        this.imageUrl = imageUrl;
        this.attributes = attributes;
    }

    public VariantId getId() { return id; }
    public String getSku() { return sku; }
    public BigDecimal getPriceAmount() { return priceAmount; }
    public String getCurrency() { return currency; }
    public Integer getStockQuantity() { return stockQuantity; }
    public Integer getReservedQuantity() { return reservedQuantity; }
    public String getImageUrl() { return imageUrl; }
    public List<AttributePair> getAttributes() { return attributes; }

    public static class AttributePair {
        private final String name;
        private final String value;
        public AttributePair(String name, String value) { this.name = name; this.value = value; }
        public String getName() { return name; }
        public String getValue() { return value; }
    }
}
