package ie.universityofgalway.groupnine.infrastructure.messaging.adapter;

import ie.universityofgalway.groupnine.infrastructure.messaging.jpa.ProcessedEventEntity;
import ie.universityofgalway.groupnine.infrastructure.messaging.jpa.ProcessedEventJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ProcessedEventPersistenceAdapterTest {

    private ProcessedEventJpaRepository repo;
    private ProcessedEventPersistenceAdapter adapter;

    @BeforeEach
    void setup() {
        repo = Mockito.mock(ProcessedEventJpaRepository.class);
        adapter = new ProcessedEventPersistenceAdapter(repo);
    }

    @Test
    void alreadyProcessed_delegates_to_repo() {
        when(repo.existsBySourceAndKey("s", "k")).thenReturn(true);
        assertTrue(adapter.alreadyProcessed("s", "k"));
        when(repo.existsBySourceAndKey("s", "k")).thenReturn(false);
        assertFalse(adapter.alreadyProcessed("s", "k"));
    }

    @Test
    void markProcessed_saves_entity_with_timestamp() {
        adapter.markProcessed("src", "key");
        ArgumentCaptor<ProcessedEventEntity> cap = ArgumentCaptor.forClass(ProcessedEventEntity.class);
        verify(repo).save(cap.capture());
        ProcessedEventEntity e = cap.getValue();
        assertEquals("src", e.getSource());
        assertEquals("key", e.getKey());
        assertNotNull(e.getProcessedAt());
    }
}

