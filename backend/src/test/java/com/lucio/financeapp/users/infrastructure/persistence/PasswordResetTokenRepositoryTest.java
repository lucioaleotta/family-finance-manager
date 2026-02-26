package com.lucio.financeapp.users.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.lucio.financeapp.users.domain.PasswordResetToken;
import com.lucio.financeapp.users.domain.User;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.AUTO_CONFIGURED)
@SuppressWarnings("null")
class PasswordResetTokenRepositoryTest {

    @Autowired
    private SpringDataPasswordResetTokenRepository tokenRepository;

    @Autowired
    private SpringDataUserRepository userRepository;

    @Test
    void shouldFindOnlyActiveToken() {
        User user = new User(UUID.randomUUID(), "anna", "anna@example.com", "hash-1", "EUR");
        userRepository.save(user);

        PasswordResetToken active = PasswordResetToken.issue(
                user.getId(),
                "active-hash",
                Instant.now().plus(10, ChronoUnit.MINUTES),
                Instant.now());

        PasswordResetToken expired = PasswordResetToken.issue(
                user.getId(),
                "expired-hash",
                Instant.now().minus(10, ChronoUnit.MINUTES),
                Instant.now());

        PasswordResetToken used = PasswordResetToken.issue(
                user.getId(),
                "used-hash",
                Instant.now().plus(10, ChronoUnit.MINUTES),
                Instant.now());
        used.markUsed(Instant.now());

        tokenRepository.save(active);
        tokenRepository.save(expired);
        tokenRepository.save(used);

        Optional<PasswordResetToken> foundActive = tokenRepository.findFirstByTokenHashAndUsedAtIsNullAndExpiresAtAfter(
                "active-hash", Instant.now());
        Optional<PasswordResetToken> foundExpired = tokenRepository
                .findFirstByTokenHashAndUsedAtIsNullAndExpiresAtAfter(
                        "expired-hash", Instant.now());
        Optional<PasswordResetToken> foundUsed = tokenRepository.findFirstByTokenHashAndUsedAtIsNullAndExpiresAtAfter(
                "used-hash", Instant.now());

        assertThat(foundActive).isPresent();
        assertThat(foundExpired).isEmpty();
        assertThat(foundUsed).isEmpty();
    }
}
