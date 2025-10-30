package ie.universityofgalway.groupnine.delivery.worker.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class PaymentsWorkerConfig {
    public static final String EX_PAYMENTS_EVENTS = "payments.events";
    public static final String EX_APP_DLX = "app.dlx";
    public static final String Q_ORDERS_PAYMENT_HANDLER = "q.orders.payment-handler";

    private static Map<String, Object> dlArgs() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", EX_APP_DLX);
        return args;
    }
    private static String dlqName(String main) { return main + ".dlq"; }
    private static String dlqRoutingKey(String main) { return "dlq." + main; }

    @Bean
    /**
     * Declares the payments events exchange and the orders payment handler queue with DLQ routing.
     */
    public Declarables paymentsTopology() {
        List<Declarable> decs = new ArrayList<>();
        TopicExchange paymentsEvents = ExchangeBuilder.topicExchange(EX_PAYMENTS_EVENTS).durable(true).build();
        DirectExchange dlx = ExchangeBuilder.directExchange(EX_APP_DLX).durable(true).build();
        decs.add(paymentsEvents); decs.add(dlx);

        Queue qOrdersPayment = QueueBuilder.durable(Q_ORDERS_PAYMENT_HANDLER).withArguments(dlArgs()).build();
        Queue qOrdersPaymentDlq = QueueBuilder.durable(dlqName(Q_ORDERS_PAYMENT_HANDLER)).build();
        decs.add(qOrdersPayment); decs.add(qOrdersPaymentDlq);
        decs.add(BindingBuilder.bind(qOrdersPayment).to(paymentsEvents).with("payment.event.#"));
        decs.add(BindingBuilder.bind(qOrdersPaymentDlq).to(dlx).with(dlqRoutingKey(Q_ORDERS_PAYMENT_HANDLER)));
        return new Declarables(decs);
    }
}
