package ie.universityofgalway.groupnine.delivery.rest.product.dto;


import java.util.List;
/**
 * Standard page wrapper used by REST endpoints to return paginated results.
 */
public record PageResponse<T>(
  List<T> content, int page, int size, long totalElements, int totalPages
) {}

