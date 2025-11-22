package ie.universityofgalway.groupnine.delivery.rest.product;

import ie.universityofgalway.groupnine.delivery.rest.product.dto.ProductManagementResponse;
import ie.universityofgalway.groupnine.delivery.rest.product.dto.ProductRequest;
import ie.universityofgalway.groupnine.delivery.rest.product.dto.VariantRequest;
import ie.universityofgalway.groupnine.delivery.rest.product.dto.VariantManagementResponse;
import ie.universityofgalway.groupnine.domain.product.Money;
import ie.universityofgalway.groupnine.domain.product.Product;
import ie.universityofgalway.groupnine.domain.product.ProductId;
import ie.universityofgalway.groupnine.domain.product.Sku;
import ie.universityofgalway.groupnine.domain.product.Stock;
import ie.universityofgalway.groupnine.domain.product.Variant;
import ie.universityofgalway.groupnine.domain.product.VariantId;
import ie.universityofgalway.groupnine.domain.security.RequireRoles;
import ie.universityofgalway.groupnine.domain.user.Role;
import ie.universityofgalway.groupnine.service.product.admin.usecase.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import ie.universityofgalway.groupnine.service.product.admin.UpdateVariantCommand;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;
import java.util.List;
import java.math.BigDecimal;

import static ie.universityofgalway.groupnine.util.Routes.PRODUCT_MANAGEMENT;

@RestController
@RequestMapping(PRODUCT_MANAGEMENT)
@RequireRoles({Role.ADMIN, Role.MANAGER})
public class ProductManagementController {

    private final CreateProductUseCase createProduct;
    private final GetProductUseCase getProduct;
    private final UpdateProductUseCase updateProduct;
    private final DeleteProductUseCase deleteProduct;
    private final AddVariantUseCase addVariant;
    private final UpdateVariantUseCase updateVariant;
    private final DeleteVariantUseCase deleteVariant;
    private final ListProductsUseCase listProducts;
    private final UploadVariantImageUseCase uploadVariantImage;

    public ProductManagementController(CreateProductUseCase createProduct,
                                       GetProductUseCase getProduct,
                                       UpdateProductUseCase updateProduct,
                                       DeleteProductUseCase deleteProduct,
                                       AddVariantUseCase addVariant,
                                       UpdateVariantUseCase updateVariant,
                                       DeleteVariantUseCase deleteVariant,
                                       ListProductsUseCase listProducts,
                                       UploadVariantImageUseCase uploadVariantImage) {
        this.createProduct = createProduct;
        this.getProduct = getProduct;
        this.updateProduct = updateProduct;
        this.deleteProduct = deleteProduct;
        this.addVariant = addVariant;
        this.updateVariant = updateVariant;
        this.deleteVariant = deleteVariant;
        this.listProducts = listProducts;
        this.uploadVariantImage = uploadVariantImage;
    }

    @PostMapping
    public ResponseEntity<ProductManagementResponse> create(@RequestBody @Valid ProductRequest req) {
        Product created = createProduct.execute(ProductManagementDtoMapper.toDomain(req));
        ProductManagementResponse dto = ProductManagementDtoMapper.toDto(created);
        return ResponseEntity.created(URI.create(PRODUCT_MANAGEMENT + "/" + dto.getId())).body(dto);
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductManagementResponse> get(@PathVariable("productId") UUID productId) {
        return getProduct.byId(new ProductId(productId))
                .map(p -> ResponseEntity.ok(ProductManagementDtoMapper.toDto(p)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{productId}")
    public ResponseEntity<ProductManagementResponse> update(@PathVariable("productId") UUID productId, @RequestBody ProductRequest partial) {
        Product updated = updateProduct.execute(ProductManagementDtoMapper.toUpdateCommand(productId, partial));
        return ResponseEntity.ok(ProductManagementDtoMapper.toDto(updated));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> delete(@PathVariable("productId") UUID productId) {
        deleteProduct.execute(new ProductId(productId));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{productId}/variants")
    public ResponseEntity<VariantManagementResponse> addVariant(@PathVariable("productId") UUID productId, @RequestBody @Valid VariantRequest req) {
        Variant created = addVariant.execute(new ProductId(productId), ProductManagementDtoMapper.toDomain(req));
        return ResponseEntity.ok(ProductManagementDtoMapper.toDto(created));
    }

    @PatchMapping("/{productId}/variants/{variantId}")
    public ResponseEntity<VariantManagementResponse> updateVariant(@PathVariable("productId") UUID productId, @PathVariable("variantId") UUID variantId, @RequestBody VariantRequest req) {
        Variant updated = updateVariant.execute(new ProductId(productId), ProductManagementDtoMapper.toUpdateCommand(variantId, req));
        return ResponseEntity.ok(ProductManagementDtoMapper.toDto(updated));
    }

    @DeleteMapping("/{productId}/variants/{variantId}")
    public ResponseEntity<Void> deleteVariant(@PathVariable("productId") UUID productId, @PathVariable("variantId") UUID variantId) {
        deleteVariant.execute(new ProductId(productId), new VariantId(variantId));
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<org.springframework.data.domain.Page<ProductManagementResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<Product> rs = listProducts.execute(PageRequest.of(page, size));
        return ResponseEntity.ok(rs.map(ProductManagementDtoMapper::toDto));
    }

    /**
     * Pure form-data endpoint (no JSON) for creating a product with variants and optional images.
     * Expected fields:
     * - name, description (optional), category, status
     * - variantSku (repeatable)
     * - variantPriceAmount (repeatable)
     * - variantPriceCurrency (repeatable)
     * - variantStockQuantity (repeatable)
     * - variantStockReserved (repeatable, optional; defaults to 0)
     * - images (repeatable file parts, optional; order must correspond to variantSku)
     */
    @PostMapping(path = "/form", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductManagementResponse> createWithForm(
            @RequestParam(name = "name") String name,
            @RequestParam(name = "description", required = false) String description,
            @RequestParam(name = "category") String category,
            @RequestParam(name = "status") String status,
            @RequestParam(name = "variantSku") List<String> variantSku,
            @RequestParam(name = "variantPriceAmount") List<String> variantPriceAmount,
            @RequestParam(name = "variantPriceCurrency") List<String> variantPriceCurrency,
            @RequestParam(name = "variantStockQuantity") List<String> variantStockQuantity,
            @RequestParam(name = "variantStockReserved", required = false) List<String> variantStockReserved,
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) throws Exception {
        // Basic list size validation
        int n = variantSku.size();
        if (variantPriceAmount.size() != n || variantPriceCurrency.size() != n || variantStockQuantity.size() != n) {
            return ResponseEntity.badRequest().build();
        }
        // Build domain product from raw fields
        java.util.List<Variant> variants = new java.util.ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            String sku = variantSku.get(i);
            BigDecimal amount;
            int qty;
            int res = 0;
            try {
                amount = new BigDecimal(variantPriceAmount.get(i));
                qty = Integer.parseInt(variantStockQuantity.get(i));
                if (variantStockReserved != null && variantStockReserved.size() > i) {
                    String r = variantStockReserved.get(i);
                    if (r != null && !r.isBlank()) res = Integer.parseInt(r);
                }
            } catch (NumberFormatException ex) {
                return ResponseEntity.badRequest().build();
            }
            java.util.Currency cur = java.util.Currency.getInstance(variantPriceCurrency.get(i).toUpperCase());
            variants.add(new Variant(
                    null,
                    new Sku(sku),
                    new Money(amount, cur),
                    new Stock(qty, res),
                    java.util.List.of(),
                    null
            ));
        }

        Product toCreate = new Product(
                null,
                name,
                description,
                category,
                ie.universityofgalway.groupnine.domain.product.ProductStatus.valueOf(status.toUpperCase()),
                variants,
                null,
                null
        );

        Product created = createProduct.execute(toCreate);

        // Image upload stage: align by index (images[i] for variant i)
        if (images != null && !images.isEmpty()) {
            for (int i = 0; i < Math.min(images.size(), created.getVariants().size()); i++) {
                MultipartFile file = images.get(i);
                if (file == null || file.isEmpty()) continue;
                Variant v = created.getVariants().get(i);
                uploadVariantImage.execute(created.getId(), v.getId(), file.getOriginalFilename(), file.getContentType(), file.getInputStream());
                String url = "/products/" + created.getId().getId() + "/variants/" + v.getId().getId() + "/image";
                updateVariant.execute(created.getId(), new UpdateVariantCommand(v.getId(), null, null, null, null, null, url, null));
            }
            created = getProduct.byId(created.getId()).orElse(created);
        }

        ProductManagementResponse dto = ProductManagementDtoMapper.toDto(created);
        return ResponseEntity.created(URI.create(PRODUCT_MANAGEMENT + "/" + dto.getId())).body(dto);
    }
}
