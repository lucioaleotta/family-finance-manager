package com.lucio.financeapp.assets.application;

import com.lucio.financeapp.assets.domain.InvestmentSnapshot;
import com.lucio.financeapp.assets.domain.ports.InvestmentSnapshotRepository;
import com.lucio.financeapp.shared.domain.Money;
import com.lucio.financeapp.transactions.domain.Account;
import com.lucio.financeapp.transactions.domain.AccountType;
import com.lucio.financeapp.transactions.domain.ports.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.UUID;

@Service
@Transactional
public class UpsertInvestmentSnapshotUseCase {

    private final InvestmentSnapshotRepository repository;
    private final AccountRepository accountRepository;

    public UpsertInvestmentSnapshotUseCase(InvestmentSnapshotRepository repository,
            AccountRepository accountRepository) {
        this.repository = repository;
        this.accountRepository = accountRepository;
    }

    public void handle(UUID userId, Command command) {
        Account account = accountRepository.findByIdAndUserId(command.accountId(), userId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + command.accountId()));
        if (account.getType() != AccountType.INVESTMENT) {
            throw new IllegalArgumentException("Account type must be INVESTMENT");
        }

        Money money = Money.of(command.totalInvested(), account.getCurrency());

        InvestmentSnapshot snapshot = repository.findByMonthAndAccountId(command.month(), command.accountId())
                .orElseGet(() -> InvestmentSnapshot.of(command.month(), command.accountId(), money, command.note()));

        snapshot.update(money, command.note());
        repository.save(snapshot);

    }

    public record Command(YearMonth month, UUID accountId, BigDecimal totalInvested, String note) {
    }
}
