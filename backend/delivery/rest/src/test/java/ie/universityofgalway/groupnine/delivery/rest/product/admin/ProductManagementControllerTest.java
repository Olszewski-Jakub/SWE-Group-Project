package ie.universityofgalway.groupnine.delivery.rest.product.admin;

import ie.universityofgalway.groupnine.delivery.rest.product.ProductManagementController;
import ie.universityofgalway.groupnine.delivery.rest.product.dto.MoneyDto;
import ie.universityofgalway.groupnine.delivery.rest.product.dto.ProductRequest;
import ie.universityofgalway.groupnine.delivery.rest.product.dto.StockDto;
import ie.universityofgalway.groupnine.delivery.rest.product.dto.VariantRequest;
import ie.universityofgalway.groupnine.delivery.rest.rbac.RbacTestSecurityConfig;
import ie.universityofgalway.groupnine.util.Routes;
import ie.universityofgalway.groupnine.domain.product.Money;
import ie.universityofgalway.groupnine.domain.product.Product;
import ie.universityofgalway.groupnine.domain.product.ProductId;
import ie.universityofgalway.groupnine.domain.product.ProductStatus;
import ie.universityofgalway.groupnine.domain.product.Sku;
import ie.universityofgalway.groupnine.domain.product.Stock;
import ie.universityofgalway.groupnine.domain.product.Variant;
import ie.universityofgalway.groupnine.domain.product.VariantId;
import ie.universityofgalway.groupnine.service.product.admin.usecase.AddVariantUseCase;
import ie.universityofgalway.groupnine.service.product.admin.usecase.CreateProductUseCase;
import ie.universityofgalway.groupnine.service.product.admin.usecase.DeleteProductUseCase;
import ie.universityofgalway.groupnine.service.product.admin.usecase.DeleteVariantUseCase;
import ie.universityofgalway.groupnine.service.product.admin.usecase.GetProductUseCase;
import ie.universityofgalway.groupnine.service.product.admin.usecase.ListProductsUseCase;
import ie.universityofgalway.groupnine.service.product.admin.usecase.UpdateProductUseCase;
import ie.universityofgalway.groupnine.service.product.admin.usecase.UpdateVariantUseCase;
import ie.universityofgalway.groupnine.testsupport.web.CommonWebMvcTest;
import ie.universityofgalway.groupnine.testsupport.web.DeliveryWebMvcTest;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DeliveryWebMvcTest(controllers = ProductManagementController.class)
@Import(RbacTestSecurityConfig.class)
class ProductManagementControllerTest extends CommonWebMvcTest {

    @MockitoBean
    CreateProductUseCase createProduct;
    @MockitoBean
    GetProductUseCase getProduct;
    @MockitoBean
    UpdateProductUseCase updateProduct;
    @MockitoBean
    DeleteProductUseCase deleteProduct;
    @MockitoBean
    AddVariantUseCase addVariant;
    @MockitoBean
    UpdateVariantUseCase updateVariant;
    @MockitoBean
    DeleteVariantUseCase deleteVariant;
    @MockitoBean
    ListProductsUseCase listProducts;

    private AutoCloseable withRoles(String... roles) {
        var auth = new UsernamePasswordAuthenticationToken(
                "00000000-0000-0000-0000-000000000001",
                "token",
                java.util.Arrays.stream(roles)
                        .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
                        .map(SimpleGrantedAuthority::new)
                        .toList()
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
        return () -> SecurityContextHolder.clearContext();
    }

    private Product sampleProduct(UUID id) {
        return new Product(new ProductId(id), "Colombian Beans", "desc", "coffee", ProductStatus.DRAFT,
                List.of(new Variant(new VariantId(UUID.randomUUID()), new Sku("COF-250G"),
                        new Money(new BigDecimal("8.99"), java.util.Currency.getInstance("EUR")),
                        new Stock(100, 0), List.of())),
                java.time.Instant.now(), java.time.Instant.now());
    }

    @Test
    void create_requires_authentication() throws Exception {
        ProductRequest req = new ProductRequest("Colombian Beans", "desc", "coffee", ProductStatus.DRAFT,
                List.of(new VariantRequest("COF-250G", new MoneyDto(new BigDecimal("8.99"), "EUR"), new StockDto(100, 0), null, List.of(), null)));
        mockMvc.perform(post(Routes.PRODUCT_MANAGEMENT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void create_returns_201_with_location_for_admin() throws Exception {
        try (var ignored = withRoles("ADMIN")) {
            UUID id = UUID.randomUUID();
            when(createProduct.execute(any())).thenReturn(sampleProduct(id));

            ProductRequest req = new ProductRequest("Colombian Beans", "desc", "coffee", ProductStatus.DRAFT,
                    List.of(new VariantRequest("COF-250G", new MoneyDto(new BigDecimal("8.99"), "EUR"), new StockDto(100, 0), null, List.of(), null)));

            mockMvc.perform(post(Routes.PRODUCT_MANAGEMENT)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(req)))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", org.hamcrest.Matchers.containsString(id.toString())))
                    .andExpect(jsonPath("$.id").value(id.toString()))
                    .andExpect(jsonPath("$.name").value("Colombian Beans"));
        }
    }

    @Test
    void get_returns_404_when_missing_and_200_when_found() throws Exception {
        try (var ignored = withRoles("ADMIN")) {
            UUID id = UUID.randomUUID();
            when(getProduct.byId(any())).thenReturn(Optional.empty());
            mockMvc.perform(get(Routes.PRODUCT_MANAGEMENT + "/{id}", id))
                    .andExpect(status().isNotFound());

            when(getProduct.byId(any())).thenReturn(Optional.of(sampleProduct(id)));
            mockMvc.perform(get(Routes.PRODUCT_MANAGEMENT + "/{id}", id))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(id.toString()));
        }
    }

    @Test
    void patch_updates_product_for_admin() throws Exception {
        try (var ignored = withRoles("ADMIN")) {
            UUID id = UUID.randomUUID();
            when(updateProduct.execute(any())).thenReturn(sampleProduct(id));
            ProductRequest partial = new ProductRequest("New Name", null, null, ProductStatus.ACTIVE, null);
            mockMvc.perform(patch(Routes.PRODUCT_MANAGEMENT + "/{id}", id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(partial)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(id.toString()));
        }
    }

    @Test
    void delete_returns_204_for_admin() throws Exception {
        try (var ignored = withRoles("ADMIN")) {
            UUID id = UUID.randomUUID();
            mockMvc.perform(delete(Routes.PRODUCT_MANAGEMENT + "/{id}", id))
                    .andExpect(status().isNoContent());
        }
    }
}

