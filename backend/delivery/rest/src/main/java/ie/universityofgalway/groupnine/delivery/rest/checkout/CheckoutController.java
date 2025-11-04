package ie.universityofgalway.groupnine.delivery.rest.checkout;

import ie.universityofgalway.groupnine.delivery.rest.checkout.dto.CheckoutSessionRequest;
import ie.universityofgalway.groupnine.delivery.rest.checkout.dto.CheckoutSessionResponse;
import ie.universityofgalway.groupnine.util.Routes;
import ie.universityofgalway.groupnine.delivery.rest.util.auth.AccessTokenUserResolver;
import ie.universityofgalway.groupnine.domain.cart.CartId;
import ie.universityofgalway.groupnine.domain.user.User;
import ie.universityofgalway.groupnine.service.payments.usecase.StartCheckoutUseCase;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@Validated
@RestController
@RequestMapping(Routes.V1 + "/checkout")
public class CheckoutController {

    private final AccessTokenUserResolver userResolver;
    private final StartCheckoutUseCase startCheckout;
    private final String baseUrl;
    private final String successPath;
    private final String cancelPath;

    public CheckoutController(AccessTokenUserResolver userResolver,
                              StartCheckoutUseCase startCheckout,
                              @Value("${app.base-url}") String baseUrl,
                              @Value("${app.checkout.success-path}") String successPath,
                              @Value("${app.checkout.cancel-path}") String cancelPath) {
        this.userResolver = userResolver;
        this.startCheckout = startCheckout;
        this.baseUrl = baseUrl != null ? baseUrl : "http://localhost:3000";
        this.successPath = successPath != null ? successPath : "/checkout/success";
        this.cancelPath = cancelPath != null ? cancelPath : "/checkout/cancel";
    }

    /**
     * Creates a hosted Checkout session for the authenticated user and the provided cart.
     *
     * @param req  request body containing the cart id
     * @param http current HTTP servlet request (used to resolve the authenticated user)
     * @return 201 Created with a Location header set to the provider checkout URL and a body
     *         containing order id, session id and checkout URL
     */
    @PostMapping("/sessions")
    public ResponseEntity<CheckoutSessionResponse> createCheckoutSession(
            @Valid @RequestBody CheckoutSessionRequest req,
            HttpServletRequest http
    ) {
        User user = userResolver.requireUser(http);


        String successUrl = join(baseUrl, successPath);
        String cancelUrl = join(baseUrl, cancelPath);

        var result = startCheckout.execute(CartId.of(req.cartId), user.getId(), successUrl, cancelUrl);

        CheckoutSessionResponse body = new CheckoutSessionResponse(
                result.getOrderId().toString(), result.getSessionId(), result.getCheckoutUrl());

        return ResponseEntity.created(URI.create(result.getCheckoutUrl())).body(body);
    }

    private static String join(String base, String path) {
        if (base.endsWith("/") && path.startsWith("/")) return base.substring(0, base.length()-1) + path;
        if (!base.endsWith("/") && !path.startsWith("/")) return base + "/" + path;
        return base + path;
    }
}
