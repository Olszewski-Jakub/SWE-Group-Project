package ie.universityofgalway.groupnine.infrastructure.product;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

  // TO DO: attributes filtering
  /**
   * Searches products with optional text and category filters,
   * and ensures at least one variant satisfies the provided price bounds.
   * Behavior:
   *  Category: ignored when null/blank; case-insensitive equality against product category.
   *  Key: ignored when null/blank; case-insensitive substring match on name or description.
   *  Price bounds: a product is included if there exists at least one variant whose priceCents
   *    is >= minPrice (when provided) and <= maxPrice (when provided).
   *  Uses EXISTS to avoid row duplication from joins; returns distinct products only.
   * Parameters:
   * @param category   optional category filter; case-insensitive; null/blank to ignore
   * @param key        optional keyword filter for name/description; null/blank to ignore
   * @param minPrice   optional minimum variant price in cents; null to ignore lower bound
   * @param maxPrice   optional maximum variant price in cents; null to ignore upper bound
   * @param pageable   pagination and sorting information
   * @return a page of ProductEntity records that match the filters
   */
  @Query("""
    SELECT p
    FROM ProductEntity p
    WHERE (:category IS NULL OR :category = '' OR LOWER(p.category) = LOWER(:category))
      AND (:key IS NULL OR :key = '' OR
           LOWER(p.name) LIKE CONCAT('%', LOWER(:key), '%') OR
           LOWER(p.description) LIKE CONCAT('%', LOWER(:key), '%'))
      AND EXISTS (
           SELECT 1
           FROM VariantEntity v
           WHERE v.product = p
             AND (:minPrice IS NULL OR v.priceCents >= :minPrice)
             AND (:maxPrice IS NULL OR v.priceCents <= :maxPrice)
      )
""")
  Page<ProductEntity> search(@Param("category") String category,
                             @Param("key") String key,
                             @Param("minPrice") Integer minPrice,
                             @Param("maxPrice") Integer maxPrice,
                             Pageable pageable);


  /**
   * Finds a product by its public UUID identifier.
   * @param uuid The UUID of the product.
   * @return An {@link Optional} containing the found product or empty if not found.
   */
  Optional<ProductEntity> findByUuid(UUID uuid); 
}