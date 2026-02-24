package com.lucio.financeapp.assets.infrastructure.web;

import com.lucio.financeapp.assets.api.CategoryMonthlyTotalView;
import com.lucio.financeapp.assets.application.UpsertInvestmentSnapshotUseCase;
import com.lucio.financeapp.assets.application.ListLastInvestmentSnapshotsUseCase;
import com.lucio.financeapp.assets.application.ComputeInvestmentMonthlyTotalsUseCase;
import com.lucio.financeapp.assets.api.InvestmentSnapshotView;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/assets/investments")
public class InvestmentSnapshotController {

    private final UpsertInvestmentSnapshotUseCase useCase;
    private final ListLastInvestmentSnapshotsUseCase listLastUseCase;
    private final ComputeInvestmentMonthlyTotalsUseCase totalsUseCase;

    public InvestmentSnapshotController(UpsertInvestmentSnapshotUseCase useCase,
            ListLastInvestmentSnapshotsUseCase listLastUseCase,
            ComputeInvestmentMonthlyTotalsUseCase totalsUseCase) {
        this.useCase = useCase;
        this.listLastUseCase = listLastUseCase;
        this.totalsUseCase = totalsUseCase;
    }

    @PutMapping("/snapshots")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void upsert(@Valid @RequestBody UpsertRequest request) {
        useCase.handle(new UpsertInvestmentSnapshotUseCase.Command(
                YearMonth.parse(request.month()),
                request.accountId(),
                request.totalInvested(),
                request.note()));
    }

    @GetMapping("/snapshots/last-12")
    public List<InvestmentSnapshotView> last12(
            @RequestParam(value = "month", required = false) String month,
            @RequestParam("accountId") UUID accountId) {
        YearMonth endMonth = month == null ? null : YearMonth.parse(month);
        return listLastUseCase.handle(endMonth, accountId);
    }

    @GetMapping("/totals")
    public List<CategoryMonthlyTotalView> totals(
            @RequestParam(value = "month", required = false) String month) {
        YearMonth targetMonth = month == null ? YearMonth.now() : YearMonth.parse(month);
        return totalsUseCase.handle(targetMonth);
    }

    record UpsertRequest(
            @NotNull String month, // YYYY-MM
            @NotNull UUID accountId,
            @NotNull @PositiveOrZero BigDecimal totalInvested,
            String note) {
    }
}
