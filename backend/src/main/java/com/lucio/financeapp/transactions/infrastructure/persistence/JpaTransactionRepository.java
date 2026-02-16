package com.lucio.financeapp.transactions.infrastructure.persistence;

import com.lucio.financeapp.transactions.domain.Transaction;
import com.lucio.financeapp.transactions.domain.ports.TransactionRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;
import java.util.Optional;

@Repository
public class JpaTransactionRepository implements TransactionRepository {

    private final SpringDataTransactionRepository delegate;

    public JpaTransactionRepository(SpringDataTransactionRepository delegate) {
        this.delegate = delegate;
    }

    @Override
    public Transaction save(Transaction transaction) {
        return delegate.save(transaction);
    }

    @Override
    public List<Transaction> findByMonth(YearMonth month) {
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();
        return delegate.findByDateBetween(start, end);
    }

    @Override
    public Optional<Transaction> findById(UUID id) {
        return delegate.findById(id);
    }

    @Override
    public void deleteById(UUID id) {
        delegate.deleteById(id);
    }

    @Override
    public List<Transaction> findByMonthAndAccount(YearMonth month, UUID accountId) {
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();
        return delegate.findByAccountIdAndDateBetween(accountId, start, end);
    }

    @Override
    public List<Transaction> findByAccountUpTo(UUID accountId, LocalDate asOf) {
        return delegate.findByAccountIdAndDateLessThanEqual(accountId, asOf);
    }
}

interface SpringDataTransactionRepository extends JpaRepository<Transaction, UUID> {
    List<Transaction> findByDateBetween(LocalDate start, LocalDate end);

    List<Transaction> findByAccountIdAndDateBetween(UUID accountId, LocalDate start, LocalDate end);

    List<Transaction> findByAccountIdAndDateLessThanEqual(UUID accountId, LocalDate asOf);
}
