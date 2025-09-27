package ie.universityofgalway.groupnine.service.session.usecase;

import ie.universityofgalway.groupnine.domain.session.Session;
import ie.universityofgalway.groupnine.domain.user.UserId;
import ie.universityofgalway.groupnine.service.auth.port.RandomTokenPort;
import ie.universityofgalway.groupnine.service.auth.port.SessionRepositoryPort;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class GetSessionChainUseCaseTest {
    @Test
    void walksForwardChainByStartId() {
        SessionRepositoryPort repo = mock(SessionRepositoryPort.class);
        RandomTokenPort random = mock(RandomTokenPort.class);
        Instant now = Instant.parse("2024-01-01T00:00:00Z");
        UserId uid = UserId.newId();

        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();
        Session s1 = new Session(a, uid, "h1", "ua", null, now, now.plusSeconds(60), null, b, null);
        Session s2 = new Session(b, uid, "h2", "ua", null, now, now.plusSeconds(60), null, null, null);
        when(repo.findById(a)).thenReturn(Optional.of(s1));
        when(repo.findById(b)).thenReturn(Optional.of(s2));

        GetSessionChainUseCase uc = new GetSessionChainUseCase(repo, random);
        List<GetSessionChainUseCase.SessionNode> nodes = uc.bySessionId(a);
        assertEquals(2, nodes.size());
        assertEquals(a, nodes.get(0).id());
        assertEquals(b, nodes.get(1).id());
    }
}
