package ie.universityofgalway.groupnine.infrastructure.product;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * Spring Data JPA repository for {@link ProductEntity}.
 * Provides CRUD operations and custom queries for products.
 */
public interface ProductJpaRepository extends JpaRepository<ProductEntity, Long> {

  /**
   * Finds a page of products that have at least one available variant.
   * @param pageable The pagination information.
   * @return A {@link Page} of available products.
   */
  @Query("SELECT DISTINCT p FROM ProductEntity p JOIN p.variants v WHERE v.available = true")
  Page<ProductEntity> findByAvailableTrue(Pageable pageable);

  /**
   * Finds a page of products in a given category that have at least one available variant.
   * @param category The product category to search for (case-insensitive).
   * @param pageable The pagination information.
   * @return A {@link Page} of available products in the specified category.
   */
  @Query("SELECT DISTINCT p FROM ProductEntity p JOIN p.variants v WHERE LOWER(p.category) = LOWER(:category) AND v.available = true")
  Page<ProductEntity> findByCategoryIgnoreCaseAndAvailableTrue(String category, Pageable pageable);

  /**
   * Finds a product by its public UUID identifier.
   * @param uuid The UUID of the product.
   * @return An {@link Optional} containing the found product or empty if not found.
   */
  Optional<ProductEntity> findByUuid(UUID uuid); 
}