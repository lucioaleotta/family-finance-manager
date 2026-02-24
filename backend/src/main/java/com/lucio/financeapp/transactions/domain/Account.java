package com.lucio.financeapp.transactions.domain;

import jakarta.persistence.*;
import com.lucio.financeapp.shared.domain.Currency;

import java.util.UUID;

@Entity
@Table(name = "accounts")
public class Account {

    @Id
    private UUID id;

    @Column(name = "user_id")
    private UUID userId;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 3)
    private Currency currency;

    protected Account() {
    }

    private Account(UUID id, UUID userId, String name, AccountType type, Currency currency) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.type = type;
        this.currency = currency;
    }

    public static Account of(UUID userId, String name, AccountType type, Currency currency) {
        ensureSupportedType(type);
        return new Account(UUID.randomUUID(), userId, name, type, currency);
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public AccountType getType() {
        return type;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void update(String name, AccountType type, Currency currency) {
        ensureSupportedType(type);
        this.name = name;
        this.type = type;
        this.currency = currency;
    }

    private static void ensureSupportedType(AccountType type) {
        if (type == null || !type.isSupportedForManualAccount()) {
            throw new IllegalArgumentException("Only CHECKING, LIQUIDITY and INVESTMENT account types are allowed");
        }
    }
}
