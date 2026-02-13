package com.lucio.financeapp.reporting.api;

import java.math.BigDecimal;
import java.time.YearMonth;

public record MonthlySummaryView(
        YearMonth month,
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        BigDecimal result) {
}
