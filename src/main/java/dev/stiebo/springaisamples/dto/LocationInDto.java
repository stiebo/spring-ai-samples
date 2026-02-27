package dev.stiebo.springaisamples.dto;

import jakarta.validation.constraints.NotBlank;

public record LocationInDto(
        @NotBlank
        String location
) {
}
