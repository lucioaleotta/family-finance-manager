package com.lucio.financeapp.assets.api;

import com.lucio.financeapp.shared.domain.Currency;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.UUID;

public record LiquiditySnapshotView(
        YearMonth month,
        UUID accountId,
        String accountName,
        BigDecimal liquidity,
        Currency currency,
        String note) {
}
