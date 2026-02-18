package com.lucio.financeapp.assets.application;

import com.lucio.financeapp.assets.api.NetWorthMonthlyView;
import com.lucio.financeapp.assets.api.NetWorthReconciliationView;
import com.lucio.financeapp.assets.domain.ports.InvestmentSnapshotRepository;
import com.lucio.financeapp.transactions.api.AccountView;
import com.lucio.financeapp.transactions.api.TransactionFacade;
import com.lucio.financeapp.transactions.api.TransactionView;
import com.lucio.financeapp.transactions.application.ComputeAccountBalanceUseCase;
import com.lucio.financeapp.transactions.application.ListAccountsUseCase;
import com.lucio.financeapp.shared.domain.Currency;
import com.lucio.financeapp.transactions.domain.TransactionKind;
import com.lucio.financeapp.transactions.domain.TransactionType;
import com.lucio.financeapp.config.FinanceProperties;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ComputeNetWorthReconciliationUseCase {

        private final ComputeNetWorthTimelineUseCase netWorthTimeline;
        private final TransactionFacade transactionFacade;

        // per calcolare net worth del mese precedente (Dec anno-1) senza dipendere da
        // reporting
        private final ListAccountsUseCase listAccounts;
        private final ComputeAccountBalanceUseCase accountBalance;
        private final InvestmentSnapshotRepository snapshots;
        private final FinanceProperties financeProperties;

        public ComputeNetWorthReconciliationUseCase(ComputeNetWorthTimelineUseCase netWorthTimeline,
                        TransactionFacade transactionFacade,
                        ListAccountsUseCase listAccounts,
                        ComputeAccountBalanceUseCase accountBalance,
                        InvestmentSnapshotRepository snapshots, FinanceProperties financeProperties) {
                this.netWorthTimeline = netWorthTimeline;
                this.transactionFacade = transactionFacade;
                this.listAccounts = listAccounts;
                this.accountBalance = accountBalance;
                this.snapshots = snapshots;
                this.financeProperties = financeProperties;
        }

        public List<NetWorthReconciliationView> handle(int year) {
                Currency currency = financeProperties.getBaseCurrency();
                List<NetWorthMonthlyView> timeline = netWorthTimeline.handle(year);

                // Net worth del mese precedente (Dec dell'anno prima) per avere delta anche a
                // Gennaio
                YearMonth prevMonth = YearMonth.of(year - 1, 12);
                BigDecimal prevNetWorth = computeNetWorthAt(prevMonth, currency);

                final class NetWorthHolder {
                        BigDecimal value;

                        NetWorthHolder(BigDecimal value) {
                                this.value = value;
                        }
                }

                NetWorthHolder holder = new NetWorthHolder(prevNetWorth);

                return timeline.stream()
                                .map(current -> {
                                        YearMonth month = current.month();

                                        BigDecimal cashflow = computeCashflow(month); // STANDARD only, same currency
                                                                                      // assumed
                                        BigDecimal netWorth = current.netWorth();
                                        BigDecimal netWorthDelta = netWorth.subtract(holder.value);
                                        BigDecimal unexplained = netWorthDelta.subtract(cashflow);

                                        holder.value = netWorth; // advance baseline

                                        return new NetWorthReconciliationView(
                                                        month,
                                                        currency,
                                                        cashflow,
                                                        netWorth,
                                                        netWorthDelta,
                                                        unexplained);
                                })
                                .toList();
        }

        private BigDecimal computeCashflow(YearMonth month) {
                List<TransactionView> txs = transactionFacade.findByMonth(month);

                // Cashflow = solo STANDARD (esclude TRANSFER)
                List<TransactionView> standard = txs.stream()
                                .filter(t -> t.kind() == TransactionKind.STANDARD)
                                .toList();

                BigDecimal income = standard.stream()
                                .filter(t -> t.type() == TransactionType.INCOME)
                                .map(t -> t.amount().getAmount())
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal expense = standard.stream()
                                .filter(t -> t.type() == TransactionType.EXPENSE)
                                .map(t -> t.amount().getAmount())
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                return income.subtract(expense);
        }

        private BigDecimal computeNetWorthAt(YearMonth ym, Currency currency) {
                LocalDate asOf = ym.atEndOfMonth();

                List<AccountView> accounts = listAccounts.handle().stream()
                                .filter(a -> a.currency() == currency)
                                .toList();

                BigDecimal liquidity = accounts.stream()
                                .map(a -> accountBalance.handle(a.id(), asOf).balance())
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal invested = snapshots.findByMonthAndCurrency(ym, currency)
                                .map(s -> s.getTotalInvested().getAmount())
                                .orElse(BigDecimal.ZERO);

                return liquidity.add(invested);
        }
}
