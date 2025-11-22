package ie.universityofgalway.groupnine.service.product.admin.usecase;

import ie.universityofgalway.groupnine.domain.product.ProductId;
import ie.universityofgalway.groupnine.domain.product.VariantId;
import ie.universityofgalway.groupnine.service.product.port.ImageStoragePort;
import ie.universityofgalway.groupnine.service.product.port.ProductPort;

import java.io.IOException;
import java.io.InputStream;
import java.util.NoSuchElementException;

/**
 * Use case for uploading/saving a product variant image.
 */
public class UploadVariantImageUseCase {

    private final ProductPort products;
    private final ImageStoragePort images;

    public UploadVariantImageUseCase(ProductPort products, ImageStoragePort images) {
        this.products = products;
        this.images = images;
    }

    /**
     * Validates product & variant existence and stores the image via the storage port.
     */
    public void execute(ProductId productId,
                        VariantId variantId,
                        String originalFilename,
                        String contentType,
                        InputStream data) throws IOException {
        products.findById(productId).orElseThrow(() -> new NoSuchElementException("Product not found"));
        products.findVariantById(variantId).orElseThrow(() -> new NoSuchElementException("Variant not found"));
        images.saveVariantImage(productId, variantId, originalFilename, contentType, data);
    }
}

