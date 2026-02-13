package com.lucio.financeapp.transactions.infrastructure.persistence;

import com.lucio.financeapp.transactions.domain.Account;
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
    public Account save(@NotNull Account account) {
        return delegate.save(account);
    }

    @Override
    public List<Account> findAll() {
        return delegate.findAll();
    }

    @Override
    public Optional<Account> findById(UUID id) {
        return delegate.findById(id);
    }

    @Override
    public Optional<Account> findByName(String name) {
        return delegate.findByName(name);
    }
}

interface SpringDataAccountRepository extends JpaRepository<Account, UUID> {
    Optional<Account> findByName(String name);
}
