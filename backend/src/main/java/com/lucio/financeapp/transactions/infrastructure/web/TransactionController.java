package com.lucio.financeapp.transactions.infrastructure.web;

import com.lucio.financeapp.transactions.api.TransactionView;
import com.lucio.financeapp.transactions.application.DeleteTransactionUseCase;
import com.lucio.financeapp.transactions.application.ListTransactionsByMonthUseCase;
import com.lucio.financeapp.transactions.application.RegisterTransactionUseCase;
import com.lucio.financeapp.transactions.application.UpdateTransactionUseCase;
import com.lucio.financeapp.shared.domain.Currency;
import com.lucio.financeapp.transactions.domain.TransactionType;
import com.lucio.financeapp.users.infrastructure.security.CurrentUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final RegisterTransactionUseCase registerUseCase;
    private final ListTransactionsByMonthUseCase listUseCase;
    private final UpdateTransactionUseCase updateUseCase;
    private final DeleteTransactionUseCase deleteUseCase;
    private final CurrentUser currentUser;

    public TransactionController(RegisterTransactionUseCase registerUseCase,
            ListTransactionsByMonthUseCase listUseCase,
            UpdateTransactionUseCase updateUseCase,
            DeleteTransactionUseCase deleteUseCase,
            CurrentUser currentUser) {
        this.registerUseCase = registerUseCase;
        this.listUseCase = listUseCase;
        this.updateUseCase = updateUseCase;
        this.deleteUseCase = deleteUseCase;
        this.currentUser = currentUser;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UUID register(@Valid @RequestBody CreateOrUpdateTransactionRequest request) {
        UUID userId = currentUser.requireUserId();
        return registerUseCase.handle(userId, request.toRegisterCommand());
    }

    @GetMapping
    public List<TransactionView> listByMonth(@RequestParam("month") String month) {
        UUID userId = currentUser.requireUserId();
        return listUseCase.handle(userId, YearMonth.parse(month)); // YYYY-MM
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(@PathVariable UUID id, @Valid @RequestBody CreateOrUpdateTransactionRequest request) {
        UUID userId = currentUser.requireUserId();
        updateUseCase.handle(userId, id, request.toUpdateCommand());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        UUID userId = currentUser.requireUserId();
        deleteUseCase.handle(userId, id);
    }

    record CreateOrUpdateTransactionRequest(
            UUID accountId,
            @NotNull BigDecimal amount,
            @NotNull Currency currency,

            @NotNull LocalDate date,
            @NotNull TransactionType type,
            @NotNull String category,
            String description) {
        RegisterTransactionUseCase.RegisterTransactionCommand toRegisterCommand() {
            return new RegisterTransactionUseCase.RegisterTransactionCommand(
                    accountId, amount, currency, date, type, category, description);
        }

        UpdateTransactionUseCase.UpdateTransactionCommand toUpdateCommand() {
            return new UpdateTransactionUseCase.UpdateTransactionCommand(
                    accountId, amount, currency, date, type, category, description);
        }
    }

}
