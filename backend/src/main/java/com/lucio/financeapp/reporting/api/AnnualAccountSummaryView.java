package com.lucio.financeapp.reporting.api;

import java.math.BigDecimal;
import java.util.UUID;

public record AnnualAccountSummaryView(
        int year,
        UUID accountId,
        BigDecimal income,
        BigDecimal expense,
        BigDecimal net) {
}
