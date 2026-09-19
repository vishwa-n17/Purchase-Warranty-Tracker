package com.purchasewarrantytracker.repository;

import com.purchasewarrantytracker.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class UserRepositorySchemaTest {

    private UserRepository createUserRepository() throws Exception {
        EmbeddedDatabase database = new EmbeddedDatabaseBuilder()
                .generateUniqueName(true)
                .setType(EmbeddedDatabaseType.H2)
                .setScriptEncoding("UTF-8")
                .ignoreFailedDrops(true)
                .build();

        String schema = new BufferedReader(
                new InputStreamReader(
                        getClass().getClassLoader().getResourceAsStream("schema-h2.sql")))
                .lines()
                .collect(Collectors.joining("\n"));

        JdbcTemplate jdbcTemplate = new JdbcTemplate(database);
        jdbcTemplate.execute(schema);

        return new UserRepository(jdbcTemplate);
    }

    @Test
    void saveUserPersistsAgainstH2Schema() throws Exception {
        UserRepository userRepository = createUserRepository();

        User user = new User();
        user.setName("Schema Test User");
        user.setEmail("schema-test@example.com");
        user.setPassword("encoded-password");
        user.setCreatedAt(LocalDateTime.now());

        User saved = userRepository.save(user);

        assertNotNull(saved.getId());
        assertEquals("Schema Test User", saved.getName());
        assertEquals("schema-test@example.com", saved.getEmail());
    }

    @Test
    void findByIdReturnsSavedUser() throws Exception {
        UserRepository userRepository = createUserRepository();

        User user = new User();
        user.setName("Find Test User");
        user.setEmail("find-test@example.com");
        user.setPassword("encoded-password");
        user.setCreatedAt(LocalDateTime.now());

        User saved = userRepository.save(user);
        Long userId = saved.getId();

        User found = userRepository.findById(userId)
                .orElseThrow(() -> new AssertionError("User not found"));

        assertEquals("Find Test User", found.getName());
        assertEquals("find-test@example.com", found.getEmail());
    }
}
