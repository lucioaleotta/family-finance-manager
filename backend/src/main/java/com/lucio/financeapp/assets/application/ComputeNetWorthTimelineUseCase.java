package com.lucio.financeapp.assets.application;

import com.lucio.financeapp.assets.api.NetWorthMonthlyView;
import com.lucio.financeapp.assets.domain.ports.InvestmentSnapshotRepository;
import com.lucio.financeapp.transactions.api.AccountView;
import com.lucio.financeapp.transactions.application.ListAccountsUseCase;
import com.lucio.financeapp.transactions.application.ComputeAccountBalanceUseCase;
import com.lucio.financeapp.shared.domain.Currency;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ComputeNetWorthTimelineUseCase {

    private final ListAccountsUseCase listAccounts;
    private final ComputeAccountBalanceUseCase accountBalance;
    private final InvestmentSnapshotRepository snapshots;

    public ComputeNetWorthTimelineUseCase(ListAccountsUseCase listAccounts,
            ComputeAccountBalanceUseCase accountBalance,
            InvestmentSnapshotRepository snapshots) {
        this.listAccounts = listAccounts;
        this.accountBalance = accountBalance;
        this.snapshots = snapshots;
    }

    public List<NetWorthMonthlyView> handle(int year, Currency currency) {
        List<AccountView> accounts = listAccounts.handle().stream()
                .filter(a -> a.currency() == currency)
                .toList();

        return java.util.stream.IntStream.rangeClosed(1, 12)
                .mapToObj(m -> {
                    YearMonth ym = YearMonth.of(year, m);
                    LocalDate asOf = ym.atEndOfMonth();

                    BigDecimal liquidity = accounts.stream()
                            .map(a -> accountBalance.handle(a.id(), asOf).balance())
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    BigDecimal invested = snapshots.findByMonthAndCurrency(ym, currency)
                            .map(s -> s.getTotalInvested().getAmount())
                            .orElse(BigDecimal.ZERO);

                    BigDecimal netWorth = liquidity.add(invested);

                    return new NetWorthMonthlyView(ym, currency, liquidity, invested, netWorth);
                })
                .toList();
    }
}
