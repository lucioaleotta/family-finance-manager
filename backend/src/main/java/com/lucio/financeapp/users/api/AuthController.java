package com.lucio.financeapp.users.api;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.lucio.financeapp.users.application.RegisterUserUseCase;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final RegisterUserUseCase registerUseCase;

    public AuthController(RegisterUserUseCase registerUseCase) {
        this.registerUseCase = registerUseCase;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        registerUseCase.handle(request);
        return ResponseEntity.ok().build();
    }
}
