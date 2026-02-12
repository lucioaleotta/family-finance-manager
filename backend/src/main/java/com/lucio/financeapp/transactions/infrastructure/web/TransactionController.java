package com.lucio.financeapp.transactions.infrastructure.web;

import com.lucio.financeapp.transactions.application.RegisterTransactionUseCase;
import com.lucio.financeapp.transactions.domain.TransactionType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final RegisterTransactionUseCase useCase;

    public TransactionController(RegisterTransactionUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UUID register(@RequestBody CreateTransactionRequest request) {
        return useCase.handle(request.toCommand());
    }

    record CreateTransactionRequest(
            @NotNull @Positive BigDecimal amount,
            @NotNull LocalDate date,
            @NotNull TransactionType type,
            @NotNull String category,
            String description
    ) {
        RegisterTransactionUseCase.RegisterTransactionCommand toCommand() {
            return new RegisterTransactionUseCase.RegisterTransactionCommand(
                    amount, date, type, category, description
            );
        }
    }
}
