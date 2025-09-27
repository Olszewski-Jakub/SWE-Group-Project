package ie.universityofgalway.groupnine.infrastructure.auth.adapter;

import ie.universityofgalway.groupnine.domain.user.Email;
import ie.universityofgalway.groupnine.domain.user.User;
import ie.universityofgalway.groupnine.domain.user.UserId;
import ie.universityofgalway.groupnine.domain.user.UserStatus;
import ie.universityofgalway.groupnine.infrastructure.auth.jpa.UserEntity;
import ie.universityofgalway.groupnine.infrastructure.auth.jpa.UserJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JpaUserRepositoryAdapterTest {

    private UserJpaRepository repo;
    private JpaUserRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        repo = mock(UserJpaRepository.class);
        adapter = new JpaUserRepositoryAdapter(repo);
    }

    @Test
    void save_and_find_roundtrip() {
        Instant now = Instant.parse("2024-01-01T00:00:00Z");
        User domain = new User(UserId.newId(), Email.of("u@example.com"), "U", "S", UserStatus.ACTIVE, false, "hash", now, now);

        when(repo.save(any(UserEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(repo.existsByEmail("u@example.com")).thenReturn(true);

        User saved = adapter.save(domain);
        assertEquals(domain.getEmail(), saved.getEmail());
        assertFalse(saved.isEmailVerified());

        assertTrue(adapter.existsByEmail(Email.of("u@example.com")));

        // map back from repo
        UserEntity e = new UserEntity();
        e.setId(saved.getId().value());
        e.setEmail(saved.getEmail().value());
        e.setFirstName(saved.getFirstName());
        e.setLastName(saved.getLastName());
        e.setStatus(saved.getStatus().name());
        e.setEmailVerified(saved.isEmailVerified());
        e.setPasswordHash(saved.getPasswordHash());
        e.setCreatedAt(saved.getCreatedAt());
        e.setUpdatedAt(saved.getUpdatedAt());

        when(repo.findByEmail(eq("u@example.com"))).thenReturn(Optional.of(e));

        Optional<User> found = adapter.findByEmail(Email.of("u@example.com"));
        assertTrue(found.isPresent());
        assertEquals("U", found.get().getFirstName());
    }
}
