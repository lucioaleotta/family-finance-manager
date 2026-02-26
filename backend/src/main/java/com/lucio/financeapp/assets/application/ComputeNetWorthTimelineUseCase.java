package com.lucio.financeapp.assets.application;

import com.lucio.financeapp.assets.api.NetWorthMonthlyView;
import com.lucio.financeapp.assets.domain.LiquiditySnapshot;
import com.lucio.financeapp.assets.domain.ports.LiquiditySnapshotRepository;
import com.lucio.financeapp.assets.domain.ports.InvestmentSnapshotRepository;
import com.lucio.financeapp.transactions.api.AccountView;
import com.lucio.financeapp.transactions.application.ListAccountsUseCase;
import com.lucio.financeapp.shared.domain.Currency;
import com.lucio.financeapp.config.FinanceProperties;
import com.lucio.financeapp.transactions.domain.AccountType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ComputeNetWorthTimelineUseCase {

        private final ListAccountsUseCase listAccounts;
        private final LiquiditySnapshotRepository liquiditySnapshots;
        private final InvestmentSnapshotRepository snapshots;
        private final FinanceProperties financeProperties;

        public ComputeNetWorthTimelineUseCase(ListAccountsUseCase listAccounts,
                        LiquiditySnapshotRepository liquiditySnapshots,
                        InvestmentSnapshotRepository snapshots, FinanceProperties financeProperties) {
                this.listAccounts = listAccounts;
                this.liquiditySnapshots = liquiditySnapshots;
                this.snapshots = snapshots;
                this.financeProperties = financeProperties;
        }

        public List<NetWorthMonthlyView> handle(UUID userId, int year) {
                Currency currency = Currency.valueOf(financeProperties.getBaseCurrency());
                List<AccountView> accounts = listAccounts.handle(userId).stream()
                                .filter(a -> a.currency() == currency)
                                .toList();
                Set<UUID> liquidityAccountIds = accounts.stream()
                                .filter(a -> a.type() == AccountType.LIQUIDITY)
                                .map(AccountView::id)
                                .collect(java.util.stream.Collectors.toSet());
                List<UUID> investmentAccountIds = accounts.stream()
                                .filter(a -> a.type() == AccountType.INVESTMENT)
                                .map(AccountView::id)
                                .toList();

                return java.util.stream.IntStream.rangeClosed(1, 12)
                                .mapToObj(m -> {
                                        YearMonth ym = YearMonth.of(year, m);

                                        BigDecimal liquidity = liquiditySnapshots.findByMonthBetween(ym, ym).stream()
                                                        .filter(s -> liquidityAccountIds.contains(s.getAccountId()))
                                                        .map(LiquiditySnapshot::getLiquidity)
                                                        .map(money -> money.getAmount())
                                                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                                        BigDecimal invested = snapshots
                                                        .findByMonthAndAccountIds(ym, investmentAccountIds)
                                                        .stream()
                                                        .map(s -> s.getTotalInvested().getAmount())
                                                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                                        BigDecimal netWorth = liquidity.add(invested);

                                        return new NetWorthMonthlyView(ym, currency, liquidity, invested, netWorth);
                                })
                                .toList();
        }
}
