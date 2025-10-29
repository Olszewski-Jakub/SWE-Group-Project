package ie.universityofgalway.groupnine.infrastructure.cart;

import ie.universityofgalway.groupnine.domain.cart.*;
import ie.universityofgalway.groupnine.domain.product.*;
import ie.universityofgalway.groupnine.domain.user.UserId;
import ie.universityofgalway.groupnine.service.product.VariantPort;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the {@link CartPersistenceAdapter}.
 */
class CartPersistenceAdapterTest {

    private CartJpaRepository repository;
    private VariantPort variantPort;
    private CartPersistenceAdapter adapter;

    private Variant variant1;
    private CartItem cartItem;
    private CartId cartId;
    private UserId userId;

    /**
     * Sets up mock dependencies and reusable test data before each test.
     */
    @BeforeEach
    void setUp() {
        repository = mock(CartJpaRepository.class);
        variantPort = mock(VariantPort.class);
        adapter = new CartPersistenceAdapter(repository, variantPort);

        cartId = new CartId(UUID.randomUUID());
        userId = UserId.of(UUID.randomUUID());

        variant1 = new Variant(
                new VariantId(UUID.randomUUID()),
                new Sku("SKU1"),
                new Money(BigDecimal.TEN, Currency.getInstance("EUR")),
                new Stock(100, 0),
                List.of()
        );
        cartItem = new CartItem(variant1, 2);
    }

    /**
     * Verifies that a {@link ShoppingCart} domain object is correctly mapped to
     * a {@link ShoppingCartEntity} and passed to the repository's save method.
     */
    @Test
    void save_shouldMapDomainCartToEntityAndSave() {
        ShoppingCart cart = ShoppingCart.createNew(userId);
        cart.addItem(variant1, cartItem.getQuantity());

        when(repository.findById(cart.getId().getId())).thenReturn(Optional.empty());
        ArgumentCaptor<ShoppingCartEntity> entityCaptor = ArgumentCaptor.forClass(ShoppingCartEntity.class);
        when(repository.save(entityCaptor.capture())).thenAnswer(i -> i.getArgument(0));

        adapter.save(cart);

        ShoppingCartEntity capturedEntity = entityCaptor.getValue();
        assertNotNull(capturedEntity);
        assertEquals(cart.getId().getId(), capturedEntity.getUuid());
        assertEquals(cart.getUserId().getId(), capturedEntity.getUserId());
        assertEquals(cart.getStatus(), capturedEntity.getStatus());
        assertEquals(1, capturedEntity.getItems().size());
    }

    /**
     * Verifies that an existing {@link ShoppingCartEntity} from the repository
     * is correctly mapped back into a {@link ShoppingCart} domain object,
     * including its associated items.
     */
    @Test
    void findById_shouldReturnDomainCartWhenEntityExists() {
        UUID cartUuid = cartId.getId();
        UUID userIdUuid = userId.getId();
        UUID variantUuid = variant1.getId().getId();

        ShoppingCartEntity entity = new ShoppingCartEntity(cartUuid, userIdUuid);
        entity.addItem(variantUuid, 2);

        when(repository.findById(cartUuid)).thenReturn(Optional.of(entity));
        when(variantPort.findById(any(VariantId.class))).thenReturn(Optional.of(variant1));

        Optional<ShoppingCart> result = adapter.findById(cartId);

        assertTrue(result.isPresent());
        ShoppingCart mappedCart = result.get();
        assertEquals(1, mappedCart.getItems().size());
        assertEquals(variant1, mappedCart.getItems().get(0).getVariant());
    }
}