package com.lucio.financeapp.users.application;

import java.util.Objects;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import com.lucio.financeapp.users.api.RegisterRequest;
import com.lucio.financeapp.users.domain.User;
import com.lucio.financeapp.users.infrastructure.persistence.UserJpaRepository;

@Component
public class RegisterUserUseCase {

    private final UserJpaRepository repo;
    private final PasswordEncoder passwordEncoder;

    public RegisterUserUseCase(UserJpaRepository repo,
            PasswordEncoder passwordEncoder) {
        this.repo = repo;
        this.passwordEncoder = passwordEncoder;
    }

    public void handle(RegisterRequest request) {

        String username = Objects.requireNonNull(request.username(), "username is required").trim();
        String email = Objects.requireNonNull(request.email(), "email is required").trim().toLowerCase();
        String password = Objects.requireNonNull(request.password(), "password is required");
        String baseCurrency = request.baseCurrency() == null ? "EUR" : request.baseCurrency();

        if (username.isBlank()) {
            throw new IllegalArgumentException("username is required");
        }

        if (email.isBlank()) {
            throw new IllegalArgumentException("email is required");
        }

        PasswordPolicy.validateOrThrow(password);

        if (repo.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }

        if (repo.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = new User(
                UUID.randomUUID(),
                username,
                email,
                passwordEncoder.encode(password),
                baseCurrency);

        repo.save(user);
    }
}
