package com.lucio.financeapp.transactions.application;

import com.lucio.financeapp.transactions.domain.ports.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class DeleteAccountUseCase {

    private final AccountRepository repository;

    public DeleteAccountUseCase(AccountRepository repository) {
        this.repository = repository;
    }

    public void handle(UUID accountId) {
        if (repository.findById(accountId).isEmpty()) {
            throw new IllegalArgumentException("Account not found: " + accountId);
        }
        repository.deleteById(accountId);
    }
}
