package com.lucio.financeapp.assets.api;

import com.lucio.financeapp.shared.domain.Currency;

import java.math.BigDecimal;

public record CategoryMonthlyTotalView(
        Currency currency,
        BigDecimal total) {
}
