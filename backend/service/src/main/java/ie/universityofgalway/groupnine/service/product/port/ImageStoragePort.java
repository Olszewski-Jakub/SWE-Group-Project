package ie.universityofgalway.groupnine.service.product.port;

import ie.universityofgalway.groupnine.domain.product.ProductId;
import ie.universityofgalway.groupnine.domain.product.VariantId;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

/**
 * Port for storing and loading product/variant images.
 * Implementations live in the infrastructure module.
 */
public interface ImageStoragePort {

    /**
     * Saves the image for a given product variant to persistent storage.
     * Allowed content types are validated by the implementation.
     *
     * @param productId the product id
     * @param variantId the variant id
     * @param originalFilename original filename (used to derive extension)
     * @param contentType mime type, e.g. image/png
     * @param data image byte stream
     * @throws IOException when persisting fails
     * @throws IllegalArgumentException when contentType or filename is invalid
     */
    void saveVariantImage(ProductId productId,
                          VariantId variantId,
                          String originalFilename,
                          String contentType,
                          InputStream data) throws IOException;

    /**
     * Loads the image data for a given product variant.
     *
     * @param productId the product id
     * @param variantId the variant id
     * @return optional image data if present
     * @throws IOException on read error
     */
    Optional<ImageData> loadVariantImage(ProductId productId, VariantId variantId) throws IOException;

    /**
     * Simple container for in-memory image content and metadata.
     */
    final class ImageData {
        private final byte[] bytes;
        private final String contentType;

        public ImageData(byte[] bytes, String contentType) {
            this.bytes = bytes;
            this.contentType = contentType;
        }

        public byte[] getBytes() { return bytes; }
        public String getContentType() { return contentType; }
    }
}

