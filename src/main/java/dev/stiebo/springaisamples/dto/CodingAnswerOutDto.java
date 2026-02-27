package dev.stiebo.springaisamples.dto;

public record CodingAnswerOutDto(
        String answer,
        String explanation,
        String codeExample
) {
}
