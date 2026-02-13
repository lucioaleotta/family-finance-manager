package com.lucio.financeapp.transactions.infrastructure.web;

import com.lucio.financeapp.transactions.api.TransactionView;
import com.lucio.financeapp.transactions.application.DeleteTransactionUseCase;
import com.lucio.financeapp.transactions.application.ListTransactionsByMonthUseCase;
import com.lucio.financeapp.transactions.application.RegisterTransactionUseCase;
import com.lucio.financeapp.transactions.application.UpdateTransactionUseCase;
import com.lucio.financeapp.shared.domain.Money;
import com.lucio.financeapp.transactions.domain.Currency;
import com.lucio.financeapp.transactions.domain.TransactionType;
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

    public TransactionController(RegisterTransactionUseCase registerUseCase,
            ListTransactionsByMonthUseCase listUseCase,
            UpdateTransactionUseCase updateUseCase,
            DeleteTransactionUseCase deleteUseCase) {
        this.registerUseCase = registerUseCase;
        this.listUseCase = listUseCase;
        this.updateUseCase = updateUseCase;
        this.deleteUseCase = deleteUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UUID register(@Valid @RequestBody CreateOrUpdateTransactionRequest request) {
        return registerUseCase.handle(request.toRegisterCommand());
    }

    @GetMapping
    public List<TransactionView> listByMonth(@RequestParam("month") String month) {
        return listUseCase.handle(YearMonth.parse(month)); // YYYY-MM
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(@PathVariable UUID id, @Valid @RequestBody CreateOrUpdateTransactionRequest request) {
        updateUseCase.handle(id, request.toUpdateCommand());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        deleteUseCase.handle(id);
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
