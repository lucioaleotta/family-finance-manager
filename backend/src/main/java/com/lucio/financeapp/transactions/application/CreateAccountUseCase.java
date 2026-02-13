package com.lucio.financeapp.transactions.application;

import com.lucio.financeapp.transactions.domain.Account;
import com.lucio.financeapp.transactions.domain.AccountType;
import com.lucio.financeapp.transactions.domain.Currency;
import com.lucio.financeapp.transactions.domain.ports.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class CreateAccountUseCase {

    private final AccountRepository repository;

    public CreateAccountUseCase(AccountRepository repository) {
        this.repository = repository;
    }

    public UUID handle(CreateAccountCommand command) {
        Account account = Account.of(command.name(), command.type(), command.currency());
        repository.save(account);
        return account.getId();
    }

    public record CreateAccountCommand(String name, AccountType type, Currency currency) {
    }
}
