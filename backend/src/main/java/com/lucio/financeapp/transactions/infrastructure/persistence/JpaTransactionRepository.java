/**
 * JpaTransactionRepository
 * 
 * Repository implementation for managing Transaction entities using Spring Data JPA.
 * This class acts as an adapter between the domain layer (TransactionRepository interface)
 * and the Spring Data persistence layer (SpringDataTransactionRepository).
 * 
 * It provides the following operations:
 * - save(Transaction): Persists a transaction to the database
 * - findById(UUID): Retrieves a transaction by its unique identifier
 * - deleteById(UUID): Removes a transaction from the database
 * - findByMonth(YearMonth): Finds all transactions within a specific month, ordered by date (descending)
 * - findByMonthAndAccount(YearMonth, UUID): Finds transactions for a specific account within a month
 * - findByAccountUpTo(UUID, LocalDate): Finds all transactions for an account up to a specific date
 * 
 * The repository uses composition pattern with a delegate (SpringDataTransactionRepository)
 * to handle the actual database queries, following the Adapter design pattern to maintain
 * separation between domain and infrastructure layers.
 */
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
    @SuppressWarnings("null")
    public Transaction save(Transaction transaction) {
        return delegate.save(transaction);
    }

    @Override
    public List<Transaction> findByMonthAndUserId(YearMonth month, UUID userId) {
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();
        return delegate.findByUserIdAndDateBetweenOrderByDateDesc(userId, start, end);
    }

    @Override
    @SuppressWarnings("null")
    public Optional<Transaction> findByIdAndUserId(UUID id, UUID userId) {
        return delegate.findByIdAndUserId(id, userId);
    }

    @Override
    @SuppressWarnings("null")
    public void deleteByIdAndUserId(UUID id, UUID userId) {
        delegate.deleteByIdAndUserId(id, userId);
    }

    @Override
    public List<Transaction> findByMonthAndAccount(YearMonth month, UUID accountId, UUID userId) {
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();
        return delegate.findByUserIdAndAccountIdAndDateBetweenOrderByDateDescCreatedAtDesc(userId, accountId, start,
                end);
    }

    @Override
    public List<Transaction> findByAccountUpTo(UUID accountId, UUID userId, LocalDate asOf) {
        return delegate.findByUserIdAndAccountIdAndDateLessThanEqualOrderByDateDescCreatedAtDesc(userId, accountId,
                asOf);
    }
}

interface SpringDataTransactionRepository extends JpaRepository<Transaction, UUID> {
    List<Transaction> findByUserIdAndDateBetweenOrderByDateDesc(UUID userId, LocalDate start, LocalDate end);

    List<Transaction> findByUserIdAndAccountIdAndDateBetweenOrderByDateDescCreatedAtDesc(UUID userId, UUID accountId,
            LocalDate start, LocalDate end);

    List<Transaction> findByUserIdAndAccountIdAndDateLessThanEqualOrderByDateDescCreatedAtDesc(UUID userId,
            UUID accountId, LocalDate asOf);

    Optional<Transaction> findByIdAndUserId(UUID id, UUID userId);

    void deleteByIdAndUserId(UUID id, UUID userId);
}
