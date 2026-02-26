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

    public void handle(UUID userId, UUID accountId) {
        if (repository.findByIdAndUserId(accountId, userId).isEmpty()) {
            throw new IllegalArgumentException("Account not found: " + accountId);
        }
        repository.deleteByIdAndUserId(accountId, userId);
    }
}
