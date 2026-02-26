package com.lucio.financeapp.assets.domain.ports;

import com.lucio.financeapp.assets.domain.InvestmentSnapshot;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvestmentSnapshotRepository {
    InvestmentSnapshot save(InvestmentSnapshot snapshot);

    Optional<InvestmentSnapshot> findByMonthAndAccountId(YearMonth month, UUID accountId);

    List<InvestmentSnapshot> findByMonthBetweenAndAccountId(YearMonth start, YearMonth end, UUID accountId);

    List<InvestmentSnapshot> findByMonthAndAccountIds(YearMonth month, List<UUID> accountIds);

    List<InvestmentSnapshot> findByMonthBetween(YearMonth start, YearMonth end);
}
