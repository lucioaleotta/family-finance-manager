package com.lucio.financeapp.transactions.application;

import com.lucio.financeapp.shared.domain.Currency;
import com.lucio.financeapp.transactions.domain.Account;
import com.lucio.financeapp.transactions.domain.AccountType;
import com.lucio.financeapp.transactions.domain.ports.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class UpdateAccountUseCase {

    private final AccountRepository repository;

    public UpdateAccountUseCase(AccountRepository repository) {
        this.repository = repository;
    }

    public void handle(UpdateAccountCommand command) {
        Account account = repository.findById(command.id())
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        String name = command.name().trim();

        repository.findByName(name)
                .filter(existing -> !existing.getId().equals(command.id()))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Account name already exists");
                });

        account.update(name, command.type(), command.currency());
        repository.save(account);
    }

    public record UpdateAccountCommand(UUID id, String name, AccountType type, Currency currency) {
    }
}
