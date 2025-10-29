package ie.universityofgalway.groupnine.service.product.admin.usecase;

import ie.universityofgalway.groupnine.domain.product.*;
import ie.universityofgalway.groupnine.service.product.port.ProductPort;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class DeleteUseCasesTest {

    private static class StubPort implements ProductPort {
        Product existingProduct;
        Variant existingVariant;
        boolean deletedProduct;
        boolean deletedVariant;
        @Override public org.springframework.data.domain.Page<Product> findAvailable(org.springframework.data.domain.Pageable pageable){return org.springframework.data.domain.Page.empty();}
        @Override public org.springframework.data.domain.Page<Product> findAvailableByCategory(String category, org.springframework.data.domain.Pageable pageable){return org.springframework.data.domain.Page.empty();}
        @Override public org.springframework.data.domain.Page<Product> search(SearchQuery query, org.springframework.data.domain.Pageable pageable){return org.springframework.data.domain.Page.empty();}
        @Override public Optional<Product> findById(ProductId id){return Optional.ofNullable(existingProduct);}    
        @Override public org.springframework.data.domain.Page<Product> listAll(org.springframework.data.domain.Pageable pageable){return org.springframework.data.domain.Page.empty();}
        @Override public boolean productExistsByUuid(UUID uuid){return false;}
        @Override public Product saveProduct(Product product){return product;}
        @Override public void deleteProduct(ProductId id){deletedProduct = true;}
        @Override public boolean variantExistsBySku(String sku){return false;}
        @Override public Optional<Variant> findVariantById(VariantId id){return Optional.ofNullable(existingVariant);}    
        @Override public Variant saveVariant(ProductId productId, Variant variant){return variant;}
        @Override public void deleteVariant(ProductId productId, VariantId variantId){deletedVariant = true;}
    }

    @Test
    void deleteProductHappy() {
        StubPort port = new StubPort();
        UUID pid = UUID.randomUUID();
        port.existingProduct = new Product(new ProductId(pid), "P", "d", "c", ProductStatus.DRAFT, java.util.List.of(), java.time.Instant.now(), java.time.Instant.now());
        DeleteProductUseCase uc = new DeleteProductUseCase(port);
        uc.execute(new ProductId(pid));
        assertTrue(port.deletedProduct);
    }

    @Test
    void deleteProductMissing() {
        StubPort port = new StubPort();
        DeleteProductUseCase uc = new DeleteProductUseCase(port);
        assertThrows(java.util.NoSuchElementException.class, () -> uc.execute(new ProductId(UUID.randomUUID())));
    }

    @Test
    void deleteVariantHappy() {
        StubPort port = new StubPort();
        UUID pid = UUID.randomUUID();
        UUID vid = UUID.randomUUID();
        port.existingProduct = new Product(new ProductId(pid), "P", "d", "c", ProductStatus.DRAFT, java.util.List.of(), java.time.Instant.now(), java.time.Instant.now());
        port.existingVariant = new Variant(new VariantId(vid), new Sku("SKU-1"), new Money(java.math.BigDecimal.ONE, java.util.Currency.getInstance("EUR")), new Stock(1,0), java.util.List.of());
        DeleteVariantUseCase uc = new DeleteVariantUseCase(port);
        uc.execute(new ProductId(pid), new VariantId(vid));
        assertTrue(port.deletedVariant);
    }
}

