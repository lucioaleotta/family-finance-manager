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
        return delegate.findByCurrencyAndMonthBetween(currency.name(), start, end);
    }
}

interface SpringDataInvestmentSnapshotRepository extends JpaRepository<InvestmentSnapshot, UUID> {
    Optional<InvestmentSnapshot> findByMonthAndCurrency(String month, String currency);

    List<InvestmentSnapshot> findByCurrencyAndMonthBetween(String currency, String startMonth, String endMonth);
}
