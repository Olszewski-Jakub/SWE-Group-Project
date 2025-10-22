package ie.universityofgalway.groupnine.delivery.rest.cart;

import ie.universityofgalway.groupnine.delivery.rest.cart.dto.AddCartItemRequest;
import ie.universityofgalway.groupnine.delivery.rest.cart.dto.UpdateCartItemQuantityRequest;
import ie.universityofgalway.groupnine.domain.cart.CartId;
import ie.universityofgalway.groupnine.domain.cart.ShoppingCart;
import ie.universityofgalway.groupnine.domain.cart.InsufficientStockException;
import ie.universityofgalway.groupnine.domain.product.VariantId;
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
    private CartId cartIdObj;

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
        cartIdObj = testCart.id(); // CartId domain object

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
                .andExpect(jsonPath("$.id").value(cartIdObj.getId().toString()));
    }

    @Test
    void addItem_addsItemAndReturnsUpdatedCart() throws Exception {
        UUID variantUuid = UUID.randomUUID();
        VariantId variantIdObj = new VariantId(variantUuid);

        when(getOrCreateUserCartUseCase.execute(testUser.getId())).thenReturn(testCart);
        when(addCartItemUseCase.execute(eq(cartIdObj), eq(variantIdObj), eq(2)))
                .thenReturn(testCart);

        mockMvc.perform(put("/api/v1/cart/my/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"variantId\":\"" + variantUuid + "\",\"quantity\":2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(cartIdObj.getId().toString()));
    }

    @Test
    void removeItem_removesItemAndReturnsUpdatedCart() throws Exception {
        UUID variantUuid = UUID.randomUUID();
        VariantId variantIdObj = new VariantId(variantUuid);

        when(getOrCreateUserCartUseCase.execute(testUser.getId())).thenReturn(testCart);
        when(removeCartItemUseCase.execute(cartIdObj, variantIdObj)).thenReturn(testCart);

        mockMvc.perform(delete("/api/v1/cart/my/items/{variantId}", variantUuid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(cartIdObj.getId().toString()));
    }

    @Test
    void getCart_returnsCartIfOwner() throws Exception {
        UUID cartUuid = cartIdObj.getId();
        when(getCartUseCase.execute(cartIdObj)).thenReturn(testCart);

        mockMvc.perform(get("/api/v1/cart/" + cartUuid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(cartUuid.toString()));
    }

    @Test
    void getCart_forbiddenIfNotOwner() throws Exception {
        UUID cartUuid = cartIdObj.getId();
        ShoppingCart anotherCart = ShoppingCart.createNew(UserId.newId());
        when(getCartUseCase.execute(cartIdObj)).thenReturn(anotherCart);

        mockMvc.perform(get("/api/v1/cart/" + cartUuid))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void updateItemQuantity_updatesQuantity() throws Exception {
        UUID variantUuid = UUID.randomUUID();
        VariantId variantIdObj = new VariantId(variantUuid);

        when(getCartUseCase.execute(cartIdObj)).thenReturn(testCart);
        when(updateCartItemUseCase.execute(cartIdObj, variantIdObj, 5)).thenReturn(testCart);

        mockMvc.perform(patch("/api/v1/cart/" + cartIdObj.getId() + "/items/" + variantUuid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":5}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(cartIdObj.getId().toString()));
    }

    @Test
    void updateItemQuantity_removesItemWhenZero() throws Exception {
        UUID variantUuid = UUID.randomUUID();
        VariantId variantIdObj = new VariantId(variantUuid);

        when(getCartUseCase.execute(cartIdObj)).thenReturn(testCart);
        when(removeCartItemUseCase.execute(cartIdObj, variantIdObj)).thenReturn(testCart);

        mockMvc.perform(patch("/api/v1/cart/" + cartIdObj.getId() + "/items/" + variantUuid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(cartIdObj.getId().toString()));
    }

    @Test
    void clearCart_clearsAllItems() throws Exception {
        when(getCartUseCase.execute(cartIdObj)).thenReturn(testCart);
        when(clearCartUseCase.execute(cartIdObj)).thenReturn(testCart);

        mockMvc.perform(delete("/api/v1/cart/" + cartIdObj.getId() + "/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(cartIdObj.getId().toString()));
    }

    @Test
    void addItem_conflictOnInsufficientStock() throws Exception {
        UUID variantUuid = UUID.randomUUID();
        VariantId variantIdObj = new VariantId(variantUuid);

        when(getOrCreateUserCartUseCase.execute(testUser.getId())).thenReturn(testCart);
        when(addCartItemUseCase.execute(eq(cartIdObj), eq(variantIdObj), eq(10)))
                .thenThrow(new InsufficientStockException("Not enough stock"));

        mockMvc.perform(put("/api/v1/cart/my/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"variantId\":\"" + variantUuid + "\",\"quantity\":10}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("INSUFFICIENT_STOCK"));
    }

    @Test
    void getCart_notFound() throws Exception {
        UUID randomCartUuid = UUID.randomUUID();
        CartId randomCartIdObj = new CartId(randomCartUuid);

        when(getCartUseCase.execute(randomCartIdObj))
                .thenThrow(new IllegalArgumentException("Cart not found"));

        mockMvc.perform(get("/api/v1/cart/" + randomCartUuid))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
    }
}
