package com.lucio.financeapp.assets.infrastructure.web;

import com.lucio.financeapp.assets.application.UpsertInvestmentSnapshotUseCase;
import com.lucio.financeapp.assets.application.ListLastInvestmentSnapshotsUseCase;
import com.lucio.financeapp.assets.api.InvestmentSnapshotView;
import com.lucio.financeapp.config.FinanceProperties;
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
    private final ListLastInvestmentSnapshotsUseCase listLastUseCase;
    private final FinanceProperties financeProperties;

    public InvestmentSnapshotController(UpsertInvestmentSnapshotUseCase useCase,
            ListLastInvestmentSnapshotsUseCase listLastUseCase,
            FinanceProperties financeProperties) {
        this.useCase = useCase;
        this.listLastUseCase = listLastUseCase;
        this.financeProperties = financeProperties;
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

    @GetMapping("/snapshots/last-12")
    public java.util.List<InvestmentSnapshotView> last12(
            @RequestParam(value = "month", required = false) String month,
            @RequestParam(value = "currency", required = false) Currency currency) {
        YearMonth endMonth = month == null ? null : YearMonth.parse(month);
        Currency targetCurrency = currency == null ? financeProperties.getBaseCurrency() : currency;
        return listLastUseCase.handle(endMonth, targetCurrency);
    }

    record UpsertRequest(
            @NotNull String month, // YYYY-MM
            @NotNull @PositiveOrZero BigDecimal totalInvested,
            @NotNull Currency currency,
            String note) {
    }
}
