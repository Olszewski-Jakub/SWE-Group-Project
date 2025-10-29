package ie.universityofgalway.groupnine.service.product.admin.usecase;

import ie.universityofgalway.groupnine.domain.product.*;
import ie.universityofgalway.groupnine.service.product.port.ProductPort;
import ie.universityofgalway.groupnine.service.product.admin.UpdateProductCommand;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UpdateProductUseCaseTest {

    private static class CapturingPort implements ProductPort {
        Product saved;
        Product existing;
        @Override public org.springframework.data.domain.Page<Product> findAvailable(org.springframework.data.domain.Pageable pageable){return org.springframework.data.domain.Page.empty();}
        @Override public org.springframework.data.domain.Page<Product> findAvailableByCategory(String category, org.springframework.data.domain.Pageable pageable){return org.springframework.data.domain.Page.empty();}
        @Override public org.springframework.data.domain.Page<Product> search(SearchQuery query, org.springframework.data.domain.Pageable pageable){return org.springframework.data.domain.Page.empty();}
        @Override public Optional<Product> findById(ProductId id){return Optional.ofNullable(existing);}    
        @Override public org.springframework.data.domain.Page<Product> listAll(org.springframework.data.domain.Pageable pageable){return org.springframework.data.domain.Page.empty();}
        @Override public boolean productExistsByUuid(UUID uuid){return false;}
        @Override public Product saveProduct(Product product){this.saved = product; return product;}
        @Override public void deleteProduct(ProductId id){}
        @Override public boolean variantExistsBySku(String sku){return false;}
        @Override public Optional<Variant> findVariantById(VariantId id){return Optional.empty();}
        @Override public Variant saveVariant(ProductId productId, Variant variant){return variant;}
        @Override public void deleteVariant(ProductId productId, VariantId variantId){}
    }

    @Test
    void updatesNameAndStatus() {
        CapturingPort port = new CapturingPort();
        UUID id = UUID.randomUUID();
        port.existing = new Product(new ProductId(id), "Old", "d", "c", ProductStatus.DRAFT, java.util.List.of(), java.time.Instant.now(), java.time.Instant.now());
        UpdateProductUseCase uc = new UpdateProductUseCase(port);
        UpdateProductCommand cmd = new UpdateProductCommand(new ProductId(id), "New Name", null, null, ProductStatus.ACTIVE);

        Product out = uc.execute(cmd);
        assertEquals("New Name", out.getName());
        assertEquals(ProductStatus.ACTIVE, out.getStatus());
        assertNotNull(port.saved);
    }

    @Test
    void rejectsArchivedToActive() {
        CapturingPort port = new CapturingPort();
        UUID id = UUID.randomUUID();
        port.existing = new Product(new ProductId(id), "Old", "d", "c", ProductStatus.ARCHIVED, java.util.List.of(), java.time.Instant.now(), java.time.Instant.now());
        UpdateProductUseCase uc = new UpdateProductUseCase(port);
        UpdateProductCommand cmd = new UpdateProductCommand(new ProductId(id), null, null, null, ProductStatus.ACTIVE);
        assertThrows(RuntimeException.class, () -> uc.execute(cmd));
    }
}
