package com.lucio.financeapp.users.infrastructure.persistence;

import java.util.Optional;
import java.util.Objects;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import com.lucio.financeapp.users.domain.User;

@Repository
public class UserJpaRepository {

    private final SpringDataUserRepository delegate;

    public UserJpaRepository(SpringDataUserRepository delegate) {
        this.delegate = delegate;
    }

    public User save(@NonNull User user) {
        return Objects.requireNonNull(delegate.save(user));
    }

    public Optional<User> findByUsername(@NonNull String username) {
        return delegate.findByUsername(username);
    }

    public Optional<User> findById(@NonNull UUID id) {
        return delegate.findById(id);
    }
}

interface SpringDataUserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByUsername(String username);
}
