package com.lucio.financeapp.transactions.domain.ports;

import com.lucio.financeapp.transactions.domain.Transaction;

import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

public interface TransactionRepository {

    Transaction save(Transaction transaction);

    List<Transaction> findByMonth(YearMonth month);

    Transaction findById(UUID id);
}
