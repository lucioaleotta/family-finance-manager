package com.lucio.financeapp.transactions.infrastructure.persistence;

import com.lucio.financeapp.shared.domain.Currency;
import com.lucio.financeapp.shared.domain.Money;
import com.lucio.financeapp.transactions.domain.Transaction;
import com.lucio.financeapp.transactions.domain.TransactionType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.AUTO_CONFIGURED)
class JpaTransactionRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private SpringDataTransactionRepository repository;

    @Test
    void findByMonthOrdersByDateDesc() {
        UUID accountId = UUID.randomUUID();
        YearMonth month = YearMonth.of(2024, 5);

        Transaction first = transaction(accountId, LocalDate.of(2024, 5, 3));
        Transaction second = transaction(accountId, LocalDate.of(2024, 5, 20));
        Transaction third = transaction(accountId, LocalDate.of(2024, 5, 1));

        entityManager.persist(first);
        entityManager.persist(second);
        entityManager.persist(third);
        entityManager.flush();

        List<Transaction> result = repository.findByDateBetweenOrderByDateDesc(
                month.atDay(1),
                month.atEndOfMonth());

        assertThat(result)
                .extracting(Transaction::getDate)
                .containsExactly(
                        LocalDate.of(2024, 5, 20),
                        LocalDate.of(2024, 5, 3),
                        LocalDate.of(2024, 5, 1));
    }

    @Test
    void findByAccountUpToOrdersByDateDesc() {
        UUID accountId = UUID.randomUUID();
        UUID otherAccountId = UUID.randomUUID();

        entityManager.persist(transaction(accountId, LocalDate.of(2024, 6, 2)));
        entityManager.persist(transaction(accountId, LocalDate.of(2024, 6, 10)));
        entityManager.persist(transaction(otherAccountId, LocalDate.of(2024, 6, 15)));
        entityManager.persist(transaction(accountId, LocalDate.of(2024, 5, 30)));
        entityManager.flush();

        List<Transaction> result = repository.findByAccountIdAndDateLessThanEqualOrderByDateDescCreatedAtDesc(
                accountId,
                LocalDate.of(2024, 6, 10));

        assertThat(result)
                .extracting(Transaction::getDate)
                .containsExactly(
                        LocalDate.of(2024, 6, 10),
                        LocalDate.of(2024, 6, 2),
                        LocalDate.of(2024, 5, 30));
    }

    private Transaction transaction(UUID accountId, LocalDate date) {
        return Transaction.standard(
                accountId,
                Money.of(BigDecimal.valueOf(10), Currency.EUR),
                date,
                TransactionType.EXPENSE,
                "food",
                "test");
    }
}
