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
import ie.universityofgalway.groupnine.service.product.admin.usecase.UploadVariantImageUseCase;
import ie.universityofgalway.groupnine.testsupport.web.CommonWebMvcTest;
import ie.universityofgalway.groupnine.testsupport.web.DeliveryWebMvcTest;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
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
    @MockitoBean
    UploadVariantImageUseCase uploadVariantImage;

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

    @Test
    void add_variant_returns_200_for_admin() throws Exception {
        try (var ignored = withRoles("ADMIN")) {
            UUID pid = UUID.randomUUID();
            UUID vid = UUID.randomUUID();
            Variant created = new Variant(new VariantId(vid), new Sku("COF-500G"),
                    new Money(new BigDecimal("12.50"), java.util.Currency.getInstance("EUR")),
                    new Stock(50, 0), List.of());
            when(addVariant.execute(any(), any())).thenReturn(created);

            VariantRequest req = new VariantRequest("COF-500G", new MoneyDto(new BigDecimal("12.50"), "EUR"), new StockDto(50, 0), null, List.of(), null);
            mockMvc.perform(post(Routes.PRODUCT_MANAGEMENT + "/{id}/variants", pid)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(vid.toString()))
                    .andExpect(jsonPath("$.sku").value("COF-500G"));
        }
    }

    @Test
    void update_variant_returns_200_for_admin() throws Exception {
        try (var ignored = withRoles("ADMIN")) {
            UUID pid = UUID.randomUUID();
            UUID vid = UUID.randomUUID();
            Variant updated = new Variant(new VariantId(vid), new Sku("COF-500G"),
                    new Money(new BigDecimal("13.00"), java.util.Currency.getInstance("EUR")),
                    new Stock(40, 2), List.of());
            when(updateVariant.execute(any(), any())).thenReturn(updated);

            VariantRequest req = new VariantRequest("COF-500G", new MoneyDto(new BigDecimal("13.00"), "EUR"), new StockDto(40, 2), null, List.of(), null);
            mockMvc.perform(patch(Routes.PRODUCT_MANAGEMENT + "/{pid}/variants/{vid}", pid, vid)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(vid.toString()))
                    .andExpect(jsonPath("$.price.amount").value(13.00));
        }
    }

    @Test
    void delete_variant_returns_204_for_admin() throws Exception {
        try (var ignored = withRoles("ADMIN")) {
            UUID pid = UUID.randomUUID();
            UUID vid = UUID.randomUUID();
            mockMvc.perform(delete(Routes.PRODUCT_MANAGEMENT + "/{pid}/variants/{vid}", pid, vid))
                    .andExpect(status().isNoContent());
        }
    }

    @Test
    void create_with_form_success_with_images_and_attributes() throws Exception {
        try (var ignored = withRoles("ADMIN")) {
            UUID pid = UUID.randomUUID();
            UUID v1 = UUID.randomUUID();
            UUID v2 = UUID.randomUUID();
            Product created = new Product(new ProductId(pid), "Beans", "desc", "coffee", ProductStatus.DRAFT,
                    List.of(
                            new Variant(new VariantId(v1), new Sku("COF-250G"), new Money(new BigDecimal("8.99"), java.util.Currency.getInstance("EUR")), new Stock(100, 0), List.of()),
                            new Variant(new VariantId(v2), new Sku("COF-500G"), new Money(new BigDecimal("14.99"), java.util.Currency.getInstance("EUR")), new Stock(50, 0), List.of())
                    ), java.time.Instant.now(), java.time.Instant.now());
            when(createProduct.execute(any())).thenReturn(created);

            // After image upload + update, controller reloads product
            Product withImages = new Product(new ProductId(pid), "Beans", "desc", "coffee", ProductStatus.DRAFT,
                    List.of(
                            new Variant(new VariantId(v1), new Sku("COF-250G"), new Money(new BigDecimal("8.99"), java.util.Currency.getInstance("EUR")), new Stock(100, 0), List.of(), "/img1"),
                            new Variant(new VariantId(v2), new Sku("COF-500G"), new Money(new BigDecimal("14.99"), java.util.Currency.getInstance("EUR")), new Stock(50, 0), List.of(), "/img2")
                    ), java.time.Instant.now(), java.time.Instant.now());
            when(getProduct.byId(any())).thenReturn(Optional.of(withImages));

            MockMultipartFile img1 = new MockMultipartFile("images", "1.jpg", "image/jpeg", new byte[]{1,2,3});
            MockMultipartFile img2 = new MockMultipartFile("images", "2.jpg", "image/jpeg", new byte[]{4,5});

            String attrsJson = "[ {\"roast\":\"medium\", \"weight_g\":\"250\"}, {\"origin\":\"Brazil\", \"count\":\"2\"} ]";

            mockMvc.perform(multipart(Routes.PRODUCT_MANAGEMENT + "/form")
                            .file(img1)
                            .file(img2)
                            .param("name", "Beans")
                            .param("description", "desc")
                            .param("category", "coffee")
                            .param("status", "DRAFT")
                            .param("variantSku", "COF-250G", "COF-500G")
                            .param("variantPriceAmount", "8.99", "14.99")
                            .param("variantPriceCurrency", "EUR", "EUR")
                            .param("variantStockQuantity", "100", "50")
                            .param("variantStockReserved", "0", "0")
                            .param("variantAttributes", attrsJson)
                            .characterEncoding(StandardCharsets.UTF_8))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", org.hamcrest.Matchers.containsString(pid.toString())))
                    .andExpect(jsonPath("$.id").value(pid.toString()))
                    .andExpect(jsonPath("$.variants[0].imageUrl").value("/img1"))
                    .andExpect(jsonPath("$.variants[1].imageUrl").value("/img2"));
        }
    }

    @Test
    void create_with_form_returns_400_when_list_sizes_mismatch() throws Exception {
        try (var ignored = withRoles("ADMIN")) {
            MockMultipartFile img1 = new MockMultipartFile("images", "1.jpg", "image/jpeg", new byte[]{1});
            mockMvc.perform(multipart(Routes.PRODUCT_MANAGEMENT + "/form")
                            .file(img1)
                            .param("name", "Beans")
                            .param("category", "coffee")
                            .param("status", "DRAFT")
                            .param("variantSku", "COF-250G")
                            .param("variantPriceAmount", "8.99")
                            // Missing currency/stock lists for size alignment
                            .characterEncoding(StandardCharsets.UTF_8))
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    void create_with_form_returns_400_on_invalid_numbers() throws Exception {
        try (var ignored = withRoles("ADMIN")) {
            mockMvc.perform(multipart(Routes.PRODUCT_MANAGEMENT + "/form")
                            .param("name", "Beans")
                            .param("category", "coffee")
                            .param("status", "DRAFT")
                            .param("variantSku", "COF-250G")
                            .param("variantPriceAmount", "not-a-number")
                            .param("variantPriceCurrency", "EUR")
                            .param("variantStockQuantity", "100")
                            .characterEncoding(StandardCharsets.UTF_8))
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    void create_with_form_returns_400_on_invalid_attributes_json() throws Exception {
        try (var ignored = withRoles("ADMIN")) {
            mockMvc.perform(multipart(Routes.PRODUCT_MANAGEMENT + "/form")
                            .param("name", "Beans")
                            .param("category", "coffee")
                            .param("status", "DRAFT")
                            .param("variantSku", "COF-250G")
                            .param("variantPriceAmount", "8.99")
                            .param("variantPriceCurrency", "EUR")
                            .param("variantStockQuantity", "100")
                            .param("variantAttributes", "{ not-an-array }")
                            .characterEncoding(StandardCharsets.UTF_8))
                    .andExpect(status().isBadRequest());
        }
    }
}
