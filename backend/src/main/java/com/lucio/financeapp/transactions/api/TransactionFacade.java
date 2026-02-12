package com.lucio.financeapp.transactions.api;

import java.time.YearMonth;
import java.util.List;

public interface TransactionFacade {
    List<TransactionView> findByMonth(YearMonth month);
}
