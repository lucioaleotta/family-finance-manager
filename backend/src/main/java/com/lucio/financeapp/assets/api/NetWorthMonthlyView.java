package com.lucio.financeapp.assets.api;

import com.lucio.financeapp.shared.domain.Currency;

import java.math.BigDecimal;
import java.time.YearMonth;

public record NetWorthMonthlyView(
        YearMonth month,
        Currency currency,
        BigDecimal liquidity,
        BigDecimal invested,
        BigDecimal netWorth) {
}
