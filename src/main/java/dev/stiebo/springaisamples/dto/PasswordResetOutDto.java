package dev.stiebo.springaisamples.dto;

public record PasswordResetOutDto(
        String username,
        String newPassword,
        String message
) {
}
