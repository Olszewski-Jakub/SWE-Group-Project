package ie.universityofgalway.groupnine.delivery.rest.cart;

import ie.universityofgalway.groupnine.delivery.rest.cart.dto.AddCartItemRequest;
import ie.universityofgalway.groupnine.delivery.rest.cart.dto.CartResponse;
import ie.universityofgalway.groupnine.delivery.rest.cart.dto.UpdateCartItemQuantityRequest;
import ie.universityofgalway.groupnine.delivery.rest.cart.dto.UpdateCartItemRequest;
import ie.universityofgalway.groupnine.delivery.rest.util.Routes;
import ie.universityofgalway.groupnine.delivery.rest.util.auth.AccessTokenUserResolver;
import ie.universityofgalway.groupnine.domain.cart.ShoppingCart;
import ie.universityofgalway.groupnine.domain.user.User;
import ie.universityofgalway.groupnine.service.cart.usecase.AddCartItemUseCase;
import ie.universityofgalway.groupnine.service.cart.usecase.GetCartUseCase;
import ie.universityofgalway.groupnine.service.cart.usecase.GetOrCreateUserCartUseCase;
import ie.universityofgalway.groupnine.service.cart.usecase.RemoveCartItemUseCase;
import ie.universityofgalway.groupnine.service.cart.usecase.UpdateCartItemUseCase;
import ie.universityofgalway.groupnine.service.cart.usecase.ClearCartUseCase;
import ie.universityofgalway.groupnine.domain.cart.InsufficientStockException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.time.OffsetDateTime;
import java.util.NoSuchElementException;

/**
 * REST controller responsible for managing shopping carts within the e-commerce platform.
 *
 * <p>The {@code CartController} exposes endpoints for:
 * <ul>
 *   <li>Retrieving or creating the authenticated user’s shopping cart</li>
 *   <li>Adding, updating, or removing items in a cart</li>
 *   <li>Clearing all items from a cart</li>
 *   <li>Fetching a specific cart by its ID (for administrative or special use cases)</li>
 * </ul>
 *
 * <p>All operations that modify or retrieve a user's cart require authentication,
 * which is resolved through {@link AccessTokenUserResolver}.</p>
 *
 * <p>Exception handlers within this controller translate domain and validation errors
 * into structured {@code ApiError} responses, ensuring consistent HTTP responses
 * across the API.</p>
 *
 */
@Validated
@RestController
@RequestMapping(Routes.CART)
public class CartController {

    private final AccessTokenUserResolver userResolver;
    private final GetCartUseCase getCartUseCase;
    private final GetOrCreateUserCartUseCase getOrCreateUserCartUseCase;
    private final AddCartItemUseCase addCartItemUseCase;
    private final RemoveCartItemUseCase removeCartItemUseCase;
    private final UpdateCartItemUseCase updateCartItemUseCase;
    private final ClearCartUseCase clearCartUseCase;

    public CartController(
            AccessTokenUserResolver userResolver,
            GetCartUseCase getCartUseCase,
            GetOrCreateUserCartUseCase getOrCreateUserCartUseCase,
            AddCartItemUseCase addCartItemUseCase,
            RemoveCartItemUseCase removeCartItemUseCase,
            UpdateCartItemUseCase updateCartItemUseCase,
            ClearCartUseCase clearCartUseCase
    ) {
        this.userResolver = userResolver;
        this.getCartUseCase = getCartUseCase;
        this.getOrCreateUserCartUseCase = getOrCreateUserCartUseCase;
        this.addCartItemUseCase = addCartItemUseCase;
        this.removeCartItemUseCase = removeCartItemUseCase;
        this.updateCartItemUseCase = updateCartItemUseCase;
        this.clearCartUseCase = clearCartUseCase;
    }

    /**
     * Get or create the current user's cart.
     */
    @GetMapping("/my")
    public ResponseEntity<CartResponse> getOrCreateMyCart(HttpServletRequest request) {
        User user = userResolver.requireUser(request);
        var cart = getOrCreateUserCartUseCase.execute(user.getId());
        return ResponseEntity.ok(CartDtoMapper.toDto(cart));
    }

    /**
     * Add an item to the current user's cart. Automatically creates a cart if none exists.
     */
    @PutMapping("/my/items")
    public ResponseEntity<CartResponse> addItem(
            @Valid @RequestBody AddCartItemRequest req,
            HttpServletRequest request
    ) {
        User user = userResolver.requireUser(request);
        var cart = getOrCreateUserCartUseCase.execute(user.getId());
        var updated = addCartItemUseCase.execute(cart.id().id().toString(), req.variantId(), req.quantity());
        return ResponseEntity.ok(CartDtoMapper.toDto(updated));
    }

    /**
     * Remove an item from the current user's cart.
     */
    @DeleteMapping("/my/items/{variantId}")
    public ResponseEntity<CartResponse> removeItem(
            @PathVariable("variantId") UUID variantId,
            HttpServletRequest request
    ) {
        User user = userResolver.requireUser(request);
        var cart = getOrCreateUserCartUseCase.execute(user.getId());
        var updated = removeCartItemUseCase.execute(cart.id().id().toString(), variantId);
        return ResponseEntity.ok(CartDtoMapper.toDto(updated));
    }

    /**
     * fetch a specific cart by ID (for admins or specific use cases).
     */
    @GetMapping("/{cartId}")
    public ResponseEntity<CartResponse> getCart(@PathVariable("cartId") String cartId, HttpServletRequest request) {
        var cart = getCartUseCase.execute(cartId);
        verifyOwnership(cart.userId().value(), request);
        return ResponseEntity.ok(CartDtoMapper.toDto(cart));
    }

    @PatchMapping("/{cartId}/items/{variantId}")
    public ResponseEntity<CartResponse> updateItemQuantity(
            @PathVariable("cartId")String cartId,
            @PathVariable("variantId") UUID variantId,
            @RequestBody UpdateCartItemQuantityRequest req, //  input DTO
            HttpServletRequest request
    ) {
        var cart = getCartUseCase.execute(cartId);
        verifyOwnership(cart.userId().value(), request);

        ShoppingCart updated;
        int quantity = req.quantity();
        if (quantity == 0) {
            updated = removeCartItemUseCase.execute(cartId, variantId);
        } else {
            updated = updateCartItemUseCase.execute(cartId, variantId, quantity);
        }

        return ResponseEntity.ok(CartDtoMapper.toDto(updated));
    }


    @DeleteMapping("/{cartId}/items")
    public ResponseEntity<CartResponse> clearCart(
            @PathVariable("cartId") String cartId,
            HttpServletRequest request
    ) {
        var cart = getCartUseCase.execute(cartId);
        verifyOwnership(cart.userId().value(), request);

        var cleared = clearCartUseCase.execute(cartId);
        return ResponseEntity.ok(CartDtoMapper.toDto(cleared));
    }


    private void verifyOwnership(UUID cartUserId, HttpServletRequest request) {
        User user = userResolver.requireUser(request);
        if (!cartUserId.equals(user.getId().value())) {
            throw new IllegalStateException("Unauthorized access to cart");
        }
    }


    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ApiError> handleNotFound(NoSuchElementException ex) {
        return build(HttpStatus.NOT_FOUND, "NOT_FOUND", ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleBadRequest(IllegalArgumentException ex) {
        return build(HttpStatus.BAD_REQUEST, "BAD_REQUEST", ex.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiError> handleForbidden(IllegalStateException ex) {
        if ("Unauthorized access to cart".equals(ex.getMessage())) {
            return build(HttpStatus.FORBIDDEN, "FORBIDDEN", ex.getMessage());
        }
        return build(HttpStatus.BAD_REQUEST, "BAD_REQUEST", ex.getMessage());
    }


    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ApiError> handleConflict(InsufficientStockException ex) {
        return build(HttpStatus.CONFLICT, "INSUFFICIENT_STOCK", ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnknown(Exception ex) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "An unexpected error occurred");
    }

    private ResponseEntity<ApiError> build(HttpStatus status, String code, String message) {
        ApiError body = new ApiError(status.value(), code, message, OffsetDateTime.now().toString());
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, "application/json");
        return new ResponseEntity<>(body, headers, status);
    }

/** Private record used only within this controller */
    private record ApiError(int status, String code, String message, String timestamp) {}
}
