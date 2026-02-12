package com.lucio.financeapp.reporting.infrastructure.web;

import com.lucio.financeapp.reporting.api.MonthlyBalanceView;
import com.lucio.financeapp.reporting.application.ComputeMonthlyBalanceUseCase;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;

@RestController
@RequestMapping("/api/reporting")
public class ReportingController {

    private final ComputeMonthlyBalanceUseCase useCase;

    public ReportingController(ComputeMonthlyBalanceUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping("/monthly")
    public MonthlyBalanceView monthly(@RequestParam("month") String month) {
        return useCase.handle(YearMonth.parse(month)); // formato: YYYY-MM
    }
}
