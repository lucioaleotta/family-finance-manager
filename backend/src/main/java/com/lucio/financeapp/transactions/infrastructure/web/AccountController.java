package com.lucio.financeapp.transactions.infrastructure.web;

import com.lucio.financeapp.transactions.api.AccountView;
import com.lucio.financeapp.transactions.application.CreateAccountUseCase;
import com.lucio.financeapp.transactions.application.DeleteAccountUseCase;
import com.lucio.financeapp.transactions.application.UpdateAccountUseCase;
import com.lucio.financeapp.transactions.application.ListAccountsUseCase;
import com.lucio.financeapp.transactions.domain.AccountType;
import com.lucio.financeapp.shared.domain.Currency;
import com.lucio.financeapp.transactions.api.AccountBalanceView;
import com.lucio.financeapp.transactions.application.ComputeAccountBalanceUseCase;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final CreateAccountUseCase createUseCase;
    private final UpdateAccountUseCase updateUseCase;
    private final DeleteAccountUseCase deleteUseCase;
    private final ListAccountsUseCase listUseCase;
    private final ComputeAccountBalanceUseCase balanceUseCase;

    public AccountController(CreateAccountUseCase createUseCase,
            UpdateAccountUseCase updateUseCase,
            DeleteAccountUseCase deleteUseCase,
            ListAccountsUseCase listUseCase,
            ComputeAccountBalanceUseCase balanceUseCase) {
        this.createUseCase = createUseCase;
        this.updateUseCase = updateUseCase;
        this.deleteUseCase = deleteUseCase;
        this.listUseCase = listUseCase;
        this.balanceUseCase = balanceUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UUID create(@Valid @RequestBody CreateAccountRequest request) {
        try {
            return createUseCase.handle(new CreateAccountUseCase.CreateAccountCommand(
                    request.name(), request.type(), request.currency()));
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
    }

    @GetMapping
    public List<AccountView> list(@RequestParam(value = "type", required = false) AccountType type) {
        return type == null ? listUseCase.handle() : listUseCase.handle(type);
    }

    @PutMapping("/{accountId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(@PathVariable UUID accountId, @Valid @RequestBody UpdateAccountRequest request) {
        try {
            updateUseCase.handle(new UpdateAccountUseCase.UpdateAccountCommand(
                    accountId, request.name(), request.type(), request.currency()));
        } catch (IllegalArgumentException ex) {
            String message = ex.getMessage();
            boolean notFound = message != null && message.toLowerCase().contains("not found");
            throw new ResponseStatusException(notFound ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST, message);
        }
    }

    @DeleteMapping("/{accountId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID accountId) {
        try {
            deleteUseCase.handle(accountId);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Account cannot be deleted because it is referenced by existing data");
        }
    }

    record CreateAccountRequest(
            @NotBlank String name,
            @NotNull AccountType type,
            @NotNull Currency currency) {
    }

    record UpdateAccountRequest(
            @NotBlank String name,
            @NotNull AccountType type,
            @NotNull Currency currency) {
    }

    @GetMapping("/{accountId}/balance")
    public AccountBalanceView balance(@PathVariable UUID accountId, @RequestParam("asOf") String asOf) {
        return balanceUseCase.handle(accountId, LocalDate.parse(asOf));
    }

}
