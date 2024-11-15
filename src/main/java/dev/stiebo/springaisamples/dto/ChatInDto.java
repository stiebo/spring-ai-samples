package dev.stiebo.springaisamples.dto;

import jakarta.validation.constraints.NotBlank;

public record ChatInDto(
        @NotBlank
        String question
) {
}
