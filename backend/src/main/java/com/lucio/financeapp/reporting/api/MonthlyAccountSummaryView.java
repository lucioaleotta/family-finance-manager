package com.lucio.financeapp.reporting.api;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.UUID;

public record MonthlyAccountSummaryView(
        YearMonth month,
        UUID accountId,
        BigDecimal income,
        BigDecimal expense,
        BigDecimal net) {
}
