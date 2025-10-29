package ie.universityofgalway.groupnine.service.product;

import ie.universityofgalway.groupnine.domain.product.Product;
import ie.universityofgalway.groupnine.domain.product.ProductId;
import ie.universityofgalway.groupnine.service.product.port.ProductPort;
import ie.universityofgalway.groupnine.service.product.usecase.ProductService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ProductServiceTest {

    @Test
    void listDelegatesToPort() {
        ProductPort port = Mockito.mock(ProductPort.class);
        Mockito.when(port.findAvailable(Mockito.any())).thenReturn(new PageImpl<>(List.of()));
        Mockito.when(port.findAvailableByCategory(Mockito.anyString(), Mockito.any())).thenReturn(new PageImpl<>(List.of()));
        ProductService svc = new ProductService(port);
        assertNotNull(svc.list(0, 10, null));
        assertNotNull(svc.list(0, 10, "coffee"));
    }

    @Test
    void getByIdParsesUuidAndThrowsWhenMissing() {
        ProductPort port = Mockito.mock(ProductPort.class);
        Mockito.when(port.findById(Mockito.any())).thenReturn(Optional.empty());
        ProductService svc = new ProductService(port);
        assertThrows(IllegalArgumentException.class, () -> svc.getById(" "));
        assertThrows(IllegalArgumentException.class, () -> svc.getById("not-a-uuid"));
        assertThrows(java.util.NoSuchElementException.class, () -> svc.getById(UUID.randomUUID().toString()));
    }
}
