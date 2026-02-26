package com.lucio.financeapp.users.infrastructure.persistence;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.lucio.financeapp.users.domain.PasswordResetToken;

@Repository
public class PasswordResetTokenJpaRepository {

    private final SpringDataPasswordResetTokenRepository delegate;

    public PasswordResetTokenJpaRepository(SpringDataPasswordResetTokenRepository delegate) {
        this.delegate = delegate;
    }

    public PasswordResetToken save(PasswordResetToken token) {
        return delegate.save(Objects.requireNonNull(token));
    }

    public Optional<PasswordResetToken> findActiveByTokenHash(String tokenHash, Instant now) {
        return delegate.findFirstByTokenHashAndUsedAtIsNullAndExpiresAtAfter(
                Objects.requireNonNull(tokenHash),
                Objects.requireNonNull(now));
    }

    public void deleteByUserId(UUID userId) {
        delegate.deleteByUserId(Objects.requireNonNull(userId));
    }

    public void deleteByUserIdAndIdNot(UUID userId, UUID id) {
        delegate.deleteByUserIdAndIdNot(Objects.requireNonNull(userId), Objects.requireNonNull(id));
    }
}

interface SpringDataPasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {

    Optional<PasswordResetToken> findFirstByTokenHashAndUsedAtIsNullAndExpiresAtAfter(String tokenHash, Instant now);

    void deleteByUserId(UUID userId);

    void deleteByUserIdAndIdNot(UUID userId, UUID id);
}
