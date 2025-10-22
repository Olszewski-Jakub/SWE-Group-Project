package ie.universityofgalway.groupnine.delivery.rest.cart;

import ie.universityofgalway.groupnine.delivery.rest.cart.dto.AddCartItemRequest;
import ie.universityofgalway.groupnine.delivery.rest.cart.dto.CartResponse;
import ie.universityofgalway.groupnine.delivery.rest.cart.dto.UpdateCartItemQuantityRequest;
import ie.universityofgalway.groupnine.delivery.rest.cart.dto.UpdateCartItemRequest;
import ie.universityofgalway.groupnine.delivery.rest.util.Routes;
import ie.universityofgalway.groupnine.delivery.rest.util.auth.AccessTokenUserResolver;
import ie.universityofgalway.groupnine.domain.cart.CartId;
import ie.universityofgalway.groupnine.domain.cart.ShoppingCart;
import ie.universityofgalway.groupnine.domain.product.VariantId;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.time.OffsetDateTime;
import java.util.NoSuchElementException;

/**
 * REST controller responsible for managing shopping carts in the e-commerce platform.
 *
 * <p>This controller provides endpoints to:</p>
 * <ul>
 *     <li>Retrieve or create the current authenticated user's cart</li>
 *     <li>Add items to a cart</li>
 *     <li>Remove items from a cart</li>
 *     <li>Update the quantity of an item in a cart</li>
 *     <li>Clear all items from a cart</li>
 *     <li>Fetch a specific cart by its ID (for administrative or special use cases)</li>
 * </ul>
 *
 * <p>All operations require user authentication, which is resolved through {@link AccessTokenUserResolver}.</p>
 *
 * <p>Exception handlers map domain and validation errors into structured {@code ApiError} responses,
 * ensuring consistent HTTP responses.</p>
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

    @Autowired
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
     * Retrieve the current authenticated user's cart or create one if none exists.
     *
     * @param request the HTTP request, used to resolve the authenticated user
     * @return the current shopping cart
     */
    @GetMapping("/my")
    public ResponseEntity<CartResponse> getOrCreateMyCart(HttpServletRequest request) {
        User user = userResolver.requireUser(request);
        ShoppingCart cart = getOrCreateUserCartUseCase.execute(user.getId());
        return ResponseEntity.ok(CartDtoMapper.toDto(cart));
    }

    /**
     * Add an item to the current user's cart. Automatically creates a cart if none exists.
     *
     * @param req the request containing the variant ID and quantity to add
     * @param request the HTTP request, used to resolve the authenticated user
     * @return the updated shopping cart
     */
    @PutMapping("/my/items")
    public ResponseEntity<CartResponse> addItem(
            @Valid @RequestBody AddCartItemRequest req,
            HttpServletRequest request
    ) {
        User user = userResolver.requireUser(request);
        ShoppingCart cart = getOrCreateUserCartUseCase.execute(user.getId());
        ShoppingCart updated = addCartItemUseCase.execute(
                cart.id(),                // CartId
                new VariantId(req.getVariantId()), // VariantId
                req.getQuantity()
        );
        return ResponseEntity.ok(CartDtoMapper.toDto(updated));
    }

    /**
     * Remove an item from the current user's cart.
     *
     * @param variantId the ID of the variant to remove
     * @param request the HTTP request, used to resolve the authenticated user
     * @return the updated shopping cart
     */
    @DeleteMapping("/my/items/{variantId}")
    public ResponseEntity<CartResponse> removeItem(
            @PathVariable("variantId") UUID variantId,
            HttpServletRequest request
    ) {
        User user = userResolver.requireUser(request);
        ShoppingCart cart = getOrCreateUserCartUseCase.execute(user.getId());
        ShoppingCart updated = removeCartItemUseCase.execute(
                cart.id(),
                new VariantId(variantId)
        );
        return ResponseEntity.ok(CartDtoMapper.toDto(updated));
    }

    /**
     * Fetch a specific cart by its ID.
     *
     * @param cartId the UUID of the cart
     * @param request the HTTP request, used to resolve the authenticated user
     * @return the requested shopping cart
     */
    @GetMapping("/{cartId}")
    public ResponseEntity<CartResponse> getCart(@PathVariable("cartId") UUID cartId, HttpServletRequest request) {
        ShoppingCart cart = getCartUseCase.execute(new CartId(cartId));
        verifyOwnership(cart.userId().value(), request);
        return ResponseEntity.ok(CartDtoMapper.toDto(cart));
    }

    /**
     * Update the quantity of an item in the cart.
     * If quantity is 0, the item will be removed.
     *
     * @param cartId the UUID of the cart
     * @param variantId the ID of the variant
     * @param req the request containing the new quantity
     * @param request the HTTP request, used to resolve the authenticated user
     * @return the updated shopping cart
     */
    @PatchMapping("/{cartId}/items/{variantId}")
    public ResponseEntity<CartResponse> updateItemQuantity(
            @PathVariable("cartId") UUID cartId,
            @PathVariable("variantId") UUID variantId,
            @RequestBody UpdateCartItemQuantityRequest req,
            HttpServletRequest request
    ) {
        ShoppingCart cart = getCartUseCase.execute(new CartId(cartId));
        verifyOwnership(cart.userId().value(), request);

        ShoppingCart updated;
        if (req.getQuantity() == 0) {
            updated = removeCartItemUseCase.execute(
                    cart.id(),
                    new VariantId(variantId)
            );
        } else {
            updated = updateCartItemUseCase.execute(
                    cart.id(),
                    new VariantId(variantId),
                    req.getQuantity()
            );
        }

        return ResponseEntity.ok(CartDtoMapper.toDto(updated));
    }

    /**
     * Clear all items from a cart.
     *
     * @param cartId the UUID of the cart
     * @param request the HTTP request, used to resolve the authenticated user
     * @return the cleared shopping cart
     */
    @DeleteMapping("/{cartId}/items")
    public ResponseEntity<CartResponse> clearCart(@PathVariable("cartId") UUID cartId, HttpServletRequest request) {
        ShoppingCart cart = getCartUseCase.execute(new CartId(cartId));
        verifyOwnership(cart.userId().value(), request);

        ShoppingCart cleared = clearCartUseCase.execute(cart.id());
        return ResponseEntity.ok(CartDtoMapper.toDto(cleared));
    }

    /**
     * Verifies that the authenticated user is the owner of the cart.
     *
     * @param cartUserId the UUID of the user who owns the cart
     * @param request the HTTP request containing authentication information
     * @throws IllegalStateException if the authenticated user does not own the cart
     */
    private void verifyOwnership(UUID cartUserId, HttpServletRequest request) {
        User user = userResolver.requireUser(request);
        if (!cartUserId.equals(user.getId().value())) {
            throw new IllegalStateException("Unauthorized access to cart");
        }
    }

    /**
     * Handles cases where a requested resource was not found.
     *
     * @param ex the exception thrown when an element is not found
     * @return a ResponseEntity containing an {@link ApiError} with HTTP 404 NOT_FOUND
     */
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ApiError> handleNotFound(NoSuchElementException ex) {
        return build(HttpStatus.NOT_FOUND, "NOT_FOUND", ex.getMessage());
    }

    /**
     * Handles invalid request arguments, such as malformed input or illegal values.
     *
     * @param ex the exception thrown for invalid arguments
     * @return a ResponseEntity containing an {@link ApiError} with HTTP 400 BAD_REQUEST
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleBadRequest(IllegalArgumentException ex) {
        return build(HttpStatus.BAD_REQUEST, "BAD_REQUEST", ex.getMessage());
    }

    /**
     * Handles illegal state conditions, primarily for forbidden access attempts.
     *
     * @param ex the exception thrown for an illegal state
     * @return a ResponseEntity containing an {@link ApiError} with HTTP 403 FORBIDDEN
     *         if the user attempted to access a cart they do not own,
     *         otherwise HTTP 400 BAD_REQUEST
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiError> handleForbidden(IllegalStateException ex) {
        if ("Unauthorized access to cart".equals(ex.getMessage())) {
            return build(HttpStatus.FORBIDDEN, "FORBIDDEN", ex.getMessage());
        }
        return build(HttpStatus.BAD_REQUEST, "BAD_REQUEST", ex.getMessage());
    }

    /**
     * Handles conflicts due to insufficient stock when adding or updating cart items.
     *
     * @param ex the exception thrown when requested quantity exceeds available stock
     * @return a ResponseEntity containing an {@link ApiError} with HTTP 409 CONFLICT
     */
    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ApiError> handleConflict(InsufficientStockException ex) {
        return build(HttpStatus.CONFLICT, "INSUFFICIENT_STOCK", ex.getMessage());
    }

    /**
     * Handles all unexpected exceptions that are not explicitly handled elsewhere.
     *
     * @param ex the exception thrown
     * @return a ResponseEntity containing an {@link ApiError} with HTTP 500 INTERNAL_SERVER_ERROR
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnknown(Exception ex) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "An unexpected error occurred");
    }

    /**
     * Builds a consistent {@link ApiError} response with HTTP headers and status code.
     *
     * @param status the HTTP status to return
     * @param code the internal error code for the API
     * @param message the error message to include in the response
     * @return a ResponseEntity containing an {@link ApiError} with the given status and message
     */
    private ResponseEntity<ApiError> build(HttpStatus status, String code, String message) {
        ApiError body = new ApiError(status.value(), code, message, OffsetDateTime.now().toString());
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, "application/json");
        return new ResponseEntity<>(body, headers, status);
    }

    /**
     * Private record representing the structure of API error responses.
     *
     * @param status the HTTP status code
     * @param code a short internal error code
     * @param message a descriptive error message
     * @param timestamp ISO 8601 timestamp of when the error occurred
     */
    private record ApiError(int status, String code, String message, String timestamp) {}
}
