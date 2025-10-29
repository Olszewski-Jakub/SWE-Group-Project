package ie.universityofgalway.groupnine.infrastructure.health;

import com.rabbitmq.client.Connection;
import ie.universityofgalway.groupnine.service.health.probe.RabbitProbe;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * RabbitMQ probe backed by Spring AMQP {@link org.springframework.amqp.rabbit.connection.ConnectionFactory}.
 * <p>
 * Creates a broker connection to assert liveness and extracts both connection-factory
 * configuration and broker server properties for diagnostics.
 */
@Component
public class RabbitProbeImpl implements RabbitProbe {
    private final ConnectionFactory connectionFactory;

    public RabbitProbeImpl(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    /** @return true if a connection can be created and is open. */
    @Override
    public boolean ping() {
        try (org.springframework.amqp.rabbit.connection.Connection c = connectionFactory.createConnection()) {
            return c != null && c.isOpen();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Provides a small diagnostic map including host/port/vhost, cache configuration,
     * publisher return/confirm settings (when available), and broker version/limits.
     */
    @Override
    public Map<String, Object> serverInfo() {
        Map<String, Object> info = new HashMap<>();
        // Spring CachingConnectionFactory details
        try {
            if (connectionFactory instanceof CachingConnectionFactory cf) {
                info.put("host", cf.getHost());
                info.put("port", cf.getPort());
                info.put("virtualHost", cf.getVirtualHost());
                info.put("cacheMode", String.valueOf(cf.getCacheMode()));
                info.put("channelCacheSize", cf.getChannelCacheSize());
                try {
                    info.put("publisherReturns", cf.isPublisherReturns());
                } catch (Throwable ignored) {
                }
                try {
                    java.lang.reflect.Method m = cf.getClass().getMethod("getPublisherConfirmType");
                    Object val = m.invoke(cf);
                    if (val != null) info.put("publisherConfirmType", String.valueOf(val));
                } catch (Throwable ignored) {
                    try {
                        java.lang.reflect.Method legacy = cf.getClass().getMethod("isPublisherConfirms");
                        Object val = legacy.invoke(cf);
                        if (val != null) info.put("publisherConfirms", String.valueOf(val));
                    } catch (Throwable ignored2) {
                    }
                }
            }
        } catch (Throwable ignored) {
        }

        // Broker server properties
        try (org.springframework.amqp.rabbit.connection.Connection c = connectionFactory.createConnection()) {
            Connection delegate = c.getDelegate();
            if (delegate != null && delegate.getServerProperties() != null) {
                Object ver = delegate.getServerProperties().get("version");
                if (ver != null) info.put("version", String.valueOf(ver));
                Object product = delegate.getServerProperties().get("product");
                if (product != null) info.put("product", String.valueOf(product));
            }
            try {
                assert delegate != null;
                info.put("channelMax", delegate.getChannelMax());
            } catch (Throwable ignored) {
            }
            try {
                assert delegate != null;
                info.put("frameMax", delegate.getFrameMax());
            } catch (Throwable ignored) {
            }
        } catch (Exception ignored) {
        }
        return info;

    }
}
