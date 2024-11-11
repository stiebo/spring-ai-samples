package dev.stiebo.aiutilities.configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(title = "AI Demo Utilities Bundle", version = "0.0.1",
                description = "The AI Demo Utilities bundle showcases how AI can transform information processing " +
                        "and interaction with various types of documents"))
public class OpenApiConfig {
}
