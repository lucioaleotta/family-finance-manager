package com.lucio.financeapp.users.infrastructure.persistence;

import java.util.Optional;
import java.util.Objects;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.lucio.financeapp.users.domain.User;

@Repository
public class UserJpaRepository {

    private final SpringDataUserRepository delegate;

    public UserJpaRepository(SpringDataUserRepository delegate) {
        this.delegate = delegate;
    }

    public User save(User user) {
        return delegate.save(Objects.requireNonNull(user));
    }

    public Optional<User> findByUsername(String username) {
        return delegate.findByUsername(Objects.requireNonNull(username));
    }

    public Optional<User> findByEmail(String email) {
        return delegate.findByEmailIgnoreCase(Objects.requireNonNull(email));
    }

    public Optional<User> findById(UUID id) {
        return delegate.findById(Objects.requireNonNull(id));
    }
}

interface SpringDataUserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByUsername(String username);

    Optional<User> findByEmailIgnoreCase(String email);
}
