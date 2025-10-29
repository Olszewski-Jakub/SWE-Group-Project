package ie.universityofgalway.groupnine.infrastructure.health;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RedisProbeImplTest {

    @Test
    void pingTrueWhenPong() {
        StringRedisTemplate template = mock(StringRedisTemplate.class);
        RedisConnectionFactory cf = mock(RedisConnectionFactory.class);
        RedisConnection conn = mock(RedisConnection.class);
        when(template.getRequiredConnectionFactory()).thenReturn(cf);
        when(cf.getConnection()).thenReturn(conn);
        when(conn.ping()).thenReturn("PONG");
        RedisProbeImpl probe = new RedisProbeImpl(template);
        assertTrue(probe.ping());
    }

    @Test
    void dbSizeReturnsValue() {
        StringRedisTemplate template = mock(StringRedisTemplate.class);
        RedisConnectionFactory cf = mock(RedisConnectionFactory.class);
        RedisConnection conn = mock(RedisConnection.class);
        when(template.getRequiredConnectionFactory()).thenReturn(cf);
        when(cf.getConnection()).thenReturn(conn);
        var server = mock(org.springframework.data.redis.connection.RedisServerCommands.class);
        when(conn.serverCommands()).thenReturn(server);
        when(server.dbSize()).thenReturn(42L);
        RedisProbeImpl probe = new RedisProbeImpl(template);
        assertEquals(42L, probe.dbSize());
    }

    @Test
    void serverInfoAggregatesSections() {
        StringRedisTemplate template = mock(StringRedisTemplate.class);
        RedisConnectionFactory cf = mock(RedisConnectionFactory.class);
        RedisConnection conn = mock(RedisConnection.class);
        when(template.getRequiredConnectionFactory()).thenReturn(cf);
        when(cf.getConnection()).thenReturn(conn);
        var serverCmd = mock(org.springframework.data.redis.connection.RedisServerCommands.class);
        when(conn.serverCommands()).thenReturn(serverCmd);
        Properties server = new Properties();
        server.put("redis_version", "7.2.0"); server.put("redis_mode","standalone");
        when(serverCmd.info("server")).thenReturn(server);
        when(serverCmd.info("memory")).thenReturn(new Properties());
        when(serverCmd.info("clients")).thenReturn(new Properties());
        when(serverCmd.info("stats")).thenReturn(new Properties());
        when(serverCmd.info("replication")).thenReturn(new Properties());
        when(serverCmd.info("keyspace")).thenReturn(new Properties());
        RedisProbeImpl probe = new RedisProbeImpl(template);
        var info = probe.serverInfo();
        assertEquals("7.2.0", info.get("version"));
        assertEquals("standalone", info.get("mode"));
    }
}

