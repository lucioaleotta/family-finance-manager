package com.lucio.financeapp.users.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

import com.lucio.financeapp.users.api.RegisterRequest;
import com.lucio.financeapp.users.domain.User;
import com.lucio.financeapp.users.infrastructure.persistence.UserJpaRepository;

@ExtendWith(MockitoExtension.class)
class RegisterUserUseCaseTest {

    @Mock
    private UserJpaRepository repo;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private RegisterUserUseCase useCase;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    @Test
    void shouldRegisterUserWithDefaultCurrencyWhenNotProvided() {
        RegisterRequest request = new RegisterRequest("lucio", "StrongPass1", null);

        when(repo.findByUsername("lucio")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("StrongPass1")).thenReturn("encoded-password");
        when(repo.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        useCase.handle(request);

        verify(repo).save(userCaptor.capture());
        User saved = userCaptor.getValue();

        assertEquals("lucio", saved.getUsername());
        assertEquals("encoded-password", saved.getPasswordHash());
        assertEquals("EUR", saved.getBaseCurrency());
    }

    @Test
    void shouldRejectDuplicateUsername() {
        RegisterRequest request = new RegisterRequest("lucio", "StrongPass1", "CHF");
        when(repo.findByUsername("lucio"))
                .thenReturn(Optional.of(new User(UUID.randomUUID(), "lucio", "existing-hash", "CHF")));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> useCase.handle(request));

        assertEquals("Username already exists", ex.getMessage());
        verify(passwordEncoder, never()).encode(any(String.class));
        verify(repo, never()).save(any(User.class));
    }

    @Test
    void shouldRejectWeakPassword() {
        RegisterRequest request = new RegisterRequest("lucio", "weak", "EUR");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> useCase.handle(request));

        assertEquals("password is too weak", ex.getMessage());
        verify(passwordEncoder, never()).encode(any(String.class));
        verify(repo, never()).save(any(User.class));
    }
}
