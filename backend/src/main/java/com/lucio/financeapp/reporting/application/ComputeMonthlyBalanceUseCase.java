package com.lucio.financeapp.reporting.application;

import com.lucio.financeapp.reporting.api.MonthlyBalanceView;
import com.lucio.financeapp.transactions.api.TransactionFacade;
import com.lucio.financeapp.transactions.api.TransactionView;
import com.lucio.financeapp.transactions.domain.TransactionType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.lucio.financeapp.transactions.domain.TransactionKind;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ComputeMonthlyBalanceUseCase {

    private final TransactionFacade transactionFacade;

    public ComputeMonthlyBalanceUseCase(TransactionFacade transactionFacade) {
        this.transactionFacade = transactionFacade;
    }

    public MonthlyBalanceView handle(UUID userId, YearMonth month) {
        List<TransactionView> txs = transactionFacade.findByMonth(userId, month);

        var standard = txs.stream()
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

        BigDecimal savings = income.subtract(expense);

        return new MonthlyBalanceView(month, income, expense, savings);
    }
}
