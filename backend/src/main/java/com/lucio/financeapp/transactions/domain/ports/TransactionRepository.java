package com.lucio.financeapp.transactions.domain.ports;

import com.lucio.financeapp.transactions.domain.Transaction;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.time.LocalDate;

public interface TransactionRepository {

    Transaction save(Transaction transaction);

    List<Transaction> findByMonthAndUserId(YearMonth month, UUID userId);

    Optional<Transaction> findByIdAndUserId(UUID id, UUID userId);

    void deleteByIdAndUserId(UUID id, UUID userId);

    List<Transaction> findByMonthAndAccount(YearMonth month, UUID accountId, UUID userId);

    List<Transaction> findByAccountUpTo(UUID accountId, UUID userId, LocalDate asOf);
}
