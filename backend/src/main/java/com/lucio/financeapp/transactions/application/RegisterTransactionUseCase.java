package com.lucio.financeapp.transactions.application;

import com.lucio.financeapp.transactions.domain.Transaction;
import com.lucio.financeapp.transactions.domain.TransactionType;
import com.lucio.financeapp.transactions.domain.ports.TransactionRepository;
import com.lucio.financeapp.shared.domain.Money;
import com.lucio.financeapp.transactions.domain.Currency;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Service
@Transactional
public class RegisterTransactionUseCase {

        private final TransactionRepository repository;
        private final DefaultAccountService defaultAccountService;

        public RegisterTransactionUseCase(TransactionRepository repository,
                        DefaultAccountService defaultAccountService) {
                this.repository = repository;
                this.defaultAccountService = defaultAccountService;
        }

        public UUID handle(RegisterTransactionCommand command) {
                UUID accountId = command.accountId() != null
                                ? command.accountId()
                                : defaultAccountService.getOrCreateDefaultAccountId();

                Money money = Money.of(
                                command.amount(),
                                command.currency());

                Transaction tx = Transaction.of(
                                accountId,
                                money,
                                command.date(),
                                command.type(),
                                command.category(),
                                command.description());

                repository.save(tx);
                return tx.getId();
        }

        public record RegisterTransactionCommand(
                        UUID accountId,
                        BigDecimal amount,
                        Currency currency,
                        LocalDate date,
                        TransactionType type,
                        String category,
                        String description) {
        }

}
