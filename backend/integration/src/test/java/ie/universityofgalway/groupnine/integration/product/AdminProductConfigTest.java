package ie.universityofgalway.groupnine.integration.product;

import ie.universityofgalway.groupnine.service.product.port.ProductPort;
import ie.universityofgalway.groupnine.service.product.admin.usecase.*;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.junit.jupiter.api.Assertions.*;

@SpringJUnitConfig
@ContextConfiguration(classes = {AdminProductConfig.class, AdminProductConfigTest.TestBeans.class})
class AdminProductConfigTest {

    @TestConfiguration
    static class TestBeans {
        @Bean ProductPort productPort() { return new ProductPort() {
            @Override public org.springframework.data.domain.Page<ie.universityofgalway.groupnine.domain.product.Product> findAvailable(org.springframework.data.domain.Pageable pageable){return org.springframework.data.domain.Page.empty();}
            @Override public org.springframework.data.domain.Page<ie.universityofgalway.groupnine.domain.product.Product> findAvailableByCategory(String category, org.springframework.data.domain.Pageable pageable){return org.springframework.data.domain.Page.empty();}
            @Override public org.springframework.data.domain.Page<ie.universityofgalway.groupnine.domain.product.Product> search(ie.universityofgalway.groupnine.domain.product.SearchQuery query, org.springframework.data.domain.Pageable pageable){return org.springframework.data.domain.Page.empty();}
            @Override public java.util.Optional<ie.universityofgalway.groupnine.domain.product.Product> findById(ie.universityofgalway.groupnine.domain.product.ProductId id){return java.util.Optional.empty();}
            @Override public org.springframework.data.domain.Page<ie.universityofgalway.groupnine.domain.product.Product> listAll(org.springframework.data.domain.Pageable pageable){return org.springframework.data.domain.Page.empty();}
            @Override public boolean productExistsByUuid(java.util.UUID uuid){return false;}
            @Override public ie.universityofgalway.groupnine.domain.product.Product saveProduct(ie.universityofgalway.groupnine.domain.product.Product product){return product;}
            @Override public void deleteProduct(ie.universityofgalway.groupnine.domain.product.ProductId id){}
            @Override public boolean variantExistsBySku(String sku){return false;}
            @Override public java.util.Optional<ie.universityofgalway.groupnine.domain.product.Variant> findVariantById(ie.universityofgalway.groupnine.domain.product.VariantId id){return java.util.Optional.empty();}
            @Override public ie.universityofgalway.groupnine.domain.product.Variant saveVariant(ie.universityofgalway.groupnine.domain.product.ProductId productId, ie.universityofgalway.groupnine.domain.product.Variant variant){return variant;}
            @Override public void deleteVariant(ie.universityofgalway.groupnine.domain.product.ProductId productId, ie.universityofgalway.groupnine.domain.product.VariantId variantId){}
        }; }
    }

    @org.springframework.beans.factory.annotation.Autowired CreateProductUseCase create;
    @org.springframework.beans.factory.annotation.Autowired GetProductUseCase get;
    @org.springframework.beans.factory.annotation.Autowired UpdateProductUseCase update;
    @org.springframework.beans.factory.annotation.Autowired DeleteProductUseCase delete;
    @org.springframework.beans.factory.annotation.Autowired AddVariantUseCase add;
    @org.springframework.beans.factory.annotation.Autowired UpdateVariantUseCase updateV;
    @org.springframework.beans.factory.annotation.Autowired DeleteVariantUseCase deleteV;
    @org.springframework.beans.factory.annotation.Autowired ListProductsUseCase list;

    @Test
    void beansPresent() {
        assertNotNull(create);
        assertNotNull(get);
        assertNotNull(update);
        assertNotNull(delete);
        assertNotNull(add);
        assertNotNull(updateV);
        assertNotNull(deleteV);
        assertNotNull(list);
    }
}

