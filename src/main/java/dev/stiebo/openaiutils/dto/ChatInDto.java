package dev.stiebo.openaiutils.dto;

import jakarta.validation.constraints.NotBlank;

public record ChatInDto(
        @NotBlank
        String question
) {
}
