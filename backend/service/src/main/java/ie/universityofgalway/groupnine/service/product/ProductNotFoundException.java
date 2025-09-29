package ie.universityofgalway.groupnine.service.product;


public class ProductNotFoundException extends RuntimeException {
  /**
   * Creates an exception for a missing product.
   *
   * @param id the identifier value used in the lookup (e.g., database ID or UUID string)
   */
  public ProductNotFoundException(Long id) { super("Product " + id + " not found"); }
}

