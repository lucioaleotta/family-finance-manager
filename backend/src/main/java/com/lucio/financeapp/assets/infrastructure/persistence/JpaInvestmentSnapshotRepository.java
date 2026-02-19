package com.lucio.financeapp.assets.infrastructure.persistence;

import com.lucio.financeapp.assets.domain.InvestmentSnapshot;
import com.lucio.financeapp.assets.domain.ports.InvestmentSnapshotRepository;
import com.lucio.financeapp.shared.domain.Currency;
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
    public Optional<InvestmentSnapshot> findByMonthAndCurrency(java.time.YearMonth month, Currency currency) {
        return delegate.findByMonthAndCurrency(month.toString(), currency.name());
    }

    @Override
    public List<InvestmentSnapshot> findByYearAndCurrency(int year, Currency currency) {
        String start = java.time.YearMonth.of(year, 1).toString();
        String end = java.time.YearMonth.of(year, 12).toString();
        return delegate.findByCurrencyAndMonthBetweenOrderByMonthAsc(currency.name(), start, end);
    }

    @Override
    public List<InvestmentSnapshot> findByMonthBetween(java.time.YearMonth start,
            java.time.YearMonth end,
            Currency currency) {
        return delegate.findByCurrencyAndMonthBetweenOrderByMonthAsc(currency.name(), start.toString(),
                end.toString());
    }

    @Override
    public List<InvestmentSnapshot> findByMonthBetween(java.time.YearMonth start,
            java.time.YearMonth end) {
        return delegate.findByMonthBetweenOrderByMonthAsc(start.toString(), end.toString());
    }
}

interface SpringDataInvestmentSnapshotRepository extends JpaRepository<InvestmentSnapshot, UUID> {
    Optional<InvestmentSnapshot> findByMonthAndCurrency(String month, String currency);

    List<InvestmentSnapshot> findByCurrencyAndMonthBetweenOrderByMonthAsc(String currency, String startMonth,
            String endMonth);

    List<InvestmentSnapshot> findByMonthBetweenOrderByMonthAsc(String startMonth, String endMonth);
}
