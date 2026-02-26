package com.lucio.financeapp.assets.infrastructure.persistence;

import com.lucio.financeapp.assets.domain.InvestmentSnapshot;
import com.lucio.financeapp.assets.domain.ports.InvestmentSnapshotRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaInvestmentSnapshotRepository implements InvestmentSnapshotRepository {

    private final SpringDataInvestmentSnapshotRepository delegate;

    public JpaInvestmentSnapshotRepository(SpringDataInvestmentSnapshotRepository delegate) {
        this.delegate = delegate;
    }

    @Override
    @SuppressWarnings("null")
    public InvestmentSnapshot save(InvestmentSnapshot snapshot) {
        return delegate.save(snapshot);
    }

    @Override
    public Optional<InvestmentSnapshot> findByMonthAndAccountId(java.time.YearMonth month, UUID accountId) {
        return delegate.findByMonthAndAccountId(month.toString(), accountId);
    }

    @Override
    public List<InvestmentSnapshot> findByMonthBetweenAndAccountId(java.time.YearMonth start,
            java.time.YearMonth end,
            UUID accountId) {
        return delegate.findByAccountIdAndMonthBetweenOrderByMonthAsc(accountId, start.toString(), end.toString());
    }

    @Override
    public List<InvestmentSnapshot> findByMonthAndAccountIds(java.time.YearMonth month, List<UUID> accountIds) {
        if (accountIds.isEmpty()) {
            return List.of();
        }
        return delegate.findByMonthAndAccountIdIn(month.toString(), accountIds);
    }

    @Override
    public List<InvestmentSnapshot> findByMonthBetween(java.time.YearMonth start,
            java.time.YearMonth end) {
        return delegate.findByMonthBetweenOrderByMonthAsc(start.toString(), end.toString());
    }
}

interface SpringDataInvestmentSnapshotRepository extends JpaRepository<InvestmentSnapshot, UUID> {
    Optional<InvestmentSnapshot> findByMonthAndAccountId(String month, UUID accountId);

    List<InvestmentSnapshot> findByAccountIdAndMonthBetweenOrderByMonthAsc(UUID accountId, String startMonth,
            String endMonth);

    List<InvestmentSnapshot> findByMonthAndAccountIdIn(String month, List<UUID> accountIds);

    List<InvestmentSnapshot> findByMonthBetweenOrderByMonthAsc(String startMonth, String endMonth);
}
