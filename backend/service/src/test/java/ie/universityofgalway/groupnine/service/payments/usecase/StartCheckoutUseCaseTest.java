package ie.universityofgalway.groupnine.service.payments.usecase;

import ie.universityofgalway.groupnine.domain.cart.CartId;
import ie.universityofgalway.groupnine.domain.cart.ShoppingCart;
import ie.universityofgalway.groupnine.domain.order.Order;
import ie.universityofgalway.groupnine.domain.product.*;
import ie.universityofgalway.groupnine.domain.user.UserId;
import ie.universityofgalway.groupnine.service.audit.port.AuditEventPort;
import ie.universityofgalway.groupnine.service.cart.port.ShoppingCartPort;
import ie.universityofgalway.groupnine.service.messaging.port.OutboxPort;
import ie.universityofgalway.groupnine.service.order.port.OrderPort;
import ie.universityofgalway.groupnine.service.payments.port.PaymentGatewayPort;
import ie.universityofgalway.groupnine.service.payments.port.dto.CheckoutSession;
import ie.universityofgalway.groupnine.service.payments.port.dto.ShippingOptions;
import ie.universityofgalway.groupnine.service.product.port.ProductPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class StartCheckoutUseCaseTest {

    private ShoppingCartPort carts;
    private OrderPort orders;
    private PaymentGatewayPort payments;
    private OutboxPort outbox;
    private AuditEventPort audit;
    private ProductPort products;

    @BeforeEach
    void setup() {
        carts = Mockito.mock(ShoppingCartPort.class);
        orders = Mockito.mock(OrderPort.class);
        payments = Mockito.mock(PaymentGatewayPort.class);
        outbox = Mockito.mock(OutboxPort.class);
        audit = Mockito.mock(AuditEventPort.class);
        products = Mockito.mock(ProductPort.class);
    }

    @Test
    void happy_path_creates_order_session_reserves_and_audits() {
        // Build cart with one item
        UserId uid = UserId.of(UUID.randomUUID());
        ShoppingCart cart = ShoppingCart.createNew(uid);
        VariantId vid = new VariantId(UUID.randomUUID());
        Variant variant = new Variant(vid, new Sku("SKU-1"), new Money(new BigDecimal("12.34"), Currency.getInstance("EUR")), new Stock(10, 0), List.of());
        cart.addItem(variant, 2);
        CartId cartId = cart.getId();

        when(carts.findById(eq(cartId))).thenReturn(Optional.of(cart));
        when(products.findByVariantId(eq(vid))).thenReturn(Optional.of(new Product(new ProductId(UUID.randomUUID()), "Coffee", "desc", "cat", ProductStatus.ACTIVE, List.of(), java.time.Instant.now(), java.time.Instant.now())));
        when(payments.createCheckoutSession(anyList(), eq("EUR"), any(), anyString(), anyString(), any(), any(ShippingOptions.class)))
                .thenReturn(new CheckoutSession("cs_test", "https://checkout/abc", "pi_1"));
        // Echo back saved order
        when(orders.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
        when(carts.save(any(ShoppingCart.class))).thenAnswer(inv -> inv.getArgument(0));

        StartCheckoutUseCase uc = new StartCheckoutUseCase(carts, orders, payments, outbox, audit, products, 1800, List.of("IE"), List.of());

        var result = uc.execute(cartId, uid, "https://ok", "https://ko");

        assertNotNull(result.getOrderId());
        assertEquals("cs_test", result.getSessionId());
        assertEquals("https://checkout/abc", result.getCheckoutUrl());

        // Outbox: started + inventory reserve
        verify(outbox, atLeastOnce()).enqueue(eq("orders.commands"), eq("order.checkout.started"), anyMap(), any());
        verify(outbox, atLeastOnce()).enqueue(eq("inventory.commands"), eq("inventory.reserve.request"), anyMap(), any());
        // Cart saved as checked out
        verify(carts).save(any(ShoppingCart.class));
        // Audit recorded
        verify(audit).record(eq(uid), eq("checkout_started"), anyMap(), any());
    }
}

