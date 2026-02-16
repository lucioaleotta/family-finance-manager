package com.lucio.financeapp.reporting.infrastructure.web;

import com.lucio.financeapp.reporting.api.MonthlyBalanceView;
import com.lucio.financeapp.reporting.application.ComputeMonthlyBalanceUseCase;
import org.springframework.web.bind.annotation.*;
import com.lucio.financeapp.reporting.api.AnnualTotalView;
import com.lucio.financeapp.reporting.api.MonthlySummaryView;
import com.lucio.financeapp.reporting.application.ComputeAnnualTimelineUseCase;
import com.lucio.financeapp.reporting.application.ComputeAnnualTotalUseCase;
import com.lucio.financeapp.reporting.api.MonthlyAccountSummaryView;
import com.lucio.financeapp.reporting.application.ComputeMonthlyAccountsUseCase;
import com.lucio.financeapp.reporting.api.AnnualAccountSummaryView;
import com.lucio.financeapp.reporting.application.ComputeAnnualAccountsUseCase;
import com.lucio.financeapp.reporting.api.MonthlyAccountTimelineView;
import com.lucio.financeapp.reporting.application.ComputeAnnualAccountsTimelineUseCase;

// ...
import java.util.List;
import java.time.YearMonth;

@RestController
@RequestMapping("/api/reporting")
public class ReportingController {

    private final ComputeMonthlyBalanceUseCase useCase;
    private final ComputeAnnualTimelineUseCase annualTimeline;
    private final ComputeAnnualTotalUseCase annualTotal;
    private final ComputeMonthlyAccountsUseCase monthlyAccounts;

    private final ComputeAnnualAccountsUseCase annualAccounts;

    private final ComputeAnnualAccountsTimelineUseCase annualAccountsTimeline;

    public ReportingController(ComputeMonthlyBalanceUseCase useCase,
            ComputeAnnualTimelineUseCase annualTimeline,
            ComputeAnnualTotalUseCase annualTotal,
            ComputeMonthlyAccountsUseCase monthlyAccounts,
            ComputeAnnualAccountsUseCase annualAccounts,
            ComputeAnnualAccountsTimelineUseCase annualAccountsTimeline) {
        this.useCase = useCase;
        this.annualTimeline = annualTimeline;
        this.annualTotal = annualTotal;
        this.monthlyAccounts = monthlyAccounts;
        this.annualAccounts = annualAccounts;
        this.annualAccountsTimeline = annualAccountsTimeline;
    }

    @GetMapping("/annual/timeline")
    public List<MonthlySummaryView> annualTimeline(@RequestParam("year") int year) {
        return annualTimeline.handle(year);
    }

    @GetMapping("/annual/total")
    public AnnualTotalView annualTotal(@RequestParam("year") int year) {
        return annualTotal.handle(year);
    }

    @GetMapping("/monthly")
    public MonthlyBalanceView monthly(@RequestParam("month") String month) {
        return useCase.handle(YearMonth.parse(month)); // formato: YYYY-MM
    }

    @GetMapping("/monthly/accounts")
    public List<MonthlyAccountSummaryView> monthlyAccounts(@RequestParam("month") String month) {
        return monthlyAccounts.handle(YearMonth.parse(month));
    }

    @GetMapping("/annual/accounts")
    public List<AnnualAccountSummaryView> annualAccounts(@RequestParam("year") int year) {
        return annualAccounts.handle(year);
    }

    @GetMapping("/annual/timeline/accounts")
    public List<MonthlyAccountTimelineView> annualAccountsTimeline(@RequestParam("year") int year) {
        return annualAccountsTimeline.handle(year);
    }

}
