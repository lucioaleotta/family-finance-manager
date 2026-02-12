package com.lucio.financeapp.transactions.application;

import com.lucio.financeapp.transactions.domain.Transaction;
import com.lucio.financeapp.transactions.domain.ports.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Service
@Transactional
public class RegisterTransactionUseCase {

    private final TransactionRepository repository;

    public RegisterTransactionUseCase(TransactionRepository repository) {
        this.repository = repository;
    }

    public UUID handle(RegisterTransactionCommand command) {
        Transaction tx = Transaction.of(
                command.amount(),
                command.date(),
                command.type(),
                command.category(),
                command.description()
        );
        repository.save(tx);
        return tx.getId();
    }

    public record RegisterTransactionCommand(
            BigDecimal amount,
            LocalDate date,
            com.lucio.financeapp.transactions.domain.TransactionType type,
            String category,
            String description
    ) {}
}
