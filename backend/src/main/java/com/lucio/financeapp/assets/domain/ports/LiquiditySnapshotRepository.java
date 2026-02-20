package com.lucio.financeapp.assets.domain.ports;

import com.lucio.financeapp.assets.domain.LiquiditySnapshot;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LiquiditySnapshotRepository {
    LiquiditySnapshot save(LiquiditySnapshot snapshot);

    Optional<LiquiditySnapshot> findByMonthAndAccountId(YearMonth month, UUID accountId);

    List<LiquiditySnapshot> findByMonthBetweenAndAccountId(YearMonth start, YearMonth end, UUID accountId);

    List<LiquiditySnapshot> findByMonthBetween(YearMonth start, YearMonth end);
}
