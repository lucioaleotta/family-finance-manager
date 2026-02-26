package com.lucio.financeapp.users.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.lucio.financeapp.users.domain.PasswordResetToken;
import com.lucio.financeapp.users.domain.User;
import com.lucio.financeapp.users.infrastructure.persistence.PasswordResetTokenJpaRepository;
import com.lucio.financeapp.users.infrastructure.persistence.UserJpaRepository;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class ResetPasswordUseCaseTest {

    @Mock
    private PasswordResetTokenJpaRepository tokenRepository;

    @Mock
    private UserJpaRepository userRepository;

    @Mock
    private PasswordResetTokenCodec tokenCodec;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private ResetPasswordUseCase useCase;

    @Captor
    private ArgumentCaptor<PasswordResetToken> tokenCaptor;

    @Test
    void shouldResetPasswordAndInvalidateToken() {
        UUID userId = UUID.randomUUID();
        PasswordResetToken token = PasswordResetToken.issue(
                userId,
                "hashed-token",
                Instant.now().plus(10, ChronoUnit.MINUTES),
                Instant.now());
        User user = new User(userId, "lucio", "lucio@example.com", "old-hash", "EUR");

        when(tokenCodec.hashToken("raw-token")).thenReturn("hashed-token");
        when(tokenRepository.findActiveByTokenHash(any(String.class), any(Instant.class)))
                .thenReturn(Optional.of(token));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("StrongPass1")).thenReturn("new-hash");

        useCase.handle("raw-token", "StrongPass1");

        verify(userRepository).save(user);
        assertEquals("new-hash", user.getPasswordHash());

        verify(tokenRepository).save(tokenCaptor.capture());
        PasswordResetToken savedToken = tokenCaptor.getValue();
        assertTrue(savedToken.getUsedAt() != null);
        verify(tokenRepository).deleteByUserIdAndIdNot(userId, token.getId());
    }

    @Test
    void shouldRejectInvalidOrExpiredToken() {
        when(tokenCodec.hashToken("raw-token")).thenReturn("hashed-token");
        when(tokenRepository.findActiveByTokenHash(any(String.class), any(Instant.class))).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> useCase.handle("raw-token", "StrongPass1"));

        assertEquals("token is invalid or expired", ex.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldRejectWeakPassword() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> useCase.handle("raw-token", "weak"));

        assertEquals("password is too weak", ex.getMessage());
        verify(tokenRepository, never()).findActiveByTokenHash(any(String.class), any(Instant.class));
    }

    @Test
    void shouldValidateTokenState() {
        when(tokenCodec.hashToken("raw-token")).thenReturn("hashed-token");
        when(tokenRepository.findActiveByTokenHash(any(String.class), any(Instant.class)))
                .thenReturn(Optional.of(PasswordResetToken.issue(UUID.randomUUID(), "hashed-token",
                        Instant.now().plus(5, ChronoUnit.MINUTES), Instant.now())));

        assertTrue(useCase.isTokenValid("raw-token"));

        when(tokenRepository.findActiveByTokenHash(any(String.class), any(Instant.class))).thenReturn(Optional.empty());
        assertFalse(useCase.isTokenValid("raw-token"));
        assertFalse(useCase.isTokenValid("   "));
    }
}
