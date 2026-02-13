package com.lucio.financeapp.transactions.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import com.lucio.financeapp.shared.domain.Money;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    private UUID id;

    @Embedded
    private Money amount;

    @Column(nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Column(nullable = false)
    private String category;

    private String description;

    @Column(name = "account_id")
    private UUID accountId;

    protected Transaction() {
    }

    private Transaction(UUID id,
            UUID accountId,
            Money amount,
            LocalDate date,
            TransactionType type,
            String category,
            String description) {
        this.id = id;
        this.accountId = accountId;
        this.amount = amount;
        this.date = date;
        this.type = type;
        this.category = category;
        this.description = description;
    }

    public static Transaction of(UUID accountId,
            Money amount,
            LocalDate date,
            TransactionType type,
            String category,
            String description) {
        return new Transaction(UUID.randomUUID(), accountId, amount, date, type, category, description);
    }

    public UUID getId() {
        return id;
    }

    public Money getAmount() {
        return amount;
    }

    public LocalDate getDate() {
        return date;
    }

    public TransactionType getType() {
        return type;
    }

    public String getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public void update(UUID accountId,
            Money amount,
            LocalDate date,
            TransactionType type,
            String category,
            String description) {
        this.accountId = accountId;
        this.amount = amount;
        this.date = date;
        this.type = type;
        this.category = category;
        this.description = description;
    }

}
