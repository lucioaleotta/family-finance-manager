package com.lucio.financeapp.assets.infrastructure.web;

import com.lucio.financeapp.assets.api.CategoryMonthlyTotalView;
import com.lucio.financeapp.assets.application.UpsertInvestmentSnapshotUseCase;
import com.lucio.financeapp.assets.application.ListLastInvestmentSnapshotsUseCase;
import com.lucio.financeapp.assets.application.ComputeInvestmentMonthlyTotalsUseCase;
import com.lucio.financeapp.assets.api.InvestmentSnapshotView;
import com.lucio.financeapp.users.infrastructure.security.CurrentUser;
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
    private final CurrentUser currentUser;

    public InvestmentSnapshotController(UpsertInvestmentSnapshotUseCase useCase,
            ListLastInvestmentSnapshotsUseCase listLastUseCase,
            ComputeInvestmentMonthlyTotalsUseCase totalsUseCase,
            CurrentUser currentUser) {
        this.useCase = useCase;
        this.listLastUseCase = listLastUseCase;
        this.totalsUseCase = totalsUseCase;
        this.currentUser = currentUser;
    }

    @PutMapping("/snapshots")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void upsert(@Valid @RequestBody UpsertRequest request) {
        UUID userId = currentUser.requireUserId();
        useCase.handle(userId, new UpsertInvestmentSnapshotUseCase.Command(
                YearMonth.parse(request.month()),
                request.accountId(),
                request.totalInvested(),
                request.note()));
    }

    @GetMapping("/snapshots/last-12")
    public List<InvestmentSnapshotView> last12(
            @RequestParam(value = "month", required = false) String month,
            @RequestParam("accountId") UUID accountId) {
        UUID userId = currentUser.requireUserId();
        YearMonth endMonth = month == null ? null : YearMonth.parse(month);
        return listLastUseCase.handle(userId, endMonth, accountId);
    }

    @GetMapping("/totals")
    public List<CategoryMonthlyTotalView> totals(
            @RequestParam(value = "month", required = false) String month) {
        UUID userId = currentUser.requireUserId();
        YearMonth targetMonth = month == null ? YearMonth.now() : YearMonth.parse(month);
        return totalsUseCase.handle(userId, targetMonth);
    }

    record UpsertRequest(
            @NotNull String month, // YYYY-MM
            @NotNull UUID accountId,
            @NotNull @PositiveOrZero BigDecimal totalInvested,
            String note) {
    }
}
