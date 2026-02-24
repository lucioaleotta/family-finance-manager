package com.lucio.financeapp.assets.application;

import com.lucio.financeapp.assets.domain.LiquiditySnapshot;
import com.lucio.financeapp.assets.domain.ports.LiquiditySnapshotRepository;
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
public class UpsertLiquiditySnapshotUseCase {

    private final LiquiditySnapshotRepository repository;
    private final AccountRepository accountRepository;

    public UpsertLiquiditySnapshotUseCase(LiquiditySnapshotRepository repository,
            AccountRepository accountRepository) {
        this.repository = repository;
        this.accountRepository = accountRepository;
    }

    public void handle(Command command) {
        Account account = accountRepository.findById(command.accountId())
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + command.accountId()));
        if (account.getType() != AccountType.LIQUIDITY) {
            throw new IllegalArgumentException("Account type must be LIQUIDITY");
        }

        Money money = Money.of(command.liquidity(), account.getCurrency());

        LiquiditySnapshot snapshot = repository.findByMonthAndAccountId(command.month(), command.accountId())
                .orElseGet(() -> LiquiditySnapshot.of(command.month(), command.accountId(), money, command.note()));

        snapshot.update(money, command.note());
        repository.save(snapshot);
    }

    public record Command(YearMonth month, UUID accountId, BigDecimal liquidity, String note) {
    }
}
