package com.lucio.financeapp.transactions.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    private UUID id;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Column(nullable = false)
    private String category;

    private String description;

    protected Transaction() {
    }

    private Transaction(UUID id,
            BigDecimal amount,
            LocalDate date,
            TransactionType type,
            String category,
            String description) {
        this.id = id;
        this.amount = amount;
        this.date = date;
        this.type = type;
        this.category = category;
        this.description = description;
    }

    public static Transaction of(BigDecimal amount,
            LocalDate date,
            TransactionType type,
            String category,
            String description) {
        return new Transaction(UUID.randomUUID(), amount, date, type, category, description);
    }

    public UUID getId() {
        return id;
    }

    public BigDecimal getAmount() {
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

    public void update(BigDecimal amount,
            LocalDate date,
            TransactionType type,
            String category,
            String description) {
        this.amount = amount;
        this.date = date;
        this.type = type;
        this.category = category;
        this.description = description;
    }

}
