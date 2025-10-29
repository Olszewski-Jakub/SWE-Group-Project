package ie.universityofgalway.groupnine.service.product.admin.usecase;

import ie.universityofgalway.groupnine.domain.product.*;
import ie.universityofgalway.groupnine.service.product.port.ProductPort;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AddVariantUseCaseTest {

    private static class StubPort implements ProductPort {
        Product existing;
        Variant saved;
        @Override public org.springframework.data.domain.Page<Product> findAvailable(org.springframework.data.domain.Pageable pageable){return org.springframework.data.domain.Page.empty();}
        @Override public org.springframework.data.domain.Page<Product> findAvailableByCategory(String category, org.springframework.data.domain.Pageable pageable){return org.springframework.data.domain.Page.empty();}
        @Override public org.springframework.data.domain.Page<Product> search(SearchQuery query, org.springframework.data.domain.Pageable pageable){return org.springframework.data.domain.Page.empty();}
        @Override public Optional<Product> findById(ProductId id){return Optional.ofNullable(existing);}    
        @Override public org.springframework.data.domain.Page<Product> listAll(org.springframework.data.domain.Pageable pageable){return org.springframework.data.domain.Page.empty();}
        @Override public boolean productExistsByUuid(UUID uuid){return false;}
        @Override public Product saveProduct(Product product){return product;}
        @Override public void deleteProduct(ProductId id){}
        @Override public boolean variantExistsBySku(String sku){return false;}
        @Override public Optional<Variant> findVariantById(VariantId id){return Optional.empty();}
        @Override public Variant saveVariant(ProductId productId, Variant variant){this.saved = variant; return variant;}
        @Override public void deleteVariant(ProductId productId, VariantId variantId){}
    }

    @Test
    void generatesVariantIdWhenMissing() {
        StubPort port = new StubPort();
        UUID pid = UUID.randomUUID();
        port.existing = new Product(new ProductId(pid), "P", "d", "c", ProductStatus.DRAFT, java.util.List.of(), java.time.Instant.now(), java.time.Instant.now());
        AddVariantUseCase uc = new AddVariantUseCase(port);

        Variant input = new Variant(null, new Sku("SKU-1"), new Money(new BigDecimal("10.00"), Currency.getInstance("EUR")), new Stock(5, 0), java.util.List.of());
        Variant out = uc.execute(new ProductId(pid), input);
        assertNotNull(out.getId());
        assertNotNull(port.saved);
    }

    @Test
    void validatesStockInvariant() {
        StubPort port = new StubPort();
        UUID pid = UUID.randomUUID();
        port.existing = new Product(new ProductId(pid), "P", "d", "c", ProductStatus.DRAFT, java.util.List.of(), java.time.Instant.now(), java.time.Instant.now());
        AddVariantUseCase uc = new AddVariantUseCase(port);
        Variant bad = new Variant(null, new Sku("SKU-2"), new Money(new BigDecimal("1.00"), Currency.getInstance("EUR")), new Stock(1, 2), java.util.List.of());
        assertThrows(IllegalArgumentException.class, () -> uc.execute(new ProductId(pid), bad));
    }
}

