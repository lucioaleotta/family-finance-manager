package com.lucio.financeapp.assets.api;

import java.math.BigDecimal;

import com.lucio.financeapp.shared.domain.Currency;

public record AssetsAnnualView(
        int year,
        Currency currency,
        BigDecimal liquidity,
        BigDecimal investments,
        BigDecimal netWorth) {
}
