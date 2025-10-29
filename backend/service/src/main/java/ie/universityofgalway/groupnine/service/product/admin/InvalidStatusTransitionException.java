package ie.universityofgalway.groupnine.service.product.admin;

import ie.universityofgalway.groupnine.domain.product.ProductStatus;

public class InvalidStatusTransitionException extends RuntimeException {
    public InvalidStatusTransitionException(ProductStatus from, ProductStatus to) {
        super("Invalid status transition: " + from + " -> " + to);
    }
}

