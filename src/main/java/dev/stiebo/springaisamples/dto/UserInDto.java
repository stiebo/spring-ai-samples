package dev.stiebo.springaisamples.dto;

import jakarta.validation.constraints.NotBlank;

public record UserInDto(
        @NotBlank String username,
        @NotBlank String password
) {
}
