package com.lucio.financeapp.users.domain;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "base_currency", nullable = false, length = 3)
    private String baseCurrency;

    protected User() {
    }

    public User(UUID id, String username, String passwordHash, String baseCurrency) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.baseCurrency = baseCurrency;
    }

    public UUID getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getBaseCurrency() {
        return baseCurrency;
    }
}
