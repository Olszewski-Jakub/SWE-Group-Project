package ie.universityofgalway.groupnine.delivery.rest.product;

import ie.universityofgalway.groupnine.delivery.rest.product.dto.ProductManagementResponse;
import ie.universityofgalway.groupnine.delivery.rest.product.dto.ProductRequest;
import ie.universityofgalway.groupnine.delivery.rest.product.dto.VariantRequest;
import ie.universityofgalway.groupnine.delivery.rest.product.dto.VariantManagementResponse;
import ie.universityofgalway.groupnine.domain.product.Money;
import ie.universityofgalway.groupnine.domain.product.Attribute;
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
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @RequestParam(name = "variantAttributes", required = false) String variantAttributesJson
    ) throws Exception {
        // Basic list size validation
        int n = variantSku.size();
        if (variantPriceAmount.size() != n || variantPriceCurrency.size() != n || variantStockQuantity.size() != n) {
            return ResponseEntity.badRequest().build();
        }
        // Parse optional attributes JSON aligned per variant
        List<List<Attribute>> parsedAttrs;
        try {
            parsedAttrs = parseVariantAttributesJson(variantAttributesJson, n);
        } catch (IllegalArgumentException ex) {
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
                    (parsedAttrs.size() > i && parsedAttrs.get(i) != null) ? parsedAttrs.get(i) : java.util.List.of(),
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

    // Allowed attribute keys and values (constrained for admin form)
    private static final java.util.Set<String> ALLOWED_KEYS = java.util.Set.of(
            "roast", "origin", "grind", "size_ml", "caffeine", "weight_g", "count"
    );
    private static final java.util.regex.Pattern CUSTOM_KEY_PATTERN = java.util.regex.Pattern.compile("^[a-z0-9_]{1,30}$");
    private static final java.util.Map<String, java.util.Set<String>> ALLOWED_VALUES = java.util.Map.of(
            "roast", java.util.Set.of("light", "medium", "dark"),
            "origin", java.util.Set.of("brazil", "colombia", "ethiopia", "kenya"),
            "grind", java.util.Set.of("whole_beans", "espresso", "filter", "french_press"),
            "size_ml", java.util.Set.of("250", "350", "500", "1000"),
            "caffeine", java.util.Set.of("regular", "decaf")
    );

    /**
     * Parses the optional variantAttributes JSON array into a per-variant list of Attribute pairs.
     * Enforces allowed keys and values. Numeric values are coerced to strings.
     *
     * Expected shape (array of objects, aligned by variant index):
     *   [ {"roast":"medium","origin":"Brazil","weight_g":"250","count":"10"}, ... ]
     */
    private List<List<Attribute>> parseVariantAttributesJson(String json, int expected) throws Exception {
        java.util.List<java.util.List<Attribute>> result = new java.util.ArrayList<>();
        for (int i = 0; i < expected; i++) result.add(java.util.List.of());
        if (json == null || json.isBlank()) return result;

        ObjectMapper mapper = new ObjectMapper();
        com.fasterxml.jackson.databind.JsonNode root;
        try {
            root = mapper.readTree(json);
        } catch (Exception e) {
            throw new IllegalArgumentException("variantAttributes must be a valid JSON array", e);
        }
        if (!root.isArray()) throw new IllegalArgumentException("variantAttributes must be an array");

        int idx = 0;
        for (com.fasterxml.jackson.databind.JsonNode node : root) {
            if (idx >= expected) break; // ignore extras
            if (!node.isObject()) { idx++; continue; }
            java.util.List<Attribute> attrs = new java.util.ArrayList<>();
            java.util.Iterator<String> fields = node.fieldNames();
            while (fields.hasNext()) {
                String rawKey = fields.next();
                String key = rawKey == null ? null : rawKey.trim();
                if (key == null || key.isEmpty()) continue;
                // Normalize custom keys to snake_case alphanumeric for consistency
                String normKey = key.toLowerCase(java.util.Locale.ROOT).replaceAll("[^a-z0-9_]", "_");
                // Collapse multiple underscores
                normKey = normKey.replaceAll("_+", "_");
                // Trim leading/trailing underscores
                normKey = normKey.replaceAll("^_+|_+$", "");
                if (normKey.isEmpty() || normKey.length() > 30) {
                    throw new IllegalArgumentException("Invalid attribute key: " + key);
                }
                boolean isKnownKey = ALLOWED_KEYS.contains(normKey);
                if (!isKnownKey && !CUSTOM_KEY_PATTERN.matcher(normKey).matches()) {
                    throw new IllegalArgumentException("Invalid attribute key: " + key);
                }
                com.fasterxml.jackson.databind.JsonNode valNode = node.get(rawKey);
                // Coerce to array for uniform handling
                java.util.List<String> values = new java.util.ArrayList<>();
                if (valNode == null || valNode.isNull()) continue;
                if (valNode.isArray()) {
                    valNode.forEach(el -> values.add(el.asText()));
                } else {
                    values.add(valNode.asText());
                }
                for (String vRaw : values) {
                    String v = vRaw == null ? "" : vRaw.trim();
                    if (v.isEmpty()) continue;
                    // Validate categorical values (case-insensitive)
                    if (ALLOWED_VALUES.containsKey(normKey)) {
                        String lv = v.toLowerCase();
                        if (!ALLOWED_VALUES.get(normKey).contains(lv)) {
                            throw new IllegalArgumentException("Invalid value for " + normKey + ": " + v);
                        }
                        attrs.add(new Attribute(normKey, lv));
                    } else if (normKey.equals("weight_g") || normKey.equals("count")) {
                        // Numeric positive values
                        if (!v.matches("^\\d+$")) {
                            throw new IllegalArgumentException("Invalid numeric value for " + normKey + ": " + v);
                        }
                        if (Integer.parseInt(v) <= 0) {
                            throw new IllegalArgumentException(normKey + " must be > 0");
                        }
                        attrs.add(new Attribute(normKey, v));
                    } else {
                        // Custom key: accept normalized key
                        attrs.add(new Attribute(normKey, v));
                    }
                }
            }
            result.set(idx, java.util.List.copyOf(attrs));
            idx++;
        }
        return result;
    }
}
