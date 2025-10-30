package ie.universityofgalway.groupnine.service.product.admin.usecase;

import ie.universityofgalway.groupnine.domain.product.*;
import ie.universityofgalway.groupnine.service.product.port.ProductPort;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AddVariantUseCaseTest {

    @Test
    void generates_id_when_missing_and_saves() {
        ProductPort port = Mockito.mock(ProductPort.class);
        ProductId pid = new ProductId(java.util.UUID.randomUUID());
        when(port.findById(pid)).thenReturn(Optional.of(new Product(pid, "n", "d", "c", ProductStatus.ACTIVE, java.util.List.of(), java.time.Instant.now(), java.time.Instant.now())));
        when(port.saveVariant(any(ProductId.class), any(Variant.class))).thenAnswer(inv -> inv.getArgument(1));

        Variant v = new Variant(null, new Sku("SKU"), new Money(new BigDecimal("1.00"), Currency.getInstance("EUR")), new Stock(1,0), java.util.List.of());
        AddVariantUseCase uc = new AddVariantUseCase(port);
        Variant saved = uc.execute(pid, v);

        assertNotNull(saved.getId());
        verify(port).saveVariant(eq(pid), any(Variant.class));
    }

    @Test
    void invalid_stock_throws() {
        ProductPort port = Mockito.mock(ProductPort.class);
        ProductId pid = new ProductId(java.util.UUID.randomUUID());
        when(port.findById(pid)).thenReturn(Optional.of(mock(Product.class)));

        Variant bad = new Variant(new VariantId(java.util.UUID.randomUUID()), new Sku("SKU"), new Money(new BigDecimal("1.00"), Currency.getInstance("EUR")), new Stock(1,2), java.util.List.of());
        AddVariantUseCase uc = new AddVariantUseCase(port);
        assertThrows(IllegalArgumentException.class, () -> uc.execute(pid, bad));
    }
}

