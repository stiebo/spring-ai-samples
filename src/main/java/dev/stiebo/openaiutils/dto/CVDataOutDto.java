package dev.stiebo.openaiutils.dto;

public record CVDataOutDto(
        String shortSummary,
        String firstName,
        String lastName,
        String dateOfBirth,
        String address,
        String[] employmentHistory,
        String[] education,
        String[] courses,
        String competencies,
        String[] languages,
        String hobbies,
        String miscellaneous
) {
}
