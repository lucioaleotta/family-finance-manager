package com.lucio.financeapp.assets.application;

import com.lucio.financeapp.assets.api.CategoryMonthlyTotalView;
import com.lucio.financeapp.assets.domain.ports.InvestmentSnapshotRepository;
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
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ComputeInvestmentMonthlyTotalsUseCase {

    private final AccountRepository accountRepository;
    private final InvestmentSnapshotRepository snapshotRepository;

    public ComputeInvestmentMonthlyTotalsUseCase(AccountRepository accountRepository,
            InvestmentSnapshotRepository snapshotRepository) {
        this.accountRepository = accountRepository;
        this.snapshotRepository = snapshotRepository;
    }

    public List<CategoryMonthlyTotalView> handle(UUID userId, YearMonth month) {
        List<Account> accounts = accountRepository.findByTypeAndUserId(AccountType.INVESTMENT, userId);
        List<UUID> accountIds = accounts.stream().map(Account::getId).toList();
        Map<UUID, Account> accountById = new HashMap<>();
        for (Account account : accounts) {
            accountById.put(account.getId(), account);
        }

        Map<Currency, BigDecimal> totalsByCurrency = new HashMap<>();
        snapshotRepository.findByMonthAndAccountIds(month, accountIds).forEach(snapshot -> {
            Account account = accountById.get(snapshot.getAccountId());
            if (account == null) {
                return;
            }
            totalsByCurrency.merge(account.getCurrency(), snapshot.getTotalInvested().getAmount(), BigDecimal::add);
        });

        List<CategoryMonthlyTotalView> out = new ArrayList<>();
        totalsByCurrency.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> out.add(new CategoryMonthlyTotalView(entry.getKey(), entry.getValue())));
        return out;
    }
}
