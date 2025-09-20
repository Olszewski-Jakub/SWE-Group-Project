package ie.universityofgalway.groupnine.infrastructure.health;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DatabaseProbeImplTest {

    @Mock
    DataSource dataSource;

    @Mock
    Connection connection;

    @Mock
    PreparedStatement preparedStatement;

    @Mock
    ResultSet resultSet;

    @InjectMocks
    DatabaseProbeImpl databaseProbe;

    @DisplayName("pingDatabase returns true when DB is reachable")
    @Test
    void pingDatabaseReturnsTrueWhenDatabaseIsReachable() throws Exception {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement("SELECT 1")).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);

        assertTrue(databaseProbe.pingDatabase());
    }

    @DisplayName("pingDatabase returns false when DB is unreachable")
    @Test
    void pingDatabaseReturnsFalseWhenDatabaseIsUnreachable() throws Exception {
        when(dataSource.getConnection()).thenThrow(new RuntimeException("DB not reachable"));

        assertFalse(databaseProbe.pingDatabase());
    }

    @DisplayName("pingDatabase returns false when SQL exception occurs during query")
    @Test
    void pingDatabaseReturnsFalseOnSQLExceptionDuringQuery() throws Exception {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement("SELECT 1")).thenThrow(new RuntimeException("SQL error"));

        assertFalse(databaseProbe.pingDatabase());
    }

    @DisplayName("pingDatabase returns false when result set is empty")
    @Test
    void pingDatabaseReturnsFalseWhenResultSetIsEmpty() throws Exception {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement("SELECT 1")).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        assertFalse(databaseProbe.pingDatabase());
    }
}
