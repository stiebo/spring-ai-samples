package dev.stiebo.springaisamples.repository;

public interface UserRepository {
    long countUsers();
    boolean existsByUsername(String username);
    void createUser(String username, String encodedPassword, String role);
    void deleteUserByUsername(String username);
    void updateUserPassword(String username, String encodedPassword);
    void updateUserRole(String username, String role);
    boolean isAdmin(String username);
    long countAdmins();
}
