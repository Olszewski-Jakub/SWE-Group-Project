package ie.universityofgalway.groupnine.service.product.usecase;

import ie.universityofgalway.groupnine.domain.product.ProductId;
import ie.universityofgalway.groupnine.domain.product.VariantId;
import ie.universityofgalway.groupnine.service.product.port.ImageStoragePort;
import ie.universityofgalway.groupnine.service.product.port.ImageStoragePort.ImageData;
import ie.universityofgalway.groupnine.service.product.port.ProductPort;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * Use case for retrieving a variant image from storage.
 */
public class GetVariantImageUseCase {
    private final ProductPort products;
    private final ImageStoragePort images;

    public GetVariantImageUseCase(ProductPort products, ImageStoragePort images) {
        this.products = products;
        this.images = images;
    }

    public Optional<ImageData> execute(ProductId productId, VariantId variantId) throws IOException {
        // Validate IDs exist to avoid exposing arbitrary files
        products.findById(productId).orElseThrow(() -> new NoSuchElementException("Product not found"));
        products.findVariantById(variantId).orElseThrow(() -> new NoSuchElementException("Variant not found"));
        return images.loadVariantImage(productId, variantId);
    }
}

