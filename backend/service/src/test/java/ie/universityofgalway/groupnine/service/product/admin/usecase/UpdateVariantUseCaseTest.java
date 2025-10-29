package ie.universityofgalway.groupnine.service.product.admin.usecase;

import ie.universityofgalway.groupnine.domain.product.*;
import ie.universityofgalway.groupnine.service.product.port.ProductPort;
import ie.universityofgalway.groupnine.service.product.admin.UpdateVariantCommand;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UpdateVariantUseCaseTest {

    private static class StubPort implements ProductPort {
        Product existingProduct;
        Variant existingVariant;
        Variant saved;
        @Override public org.springframework.data.domain.Page<Product> findAvailable(org.springframework.data.domain.Pageable pageable){return org.springframework.data.domain.Page.empty();}
        @Override public org.springframework.data.domain.Page<Product> findAvailableByCategory(String category, org.springframework.data.domain.Pageable pageable){return org.springframework.data.domain.Page.empty();}
        @Override public org.springframework.data.domain.Page<Product> search(SearchQuery query, org.springframework.data.domain.Pageable pageable){return org.springframework.data.domain.Page.empty();}
        @Override public Optional<Product> findById(ProductId id){return Optional.ofNullable(existingProduct);}    
        @Override public org.springframework.data.domain.Page<Product> listAll(org.springframework.data.domain.Pageable pageable){return org.springframework.data.domain.Page.empty();}
        @Override public boolean productExistsByUuid(UUID uuid){return false;}
        @Override public Product saveProduct(Product product){return product;}
        @Override public void deleteProduct(ProductId id){}
        @Override public boolean variantExistsBySku(String sku){return false;}
        @Override public Optional<Variant> findVariantById(VariantId id){return Optional.ofNullable(existingVariant);}    
        @Override public Variant saveVariant(ProductId productId, Variant variant){this.saved = variant; return variant;}
        @Override public void deleteVariant(ProductId productId, VariantId variantId){}
    }

    @Test
    void updatesPartialFields() {
        StubPort port = new StubPort();
        UUID pid = UUID.randomUUID();
        UUID vid = UUID.randomUUID();
        port.existingProduct = new Product(new ProductId(pid), "P", "d", "c", ProductStatus.DRAFT, java.util.List.of(), java.time.Instant.now(), java.time.Instant.now());
        port.existingVariant = new Variant(new VariantId(vid), new Sku("SKU-1"), new Money(new BigDecimal("5.00"), Currency.getInstance("EUR")), new Stock(10, 2), java.util.List.of());
        UpdateVariantUseCase uc = new UpdateVariantUseCase(port);

        UpdateVariantCommand cmd = new UpdateVariantCommand(new VariantId(vid), null, new BigDecimal("6.50"), null, 12, 1, null, null);
        Variant out = uc.execute(new ProductId(pid), cmd);
        assertEquals(new BigDecimal("6.50"), out.getPrice().getAmount());
        assertEquals(12, out.getStock().getQuantity());
        assertEquals(1, out.getStock().getReserved());
        assertNotNull(port.saved);
    }
}
