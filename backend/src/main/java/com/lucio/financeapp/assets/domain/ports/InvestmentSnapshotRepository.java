package com.lucio.financeapp.assets.domain.ports;

import com.lucio.financeapp.assets.domain.InvestmentSnapshot;
import com.lucio.financeapp.shared.domain.Currency;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

public interface InvestmentSnapshotRepository {
    InvestmentSnapshot save(InvestmentSnapshot snapshot);

    Optional<InvestmentSnapshot> findByMonthAndCurrency(YearMonth month, Currency currency);

    List<InvestmentSnapshot> findByYearAndCurrency(int year, Currency currency);
}
