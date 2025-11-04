package ie.universityofgalway.groupnine.delivery.rest.cart;

import ie.universityofgalway.groupnine.delivery.rest.cart.dto.AddCartItemRequest;
import ie.universityofgalway.groupnine.delivery.rest.cart.dto.CartResponse;
import ie.universityofgalway.groupnine.delivery.rest.cart.dto.UpdateCartItemQuantityRequest;
import ie.universityofgalway.groupnine.util.Routes;
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
import ie.universityofgalway.groupnine.domain.cart.exception.InsufficientStockException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.time.OffsetDateTime;
import java.util.NoSuchElementException;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for managing shopping carts, providing endpoints for creating,
 * viewing, and modifying cart contents.
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
        AccessTokenUserResolver userResolver, GetCartUseCase getCartUseCase,
        GetOrCreateUserCartUseCase getOrCreateUserCartUseCase, AddCartItemUseCase addCartItemUseCase,
        RemoveCartItemUseCase removeCartItemUseCase, UpdateCartItemUseCase updateCartItemUseCase,
        ClearCartUseCase clearCartUseCase) {
        this.userResolver = userResolver;
        this.getCartUseCase = getCartUseCase;
        this.getOrCreateUserCartUseCase = getOrCreateUserCartUseCase;
        this.addCartItemUseCase = addCartItemUseCase;
        this.removeCartItemUseCase = removeCartItemUseCase;
        this.updateCartItemUseCase = updateCartItemUseCase;
        this.clearCartUseCase = clearCartUseCase;
    }

    /**
     * Retrieves the authenticated user's cart, creating one if it doesn't exist.
     *
     * @param request The incoming HTTP request.
     * @return A ResponseEntity containing the user's cart.
     */
    @GetMapping("/my")
    public ResponseEntity<CartResponse> getOrCreateMyCart(HttpServletRequest request) {
        User user = userResolver.requireUser(request);
        ShoppingCart cart = getOrCreateUserCartUseCase.execute(user.getId());
        return ResponseEntity.ok(CartDtoMapper.toDto(cart));
    }

    /**
     * Adds an item to the authenticated user's cart.
     *
     * @param req The request body containing the variant ID and quantity.
     * @param request The incoming HTTP request.
     * @return A ResponseEntity containing the updated cart.
     */
    @PutMapping("/my/items")
    public ResponseEntity<CartResponse> addItem(@Valid @RequestBody AddCartItemRequest req, HttpServletRequest request) {
        User user = userResolver.requireUser(request);
        ShoppingCart cart = getOrCreateUserCartUseCase.execute(user.getId());
        ShoppingCart updated = addCartItemUseCase.execute(
            cart.getId(), new VariantId(req.getVariantId()), req.getQuantity());
        return ResponseEntity.ok(CartDtoMapper.toDto(updated));
    }

    /**
     * Removes an item from the authenticated user's cart.
     *
     * @param variantId The UUID of the product variant to remove.
     * @param request The incoming HTTP request.
     * @return A ResponseEntity containing the updated cart.
     */
    @DeleteMapping("/my/items/{variantId}")
    public ResponseEntity<CartResponse> removeItem(@PathVariable("variantId") UUID variantId, HttpServletRequest request) {
        User user = userResolver.requireUser(request);
        ShoppingCart cart = getOrCreateUserCartUseCase.execute(user.getId());
        ShoppingCart updated = removeCartItemUseCase.execute(
            cart.getId(), new VariantId(variantId));
        return ResponseEntity.ok(CartDtoMapper.toDto(updated));
    }

    /**
     * Retrieves a specific cart by its ID, verifying ownership.
     *
     * @param cartId The UUID of the cart to retrieve.
     * @param request The incoming HTTP request.
     * @return A ResponseEntity containing the requested cart.
     */
    @GetMapping("/{cartId}")
    public ResponseEntity<CartResponse> getCart(@PathVariable("cartId") UUID cartId, HttpServletRequest request) {
        ShoppingCart cart = getCartUseCase.execute(new CartId(cartId));
        verifyOwnership(cart.getUserId().getId(), request);
        return ResponseEntity.ok(CartDtoMapper.toDto(cart));
    }

    /**
     * Updates the quantity of a specific item in a cart.
     *
     * @param cartId The UUID of the cart.
     * @param variantId The UUID of the product variant to update.
     * @param req The request body containing the new quantity.
     * @param request The incoming HTTP request.
     * @return A ResponseEntity containing the updated cart.
     */
    @PatchMapping("/{cartId}/items/{variantId}")
    public ResponseEntity<CartResponse> updateItemQuantity(
        @PathVariable("cartId") UUID cartId, @PathVariable("variantId") UUID variantId,
        @Valid @RequestBody UpdateCartItemQuantityRequest req, HttpServletRequest request) {
        CartId domainCartId = new CartId(cartId);
        VariantId domainVariantId = new VariantId(variantId);
        ShoppingCart cart = getCartUseCase.execute(domainCartId);
        verifyOwnership(cart.getUserId().getId(), request);
        ShoppingCart updated = updateCartItemUseCase.execute(domainCartId, domainVariantId, req.getQuantity());
        return ResponseEntity.ok(CartDtoMapper.toDto(updated));
    }

    /**
     * Clears all items from a specific cart.
     *
     * @param cartId The UUID of the cart to clear.
     * @param request The incoming HTTP request.
     * @return A ResponseEntity containing the empty cart.
     */
    @DeleteMapping("/{cartId}/items")
    public ResponseEntity<CartResponse> clearCart(@PathVariable("cartId") UUID cartId, HttpServletRequest request) {
        CartId domainCartId = new CartId(cartId);
        ShoppingCart cart = getCartUseCase.execute(domainCartId);
        verifyOwnership(cart.getUserId().getId(), request);
        ShoppingCart cleared = clearCartUseCase.execute(domainCartId);
        return ResponseEntity.ok(CartDtoMapper.toDto(cleared));
    }

    /**
     * Verifies that the authenticated user is the owner of the cart.
     *
     * @param cartOwnerUuid The UUID of the cart's owner.
     * @param request The incoming HTTP request.
     * @throws IllegalStateException if the user is not the owner.
     */
    private void verifyOwnership(UUID cartOwnerUuid, HttpServletRequest request) {
        User user = userResolver.requireUser(request);
        if (!cartOwnerUuid.equals(user.getId().getId())) {
            throw new IllegalStateException("Unauthorized access to cart");
        }
    }

    /**
     * Handles exceptions when a requested resource is not found.
     *
     * @param ex The caught NoSuchElementException.
     * @return A ResponseEntity with a 404 NOT_FOUND status.
     */
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ApiError> handleNotFound(NoSuchElementException ex) {
        return build(HttpStatus.NOT_FOUND, "NOT_FOUND", ex.getMessage());
    }

    /**
     * Handles exceptions from invalid arguments in requests.
     *
     * @param ex The caught IllegalArgumentException.
     * @return A ResponseEntity with a 400 BAD_REQUEST status.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleBadRequest(IllegalArgumentException ex) {
        return build(HttpStatus.BAD_REQUEST, "BAD_REQUEST", ex.getMessage());
    }

    /**
     * Handles exceptions related to unauthorized access or invalid state.
     *
     * @param ex The caught IllegalStateException.
     * @return A ResponseEntity with a 403 FORBIDDEN or 400 BAD_REQUEST status.
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiError> handleForbidden(IllegalStateException ex) {
        if ("Unauthorized access to cart".equals(ex.getMessage())) {
            return build(HttpStatus.FORBIDDEN, "FORBIDDEN", ex.getMessage());
        }
        return build(HttpStatus.BAD_REQUEST, "BAD_REQUEST", ex.getMessage());
    }

    /**
     * Handles exceptions when there is insufficient stock to fulfill a request.
     *
     * @param ex The caught InsufficientStockException.
     * @return A ResponseEntity with a 409 CONFLICT status.
     */
    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ApiError> handleConflict(InsufficientStockException ex) {
        return build(HttpStatus.CONFLICT, "INSUFFICIENT_STOCK", ex.getMessage());
    }

    /**
     * A generic handler for all other unhandled exceptions.
     *
     * @param ex The caught Exception.
     * @return A ResponseEntity with a 500 INTERNAL_SERVER_ERROR status.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnknown(Exception ex) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "An unexpected error occurred");
    }

    /**
     * A helper method to build a standardized API error response.
     *
     * @param status The HTTP status.
     * @param code The custom error code.
     * @param message The error message.
     * @return A ResponseEntity containing the formatted ApiError.
     */
    private ResponseEntity<ApiError> build(HttpStatus status, String code, String message) {
        ApiError body = new ApiError(status.value(), code, message, OffsetDateTime.now().toString());
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, "application/json");
        return new ResponseEntity<>(body, headers, status);
    }

    private record ApiError(int status, String code, String message, String timestamp) {}
}