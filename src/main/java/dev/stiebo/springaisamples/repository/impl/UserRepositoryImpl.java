package dev.stiebo.springaisamples.repository.impl;

import dev.stiebo.springaisamples.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
public class UserRepositoryImpl implements UserRepository {

    private final JdbcTemplate jdbcTemplate;

    public UserRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public long countUsers() {
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Long.class);
        return count != null ? count : 0;
    }

    @Override
    public boolean existsByUsername(String username) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE username = ?", Integer.class, username);
        return count != null && count > 0;
    }

    @Override
    public void createUser(String username, String encodedPassword, String role) {
        jdbcTemplate.update(
                "INSERT INTO users (username, password, enabled) VALUES (?, ?, ?)",
                username, encodedPassword, true);
        jdbcTemplate.update(
                "INSERT INTO authorities (username, authority) VALUES (?, ?)",
                username, "ROLE_" + role);
        log.info("User '{}' created with role: ROLE_{}", username, role);
    }

    @Override
    public void deleteUserByUsername(String username) {
        // Delete authorities first due to foreign key constraint
        jdbcTemplate.update("DELETE FROM authorities WHERE username = ?", username);
        // Then delete the user
        int rowsDeleted = jdbcTemplate.update("DELETE FROM users WHERE username = ?", username);
        if (rowsDeleted > 0) {
            log.info("User '{}' deleted successfully", username);
        } else {
            log.warn("User '{}' not found for deletion", username);
        }
    }

    @Override
    public void updateUserPassword(String username, String encodedPassword) {
        int rowsUpdated = jdbcTemplate.update(
                "UPDATE users SET password = ? WHERE username = ?",
                encodedPassword, username);
        if (rowsUpdated > 0) {
            log.info("Password updated for user '{}'", username);
        } else {
            log.warn("User '{}' not found for password update", username);
        }
    }

    @Override
    public void updateUserRole(String username, String role) {
        int rowsUpdated = jdbcTemplate.update(
                "UPDATE authorities SET authority = ? WHERE username = ?",
                "ROLE_" + role, username);
        if (rowsUpdated > 0) {
            log.info("Role updated for user '{}' to ROLE_{}", username, role);
        } else {
            log.warn("No authority record found for user '{}' during role update", username);
        }
    }

    @Override
    public boolean isAdmin(String username) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM authorities WHERE username = ? AND authority = 'ROLE_ADMIN'",
                Integer.class, username);
        return count != null && count > 0;
    }

    @Override
    public long countAdmins() {
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM authorities WHERE authority = 'ROLE_ADMIN'", Long.class);
        return count != null ? count : 0;
    }
}
