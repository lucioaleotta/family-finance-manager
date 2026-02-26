package com.lucio.financeapp.users.application;

import java.time.Instant;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.lucio.financeapp.users.domain.PasswordResetToken;
import com.lucio.financeapp.users.domain.User;
import com.lucio.financeapp.users.infrastructure.persistence.PasswordResetTokenJpaRepository;
import com.lucio.financeapp.users.infrastructure.persistence.UserJpaRepository;

@Component
public class ResetPasswordUseCase {

    private final PasswordResetTokenJpaRepository tokenRepository;
    private final UserJpaRepository userRepository;
    private final PasswordResetTokenCodec tokenCodec;
    private final PasswordEncoder passwordEncoder;

    public ResetPasswordUseCase(PasswordResetTokenJpaRepository tokenRepository,
            UserJpaRepository userRepository,
            PasswordResetTokenCodec tokenCodec,
            PasswordEncoder passwordEncoder) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
        this.tokenCodec = tokenCodec;
        this.passwordEncoder = passwordEncoder;
    }

    public boolean isTokenValid(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }

        String tokenHash = tokenCodec.hashToken(token.trim());
        return tokenRepository.findActiveByTokenHash(tokenHash, Instant.now()).isPresent();
    }

    @Transactional
    public void handle(String token, String newPassword) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("token is invalid or expired");
        }

        PasswordPolicy.validateOrThrow(newPassword);

        String tokenHash = tokenCodec.hashToken(token.trim());
        PasswordResetToken resetToken = tokenRepository.findActiveByTokenHash(tokenHash, Instant.now())
                .orElseThrow(() -> new IllegalArgumentException("token is invalid or expired"));

        User user = userRepository.findById(resetToken.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("token is invalid or expired"));

        user.updatePasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        Instant now = Instant.now();
        resetToken.markUsed(now);
        tokenRepository.save(resetToken);
        tokenRepository.deleteByUserIdAndIdNot(user.getId(), resetToken.getId());
    }
}
