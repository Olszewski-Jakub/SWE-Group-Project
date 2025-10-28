package ie.universityofgalway.groupnine.infrastructure.cart;

import ie.universityofgalway.groupnine.domain.cart.*;
import ie.universityofgalway.groupnine.domain.product.*;
import ie.universityofgalway.groupnine.domain.user.UserId;
import ie.universityofgalway.groupnine.service.product.VariantPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link CartPersistenceAdapter}.
 */
class CartPersistenceAdapterTest {

    private CartJpaRepository repository;
    private VariantPort variantPort;
    private CartPersistenceAdapter adapter;

    private Variant variant1;
    private CartItem cartItem;
    private CartId cartId;
    private UserId userId;

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

    @Test
    void save_shouldMapDomainCartToEntityAndSave() {
        ShoppingCart cart = ShoppingCart.createNew(userId);
        cart.addItem(variant1, cartItem.getQuantity());

        // FIX: Use cart.getId().getId() for the UUID
        when(repository.findById(cart.getId().getId())).thenReturn(Optional.empty());
        ArgumentCaptor<ShoppingCartEntity> entityCaptor = ArgumentCaptor.forClass(ShoppingCartEntity.class);
        when(repository.save(entityCaptor.capture())).thenAnswer(i -> i.getArgument(0));

        adapter.save(cart);

        ShoppingCartEntity capturedEntity = entityCaptor.getValue();
        assertNotNull(capturedEntity);
        // FIX: Use getters to access UUIDs for comparison
        assertEquals(cart.getId().getId(), capturedEntity.getUuid());
        assertEquals(cart.getUserId().getId(), capturedEntity.getUserId());
        assertEquals(cart.getStatus(), capturedEntity.getStatus());
        assertEquals(1, capturedEntity.getItems().size());
    }

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
        // FIX: Use getItems()
        assertEquals(1, mappedCart.getItems().size());
        assertEquals(variant1, mappedCart.getItems().get(0).getVariant());
    }
}