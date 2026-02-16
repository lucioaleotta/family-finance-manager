package com.lucio.financeapp.transactions.api;

import com.lucio.financeapp.shared.domain.Currency;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record AccountBalanceView(
        UUID accountId,
        LocalDate asOf,
        BigDecimal balance,
        Currency currency) {
}
