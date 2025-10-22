package ie.universityofgalway.groupnine.delivery.rest.cart;

import ie.universityofgalway.groupnine.delivery.rest.cart.dto.AddCartItemRequest;
import ie.universityofgalway.groupnine.delivery.rest.cart.dto.UpdateCartItemQuantityRequest;
import ie.universityofgalway.groupnine.domain.cart.ShoppingCart;
import ie.universityofgalway.groupnine.domain.cart.InsufficientStockException;
import ie.universityofgalway.groupnine.domain.user.Email;
import ie.universityofgalway.groupnine.domain.user.User;
import ie.universityofgalway.groupnine.domain.user.UserId;
import ie.universityofgalway.groupnine.domain.user.UserStatus;
import ie.universityofgalway.groupnine.delivery.rest.util.auth.AccessTokenUserResolver;
import ie.universityofgalway.groupnine.service.cart.usecase.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CartControllerTest {

    private MockMvc mockMvc;
    private AccessTokenUserResolver userResolver;
    private GetCartUseCase getCartUseCase;
    private GetOrCreateUserCartUseCase getOrCreateUserCartUseCase;
    private AddCartItemUseCase addCartItemUseCase;
    private RemoveCartItemUseCase removeCartItemUseCase;
    private UpdateCartItemUseCase updateCartItemUseCase;
    private ClearCartUseCase clearCartUseCase;

    private User testUser;
    private ShoppingCart testCart;

    @BeforeEach
    void setup() {
        testUser = new User(
                UserId.newId(),
                Email.of("testuser@example.com"),
                "Test",
                "User",
                UserStatus.ACTIVE,
                true,
                "dummyPasswordHash",
                Instant.now(),
                Instant.now()
        );

        userResolver = Mockito.mock(AccessTokenUserResolver.class);
        getCartUseCase = Mockito.mock(GetCartUseCase.class);
        getOrCreateUserCartUseCase = Mockito.mock(GetOrCreateUserCartUseCase.class);
        addCartItemUseCase = Mockito.mock(AddCartItemUseCase.class);
        removeCartItemUseCase = Mockito.mock(RemoveCartItemUseCase.class);
        updateCartItemUseCase = Mockito.mock(UpdateCartItemUseCase.class);
        clearCartUseCase = Mockito.mock(ClearCartUseCase.class);

        testCart = ShoppingCart.createNew(testUser.getId());
        when(userResolver.requireUser(any())).thenReturn(testUser);

        mockMvc = MockMvcBuilders.standaloneSetup(new CartController(
                userResolver,
                getCartUseCase,
                getOrCreateUserCartUseCase,
                addCartItemUseCase,
                removeCartItemUseCase,
                updateCartItemUseCase,
                clearCartUseCase
        )).build();
    }

    @Test
    void getOrCreateMyCart_returnsCart() throws Exception {
        when(getOrCreateUserCartUseCase.execute(testUser.getId())).thenReturn(testCart);

        mockMvc.perform(get("/api/v1/cart/my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testCart.id().id().toString()));
    }

    @Test
    void addItem_addsItemAndReturnsUpdatedCart() throws Exception {
        UUID variantId = UUID.randomUUID();

        when(getOrCreateUserCartUseCase.execute(testUser.getId())).thenReturn(testCart);
        when(addCartItemUseCase.execute(eq(testCart.id().id().toString()), eq(variantId), eq(2)))
                .thenReturn(testCart);

        mockMvc.perform(put("/api/v1/cart/my/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"variantId\":\"" + variantId + "\",\"quantity\":2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testCart.id().id().toString()));
    }

    @Test
    void removeItem_removesItemAndReturnsUpdatedCart() throws Exception {
        UUID variantId = UUID.randomUUID();

        when(getOrCreateUserCartUseCase.execute(testUser.getId())).thenReturn(testCart);
        when(removeCartItemUseCase.execute(testCart.id().id().toString(), variantId)).thenReturn(testCart);

        mockMvc.perform(delete("/api/v1/cart/my/items/{variantId}", variantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testCart.id().id().toString()));

    }



    @Test
    void getCart_returnsCartIfOwner() throws Exception {
        String cartId = testCart.id().id().toString();
        when(getCartUseCase.execute(cartId)).thenReturn(testCart);

        mockMvc.perform(get("/api/v1/cart/" + cartId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(cartId));
    }

    @Test
    void getCart_forbiddenIfNotOwner() throws Exception {
        String cartId = testCart.id().id().toString();
        ShoppingCart anotherCart = ShoppingCart.createNew(UserId.newId());
        when(getCartUseCase.execute(cartId)).thenReturn(anotherCart);

        mockMvc.perform(get("/api/v1/cart/" + cartId))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void updateItemQuantity_updatesQuantity() throws Exception {
        UUID variantId = UUID.randomUUID();
        String cartId = testCart.id().id().toString();

        when(getCartUseCase.execute(cartId)).thenReturn(testCart);
        when(updateCartItemUseCase.execute(cartId, variantId, 5)).thenReturn(testCart);

        mockMvc.perform(patch("/api/v1/cart/" + cartId + "/items/" + variantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":5}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(cartId));
    }

    @Test
    void updateItemQuantity_removesItemWhenZero() throws Exception {
        UUID variantId = UUID.randomUUID();
        String cartId = testCart.id().id().toString();

        when(getCartUseCase.execute(cartId)).thenReturn(testCart);
        when(removeCartItemUseCase.execute(cartId, variantId)).thenReturn(testCart);

        mockMvc.perform(patch("/api/v1/cart/" + cartId + "/items/" + variantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(cartId));
    }

    @Test
    void clearCart_clearsAllItems() throws Exception {
        String cartId = testCart.id().id().toString();
        when(getCartUseCase.execute(cartId)).thenReturn(testCart);
        when(clearCartUseCase.execute(cartId)).thenReturn(testCart);

        mockMvc.perform(delete("/api/v1/cart/" + cartId + "/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(cartId));
    }

    @Test
    void addItem_conflictOnInsufficientStock() throws Exception {
        UUID variantId = UUID.randomUUID();
        String cartId = testCart.id().id().toString();
        when(getOrCreateUserCartUseCase.execute(testUser.getId())).thenReturn(testCart);
        when(addCartItemUseCase.execute(eq(cartId), eq(variantId), eq(10)))
                .thenThrow(new InsufficientStockException("Not enough stock"));

        mockMvc.perform(put("/api/v1/cart/my/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"variantId\":\"" + variantId + "\",\"quantity\":10}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("INSUFFICIENT_STOCK"));
    }

    @Test
    void getCart_notFound() throws Exception {
        String cartId = UUID.randomUUID().toString();
        when(getCartUseCase.execute(cartId)).thenThrow(new IllegalArgumentException("Cart not found"));

        mockMvc.perform(get("/api/v1/cart/" + cartId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
    }
}
