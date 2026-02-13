package com.lucio.financeapp.reporting.application;

import com.lucio.financeapp.reporting.api.MonthlyBalanceView;
import com.lucio.financeapp.transactions.api.TransactionFacade;
import com.lucio.financeapp.transactions.api.TransactionView;
import com.lucio.financeapp.transactions.domain.TransactionType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ComputeMonthlyBalanceUseCase {

    private final TransactionFacade transactionFacade;

    public ComputeMonthlyBalanceUseCase(TransactionFacade transactionFacade) {
        this.transactionFacade = transactionFacade;
    }

    public MonthlyBalanceView handle(YearMonth month) {
        List<TransactionView> txs = transactionFacade.findByMonth(month);

        BigDecimal income = txs.stream()
                .filter(t -> t.type() == TransactionType.INCOME)
                .map(TransactionView::amount)
            .map(m -> m.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal expense = txs.stream()
                .filter(t -> t.type() == TransactionType.EXPENSE)
                .map(TransactionView::amount)
            .map(m -> m.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal savings = income.subtract(expense);

        return new MonthlyBalanceView(month, income, expense, savings);
    }
}
