package com.lucio.financeapp.transactions.application;

import com.lucio.financeapp.transactions.domain.Transaction;
import com.lucio.financeapp.transactions.domain.TransactionType;
import com.lucio.financeapp.transactions.domain.ports.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Service
@Transactional
public class UpdateTransactionUseCase {

    private final TransactionRepository repository;

    public UpdateTransactionUseCase(TransactionRepository repository) {
        this.repository = repository;

    }

    public void handle(UUID id, UpdateTransactionCommand command) {
        Transaction tx = repository.findById(id)
                .orElseThrow(() -> new TransactionNotFoundException(id));

        tx.update(command.accountId(), command.amount(), command.date(), command.type(), command.category(),
                command.description());
        repository.save(tx);
    }

    public record UpdateTransactionCommand(
            UUID accountId,
            BigDecimal amount,
            LocalDate date,
            TransactionType type,
            String category,
            String description) {
    }

    public static class TransactionNotFoundException extends RuntimeException {
        public TransactionNotFoundException(UUID id) {
            super("Transaction not found: " + id);
        }
    }
}
