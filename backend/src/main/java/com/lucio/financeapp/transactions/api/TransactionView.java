package com.lucio.financeapp.transactions.api;

import com.lucio.financeapp.transactions.domain.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record TransactionView(
        UUID id,
        BigDecimal amount,
        LocalDate date,
        TransactionType type,
        String category,
        String description
) {}
