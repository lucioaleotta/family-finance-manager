package com.lucio.financeapp.transactions.api;

import com.lucio.financeapp.transactions.domain.TransactionType;
import com.lucio.financeapp.shared.domain.Money;

import java.time.LocalDate;
import java.util.UUID;

public record TransactionView(
                UUID id,
                UUID accountId,
                Money amount,
                LocalDate date,
                TransactionType type,
                String category,
                String description) {
}
