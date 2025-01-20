package dev.stiebo.springaisamples.dto;

public record EntityDetails(
        EntityType entityType,
        String shortDescription,
        String country,
        String url
) {
}
