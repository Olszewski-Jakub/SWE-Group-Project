package ie.universityofgalway.groupnine.integration.config;

import ie.universityofgalway.groupnine.service.product.admin.usecase.UploadVariantImageUseCase;
import ie.universityofgalway.groupnine.service.product.port.ImageStoragePort;
import ie.universityofgalway.groupnine.service.product.port.ProductPort;
import ie.universityofgalway.groupnine.service.product.usecase.GetVariantImageUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProductImageConfig {

    @Bean
    public UploadVariantImageUseCase uploadVariantImageUseCase(ProductPort products, ImageStoragePort images) {
        return new UploadVariantImageUseCase(products, images);
    }

    @Bean
    public GetVariantImageUseCase getVariantImageUseCase(ProductPort products, ImageStoragePort images) {
        return new GetVariantImageUseCase(products, images);
    }
}

