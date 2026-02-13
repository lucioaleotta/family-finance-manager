package com.lucio.financeapp.transactions.application;

import com.lucio.financeapp.transactions.api.TransactionView;
import com.lucio.financeapp.transactions.domain.ports.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ListTransactionsByMonthUseCase {

    private final TransactionRepository repository;

    public ListTransactionsByMonthUseCase(TransactionRepository repository) {
        this.repository = repository;
    }

    public List<TransactionView> handle(YearMonth month) {
        return repository.findByMonth(month).stream()
                .map(tx -> new TransactionView(
                        tx.getId(),
                        tx.getAccountId(),
                        tx.getAmount(),
                        tx.getDate(),
                        tx.getType(),
                        tx.getCategory(),
                        tx.getDescription()
                ))
                .toList();
    }
}
