package com.lucio.financeapp.users.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.lucio.financeapp.users.application.RegisterUserUseCase;
import com.lucio.financeapp.users.domain.User;
import com.lucio.financeapp.users.infrastructure.persistence.UserJpaRepository;
import com.lucio.financeapp.users.infrastructure.security.JwtService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final RegisterUserUseCase registerUseCase;
    private final UserJpaRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(RegisterUserUseCase registerUseCase,
            UserJpaRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {
        this.registerUseCase = registerUseCase;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        registerUseCase.handle(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        User user = userRepository.findByUsername(request.username().trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        String token = jwtService.generateAccessToken(user);
        return new LoginResponse(token, user.getId().toString(), user.getUsername(), user.getBaseCurrency());
    }

    record LoginRequest(@NotBlank String username, @NotBlank String password) {
    }

    record LoginResponse(String accessToken, String userId, String username, String baseCurrency) {
    }
}
