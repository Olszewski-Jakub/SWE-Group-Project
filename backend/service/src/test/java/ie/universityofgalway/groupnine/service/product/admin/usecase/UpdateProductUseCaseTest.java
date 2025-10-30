package ie.universityofgalway.groupnine.service.product.admin.usecase;

import ie.universityofgalway.groupnine.domain.product.*;
import ie.universityofgalway.groupnine.service.product.admin.InvalidStatusTransitionException;
import ie.universityofgalway.groupnine.service.product.admin.UpdateProductCommand;
import ie.universityofgalway.groupnine.service.product.port.ProductPort;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class UpdateProductUseCaseTest {

    @Test
    void invalid_transition_archived_to_active_throws() {
        ProductPort port = Mockito.mock(ProductPort.class);
        Product existing = new Product(new ProductId(UUID.randomUUID()), "n", "d", "c", ProductStatus.ARCHIVED, java.util.List.of(), Instant.now(), Instant.now());
        when(port.findById(existing.getId())).thenReturn(Optional.of(existing));

        UpdateProductUseCase uc = new UpdateProductUseCase(port);
        UpdateProductCommand cmd = new UpdateProductCommand(existing.getId(), null, null, null, ProductStatus.ACTIVE);
        assertThrows(InvalidStatusTransitionException.class, () -> uc.execute(cmd));
    }

    @Test
    void updates_and_saves_with_trimmed_fields() {
        ProductPort port = Mockito.mock(ProductPort.class);
        Product existing = new Product(new ProductId(UUID.randomUUID()), "Name", "d", "Cat", ProductStatus.ACTIVE, java.util.List.of(), Instant.now(), Instant.now());
        when(port.findById(existing.getId())).thenReturn(Optional.of(existing));
        when(port.saveProduct(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateProductUseCase uc = new UpdateProductUseCase(port);
        UpdateProductCommand cmd = new UpdateProductCommand(existing.getId(), " New ", "desc", " Cat2 ", ProductStatus.ACTIVE);
        Product updated = uc.execute(cmd);
        assertEquals("New", updated.getName());
        assertEquals("Cat2", updated.getCategory());
    }
}

