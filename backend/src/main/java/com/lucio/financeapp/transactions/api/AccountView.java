package com.lucio.financeapp.transactions.api;

import com.lucio.financeapp.transactions.domain.AccountType;
import com.lucio.financeapp.transactions.domain.Currency;

import java.util.UUID;

public record AccountView(
        UUID id,
        String name,
        AccountType type,
        Currency currency
) {}
