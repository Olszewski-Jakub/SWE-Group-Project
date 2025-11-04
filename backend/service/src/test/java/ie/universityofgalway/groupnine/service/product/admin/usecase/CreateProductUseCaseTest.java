package ie.universityofgalway.groupnine.service.product.admin.usecase;

import ie.universityofgalway.groupnine.domain.product.*;
import ie.universityofgalway.groupnine.service.product.port.ProductPort;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CreateProductUseCaseTest {

    @Test
    void creates_product_and_validates_variants() {
        ProductPort port = Mockito.mock(ProductPort.class);
        when(port.productExistsByUuid(any(UUID.class))).thenReturn(false);
        when(port.saveProduct(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        Variant v = new Variant(null, new Sku("SKU-1"), new Money(new BigDecimal("9.99"), Currency.getInstance("EUR")), new Stock(1,0), List.of());
        Product p = new Product(null, " Name ", "d", " Cat ", ProductStatus.ACTIVE, List.of(v), Instant.now(), Instant.now());

        CreateProductUseCase uc = new CreateProductUseCase(port);
        Product saved = uc.execute(p);

        assertNotNull(saved.getId());
        assertEquals("Name", saved.getName());
        assertEquals("Cat", saved.getCategory());
        assertEquals(1, saved.getVariants().size());
        assertNotNull(saved.getVariants().get(0).getId());
        verify(port).saveProduct(any(Product.class));
    }

    @Test
    void idempotent_create_returns_existing_when_uuid_exists() {
        ProductPort port = Mockito.mock(ProductPort.class);
        UUID uuid = UUID.randomUUID();
        Product existing = new Product(new ProductId(uuid), "x", "d", "c", ProductStatus.ACTIVE, List.of(), Instant.now(), Instant.now());
        when(port.productExistsByUuid(eq(uuid))).thenReturn(true);
        when(port.findById(eq(new ProductId(uuid)))).thenReturn(Optional.of(existing));

        Product p = new Product(new ProductId(uuid), "x", "d", "c", ProductStatus.ACTIVE, List.of(), Instant.now(), Instant.now());

        CreateProductUseCase uc = new CreateProductUseCase(port);
        Product saved = uc.execute(p);
        assertEquals(existing, saved);
        verify(port, never()).saveProduct(any());
    }

    @Test
    void invalid_name_throws() {
        ProductPort port = Mockito.mock(ProductPort.class);
        CreateProductUseCase uc = new CreateProductUseCase(port);
        Product p = new Product(null, " ", "d", "c", ProductStatus.ACTIVE, List.of(), Instant.now(), Instant.now());
        assertThrows(IllegalArgumentException.class, () -> uc.execute(p));
    }
}

