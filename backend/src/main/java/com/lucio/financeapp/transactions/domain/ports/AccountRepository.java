package com.lucio.financeapp.transactions.domain.ports;

import com.lucio.financeapp.transactions.domain.Account;
import com.lucio.financeapp.transactions.domain.AccountType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository {

    Account save(Account account);

    List<Account> findAll();

    List<Account> findByType(AccountType type);

    Optional<Account> findById(UUID id);

    Optional<Account> findByName(String name);

    void deleteById(UUID id);
}
