package com.lucio.financeapp.users.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.lucio.financeapp.users.domain.User;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.AUTO_CONFIGURED)
class SpringDataUserRepositoryTest {

    @Autowired
    private SpringDataUserRepository repository;

    @Test
    void findByUsernameReturnsUserWhenPresent() {
        User user = new User(UUID.randomUUID(), "anna", "anna@example.com", "hash-1", "EUR");
        repository.save(user);

        Optional<User> found = repository.findByUsername("anna");

        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("anna");
        assertThat(found.get().getEmail()).isEqualTo("anna@example.com");
        assertThat(found.get().getBaseCurrency()).isEqualTo("EUR");
    }

    @Test
    void findByUsernameReturnsEmptyWhenMissing() {
        Optional<User> found = repository.findByUsername("missing-user");

        assertThat(found).isEmpty();
    }

    @Test
    void findByEmailIgnoreCaseReturnsUserWhenPresent() {
        User user = new User(UUID.randomUUID(), "mario", "mario@example.com", "hash-2", "CHF");
        repository.save(user);

        Optional<User> found = repository.findByEmailIgnoreCase("MARIO@EXAMPLE.COM");

        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("mario");
    }
}
