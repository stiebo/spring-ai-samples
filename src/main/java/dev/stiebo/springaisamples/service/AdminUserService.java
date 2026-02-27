package dev.stiebo.springaisamples.service;

public interface AdminUserService {
    void addUser(String username, String password);
    void deleteUser(String username);
    String resetUserPassword(String username);
    void changeUserRole(String username, String role);
}
