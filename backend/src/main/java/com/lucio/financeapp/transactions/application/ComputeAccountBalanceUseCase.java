package com.lucio.financeapp.transactions.application;

import com.lucio.financeapp.transactions.api.AccountBalanceView;
import com.lucio.financeapp.transactions.domain.Account;
import com.lucio.financeapp.transactions.domain.TransactionType;
import com.lucio.financeapp.transactions.domain.ports.AccountRepository;
import com.lucio.financeapp.transactions.domain.ports.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ComputeAccountBalanceUseCase {

        private final AccountRepository accountRepository;
        private final TransactionRepository transactionRepository;

        public ComputeAccountBalanceUseCase(AccountRepository accountRepository,
                        TransactionRepository transactionRepository) {
                this.accountRepository = accountRepository;
                this.transactionRepository = transactionRepository;
        }

        public AccountBalanceView handle(UUID accountId, LocalDate asOf) {
                Account account = accountRepository.findById(accountId)
                                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + accountId));

                var txs = transactionRepository.findByAccountUpTo(accountId, asOf);

                // (assunzione core-banking): tutte le tx di un account devono avere stessa
                // currency dell’account
                // se vuoi, qui puoi aggiungere check e lanciare eccezione se mismatch.

                BigDecimal income = txs.stream()
                                .filter(t -> t.getType() == TransactionType.INCOME)
                                .map(t -> t.getAmount().getAmount())
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal expense = txs.stream()
                                .filter(t -> t.getType() == TransactionType.EXPENSE)
                                .map(t -> t.getAmount().getAmount())
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal balance = income.subtract(expense);

                return new AccountBalanceView(accountId, asOf, balance, account.getCurrency());
        }
}
