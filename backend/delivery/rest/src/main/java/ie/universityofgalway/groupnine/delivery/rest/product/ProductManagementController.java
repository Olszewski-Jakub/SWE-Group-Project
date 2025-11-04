package ie.universityofgalway.groupnine.delivery.rest.product;

import ie.universityofgalway.groupnine.delivery.rest.product.dto.ProductManagementResponse;
import ie.universityofgalway.groupnine.delivery.rest.product.dto.ProductRequest;
import ie.universityofgalway.groupnine.delivery.rest.product.dto.VariantRequest;
import ie.universityofgalway.groupnine.delivery.rest.product.dto.VariantManagementResponse;
import ie.universityofgalway.groupnine.domain.product.Product;
import ie.universityofgalway.groupnine.domain.product.ProductId;
import ie.universityofgalway.groupnine.domain.product.Variant;
import ie.universityofgalway.groupnine.domain.product.VariantId;
import ie.universityofgalway.groupnine.domain.security.RequireRoles;
import ie.universityofgalway.groupnine.domain.user.Role;
import ie.universityofgalway.groupnine.service.product.admin.usecase.*;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

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

    public ProductManagementController(CreateProductUseCase createProduct,
                                       GetProductUseCase getProduct,
                                       UpdateProductUseCase updateProduct,
                                       DeleteProductUseCase deleteProduct,
                                       AddVariantUseCase addVariant,
                                       UpdateVariantUseCase updateVariant,
                                       DeleteVariantUseCase deleteVariant,
                                       ListProductsUseCase listProducts) {
        this.createProduct = createProduct;
        this.getProduct = getProduct;
        this.updateProduct = updateProduct;
        this.deleteProduct = deleteProduct;
        this.addVariant = addVariant;
        this.updateVariant = updateVariant;
        this.deleteVariant = deleteVariant;
        this.listProducts = listProducts;
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
}
