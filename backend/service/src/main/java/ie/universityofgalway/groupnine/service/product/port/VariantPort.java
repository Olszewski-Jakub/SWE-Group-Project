package ie.universityofgalway.groupnine.service.product.port;

import ie.universityofgalway.groupnine.domain.product.Variant;
import ie.universityofgalway.groupnine.domain.product.VariantId;

import java.util.Optional;

public interface VariantPort {
    Optional<Variant> findById(VariantId id);
}