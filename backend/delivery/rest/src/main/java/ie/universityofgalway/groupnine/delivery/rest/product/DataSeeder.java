package ie.universityofgalway.groupnine.delivery.rest.product;

import ie.universityofgalway.groupnine.infrastructure.product.ProductEntity;
import ie.universityofgalway.groupnine.infrastructure.product.ProductJpaRepository;
import ie.universityofgalway.groupnine.infrastructure.product.VariantEntity;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.util.List;

@Component // TODO: Remove this file once Ella completes her story.
public class DataSeeder implements CommandLineRunner {

    private final ProductJpaRepository productRepository;

    public DataSeeder(ProductJpaRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public void run(String... args) {
        // This check prevents adding data every time the server restarts.
        if (productRepository.count() == 0) {
            System.out.println("Database is empty. Seeding initial product data...");
            createSampleProducts();
            System.out.println("Finished seeding data.");
        } else {
            System.out.println("Database already contains data. Skipping seed.");
        }
    }

    private void createSampleProducts() {
        // Product 1: Espresso
        ProductEntity espresso = new ProductEntity();
        espresso.setName("Classic Espresso");
        espresso.setDescription("A rich and aromatic shot of pure coffee excellence, the perfect pick-me-up.");
        espresso.setCategory("Coffee");

        VariantEntity espressoSingle = new VariantEntity();
        espressoSingle.setSku("CFE-ESP-SGL");
        espressoSingle.setPriceCents(250); // Represents €2.50
        espressoSingle.setStockQuantity(100);
        espressoSingle.setAvailable(true);
        espressoSingle.setProduct(espresso); // Link variant to the product
        espresso.getVariants().add(espressoSingle);

        // Product 2: Latte
        ProductEntity latte = new ProductEntity();
        latte.setName("Creamy Latte");
        latte.setDescription("Smooth espresso with steamed milk, topped with a light layer of foam.");
        latte.setCategory("Coffee");

        VariantEntity latteRegular = new VariantEntity();
        latteRegular.setSku("CFE-LAT-REG");
        latteRegular.setPriceCents(380); // Represents €3.80
        latteRegular.setStockQuantity(100);
        latteRegular.setAvailable(true);
        latteRegular.setProduct(latte);
        latte.getVariants().add(latteRegular);
        
        // Product 3: Croissant
        ProductEntity croissant = new ProductEntity();
        croissant.setName("Butter Croissant");
        croissant.setDescription("A flaky, buttery pastry, perfect for pairing with any coffee.");
        croissant.setCategory("Pastries");
        
        VariantEntity croissantPlain = new VariantEntity();
        croissantPlain.setSku("PST-CRS-PLN");
        croissantPlain.setPriceCents(220); // Represents €2.20
        croissantPlain.setStockQuantity(50);
        croissantPlain.setAvailable(true);
        croissantPlain.setProduct(croissant);
        croissant.getVariants().add(croissantPlain);

        // Save all the new products to the database in one operation
        productRepository.saveAll(List.of(espresso, latte, croissant));
    }
}
