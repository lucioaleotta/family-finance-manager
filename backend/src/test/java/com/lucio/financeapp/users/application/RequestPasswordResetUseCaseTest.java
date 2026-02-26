package com.lucio.financeapp.users.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.lucio.financeapp.users.domain.PasswordResetToken;
import com.lucio.financeapp.users.domain.User;
import com.lucio.financeapp.users.infrastructure.config.PasswordResetProperties;
import com.lucio.financeapp.users.infrastructure.persistence.PasswordResetTokenJpaRepository;
import com.lucio.financeapp.users.infrastructure.persistence.UserJpaRepository;

@ExtendWith(MockitoExtension.class)
class RequestPasswordResetUseCaseTest {

    @Mock
    private UserJpaRepository userRepository;

    @Mock
    private PasswordResetTokenJpaRepository tokenRepository;

    @Mock
    private PasswordResetTokenCodec tokenCodec;

    @Mock
    private PasswordResetNotifier notifier;

    @Mock
    private PasswordResetProperties properties;

    @InjectMocks
    private RequestPasswordResetUseCase useCase;

    @Captor
    private ArgumentCaptor<PasswordResetToken> tokenCaptor;

    @Test
    void shouldGenerateAndSendResetTokenWhenEmailExists() {
        User user = new User(UUID.randomUUID(), "lucio", "lucio@example.com", "hash", "EUR");
        when(userRepository.findByEmail("lucio@example.com")).thenReturn(Optional.of(user));
        when(tokenCodec.generateToken()).thenReturn("raw-token");
        when(tokenCodec.hashToken("raw-token")).thenReturn("hashed-token");
        when(properties.getTokenMinutes()).thenReturn(30L);
        when(properties.buildResetLink("raw-token")).thenReturn("http://localhost:3000/reset-password?token=raw-token");

        useCase.handle("lucio@example.com");

        verify(tokenRepository).deleteByUserId(user.getId());
        verify(tokenRepository).save(tokenCaptor.capture());
        verify(notifier).sendPasswordResetEmail("lucio@example.com",
                "http://localhost:3000/reset-password?token=raw-token");

        PasswordResetToken savedToken = tokenCaptor.getValue();
        assertEquals(user.getId(), savedToken.getUserId());
        assertEquals("hashed-token", savedToken.getTokenHash());
        assertEquals(null, savedToken.getUsedAt());
        Instant now = Instant.now();
        assertEquals(true, savedToken.getExpiresAt().isAfter(now.minusSeconds(60)));
    }

    @Test
    void shouldNotRevealEmailExistenceWhenEmailIsMissing() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        useCase.handle("missing@example.com");

        verify(tokenRepository, never()).deleteByUserId(any(UUID.class));
        verify(tokenRepository, never()).save(any(PasswordResetToken.class));
        verify(notifier, never()).sendPasswordResetEmail(any(String.class), any(String.class));
    }
}
