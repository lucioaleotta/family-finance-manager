package com.lucio.financeapp.transactions.domain.ports;

import com.lucio.financeapp.transactions.domain.Account;
import com.lucio.financeapp.transactions.domain.AccountType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository {

    Account save(Account account);

    List<Account> findAllByUserId(UUID userId);

    List<Account> findByTypeAndUserId(AccountType type, UUID userId);

    Optional<Account> findByIdAndUserId(UUID id, UUID userId);

    Optional<Account> findByNameAndUserId(String name, UUID userId);

    void deleteByIdAndUserId(UUID id, UUID userId);
}
