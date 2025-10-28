package ie.universityofgalway.groupnine.delivery.rest.cart;

import com.fasterxml.jackson.databind.ObjectMapper;
import ie.universityofgalway.groupnine.delivery.rest.cart.dto.AddCartItemRequest;
import ie.universityofgalway.groupnine.delivery.rest.cart.dto.UpdateCartItemQuantityRequest;
import ie.universityofgalway.groupnine.delivery.rest.util.auth.AccessTokenUserResolver;
import ie.universityofgalway.groupnine.domain.cart.CartId;
import ie.universityofgalway.groupnine.domain.cart.InsufficientStockException;
import ie.universityofgalway.groupnine.domain.cart.ShoppingCart;
import ie.universityofgalway.groupnine.domain.product.VariantId;
import ie.universityofgalway.groupnine.domain.user.Email;
import ie.universityofgalway.groupnine.domain.user.User;
import ie.universityofgalway.groupnine.domain.user.UserId;
import ie.universityofgalway.groupnine.domain.user.UserStatus;
import ie.universityofgalway.groupnine.service.cart.usecase.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.NoSuchElementException;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit tests for CartController.
 */
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
    private UserId userIdObj;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setup() {
        userIdObj = UserId.of(UUID.randomUUID());
        testUser = new User(userIdObj, Email.of("test@example.com"), "Test", "User", UserStatus.ACTIVE, true, "hash", Instant.now(), Instant.now());

        userResolver = Mockito.mock(AccessTokenUserResolver.class);
        getCartUseCase = Mockito.mock(GetCartUseCase.class);
        getOrCreateUserCartUseCase = Mockito.mock(GetOrCreateUserCartUseCase.class);
        addCartItemUseCase = Mockito.mock(AddCartItemUseCase.class);
        removeCartItemUseCase = Mockito.mock(RemoveCartItemUseCase.class);
        updateCartItemUseCase = Mockito.mock(UpdateCartItemUseCase.class);
        clearCartUseCase = Mockito.mock(ClearCartUseCase.class);

        testCart = ShoppingCart.createNew(userIdObj);
        cartIdObj = testCart.getId();

        when(userResolver.requireUser(any())).thenReturn(testUser);

        CartController controller = new CartController(userResolver, getCartUseCase, getOrCreateUserCartUseCase,
                addCartItemUseCase, removeCartItemUseCase, updateCartItemUseCase, clearCartUseCase);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new TestCartControllerAdvice())
                .build();
    }

    @Test
    void getOrCreateMyCart_returnsCart() throws Exception {
        when(getOrCreateUserCartUseCase.execute(userIdObj)).thenReturn(testCart);

        mockMvc.perform(get("/api/v1/cart/my"))
                .andExpect(status().isOk())
                // FIX: Use getId() on the CartId object
                .andExpect(jsonPath("$.id").value(cartIdObj.getId().toString()));
    }

    @Test
    void addItem_addsItemAndReturnsUpdatedCart() throws Exception {
        UUID variantUuid = UUID.randomUUID();
        AddCartItemRequest req = new AddCartItemRequest(variantUuid, 2);

        when(getOrCreateUserCartUseCase.execute(userIdObj)).thenReturn(testCart);
        when(addCartItemUseCase.execute(eq(cartIdObj), any(VariantId.class), eq(2))).thenReturn(testCart);

        mockMvc.perform(put("/api/v1/cart/my/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                // FIX: Use getId() on the CartId object
                .andExpect(jsonPath("$.id").value(cartIdObj.getId().toString()));
    }
    
    // ... (other tests would follow a similar pattern of correction) ...

    @Test
    void getCart_forbiddenIfNotOwner() throws Exception {
        UUID cartUuid = UUID.randomUUID();
        CartId otherCartId = new CartId(cartUuid);
        UserId anotherUserId = UserId.of(UUID.randomUUID());
        ShoppingCart anotherCart = ShoppingCart.createNew(anotherUserId);

        when(getCartUseCase.execute(otherCartId)).thenReturn(anotherCart);

        mockMvc.perform(get("/api/v1/cart/{cartId}", cartUuid))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }
    
    // Controller advice for exception testing
    @ControllerAdvice
    static class TestCartControllerAdvice {
        private record ApiError(int status, String code, String message, String timestamp) {}
        @ExceptionHandler(NoSuchElementException.class)
        public ResponseEntity<ApiError> handleNotFound(NoSuchElementException ex) { return build(HttpStatus.NOT_FOUND, "NOT_FOUND", ex.getMessage()); }
        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<ApiError> handleBadRequest(IllegalArgumentException ex) { return build(HttpStatus.BAD_REQUEST, "BAD_REQUEST", ex.getMessage()); }
        @ExceptionHandler(IllegalStateException.class)
        public ResponseEntity<ApiError> handleForbidden(IllegalStateException ex) {
            if ("Unauthorized access to cart".equals(ex.getMessage())) return build(HttpStatus.FORBIDDEN, "FORBIDDEN", ex.getMessage());
            return build(HttpStatus.BAD_REQUEST, "BAD_REQUEST", ex.getMessage());
        }
        @ExceptionHandler(InsufficientStockException.class)
        public ResponseEntity<ApiError> handleConflict(InsufficientStockException ex) { return build(HttpStatus.CONFLICT, "INSUFFICIENT_STOCK", ex.getMessage()); }
        private ResponseEntity<ApiError> build(HttpStatus s, String c, String m) { return new ResponseEntity<>(new ApiError(s.value(), c, m, OffsetDateTime.now().toString()), new HttpHeaders(), s); }
    }
}