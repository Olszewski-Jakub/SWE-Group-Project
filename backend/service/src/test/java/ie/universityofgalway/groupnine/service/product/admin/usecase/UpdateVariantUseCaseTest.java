package ie.universityofgalway.groupnine.service.product.admin.usecase;

import ie.universityofgalway.groupnine.domain.product.*;
import ie.universityofgalway.groupnine.service.product.admin.UpdateVariantCommand;
import ie.universityofgalway.groupnine.service.product.port.ProductPort;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class UpdateVariantUseCaseTest {

    @Test
    void invalid_currency_throws() {
        ProductPort port = Mockito.mock(ProductPort.class);
        ProductId pid = new ProductId(UUID.randomUUID());
        VariantId vid = new VariantId(UUID.randomUUID());
        when(port.findById(pid)).thenReturn(Optional.of(new Product(pid, "n", "d", "c", ProductStatus.ACTIVE, java.util.List.of(), java.time.Instant.now(), java.time.Instant.now())));
        when(port.findVariantById(vid)).thenReturn(Optional.of(new Variant(vid, new Sku("S"), new Money(new BigDecimal("1.00"), Currency.getInstance("EUR")), new Stock(1,0), java.util.List.of())));

        UpdateVariantUseCase uc = new UpdateVariantUseCase(port);
        UpdateVariantCommand cmd = new UpdateVariantCommand(vid, null, new BigDecimal("1.00"), "BAD", null, null, null, java.util.List.of());
        assertThrows(IllegalArgumentException.class, () -> uc.execute(pid, cmd));
    }

    @Test
    void updates_and_saves_variant() {
        ProductPort port = Mockito.mock(ProductPort.class);
        ProductId pid = new ProductId(UUID.randomUUID());
        VariantId vid = new VariantId(UUID.randomUUID());
        when(port.findById(pid)).thenReturn(Optional.of(new Product(pid, "n", "d", "c", ProductStatus.ACTIVE, java.util.List.of(), java.time.Instant.now(), java.time.Instant.now())));
        Variant existing = new Variant(vid, new Sku("S"), new Money(new BigDecimal("1.00"), Currency.getInstance("EUR")), new Stock(1,0), java.util.List.of());
        when(port.findVariantById(vid)).thenReturn(Optional.of(existing));
        when(port.saveVariant(any(ProductId.class), any(Variant.class))).thenAnswer(inv -> inv.getArgument(1));

        UpdateVariantUseCase uc = new UpdateVariantUseCase(port);
        UpdateVariantCommand cmd = new UpdateVariantCommand(vid, "NEWSKU", new BigDecimal("2.00"), "USD", 5, 0, null, java.util.List.of());
        Variant saved = uc.execute(pid, cmd);
        assertEquals("NEWSKU", saved.getSku().getValue());
        assertEquals(new BigDecimal("2.00"), saved.getPrice().getAmount());
        assertEquals("USD", saved.getPrice().getCurrency().getCurrencyCode());
        assertEquals(5, saved.getStock().getQuantity());
    }
}
