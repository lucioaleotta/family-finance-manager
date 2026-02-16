package com.lucio.financeapp.assets.api;

import com.lucio.financeapp.shared.domain.Currency;

import java.math.BigDecimal;
import java.time.YearMonth;

public record InvestmentSnapshotView(
        YearMonth month,
        BigDecimal totalInvested,
        Currency currency,
        String note) {
}
