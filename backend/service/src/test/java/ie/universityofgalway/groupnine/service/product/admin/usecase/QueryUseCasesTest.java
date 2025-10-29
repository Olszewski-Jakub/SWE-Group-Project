package ie.universityofgalway.groupnine.service.product.admin.usecase;

import ie.universityofgalway.groupnine.domain.product.*;
import ie.universityofgalway.groupnine.service.product.port.ProductPort;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class QueryUseCasesTest {

    private static class StubPort implements ProductPort {
        Optional<Product> found = Optional.empty();
        Page<Product> list = Page.empty();
        @Override public Page<Product> findAvailable(org.springframework.data.domain.Pageable pageable){return Page.empty();}
        @Override public Page<Product> findAvailableByCategory(String category, org.springframework.data.domain.Pageable pageable){return Page.empty();}
        @Override public Page<Product> search(SearchQuery query, org.springframework.data.domain.Pageable pageable){return Page.empty();}
        @Override public Optional<Product> findById(ProductId id){return found;}
        @Override public Page<Product> listAll(org.springframework.data.domain.Pageable pageable){return list;}
        @Override public boolean productExistsByUuid(UUID uuid){return false;}
        @Override public Product saveProduct(Product product){return product;}
        @Override public void deleteProduct(ProductId id){}
        @Override public boolean variantExistsBySku(String sku){return false;}
        @Override public Optional<Variant> findVariantById(VariantId id){return Optional.empty();}
        @Override public Variant saveVariant(ProductId productId, Variant variant){return variant;}
        @Override public void deleteVariant(ProductId productId, VariantId variantId){}
    }

    @Test
    void getProductDelegates() {
        StubPort port = new StubPort();
        UUID id = UUID.randomUUID();
        port.found = Optional.of(new Product(new ProductId(id), "P", "d", "c", ProductStatus.DRAFT, List.of(), java.time.Instant.now(), java.time.Instant.now()));
        GetProductUseCase uc = new GetProductUseCase(port);
        Optional<Product> rs = uc.byId(new ProductId(id));
        assertTrue(rs.isPresent());
    }

    @Test
    void listProductsDelegates() {
        StubPort port = new StubPort();
        port.list = new PageImpl<>(List.of());
        ListProductsUseCase uc = new ListProductsUseCase(port);
        Page<Product> page = uc.execute(PageRequest.of(0, 5));
        assertNotNull(page);
    }
}

