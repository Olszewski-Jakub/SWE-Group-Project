package ie.universityofgalway.groupnine.service.auth.usecase;

import ie.universityofgalway.groupnine.domain.user.*;
import ie.universityofgalway.groupnine.service.auth.port.ClockPort;
import org.mockito.Mockito;
import ie.universityofgalway.groupnine.service.auth.port.UserRepositoryPort;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AssignRoleToUserUseCaseTest {

    @Test
    void idempotentWhenUserAlreadyHasRole() {
        UserId id = UserId.of(UUID.randomUUID());
        User existing = new User(id, Email.of("u@e.com"), "f","l", UserStatus.ACTIVE, true, null, Instant.now(), Instant.now(), Set.of(Role.ADMIN));
        UserRepositoryPort repo = Mockito.mock(UserRepositoryPort.class);
        Mockito.when(repo.findById(Mockito.any())).thenReturn(Optional.of(existing));
        Mockito.when(repo.update(Mockito.any())).thenAnswer(inv -> inv.getArgument(0));
        ClockPort clock = Mockito.mock(ClockPort.class);
        Mockito.when(clock.now()).thenReturn(Instant.now());
        AssignRoleToUserUseCase uc = new AssignRoleToUserUseCase(repo, clock);
        var rs = uc.execute(id.value().toString(), "ADMIN");
        assertTrue(rs.roles().contains("ADMIN"));
    }

    @Test
    void addsRoleWhenMissing() {
        UserId id = UserId.of(UUID.randomUUID());
        User existing = new User(id, Email.of("u@e.com"), "f","l", UserStatus.ACTIVE, true, null, Instant.now(), Instant.now(), Set.of(Role.CUSTOMER));
        final User[] saved = new User[1];
        UserRepositoryPort repo = Mockito.mock(UserRepositoryPort.class);
        Mockito.when(repo.findById(Mockito.any())).thenReturn(Optional.of(existing));
        Mockito.when(repo.update(Mockito.any())).thenAnswer(inv -> { saved[0] = inv.getArgument(0); return inv.getArgument(0); });
        ClockPort clock = Mockito.mock(ClockPort.class);
        Mockito.when(clock.now()).thenReturn(Instant.now());
        AssignRoleToUserUseCase uc = new AssignRoleToUserUseCase(repo, clock);
        var rs = uc.execute(id.value().toString(), "ADMIN");
        assertTrue(rs.roles().contains("ADMIN"));
        assertNotNull(saved[0]);
        assertTrue(saved[0].hasRole(Role.ADMIN));
    }
}
