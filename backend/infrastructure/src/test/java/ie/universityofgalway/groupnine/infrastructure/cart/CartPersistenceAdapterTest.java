package ie.universityofgalway.groupnine.infrastructure.cart;

import ie.universityofgalway.groupnine.domain.cart.*;
import ie.universityofgalway.groupnine.domain.product.*;
import ie.universityofgalway.groupnine.domain.user.UserId;
import ie.universityofgalway.groupnine.service.product.VariantPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

    @BeforeEach
    void setUp() {
        repository = mock(CartJpaRepository.class);
        variantPort = mock(VariantPort.class);
        adapter = new CartPersistenceAdapter(repository, variantPort);

        // Dummy variant record
        variant1 = new Variant(
                new VariantId(UUID.randomUUID()),
                new Sku("SKU1"),
                new Money(java.math.BigDecimal.TEN, Currency.getInstance("EUR")),
                new Stock(100, 0),
                List.of()
        );

        cartItem = new CartItem(variant1, 2);
    }

    @Test
    void save_shouldMapDomainCartToEntityAndSave() {
        ShoppingCart cart = new ShoppingCart(
                new CartId(UUID.randomUUID()),
                UserId.of(UUID.randomUUID()),
                new CartItems(List.of(cartItem)),
                CartStatus.ACTIVE,
                Instant.now(),
                Instant.now()
        );

        when(repository.findById(cart.id().getId())).thenReturn(Optional.empty());
        when(repository.save(any(ShoppingCartEntity.class))).thenAnswer(i -> i.getArgument(0));

        ShoppingCart saved = adapter.save(cart);

        assertEquals(cart, saved);
        verify(repository, times(1)).save(any(ShoppingCartEntity.class));
    }

    @Test
    void findById_shouldReturnDomainCartWhenEntityExists() {
        UUID cartUuid = UUID.randomUUID();
        UUID variantId = variant1.id().id(); // record access

        ShoppingCartEntity entity = new ShoppingCartEntity(cartUuid, UUID.randomUUID());
        entity.addItem(variantId, 2);

        when(repository.findById(cartUuid)).thenReturn(Optional.of(entity));
        when(variantPort.findById(any(VariantId.class))).thenReturn(Optional.of(variant1));

        Optional<ShoppingCart> result = adapter.findById(new CartId(cartUuid));

        assertTrue(result.isPresent());
        assertEquals(1, result.get().items().asList().size());

        CartItem item = result.get().items().asList().get(0);
        assertEquals(variant1, item.getVariant());
        assertEquals(2, item.getQuantity());
    }

    @Test
    void delete_shouldCallRepositoryWhenEntityExists() {
        UUID cartUuid = UUID.randomUUID();
        ShoppingCartEntity entity = new ShoppingCartEntity(cartUuid, UUID.randomUUID());
        when(repository.findById(cartUuid)).thenReturn(Optional.of(entity));

        // Delete existing
        adapter.delete(new CartId(cartUuid));
        verify(repository, times(1)).delete(entity);

        // Delete non-existing (should not throw)
        when(repository.findById(cartUuid)).thenReturn(Optional.empty());
        adapter.delete(new CartId(cartUuid));
        verify(repository, times(1)).delete(entity); // still only called once
    }
}

