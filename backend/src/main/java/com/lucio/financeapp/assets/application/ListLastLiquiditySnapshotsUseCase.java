package com.lucio.financeapp.assets.application;

import com.lucio.financeapp.assets.api.LiquiditySnapshotView;
import com.lucio.financeapp.assets.domain.LiquiditySnapshot;
import com.lucio.financeapp.assets.domain.ports.LiquiditySnapshotRepository;
import com.lucio.financeapp.transactions.domain.Account;
import com.lucio.financeapp.transactions.domain.AccountType;
import com.lucio.financeapp.transactions.domain.ports.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ListLastLiquiditySnapshotsUseCase {

    private final LiquiditySnapshotRepository repository;
    private final AccountRepository accountRepository;

    public ListLastLiquiditySnapshotsUseCase(LiquiditySnapshotRepository repository,
            AccountRepository accountRepository) {
        this.repository = repository;
        this.accountRepository = accountRepository;
    }

    public List<LiquiditySnapshotView> handle(YearMonth endMonth, UUID accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + accountId));
        if (account.getType() != AccountType.LIQUIDITY) {
            throw new IllegalArgumentException("Account type must be LIQUIDITY");
        }

        YearMonth end = endMonth == null ? YearMonth.now() : endMonth;
        YearMonth start = end.minusMonths(11);

        List<LiquiditySnapshot> snapshots = repository.findByMonthBetweenAndAccountId(start, end, accountId);
        Map<YearMonth, LiquiditySnapshot> byMonth = snapshots.stream()
                .collect(Collectors.toMap(LiquiditySnapshot::getMonth, Function.identity(), (a, b) -> a));

        List<LiquiditySnapshotView> out = new ArrayList<>(12);
        YearMonth current = start;
        for (int i = 0; i < 12; i++) {
            LiquiditySnapshot snapshot = byMonth.get(current);
            if (snapshot != null) {
                out.add(new LiquiditySnapshotView(
                        snapshot.getMonth(),
                        accountId,
                        account.getName(),
                        snapshot.getLiquidity().getAmount(),
                        account.getCurrency(),
                        snapshot.getNote()));
            } else {
                out.add(new LiquiditySnapshotView(
                        current,
                        accountId,
                        account.getName(),
                        BigDecimal.ZERO,
                        account.getCurrency(),
                        null));
            }
            current = current.plusMonths(1);
        }

        return out;
    }
}
