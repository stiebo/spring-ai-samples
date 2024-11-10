package dev.stiebo.aiutilities.dto;

import jakarta.validation.constraints.NotBlank;

public record ChatInDto(
        @NotBlank
        String question
) {
}
