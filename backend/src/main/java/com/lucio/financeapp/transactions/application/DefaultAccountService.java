package com.lucio.financeapp.transactions.application;

import com.lucio.financeapp.transactions.domain.Account;
import com.lucio.financeapp.transactions.domain.AccountType;
import com.lucio.financeapp.transactions.domain.ports.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import com.lucio.financeapp.shared.domain.Currency;

@Service
@Transactional
public class DefaultAccountService {

    public static final String DEFAULT_ACCOUNT_NAME = "Unassigned";

    private final AccountRepository accountRepository;

    public DefaultAccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public UUID getOrCreateDefaultAccountId(UUID userId, Currency currency) {
        return accountRepository.findByNameAndUserId(DEFAULT_ACCOUNT_NAME, userId)
                .map(Account::getId)
                .orElseGet(() -> {
                    Account created = Account.of(userId, DEFAULT_ACCOUNT_NAME, AccountType.CHECKING, currency);
                    accountRepository.save(created);
                    return created.getId();
                });
    }
}
