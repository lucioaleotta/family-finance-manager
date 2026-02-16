package com.lucio.financeapp.assets.infrastructure.web;

import com.lucio.financeapp.assets.application.UpsertInvestmentSnapshotUseCase;
import com.lucio.financeapp.shared.domain.Currency;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.YearMonth;

@RestController
@RequestMapping("/api/assets/investments")
public class InvestmentSnapshotController {

    private final UpsertInvestmentSnapshotUseCase useCase;

    public InvestmentSnapshotController(UpsertInvestmentSnapshotUseCase useCase) {
        this.useCase = useCase;
    }

    @PutMapping("/snapshots")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void upsert(@Valid @RequestBody UpsertRequest request) {
        useCase.handle(new UpsertInvestmentSnapshotUseCase.Command(
                YearMonth.parse(request.month()),
                request.totalInvested(),
                request.currency(),
                request.note()));
    }

    record UpsertRequest(
            @NotNull String month, // YYYY-MM
            @NotNull @PositiveOrZero BigDecimal totalInvested,
            @NotNull Currency currency,
            String note) {
    }
}
