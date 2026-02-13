package com.lucio.financeapp.reporting.infrastructure.web;

import com.lucio.financeapp.reporting.api.MonthlyBalanceView;
import com.lucio.financeapp.reporting.application.ComputeMonthlyBalanceUseCase;
import org.springframework.web.bind.annotation.*;
import com.lucio.financeapp.reporting.api.AnnualTotalView;
import com.lucio.financeapp.reporting.api.MonthlySummaryView;
import com.lucio.financeapp.reporting.application.ComputeAnnualTimelineUseCase;
import com.lucio.financeapp.reporting.application.ComputeAnnualTotalUseCase;
// ...
import java.util.List;
import java.time.YearMonth;

@RestController
@RequestMapping("/api/reporting")
public class ReportingController {

    private final ComputeMonthlyBalanceUseCase useCase;
    private final ComputeAnnualTimelineUseCase annualTimeline;
    private final ComputeAnnualTotalUseCase annualTotal;

    public ReportingController(ComputeMonthlyBalanceUseCase useCase,
            ComputeAnnualTimelineUseCase annualTimeline,
            ComputeAnnualTotalUseCase annualTotal) {
        this.useCase = useCase;
        this.annualTimeline = annualTimeline;
        this.annualTotal = annualTotal;
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
}
