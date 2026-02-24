package com.lucio.financeapp.assets.application;

import com.lucio.financeapp.assets.api.CategoryMonthlyTotalView;
import com.lucio.financeapp.assets.domain.ports.LiquiditySnapshotRepository;
import com.lucio.financeapp.shared.domain.Currency;
import com.lucio.financeapp.transactions.domain.Account;
import com.lucio.financeapp.transactions.domain.AccountType;
import com.lucio.financeapp.transactions.domain.ports.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class ComputeLiquidityMonthlyTotalsUseCase {

    private final AccountRepository accountRepository;
    private final LiquiditySnapshotRepository snapshotRepository;

    public ComputeLiquidityMonthlyTotalsUseCase(AccountRepository accountRepository,
            LiquiditySnapshotRepository snapshotRepository) {
        this.accountRepository = accountRepository;
        this.snapshotRepository = snapshotRepository;
    }

    public List<CategoryMonthlyTotalView> handle(YearMonth month) {
        List<Account> accounts = accountRepository.findByType(AccountType.LIQUIDITY);

        Map<Currency, BigDecimal> totalsByCurrency = new HashMap<>();
        for (Account account : accounts) {
            snapshotRepository.findByMonthAndAccountId(month, account.getId()).ifPresent(snapshot -> totalsByCurrency
                    .merge(account.getCurrency(), snapshot.getLiquidity().getAmount(), BigDecimal::add));
        }

        List<CategoryMonthlyTotalView> out = new ArrayList<>();
        totalsByCurrency.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> out.add(new CategoryMonthlyTotalView(entry.getKey(), entry.getValue())));
        return out;
    }
}
