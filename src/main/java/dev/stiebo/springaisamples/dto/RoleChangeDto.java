package dev.stiebo.springaisamples.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record RoleChangeDto(
        @NotBlank
        @Pattern(regexp = "ADMIN|USER|CODER|TOURGUIDE", message = "Role must be either 'ADMIN', 'USER', 'CODER', or 'TOURGUIDE'")
        String role
) {
}
