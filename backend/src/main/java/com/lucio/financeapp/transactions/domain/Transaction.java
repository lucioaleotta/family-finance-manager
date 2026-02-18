package com.lucio.financeapp.transactions.domain;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.UUID;
import com.lucio.financeapp.shared.domain.Money;
import java.time.Instant;

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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionKind kind;

    @Column(name = "transfer_id")
    private UUID transferId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
    }

    protected Transaction() {
    }

    private Transaction(UUID id,
            UUID accountId,
            Money amount,
            LocalDate date,
            TransactionType type,
            String category,
            String description,
            TransactionKind kind,
            UUID transferId) {
        this.id = id;
        this.accountId = accountId;
        this.amount = amount;
        this.date = date;
        this.type = type;
        this.category = category;
        this.description = description;
        this.kind = kind;
        this.transferId = transferId;
        this.createdAt = Instant.now();
    

    }

    public static Transaction standard(UUID accountId,
            Money amount,
            LocalDate date,
            TransactionType type,
            String category,
            String description) {
        return new Transaction(UUID.randomUUID(), accountId, amount, date, type, category, description,
                TransactionKind.STANDARD, null);
    }

    public static Transaction transfer(UUID accountId,
            Money amount,
            LocalDate date,
            TransactionType type,
            String category,
            String description,
            UUID transferId) {
        return new Transaction(UUID.randomUUID(), accountId, amount, date, type, category, description,
                TransactionKind.TRANSFER, transferId);
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

    public TransactionKind getKind() {
        return kind;
    }

    public UUID getTransferId() {
        return transferId;
    }

    public Instant getCreatedAt() {
        return createdAt;
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
