package com.lucio.financeapp.transactions.application;

import com.lucio.financeapp.shared.domain.Money;
import com.lucio.financeapp.transactions.domain.Transaction;
import com.lucio.financeapp.shared.domain.Currency;
import com.lucio.financeapp.transactions.domain.TransactionType;
import com.lucio.financeapp.transactions.domain.ports.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Service
@Transactional
public class CreateTransferUseCase {

    private final TransactionRepository repository;

    public CreateTransferUseCase(TransactionRepository repository) {
        this.repository = repository;
    }

    public UUID handle(CreateTransferCommand command) {
        UUID transferId = UUID.randomUUID();

        Money money = Money.of(command.amount(), command.currency());

        // Expense on source account
        Transaction out = Transaction.transfer(
                command.fromAccountId(),
                money,
                command.date(),
                TransactionType.EXPENSE,
                "Transfer",
                command.description(),
                transferId);

        // Income on destination account
        Transaction in = Transaction.transfer(
                command.toAccountId(),
                money,
                command.date(),
                TransactionType.INCOME,
                "Transfer",
                command.description(),
                transferId);

        repository.save(out);
        repository.save(in);

        return transferId;
    }

    public record CreateTransferCommand(
            UUID fromAccountId,
            UUID toAccountId,
            BigDecimal amount,
            Currency currency,
            LocalDate date,
            String description) {
    }
}
