package ie.universityofgalway.groupnine.integration.config;

import ie.universityofgalway.groupnine.service.product.port.ProductPort;
import ie.universityofgalway.groupnine.service.product.admin.usecase.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AdminProductConfig {

    @Bean
    public CreateProductUseCase createProductUseCase(ProductPort port) { return new CreateProductUseCase(port); }

    @Bean
    public GetProductUseCase getProductUseCase(ProductPort port) { return new GetProductUseCase(port); }

    @Bean
    public UpdateProductUseCase updateProductUseCase(ProductPort port) { return new UpdateProductUseCase(port); }

    @Bean
    public DeleteProductUseCase deleteProductUseCase(ProductPort port) { return new DeleteProductUseCase(port); }

    @Bean
    public AddVariantUseCase addVariantUseCase(ProductPort port) { return new AddVariantUseCase(port); }

    @Bean
    public UpdateVariantUseCase updateVariantUseCase(ProductPort port) { return new UpdateVariantUseCase(port); }

    @Bean
    public DeleteVariantUseCase deleteVariantUseCase(ProductPort port) { return new DeleteVariantUseCase(port); }

    @Bean
    public ListProductsUseCase listProductsUseCase(ProductPort port) { return new ListProductsUseCase(port); }
}

