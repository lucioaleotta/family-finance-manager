package com.lucio.financeapp.transactions.application;

import com.lucio.financeapp.shared.domain.Money;
import com.lucio.financeapp.transactions.domain.Transaction;
import com.lucio.financeapp.shared.domain.Currency;
import com.lucio.financeapp.transactions.domain.TransactionType;
import com.lucio.financeapp.transactions.domain.ports.TransactionRepository;
import com.lucio.financeapp.transactions.domain.ports.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Service
@Transactional
public class CreateTransferUseCase {

    private final TransactionRepository repository;
    private final AccountRepository accountRepository;

    public CreateTransferUseCase(TransactionRepository repository, AccountRepository accountRepository) {
        this.repository = repository;
        this.accountRepository = accountRepository;
    }

    public UUID handle(UUID userId, CreateTransferCommand command) {
        if (accountRepository.findByIdAndUserId(command.fromAccountId(), userId).isEmpty()) {
            throw new IllegalArgumentException("Account not found: " + command.fromAccountId());
        }
        if (accountRepository.findByIdAndUserId(command.toAccountId(), userId).isEmpty()) {
            throw new IllegalArgumentException("Account not found: " + command.toAccountId());
        }

        UUID transferId = UUID.randomUUID();

        Money money = Money.of(command.amount(), command.currency());

        // Expense on source account
        Transaction out = Transaction.transfer(
            userId,
                command.fromAccountId(),
                money,
                command.date(),
                TransactionType.EXPENSE,
                "Transfer",
                command.description(),
                transferId);

        // Income on destination account
        Transaction in = Transaction.transfer(
            userId,
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
