package com.lucio.financeapp.reporting.application;

import com.lucio.financeapp.reporting.api.MonthlyAccountSummaryView;
import com.lucio.financeapp.transactions.api.TransactionFacade;
import com.lucio.financeapp.transactions.api.TransactionView;
import com.lucio.financeapp.transactions.domain.TransactionType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ComputeMonthlyAccountsUseCase {

    private final TransactionFacade transactionFacade;

    public ComputeMonthlyAccountsUseCase(TransactionFacade transactionFacade) {
        this.transactionFacade = transactionFacade;
    }

    public List<MonthlyAccountSummaryView> handle(UUID userId, YearMonth month) {
        List<TransactionView> txs = transactionFacade.findByMonth(userId, month);

        Map<UUID, List<TransactionView>> byAccount = txs.stream()
                .filter(t -> t.accountId() != null)
                .collect(Collectors.groupingBy(TransactionView::accountId));

        List<MonthlyAccountSummaryView> result = new ArrayList<>();

        for (var entry : byAccount.entrySet()) {
            UUID accountId = entry.getKey();
            List<TransactionView> list = entry.getValue();

            BigDecimal income = list.stream()
                    .filter(t -> t.type() == TransactionType.INCOME)
                    .map(t -> t.amount().getAmount())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal expense = list.stream()
                    .filter(t -> t.type() == TransactionType.EXPENSE)
                    .map(t -> t.amount().getAmount())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            result.add(new MonthlyAccountSummaryView(month, accountId, income, expense, income.subtract(expense)));
        }

        // ordinamento stabile
        result.sort(Comparator.comparing(MonthlyAccountSummaryView::accountId));
        return result;
    }
}
