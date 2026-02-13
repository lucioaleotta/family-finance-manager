package com.lucio.financeapp.transactions.domain;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "accounts")
public class Account {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 3)
    private Currency currency;

    protected Account() {
    }

    private Account(UUID id, String name, AccountType type, Currency currency) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.currency = currency;
    }

    public static Account of(String name, AccountType type, Currency currency) {
        return new Account(UUID.randomUUID(), name, type, currency);
    }

    public UUID getId() {
        return id;
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
}
