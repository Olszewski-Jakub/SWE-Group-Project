package ie.universityofgalway.groupnine.delivery.worker.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class InventoryWorkerConfig {
    public static final String EX_INVENTORY_COMMANDS = "inventory.commands";
    public static final String EX_APP_DLX = "app.dlx";
    public static final String Q_INVENTORY_RESERVE = "q.inventory.reservation";
    public static final String Q_INVENTORY_CONFIRM = "q.inventory.confirm";
    public static final String Q_INVENTORY_RELEASE = "q.inventory.release";
    public static final String Q_INVENTORY_SCHEDULER = "q.inventory.scheduler";

    private static Map<String, Object> dlArgs() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", EX_APP_DLX);
        return args;
    }
    private static String dlqName(String main) { return main + ".dlq"; }
    private static String dlqRoutingKey(String main) { return "dlq." + main; }

    @Bean
    /**
     * Declares inventory command exchanges, main queues (reserve/confirm/release/scheduler),
     * and their DLQs with dead‑letter bindings.
     */
    public Declarables inventoryTopology() {
        List<Declarable> decs = new ArrayList<>();
        TopicExchange inventoryCommands = ExchangeBuilder.topicExchange(EX_INVENTORY_COMMANDS).durable(true).build();
        DirectExchange dlx = ExchangeBuilder.directExchange(EX_APP_DLX).durable(true).build();
        decs.add(inventoryCommands); decs.add(dlx);

        Queue qInvReserve = QueueBuilder.durable(Q_INVENTORY_RESERVE).withArguments(dlArgs()).build();
        Queue qInvReserveDlq = QueueBuilder.durable(dlqName(Q_INVENTORY_RESERVE)).build();
        decs.add(qInvReserve); decs.add(qInvReserveDlq);
        decs.add(BindingBuilder.bind(qInvReserve).to(inventoryCommands).with("inventory.reserve.request"));
        decs.add(BindingBuilder.bind(qInvReserveDlq).to(dlx).with(dlqRoutingKey(Q_INVENTORY_RESERVE)));

        Queue qInvConfirm = QueueBuilder.durable(Q_INVENTORY_CONFIRM).withArguments(dlArgs()).build();
        Queue qInvConfirmDlq = QueueBuilder.durable(dlqName(Q_INVENTORY_CONFIRM)).build();
        decs.add(qInvConfirm); decs.add(qInvConfirmDlq);
        decs.add(BindingBuilder.bind(qInvConfirm).to(inventoryCommands).with("inventory.confirm.request"));
        decs.add(BindingBuilder.bind(qInvConfirmDlq).to(dlx).with(dlqRoutingKey(Q_INVENTORY_CONFIRM)));

        Queue qInvRelease = QueueBuilder.durable(Q_INVENTORY_RELEASE).withArguments(dlArgs()).build();
        Queue qInvReleaseDlq = QueueBuilder.durable(dlqName(Q_INVENTORY_RELEASE)).build();
        decs.add(qInvRelease); decs.add(qInvReleaseDlq);
        decs.add(BindingBuilder.bind(qInvRelease).to(inventoryCommands).with("inventory.release.request"));
        decs.add(BindingBuilder.bind(qInvReleaseDlq).to(dlx).with(dlqRoutingKey(Q_INVENTORY_RELEASE)));

        Queue qScheduler = QueueBuilder.durable(Q_INVENTORY_SCHEDULER).withArguments(dlArgs()).build();
        Queue qSchedulerDlq = QueueBuilder.durable(dlqName(Q_INVENTORY_SCHEDULER)).build();
        decs.add(qScheduler); decs.add(qSchedulerDlq);
        decs.add(BindingBuilder.bind(qSchedulerDlq).to(dlx).with(dlqRoutingKey(Q_INVENTORY_SCHEDULER)));

        return new Declarables(decs);
    }
}
