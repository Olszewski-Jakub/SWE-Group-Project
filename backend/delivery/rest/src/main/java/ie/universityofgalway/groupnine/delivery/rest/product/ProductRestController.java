package ie.universityofgalway.groupnine.delivery.rest.product;

import ie.universityofgalway.groupnine.delivery.rest.product.dto.PageResponse;
import ie.universityofgalway.groupnine.delivery.rest.product.dto.ProductResponse;
import ie.universityofgalway.groupnine.domain.product.Product;
import ie.universityofgalway.groupnine.service.product.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.OffsetDateTime;
import java.util.NoSuchElementException;

/**
 * REST controller exposing read-only product endpoints.
 */
@Validated
@RestController
@RequestMapping("/api/products")
public class ProductRestController {

  private final ProductService svc;
  /**
   * Creates the controller.
   */
  public ProductRestController(ProductService svc) {
    this.svc = svc;
  }

  /**
   * Lists available products, optionally filtered by category.
   */
  @Operation(summary = "List products with optional category filter")
  @GetMapping
  public ResponseEntity<PageResponse<ProductResponse>> list(
      @RequestParam(defaultValue = "0") @Min(value = 0, message = "page must be >= 0") int page,
      @RequestParam(defaultValue = "10") @Positive(message = "size must be > 0") int size,
      @RequestParam(required = false) String category
  ) {
    Page<Product> rs = svc.list(page, size, category);
    var dto = rs.map(ProductDtoMapper::toDto);
    return ResponseEntity.ok(new PageResponse<>(
        dto.getContent(), dto.getNumber(), dto.getSize(), dto.getTotalElements(), dto.getTotalPages()
    ));
  }

  /**
   * Retrieves a single product by its public UUID string identifier.
   */
  @Operation(summary = "Get a product by id")
  @GetMapping("/{id}")
  public ProductResponse byId(@PathVariable String id) {
    return ProductDtoMapper.toDto(svc.getById(id));
  }


  @ExceptionHandler(NoSuchElementException.class)
  public ResponseEntity<ApiError> handleNotFound(NoSuchElementException ex) {
    return build(HttpStatus.NOT_FOUND, "NOT_FOUND", ex.getMessage());
  }

  @ExceptionHandler({
      IllegalArgumentException.class,
      MethodArgumentTypeMismatchException.class,
      MissingServletRequestParameterException.class
  })
  public ResponseEntity<ApiError> handleBadRequest(Exception ex) {
    return build(HttpStatus.BAD_REQUEST, "BAD_REQUEST", ex.getMessage());
  }

  @ExceptionHandler({ ConstraintViolationException.class })
  public ResponseEntity<ApiError> handleConstraintViolation(ConstraintViolationException ex) {
    String msg = ex.getConstraintViolations().stream()
        .map(v -> v.getPropertyPath() + ": " + v.getMessage())
        .reduce((a, b) -> a + "; " + b)
        .orElse("Validation failed");
    return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", msg);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiError> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
    String msg = ex.getBindingResult().getFieldErrors().stream()
        .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
        .reduce((a, b) -> a + "; " + b)
        .orElse("Validation failed");
    return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", msg);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiError> handleUnknown(Exception ex) {
    return build(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "An unexpected error occurred");
  }

  private ResponseEntity<ApiError> build(HttpStatus status, String code, String message) {
    ApiError body = new ApiError(
        status.value(),
        code,
        message == null || message.isBlank() ? status.getReasonPhrase() : message,
        OffsetDateTime.now().toString()
    );
    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.CONTENT_TYPE, "application/json");
    return new ResponseEntity<>(body, headers, status);
  }

  private record ApiError(int status, String code, String message, String timestamp) {}
}
