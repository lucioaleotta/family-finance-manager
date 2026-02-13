package com.lucio.financeapp.transactions.infrastructure.web;

import com.lucio.financeapp.transactions.api.AccountView;
import com.lucio.financeapp.transactions.application.CreateAccountUseCase;
import com.lucio.financeapp.transactions.application.ListAccountsUseCase;
import com.lucio.financeapp.transactions.domain.AccountType;
import com.lucio.financeapp.transactions.domain.Currency;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final CreateAccountUseCase createUseCase;
    private final ListAccountsUseCase listUseCase;

    public AccountController(CreateAccountUseCase createUseCase, ListAccountsUseCase listUseCase) {
        this.createUseCase = createUseCase;
        this.listUseCase = listUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UUID create(@Valid @RequestBody CreateAccountRequest request) {
        return createUseCase.handle(new CreateAccountUseCase.CreateAccountCommand(
                request.name(), request.type(), request.currency()));
    }

    @GetMapping
    public List<AccountView> list() {
        return listUseCase.handle();
    }

    record CreateAccountRequest(
            @NotBlank String name,
            @NotNull AccountType type,
            @NotNull Currency currency) {
    }
}
