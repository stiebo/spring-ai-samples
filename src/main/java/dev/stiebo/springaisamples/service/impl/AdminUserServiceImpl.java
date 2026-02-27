package dev.stiebo.springaisamples.service.impl;

import dev.stiebo.springaisamples.repository.UserRepository;
import dev.stiebo.springaisamples.service.AdminUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Random;

@Slf4j
@Service
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private static final String PASSWORD_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
    private static final int PASSWORD_LENGTH = 16;
    private static final Random random = new SecureRandom();

    public AdminUserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void addUser(String username, String password) {
        long userCount = userRepository.countUsers();

        if (userCount == 0) {
            // Bootstrap: first call creates the initial admin, no authentication required
            userRepository.createUser(username, passwordEncoder.encode(password), "ADMIN");
        } else {
            // Subsequent calls require an authenticated admin
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            boolean isAdmin = auth != null && auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            if (!isAdmin) {
                throw new AccessDeniedException("Admin role required");
            }
            userRepository.createUser(username, passwordEncoder.encode(password), "USER");
        }
    }

    @Override
    @Transactional
    public void deleteUser(String username) {
        // Only admins can delete users
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getAuthorities().stream()
                .noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            throw new AccessDeniedException("Admin role required");
        }

        // Check if user exists
        if (!userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("User '" + username + "' not found");
        }

        // Prevent self-deletion
        if (username.equals(auth.getName())) {
            throw new IllegalStateException("Cannot delete your own account");
        }

        // Prevent deleting the last admin
        if (userRepository.isAdmin(username) && userRepository.countAdmins() <= 1) {
            throw new IllegalStateException("Cannot delete the last remaining admin user");
        }

        userRepository.deleteUserByUsername(username);
        log.info("User '{}' deleted by admin", username);
    }

    @Override
    @Transactional
    public String resetUserPassword(String username) {
        // Only admins can reset passwords
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin) {
            throw new AccessDeniedException("Admin role required");
        }

        // Check if user exists
        if (!userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("User '" + username + "' not found");
        }

        // Generate a random password
        String newPassword = generateRandomPassword();
        String encodedPassword = passwordEncoder.encode(newPassword);

        // Update the user's password
        userRepository.updateUserPassword(username, encodedPassword);
        log.info("Password reset for user '{}' by admin", username);

        return newPassword;
    }

    @Override
    @Transactional
    public void changeUserRole(String username, String role) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getAuthorities().stream()
                .noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            throw new AccessDeniedException("Admin role required");
        }

        if (!userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("User '" + username + "' not found");
        }

        // Prevent self-demotion
        if (username.equals(auth.getName())) {
            throw new IllegalStateException("Cannot change your own role");
        }

        // Prevent demoting the last admin
        if ("USER".equals(role) && userRepository.isAdmin(username) && userRepository.countAdmins() <= 1) {
            throw new IllegalStateException("Cannot demote the last remaining admin user");
        }

        userRepository.updateUserRole(username, role);
        log.info("Role of user '{}' changed to ROLE_{} by admin '{}'", username, role,
                auth.getName());
    }

    /**
     * Generate a random password with mixed character types
     *
     * @return a random password
     */
    private String generateRandomPassword() {
        StringBuilder password = new StringBuilder(PASSWORD_LENGTH);
        for (int i = 0; i < PASSWORD_LENGTH; i++) {
            password.append(PASSWORD_CHARS.charAt(random.nextInt(PASSWORD_CHARS.length())));
        }
        return password.toString();
    }
}
