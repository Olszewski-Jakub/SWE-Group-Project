package ie.universityofgalway.groupnine.infrastructure.health;

import com.rabbitmq.client.Connection;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RabbitProbeImplTest {

    @Test
    void ping_true_when_connection_opens() {
        var cf = mock(org.springframework.amqp.rabbit.connection.ConnectionFactory.class);
        var conn = mock(org.springframework.amqp.rabbit.connection.Connection.class);
        when(cf.createConnection()).thenReturn(conn);
        when(conn.isOpen()).thenReturn(true);
        RabbitProbeImpl probe = new RabbitProbeImpl(cf);
        assertTrue(probe.ping());
    }

    @Test
    void ping_false_on_exception() {
        var cf = mock(org.springframework.amqp.rabbit.connection.ConnectionFactory.class);
        when(cf.createConnection()).thenThrow(new RuntimeException("boom"));
        RabbitProbeImpl probe = new RabbitProbeImpl(cf);
        assertFalse(probe.ping());
    }

    @Test
    void serverInfo_includes_cf_and_broker_properties() throws Exception {
        CachingConnectionFactory cf = Mockito.mock(CachingConnectionFactory.class);
        when(cf.getHost()).thenReturn("localhost");
        when(cf.getPort()).thenReturn(5672);
        when(cf.getVirtualHost()).thenReturn("/");
        when(cf.getCacheMode()).thenReturn(CachingConnectionFactory.CacheMode.CHANNEL);
        when(cf.getChannelCacheSize()).thenReturn(25);
        when(cf.isPublisherReturns()).thenReturn(true);

        var springConn = mock(org.springframework.amqp.rabbit.connection.Connection.class);
        when(cf.createConnection()).thenReturn(springConn);
        Connection delegate = mock(Connection.class);
        when(springConn.getDelegate()).thenReturn(delegate);
        Map<String,Object> props = new HashMap<>();
        props.put("version", "3.11.0");
        props.put("product", "RabbitMQ");
        when(delegate.getServerProperties()).thenReturn(props);
        when(delegate.getChannelMax()).thenReturn(2047);
        when(delegate.getFrameMax()).thenReturn(131072);

        RabbitProbeImpl probe = new RabbitProbeImpl(cf);
        Map<String,Object> info = probe.serverInfo();
        assertEquals("localhost", info.get("host"));
        assertEquals(5672, info.get("port"));
        assertEquals("/", info.get("virtualHost"));
        assertEquals("3.11.0", info.get("version"));
        assertEquals("RabbitMQ", info.get("product"));
        assertEquals(2047, info.get("channelMax"));
        assertEquals(131072, info.get("frameMax"));
    }
}

