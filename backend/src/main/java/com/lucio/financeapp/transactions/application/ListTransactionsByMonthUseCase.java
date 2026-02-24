package com.lucio.financeapp.transactions.application;

import com.lucio.financeapp.transactions.api.TransactionView;
import com.lucio.financeapp.transactions.domain.ports.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ListTransactionsByMonthUseCase {

    private final TransactionRepository repository;

    public ListTransactionsByMonthUseCase(TransactionRepository repository) {
        this.repository = repository;
    }

    public List<TransactionView> handle(UUID userId, YearMonth month) {
        return repository.findByMonthAndUserId(month, userId).stream()
                .map(tx -> new TransactionView(
                        tx.getId(),
                        tx.getAccountId(),
                        tx.getAmount(),
                        tx.getDate(),
                        tx.getType(),
                        tx.getCategory(),
                        tx.getDescription(),
                        tx.getKind(),
                        tx.getTransferId()))
                .toList();
    }
}
