package com.lucio.financeapp.transactions.infrastructure.persistence;

import com.lucio.financeapp.transactions.domain.Account;
import com.lucio.financeapp.transactions.domain.AccountType;
import com.lucio.financeapp.transactions.domain.ports.AccountRepository;

import jakarta.validation.constraints.NotNull;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaAccountRepository implements AccountRepository {

    private final SpringDataAccountRepository delegate;

    public JpaAccountRepository(SpringDataAccountRepository delegate) {
        this.delegate = delegate;
    }

    @Override
    @SuppressWarnings("null")
    public Account save(@NotNull Account account) {
        return delegate.save(account);
    }

    @Override
    public List<Account> findAllByUserId(UUID userId) {
        return delegate.findByUserId(userId);
    }

    @Override
    public List<Account> findByTypeAndUserId(AccountType type, UUID userId) {
        return delegate.findByTypeAndUserId(type, userId);
    }

    @Override
    @SuppressWarnings("null")
    public Optional<Account> findByIdAndUserId(UUID id, UUID userId) {
        return delegate.findByIdAndUserId(id, userId);
    }

    @Override
    public Optional<Account> findByNameAndUserId(String name, UUID userId) {
        return delegate.findByNameAndUserId(name, userId);
    }

    @Override
    @SuppressWarnings("null")
    public void deleteByIdAndUserId(UUID id, UUID userId) {
        delegate.deleteByIdAndUserId(id, userId);
    }
}

interface SpringDataAccountRepository extends JpaRepository<Account, UUID> {
    Optional<Account> findByNameAndUserId(String name, UUID userId);

    Optional<Account> findByIdAndUserId(UUID id, UUID userId);

    List<Account> findByTypeAndUserId(AccountType type, UUID userId);

    List<Account> findByUserId(UUID userId);

    void deleteByIdAndUserId(UUID id, UUID userId);
}
