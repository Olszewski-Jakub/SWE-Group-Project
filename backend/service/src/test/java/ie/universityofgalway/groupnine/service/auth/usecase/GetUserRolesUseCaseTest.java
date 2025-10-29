package ie.universityofgalway.groupnine.service.auth.usecase;

import ie.universityofgalway.groupnine.domain.user.*;
import ie.universityofgalway.groupnine.service.auth.port.UserRepositoryPort;
import org.mockito.Mockito;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class GetUserRolesUseCaseTest {
    @Test
    void returnsUserRoles() {
        UserRepositoryPort repo = Mockito.mock(UserRepositoryPort.class);
        Mockito.when(repo.findById(Mockito.any())).thenAnswer(inv -> Optional.of(
                new User(inv.getArgument(0), Email.of("u@e.com"), "f","l", UserStatus.ACTIVE, true, null, Instant.now(), Instant.now(), Set.of(Role.ADMIN, Role.CUSTOMER))
        ));
        GetUserRolesUseCase uc = new GetUserRolesUseCase(repo);
        GetUserRolesUseCase.Result rs = uc.execute(UUID.randomUUID().toString());
        assertTrue(rs.roles().contains("ADMIN"));
        assertTrue(rs.roles().contains("CUSTOMER"));
    }

    @Test
    void throwsOnMissingUser() {
        UserRepositoryPort repo = Mockito.mock(UserRepositoryPort.class);
        Mockito.when(repo.findById(Mockito.any())).thenReturn(Optional.empty());
        GetUserRolesUseCase uc = new GetUserRolesUseCase(repo);
        assertThrows(IllegalArgumentException.class, () -> uc.execute(UUID.randomUUID().toString()));
    }
}
