package ie.universityofgalway.groupnine.delivery.rest.cart;

import ie.universityofgalway.groupnine.delivery.rest.cart.dto.AddCartItemRequest;
import ie.universityofgalway.groupnine.delivery.rest.cart.dto.CartResponse;
import ie.universityofgalway.groupnine.delivery.rest.cart.dto.UpdateCartItemQuantityRequest;
import ie.universityofgalway.groupnine.delivery.rest.util.Routes;
import ie.universityofgalway.groupnine.delivery.rest.util.auth.AccessTokenUserResolver;
import ie.universityofgalway.groupnine.domain.cart.CartId;
import ie.universityofgalway.groupnine.domain.cart.ShoppingCart;
import ie.universityofgalway.groupnine.domain.cart.InsufficientStockException;
import ie.universityofgalway.groupnine.domain.product.VariantId;
import ie.universityofgalway.groupnine.domain.user.User;
import ie.universityofgalway.groupnine.service.cart.usecase.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.NoSuchElementException;
import java.util.UUID;

/**
 * REST controller for managing shopping carts.
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

    @GetMapping("/my")
    public ResponseEntity<CartResponse> getOrCreateMyCart(HttpServletRequest request) {
        User user = userResolver.requireUser(request);
        // FIX: Use user.getId()
        ShoppingCart cart = getOrCreateUserCartUseCase.execute(user.getId());
        return ResponseEntity.ok(CartDtoMapper.toDto(cart));
    }

    @PutMapping("/my/items")
    public ResponseEntity<CartResponse> addItem(@Valid @RequestBody AddCartItemRequest req, HttpServletRequest request) {
        User user = userResolver.requireUser(request);
        // FIX: Use user.getId()
        ShoppingCart cart = getOrCreateUserCartUseCase.execute(user.getId());
        ShoppingCart updated = addCartItemUseCase.execute(
            // FIX: Use cart.getId()
            cart.getId(), new VariantId(req.getVariantId()), req.getQuantity());
        return ResponseEntity.ok(CartDtoMapper.toDto(updated));
    }

    @DeleteMapping("/my/items/{variantId}")
    public ResponseEntity<CartResponse> removeItem(@PathVariable("variantId") UUID variantId, HttpServletRequest request) {
        User user = userResolver.requireUser(request);
        // FIX: Use user.getId()
        ShoppingCart cart = getOrCreateUserCartUseCase.execute(user.getId());
        ShoppingCart updated = removeCartItemUseCase.execute(
            // FIX: Use cart.getId()
            cart.getId(), new VariantId(variantId));
        return ResponseEntity.ok(CartDtoMapper.toDto(updated));
    }

    @GetMapping("/{cartId}")
    public ResponseEntity<CartResponse> getCart(@PathVariable("cartId") UUID cartId, HttpServletRequest request) {
        ShoppingCart cart = getCartUseCase.execute(new CartId(cartId));
        // FIX: Use cart.getUserId().getId()
        verifyOwnership(cart.getUserId().getId(), request);
        return ResponseEntity.ok(CartDtoMapper.toDto(cart));
    }

    @PatchMapping("/{cartId}/items/{variantId}")
    public ResponseEntity<CartResponse> updateItemQuantity(
        @PathVariable("cartId") UUID cartId, @PathVariable("variantId") UUID variantId,
        @Valid @RequestBody UpdateCartItemQuantityRequest req, HttpServletRequest request) {
        CartId domainCartId = new CartId(cartId);
        VariantId domainVariantId = new VariantId(variantId);
        ShoppingCart cart = getCartUseCase.execute(domainCartId);
        // FIX: Use cart.getUserId().getId()
        verifyOwnership(cart.getUserId().getId(), request);
        ShoppingCart updated = updateCartItemUseCase.execute(domainCartId, domainVariantId, req.getQuantity());
        return ResponseEntity.ok(CartDtoMapper.toDto(updated));
    }

    @DeleteMapping("/{cartId}/items")
    public ResponseEntity<CartResponse> clearCart(@PathVariable("cartId") UUID cartId, HttpServletRequest request) {
        CartId domainCartId = new CartId(cartId);
        ShoppingCart cart = getCartUseCase.execute(domainCartId);
        // FIX: Use cart.getUserId().getId()
        verifyOwnership(cart.getUserId().getId(), request);
        ShoppingCart cleared = clearCartUseCase.execute(domainCartId);
        return ResponseEntity.ok(CartDtoMapper.toDto(cleared));
    }

    private void verifyOwnership(UUID cartOwnerUuid, HttpServletRequest request) {
        User user = userResolver.requireUser(request);
        // FIX: Use user.getId().getId()
        if (!cartOwnerUuid.equals(user.getId().getId())) {
            throw new IllegalStateException("Unauthorized access to cart");
        }
    }

    // --- Exception Handlers ---
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
    private record ApiError(int status, String code, String message, String timestamp) {}
}