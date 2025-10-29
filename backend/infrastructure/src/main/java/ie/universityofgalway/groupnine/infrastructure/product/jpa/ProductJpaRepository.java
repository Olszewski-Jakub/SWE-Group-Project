package ie.universityofgalway.groupnine.infrastructure.product.jpa;

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

  /**
   * Searches for products with optional text, category, and variant-level filters.
   *
   * <p>Behavior:</p>
   * <ul>
   *   <li><strong>Category:</strong> Ignored when {@code null} or blank; matched using case-insensitive equality
   *       against the product category.</li>
   *   <li><strong>Keyword (key):</strong> Ignored when {@code null} or blank; matches when either:
   *       <ul>
   *         <li>The product name or description contains the keyword (using {@code ILIKE}), or</li>
   *         <li>The trigram similarity exceeds {@code simCutoff} (via {@code pg_trgm}).</li>
   *       </ul>
   *   </li>
   *   <li><strong>Price bounds:</strong> A product is included if <em>at least one</em> variant has
   *       {@code priceCents >= minPrice} (when provided) and {@code priceCents <= maxPrice} (when provided).</li>
   *   <li><strong>Attributes (JSONB):</strong> Ignored when {@code attrJson} is {@code null}.
   *       For every requested key in {@code attrJson}:
   *       <ul>
   *         <li>The variant must contain that key.</li>
   *         <li>At least one requested value must match the variant’s value(s), treating both scalars and arrays uniformly.
   *             Comparisons are case-insensitive.</li>
   *       </ul>
   *   </li>
   *   <li><strong>Results:</strong> Returns distinct products using {@code GROUP BY p.id};
   *       sorting can use minimum or maximum variant price per product, depending on {@code sort}.</li>
   * </ul>
   *
   * @param category   optional category filter (case-insensitive); ignored if {@code null} or blank
   * @param key        optional search keyword for name or description; ignored if {@code null} or blank
   * @param minPrice   optional minimum variant price in cents; ignored if {@code null}
   * @param maxPrice   optional maximum variant price in cents; ignored if {@code null}
   * @param sort       sort rule controlling price ordering (ascending or descending); other values leave natural order
   * @param simCutoff  similarity threshold used with trigram matching
   * @param attrJson   JSON string representing attribute filters; {@code null} to skip JSONB comparison
   * @param pageable   pagination and sorting configuration
   * @return a {@link Page} of {@link ProductEntity} records matching all applied filters
   */
  @Query(
    value = """
    SELECT p.*
    FROM products p
    JOIN product_variants v ON v.product_id = p.id
    WHERE (:category IS NULL OR :category = '' OR LOWER(p.category) = LOWER(:category))
      AND (
        :key IS NULL OR :key = ''
        OR p.name ILIKE '%' || :key || '%' ESCAPE '\'
        OR p.description ILIKE '%' || :key || '%' ESCAPE '\'
        OR similarity(p.name, :key) > :simCutoff
        OR similarity(p.description, :key) > :simCutoff
      )
      AND (:minPrice IS NULL OR v.price_cents >= :minPrice)
      AND (:maxPrice IS NULL OR v.price_cents <= :maxPrice)
      AND (
        :attr IS NULL
        OR NOT EXISTS (
          SELECT 1
          FROM jsonb_object_keys(CAST(:attr AS jsonb)) AS reqk(key)
          WHERE NOT (
            jsonb_exists(v.attributes, reqk.key)
            AND EXISTS (
              SELECT 1
              FROM jsonb_array_elements_text(
                     CASE
                       WHEN jsonb_typeof(v.attributes -> reqk.key) = 'array'
                         THEN v.attributes -> reqk.key
                       ELSE jsonb_build_array(v.attributes -> reqk.key)
                     END
                   ) AS cand(val)
              JOIN jsonb_array_elements_text(CAST(:attr AS jsonb) -> reqk.key) AS req(val2)
                ON lower(cand.val) = lower(req.val2)
            )
          )
        )
      )
    GROUP BY p.id
    ORDER BY
      CASE WHEN :sort = 'PRICE_LOW_TO_HIGH' THEN MIN(v.price_cents) END ASC,
      CASE WHEN :sort = 'PRICE_HIGH_TO_LOW' THEN MAX(v.price_cents) END DESC
    """,
    countQuery = """
    SELECT COUNT(DISTINCT p.id)
    FROM products p
    JOIN product_variants v ON v.product_id = p.id
    WHERE (:category IS NULL OR :category = '' OR lower(p.category) = lower(:category))
      AND (
        :key IS NULL OR :key = ''
        OR p.name ILIKE '%' || :key || '%'
        OR p.description ILIKE '%' || :key || '%'
        OR similarity(p.name, :key) > :simCutoff
        OR similarity(p.description, :key) > :simCutoff
      )
      AND (:minPrice IS NULL OR v.price_cents >= :minPrice)
      AND (:maxPrice IS NULL OR v.price_cents <= :maxPrice)
      AND (
        :attr IS NULL
        OR NOT EXISTS (
          SELECT 1
          FROM jsonb_object_keys(CAST(:attr AS jsonb)) AS reqk(key)
          WHERE NOT (
            jsonb_exists(v.attributes, reqk.key)
            AND EXISTS (
              SELECT 1
              FROM jsonb_array_elements_text(
                     CASE
                       WHEN jsonb_typeof(v.attributes -> reqk.key) = 'array'
                         THEN v.attributes -> reqk.key
                       ELSE jsonb_build_array(v.attributes -> reqk.key)
                     END
                   ) AS cand(val)
              JOIN jsonb_array_elements_text(CAST(:attr AS jsonb) -> reqk.key) AS req(val2)
                ON lower(cand.val) = lower(req.val2)
            )
          )
        )
      )
    """,
    nativeQuery = true)
  Page<ProductEntity> search(@Param("category") String category,
                             @Param("key") String key,
                             @Param("minPrice") Integer minPrice,
                             @Param("maxPrice") Integer maxPrice,
                             @Param("sort") String sort,
                             @Param("simCutoff") Double simCutoff,
                             @Param("attr") String attrJson,
                             Pageable pageable);


  /**
   * Finds a product by its public UUID identifier.
   * @param uuid The UUID of the product.
   * @return An {@link Optional} containing the found product or empty if not found.
   */
  Optional<ProductEntity> findByUuid(UUID uuid); 

  boolean existsByUuid(UUID uuid);
}
