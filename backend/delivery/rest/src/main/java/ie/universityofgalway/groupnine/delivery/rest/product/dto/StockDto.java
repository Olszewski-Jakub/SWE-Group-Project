package ie.universityofgalway.groupnine.delivery.rest.product.dto;

import jakarta.validation.constraints.PositiveOrZero;

public class StockDto {
    @PositiveOrZero private int quantity;
    @PositiveOrZero private int reserved;
    public StockDto() {}
    public StockDto(int quantity, int reserved) { this.quantity = quantity; this.reserved = reserved; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public int getReserved() { return reserved; }
    public void setReserved(int reserved) { this.reserved = reserved; }
}
