package ie.universityofgalway.groupnine.service.product.admin.usecase;

import ie.universityofgalway.groupnine.domain.product.Product;
import ie.universityofgalway.groupnine.domain.product.ProductId;
import ie.universityofgalway.groupnine.service.product.port.ProductPort;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class DeleteAndListProductUseCasesTest {

    @Test
    void delete_throws_when_not_found() {
        ProductPort port = Mockito.mock(ProductPort.class);
        DeleteProductUseCase uc = new DeleteProductUseCase(port);
        assertThrows(java.util.NoSuchElementException.class, () -> uc.execute(new ProductId(UUID.randomUUID())));
    }

    @Test
    void list_delegates_to_port() {
        ProductPort port = Mockito.mock(ProductPort.class);
        when(port.listAll(any())).thenReturn(new PageImpl<>(java.util.List.of()));
        ListProductsUseCase uc = new ListProductsUseCase(port);
        Page<Product> page = uc.execute(PageRequest.of(0, 5));
        assertNotNull(page);
    }
}

