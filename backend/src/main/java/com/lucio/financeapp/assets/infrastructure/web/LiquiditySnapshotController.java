package com.lucio.financeapp.assets.infrastructure.web;

import com.lucio.financeapp.assets.api.CategoryMonthlyTotalView;
import com.lucio.financeapp.assets.application.ComputeLiquidityMonthlyTotalsUseCase;
import com.lucio.financeapp.assets.api.LiquiditySnapshotView;
import com.lucio.financeapp.assets.application.ListLastLiquiditySnapshotsUseCase;
import com.lucio.financeapp.assets.application.UpsertLiquiditySnapshotUseCase;
import com.lucio.financeapp.users.infrastructure.security.CurrentUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/assets/liquidity")
public class LiquiditySnapshotController {

    private final UpsertLiquiditySnapshotUseCase upsertUseCase;
    private final ListLastLiquiditySnapshotsUseCase listLastUseCase;
    private final ComputeLiquidityMonthlyTotalsUseCase totalsUseCase;
    private final CurrentUser currentUser;

    public LiquiditySnapshotController(UpsertLiquiditySnapshotUseCase upsertUseCase,
            ListLastLiquiditySnapshotsUseCase listLastUseCase,
            ComputeLiquidityMonthlyTotalsUseCase totalsUseCase,
            CurrentUser currentUser) {
        this.upsertUseCase = upsertUseCase;
        this.listLastUseCase = listLastUseCase;
        this.totalsUseCase = totalsUseCase;
        this.currentUser = currentUser;
    }

    @PutMapping("/snapshots")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void upsert(@Valid @RequestBody UpsertRequest request) {
        UUID userId = currentUser.requireUserId();
        upsertUseCase.handle(userId, new UpsertLiquiditySnapshotUseCase.Command(
                YearMonth.parse(request.month()),
                request.accountId(),
                request.liquidity(),
                request.note()));
    }

    @GetMapping("/snapshots/last-12")
    public List<LiquiditySnapshotView> last12(
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
            @NotNull String month,
            @NotNull UUID accountId,
            @NotNull @PositiveOrZero BigDecimal liquidity,
            String note) {
    }
}
