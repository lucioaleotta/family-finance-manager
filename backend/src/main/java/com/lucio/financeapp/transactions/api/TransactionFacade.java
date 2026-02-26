package com.lucio.financeapp.transactions.api;

import java.time.YearMonth;
import java.util.List;
import java.time.LocalDate;
import java.util.UUID;

public interface TransactionFacade {
    List<TransactionView> findByMonth(UUID userId, YearMonth month);

    List<TransactionView> findByMonthAndAccount(UUID userId, YearMonth month, UUID accountId);

    List<TransactionView> findByAccountUpTo(UUID userId, UUID accountId, LocalDate asOf);
}
