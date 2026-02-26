package com.lucio.financeapp.users.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.lucio.financeapp.users.application.RequestPasswordResetUseCase;
import com.lucio.financeapp.users.application.RegisterUserUseCase;
import com.lucio.financeapp.users.application.ResetPasswordUseCase;
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
    private final RequestPasswordResetUseCase requestPasswordResetUseCase;
    private final ResetPasswordUseCase resetPasswordUseCase;

    public AuthController(RegisterUserUseCase registerUseCase,
            UserJpaRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            RequestPasswordResetUseCase requestPasswordResetUseCase,
            ResetPasswordUseCase resetPasswordUseCase) {
        this.registerUseCase = registerUseCase;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.requestPasswordResetUseCase = requestPasswordResetUseCase;
        this.resetPasswordUseCase = resetPasswordUseCase;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            registerUseCase.handle(request);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
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

    @PostMapping("/forgot-password")
    public ForgotPasswordResponse forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        requestPasswordResetUseCase.handle(request.email());
        return new ForgotPasswordResponse(
                "Se l'indirizzo email è registrato, riceverai a breve le istruzioni per reimpostare la password.");
    }

    @GetMapping("/reset-password/validate")
    public TokenValidationResponse validateResetToken(@RequestParam("token") String token) {
        return new TokenValidationResponse(resetPasswordUseCase.isTokenValid(token));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "password confirmation does not match");
        }

        try {
            resetPasswordUseCase.handle(request.token(), request.newPassword());
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
    }

    record LoginRequest(@NotBlank String username, @NotBlank String password) {
    }

    record LoginResponse(String accessToken, String userId, String username, String baseCurrency) {
    }

    record ForgotPasswordRequest(@NotBlank @Email String email) {
    }

    record ForgotPasswordResponse(String message) {
    }

    record TokenValidationResponse(boolean valid) {
    }

    record ResetPasswordRequest(@NotBlank String token, @NotBlank String newPassword,
            @NotBlank String confirmPassword) {
    }
}
