package com.lucio.financeapp.assets.infrastructure.persistence;

import com.lucio.financeapp.assets.domain.LiquiditySnapshot;
import com.lucio.financeapp.assets.domain.ports.LiquiditySnapshotRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaLiquiditySnapshotRepository implements LiquiditySnapshotRepository {

    private final SpringDataLiquiditySnapshotRepository delegate;

    public JpaLiquiditySnapshotRepository(SpringDataLiquiditySnapshotRepository delegate) {
        this.delegate = delegate;
    }

    @Override
    @SuppressWarnings("null")
    public LiquiditySnapshot save(LiquiditySnapshot snapshot) {
        return delegate.save(snapshot);
    }

    @Override
    public Optional<LiquiditySnapshot> findByMonthAndAccountId(YearMonth month, UUID accountId) {
        return delegate.findByMonthAndAccountId(month.toString(), accountId);
    }

    @Override
    public List<LiquiditySnapshot> findByMonthBetweenAndAccountId(YearMonth start, YearMonth end, UUID accountId) {
        return delegate.findByAccountIdAndMonthBetweenOrderByMonthAsc(accountId, start.toString(), end.toString());
    }

    @Override
    public List<LiquiditySnapshot> findByMonthBetween(YearMonth start, YearMonth end) {
        return delegate.findByMonthBetweenOrderByMonthAsc(start.toString(), end.toString());
    }
}

interface SpringDataLiquiditySnapshotRepository extends JpaRepository<LiquiditySnapshot, UUID> {
    Optional<LiquiditySnapshot> findByMonthAndAccountId(String month, UUID accountId);

    List<LiquiditySnapshot> findByAccountIdAndMonthBetweenOrderByMonthAsc(UUID accountId, String startMonth,
            String endMonth);

    List<LiquiditySnapshot> findByMonthBetweenOrderByMonthAsc(String startMonth, String endMonth);
}
