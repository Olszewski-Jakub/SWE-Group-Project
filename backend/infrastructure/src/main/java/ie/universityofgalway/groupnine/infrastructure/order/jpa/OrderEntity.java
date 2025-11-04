package ie.universityofgalway.groupnine.infrastructure.order.jpa;

import jakarta.persistence.*;
import org.hibernate.annotations.ColumnTransformer;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "orders")
public class OrderEntity {
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "cart_id", nullable = false)
    private UUID cartId;

    @Column(name = "total_minor", nullable = false)
    private long totalMinor;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "snapshot_json", columnDefinition = "jsonb")
    @ColumnTransformer(write = "?::jsonb")
    private String snapshotJson;

    // Shipping fields
    @Column(name = "shipping_rate_id")
    private String shippingRateId;
    @Column(name = "shipping_amount_minor")
    private Long shippingAmountMinor;
    @Column(name = "shipping_currency")
    private String shippingCurrency;
    @Column(name = "shipping_name")
    private String shippingName;
    @Column(name = "shipping_phone")
    private String shippingPhone;
    @Column(name = "shipping_address_line1")
    private String shippingAddressLine1;
    @Column(name = "shipping_address_line2")
    private String shippingAddressLine2;
    @Column(name = "shipping_city")
    private String shippingCity;
    @Column(name = "shipping_state")
    private String shippingState;
    @Column(name = "shipping_postal_code")
    private String shippingPostalCode;
    @Column(name = "shipping_country")
    private String shippingCountry;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public UUID getCartId() { return cartId; }
    public void setCartId(UUID cartId) { this.cartId = cartId; }
    public long getTotalMinor() { return totalMinor; }
    public void setTotalMinor(long totalMinor) { this.totalMinor = totalMinor; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getSnapshotJson() { return snapshotJson; }
    public void setSnapshotJson(String snapshotJson) { this.snapshotJson = snapshotJson; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public String getShippingRateId() { return shippingRateId; }
    public void setShippingRateId(String shippingRateId) { this.shippingRateId = shippingRateId; }
    public Long getShippingAmountMinor() { return shippingAmountMinor; }
    public void setShippingAmountMinor(Long shippingAmountMinor) { this.shippingAmountMinor = shippingAmountMinor; }
    public String getShippingCurrency() { return shippingCurrency; }
    public void setShippingCurrency(String shippingCurrency) { this.shippingCurrency = shippingCurrency; }
    public String getShippingName() { return shippingName; }
    public void setShippingName(String shippingName) { this.shippingName = shippingName; }
    public String getShippingPhone() { return shippingPhone; }
    public void setShippingPhone(String shippingPhone) { this.shippingPhone = shippingPhone; }
    public String getShippingAddressLine1() { return shippingAddressLine1; }
    public void setShippingAddressLine1(String shippingAddressLine1) { this.shippingAddressLine1 = shippingAddressLine1; }
    public String getShippingAddressLine2() { return shippingAddressLine2; }
    public void setShippingAddressLine2(String shippingAddressLine2) { this.shippingAddressLine2 = shippingAddressLine2; }
    public String getShippingCity() { return shippingCity; }
    public void setShippingCity(String shippingCity) { this.shippingCity = shippingCity; }
    public String getShippingState() { return shippingState; }
    public void setShippingState(String shippingState) { this.shippingState = shippingState; }
    public String getShippingPostalCode() { return shippingPostalCode; }
    public void setShippingPostalCode(String shippingPostalCode) { this.shippingPostalCode = shippingPostalCode; }
    public String getShippingCountry() { return shippingCountry; }
    public void setShippingCountry(String shippingCountry) { this.shippingCountry = shippingCountry; }
}
