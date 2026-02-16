package com.lucio.financeapp.transactions.infrastructure.web;

import com.lucio.financeapp.transactions.application.CreateTransferUseCase;
import com.lucio.financeapp.shared.domain.Currency;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/transfers")
public class TransferController {

    private final CreateTransferUseCase useCase;

    public TransferController(CreateTransferUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UUID create(@Valid @RequestBody CreateTransferRequest request) {
        return useCase.handle(new CreateTransferUseCase.CreateTransferCommand(
                request.fromAccountId(),
                request.toAccountId(),
                request.amount(),
                request.currency(),
                request.date(),
                request.description()));
    }

    record CreateTransferRequest(
            @NotNull UUID fromAccountId,
            @NotNull UUID toAccountId,
            @NotNull @Positive BigDecimal amount,
            @NotNull Currency currency,
            @NotNull LocalDate date,
            String description) {
    }
}
