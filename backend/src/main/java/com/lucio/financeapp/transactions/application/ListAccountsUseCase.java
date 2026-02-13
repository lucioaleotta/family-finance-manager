package com.lucio.financeapp.transactions.application;

import com.lucio.financeapp.transactions.api.AccountView;
import com.lucio.financeapp.transactions.domain.ports.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ListAccountsUseCase {

    private final AccountRepository repository;

    public ListAccountsUseCase(AccountRepository repository) {
        this.repository = repository;
    }

    public List<AccountView> handle() {
        return repository.findAll().stream()
                .map(a -> new AccountView(a.getId(), a.getName(), a.getType(), a.getCurrency()))
                .toList();
    }
}
