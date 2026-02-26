package com.lucio.financeapp.users.application;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.lucio.financeapp.users.domain.PasswordResetToken;
import com.lucio.financeapp.users.domain.User;
import com.lucio.financeapp.users.infrastructure.config.PasswordResetProperties;
import com.lucio.financeapp.users.infrastructure.persistence.PasswordResetTokenJpaRepository;
import com.lucio.financeapp.users.infrastructure.persistence.UserJpaRepository;

@Component
public class RequestPasswordResetUseCase {

    private final UserJpaRepository userRepository;
    private final PasswordResetTokenJpaRepository tokenRepository;
    private final PasswordResetTokenCodec tokenCodec;
    private final PasswordResetNotifier notifier;
    private final PasswordResetProperties properties;

    public RequestPasswordResetUseCase(UserJpaRepository userRepository,
            PasswordResetTokenJpaRepository tokenRepository,
            PasswordResetTokenCodec tokenCodec,
            PasswordResetNotifier notifier,
            PasswordResetProperties properties) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.tokenCodec = tokenCodec;
        this.notifier = notifier;
        this.properties = properties;
    }

    @Transactional
    public void handle(String rawEmail) {
        if (rawEmail == null) {
            return;
        }

        String email = rawEmail.trim().toLowerCase();
        if (email.isBlank()) {
            return;
        }

        userRepository.findByEmail(email).ifPresent(this::createAndSendToken);
    }

    private void createAndSendToken(User user) {
        tokenRepository.deleteByUserId(user.getId());

        String token = tokenCodec.generateToken();
        String tokenHash = tokenCodec.hashToken(token);
        Instant now = Instant.now();
        Instant expiresAt = now.plus(properties.getTokenMinutes(), ChronoUnit.MINUTES);

        PasswordResetToken resetToken = PasswordResetToken.issue(user.getId(), tokenHash, expiresAt, now);
        tokenRepository.save(resetToken);

        notifier.sendPasswordResetEmail(user.getEmail(), properties.buildResetLink(token));
    }
}
