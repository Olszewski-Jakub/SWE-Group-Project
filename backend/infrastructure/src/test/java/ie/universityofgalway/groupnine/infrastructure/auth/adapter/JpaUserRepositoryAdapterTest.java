package ie.universityofgalway.groupnine.infrastructure.auth.adapter;

import ie.universityofgalway.groupnine.domain.auth.EmailAlreadyUsed;
import ie.universityofgalway.groupnine.domain.user.Email;
import ie.universityofgalway.groupnine.domain.user.User;
import ie.universityofgalway.groupnine.domain.user.UserId;
import ie.universityofgalway.groupnine.domain.user.UserStatus;
import ie.universityofgalway.groupnine.infrastructure.auth.jpa.UserEntity;
import ie.universityofgalway.groupnine.infrastructure.auth.jpa.UserJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class JpaUserRepositoryAdapterTest {
    @Test
    void existsAndFindMapCorrectly() {
        UserJpaRepository repo = mock(UserJpaRepository.class);
        JpaUserRepositoryAdapter adapter = new JpaUserRepositoryAdapter(repo);
        when(repo.existsByEmail("a@b.com")).thenReturn(true);
        assertTrue(adapter.existsByEmail(Email.of("a@b.com")));

        UserEntity e = new UserEntity();
        UUID id = UUID.randomUUID();
        e.setId(id);
        e.setEmail("a@b.com");
        e.setFirstName("fn");
        e.setLastName("ln");
        e.setEmailVerified(true);
        e.setPasswordHash("h");
        e.setCreatedAt(Instant.now());
        e.setUpdatedAt(Instant.now());
        when(repo.findByEmail("a@b.com")).thenReturn(Optional.of(e));
        Optional<User> u = adapter.findByEmail(Email.of("a@b.com"));
        assertTrue(u.isPresent());
        assertEquals(id, u.get().getId().value());
        assertEquals("a@b.com", u.get().getEmail().value());
    }

    @Test
    void saveTranslatesUniqueViolationToDomain() {
        UserJpaRepository repo = mock(UserJpaRepository.class);
        JpaUserRepositoryAdapter adapter = new JpaUserRepositoryAdapter(repo);
        User user = new User(UserId.newId(), Email.of("a@b.com"), "fn", "ln", UserStatus.ACTIVE, true, "h", Instant.now(), Instant.now());
        when(repo.save(any(UserEntity.class))).thenThrow(new DataIntegrityViolationException("dup"));
        assertThrows(EmailAlreadyUsed.class, () -> adapter.save(user));
    }
}

