package com.lucio.financeapp.assets.api;

import com.lucio.financeapp.shared.domain.Currency;

import java.math.BigDecimal;
import java.time.YearMonth;

public record NetWorthReconciliationView(
        YearMonth month,
        Currency currency,
        BigDecimal cashflow, // STANDARD only
        BigDecimal netWorth, // liquidity + invested
        BigDecimal netWorthDelta, // month vs previous month
        BigDecimal unexplained // netWorthDelta - cashflow
) {
}
