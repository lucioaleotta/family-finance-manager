package com.lucio.financeapp.users.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "username is required") @Size(min = 3, max = 50, message = "username must be between 3 and 50 characters") String username,

        @NotBlank(message = "password is required") @Size(min = 8, max = 72, message = "password must be between 8 and 72 characters") @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$", message = "password must contain uppercase, lowercase and digit") String password,

        @Pattern(regexp = "^(EUR|CHF)$", message = "baseCurrency must be EUR or CHF") String baseCurrency) {
}
