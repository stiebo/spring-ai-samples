package dev.stiebo.aiutilities.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.core.io.Resource;

public record FileResource(
        @NotBlank
        String fileName,
        @NotNull
        Resource resource,
        @NotBlank
        String contentType
) {
}
