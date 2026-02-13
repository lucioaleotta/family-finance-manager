package com.lucio.financeapp.transactions.application;

import com.lucio.financeapp.transactions.domain.Account;
import com.lucio.financeapp.transactions.domain.AccountType;
import com.lucio.financeapp.transactions.domain.ports.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import com.lucio.financeapp.transactions.domain.Currency;

@Service
@Transactional
public class DefaultAccountService {

    public static final String DEFAULT_ACCOUNT_NAME = "Unassigned";

    private final AccountRepository accountRepository;

    public DefaultAccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public UUID getOrCreateDefaultAccountId() {
        return accountRepository.findByName(DEFAULT_ACCOUNT_NAME)
                .map(Account::getId)
                .orElseGet(() -> {
                    Account created = Account.of(DEFAULT_ACCOUNT_NAME, AccountType.CHECKING, Currency.EUR);
                    accountRepository.save(created);
                    return created.getId();
                });
    }
}
