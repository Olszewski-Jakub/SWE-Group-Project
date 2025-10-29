package ie.universityofgalway.groupnine.delivery.rest.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public class MoneyDto {
    @NotNull @PositiveOrZero private BigDecimal amount;
    @NotBlank private String currency;
    public MoneyDto() {}
    public MoneyDto(BigDecimal amount, String currency) { this.amount = amount; this.currency = currency; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
}
