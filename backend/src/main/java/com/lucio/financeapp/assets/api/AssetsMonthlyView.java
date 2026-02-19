package com.lucio.financeapp.assets.api;

import java.math.BigDecimal;
import java.time.YearMonth;

import com.lucio.financeapp.shared.domain.Currency;

public record AssetsMonthlyView(
        YearMonth month,
        Currency currency,
        BigDecimal liquidity,
        BigDecimal investments,
        BigDecimal netWorth) {
}
