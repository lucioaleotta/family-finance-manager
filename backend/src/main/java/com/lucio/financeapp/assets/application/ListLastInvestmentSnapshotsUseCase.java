package com.lucio.financeapp.assets.application;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lucio.financeapp.assets.api.InvestmentSnapshotView;
import com.lucio.financeapp.assets.domain.InvestmentSnapshot;
import com.lucio.financeapp.assets.domain.ports.InvestmentSnapshotRepository;
import com.lucio.financeapp.transactions.domain.Account;
import com.lucio.financeapp.transactions.domain.AccountType;
import com.lucio.financeapp.transactions.domain.ports.AccountRepository;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ListLastInvestmentSnapshotsUseCase {

    private final InvestmentSnapshotRepository repository;
    private final AccountRepository accountRepository;

    public ListLastInvestmentSnapshotsUseCase(InvestmentSnapshotRepository repository,
            AccountRepository accountRepository) {
        this.repository = repository;
        this.accountRepository = accountRepository;
    }

    public List<InvestmentSnapshotView> handle(YearMonth endMonth, UUID accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + accountId));
        if (account.getType() != AccountType.INVESTMENT) {
            throw new IllegalArgumentException("Account type must be INVESTMENT");
        }

        YearMonth end = endMonth == null ? YearMonth.now() : endMonth;
        YearMonth start = end.minusMonths(11);

        List<InvestmentSnapshot> snapshots = repository.findByMonthBetweenAndAccountId(start, end, accountId);
        Map<YearMonth, InvestmentSnapshot> byMonth = snapshots.stream()
                .collect(Collectors.toMap(InvestmentSnapshot::getMonth, Function.identity(), (a, b) -> a));

        List<InvestmentSnapshotView> out = new ArrayList<>(12);
        YearMonth current = start;
        for (int i = 0; i < 12; i++) {
            InvestmentSnapshot snapshot = byMonth.get(current);
            if (snapshot != null) {
                out.add(new InvestmentSnapshotView(
                        snapshot.getMonth(),
                        accountId,
                        account.getName(),
                        snapshot.getTotalInvested().getAmount(),
                        account.getCurrency(),
                        snapshot.getNote()));
            } else {
                out.add(new InvestmentSnapshotView(
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
