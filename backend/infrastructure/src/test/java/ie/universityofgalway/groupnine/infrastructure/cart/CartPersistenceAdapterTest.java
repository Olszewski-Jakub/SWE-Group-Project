package ie.universityofgalway.groupnine.infrastructure.cart;

import ie.universityofgalway.groupnine.domain.cart.*;
import ie.universityofgalway.groupnine.domain.product.*;
import ie.universityofgalway.groupnine.domain.user.UserId;
import ie.universityofgalway.groupnine.service.product.VariantPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link CartPersistenceAdapter}.
 * <p>
 * Tests focus on:
 * <ul>
 *     <li>Mapping between domain {@link ShoppingCart} and JPA entities</li>
 *     <li>Save, findById, and delete behavior</li>
 *     <li>Integration with mocked {@link VariantPort} and {@link CartJpaRepository}</li>
 * </ul>
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

        // Dummy variant
        variant1 = new Variant(new VariantId(UUID.randomUUID()), new Sku("SKU1"),
                new Money(java.math.BigDecimal.TEN, Currency.getInstance("EUR")),
                new Stock(100, 0), List.of());
        cartItem = new CartItem(variant1, 2);
    }

    /**
     * Tests saving a {@link ShoppingCart} maps domain items to {@link CartItemEntity} and calls repository.save().
     */
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

        when(repository.findById(cart.id().id())).thenReturn(Optional.empty());
        when(repository.save(any(ShoppingCartEntity.class))).thenAnswer(i -> i.getArgument(0));

        ShoppingCart saved = adapter.save(cart);
        assertEquals(cart, saved);
        verify(repository, times(1)).save(any(ShoppingCartEntity.class));
    }

    /**
     * Tests that {@link CartPersistenceAdapter#findById(CartId)} maps a {@link ShoppingCartEntity}
     * to a domain {@link ShoppingCart}, fetching variants via {@link VariantPort}.
     */
    @Test
    void findById_shouldReturnDomainCartWhenEntityExists() {
        UUID cartUuid = UUID.randomUUID();
        UUID variantId = variant1.id().id();

        ShoppingCartEntity entity = new ShoppingCartEntity(cartUuid, UUID.randomUUID());
        entity.addItem(variantId, 2);

        when(repository.findById(cartUuid)).thenReturn(Optional.of(entity));
        when(variantPort.findById(any(VariantId.class))).thenReturn(Optional.of(variant1));

        Optional<ShoppingCart> result = adapter.findById(new CartId(cartUuid));
        assertTrue(result.isPresent());
        assertEquals(1, result.get().items().asList().size());
        assertEquals(variant1, result.get().items().asList().get(0).variant());
    }

    /**
     * Tests that {@link CartPersistenceAdapter#delete(CartId)} calls repository.delete()
     * if the entity exists and does nothing if not found.
     */
    @Test
    void delete_shouldCallRepositoryWhenEntityExists() {
        UUID cartUuid = UUID.randomUUID();
        ShoppingCartEntity entity = new ShoppingCartEntity(cartUuid, UUID.randomUUID());
        when(repository.findById(cartUuid)).thenReturn(Optional.of(entity));

        adapter.delete(new CartId(cartUuid));
        verify(repository, times(1)).delete(entity);

        // Test no exception if entity not found
        when(repository.findById(cartUuid)).thenReturn(Optional.empty());
        adapter.delete(new CartId(cartUuid));
        verify(repository, times(1)).delete(entity); // still only called once
    }
}
