package com.lucio.financeapp.reporting.api;

import java.math.BigDecimal;
import java.time.YearMonth;

public record MonthlyBalanceView(
        YearMonth month,
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        BigDecimal savings
) {}
