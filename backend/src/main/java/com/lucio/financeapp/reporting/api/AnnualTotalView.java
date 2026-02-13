package com.lucio.financeapp.reporting.api;

import java.math.BigDecimal;

public record AnnualTotalView(
        int year,
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        BigDecimal annualResult,
        int monthsWithData,
        BigDecimal avgMonthlySavings
) {}