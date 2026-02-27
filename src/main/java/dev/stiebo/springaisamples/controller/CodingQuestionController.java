package dev.stiebo.springaisamples.controller;

import dev.stiebo.springaisamples.dto.ChatInDto;
import dev.stiebo.springaisamples.dto.CodingAnswerOutDto;
import dev.stiebo.springaisamples.exception.ValidationErrorResponse;
import dev.stiebo.springaisamples.service.CodingQuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Validated
public class CodingQuestionController {

    private final CodingQuestionService codingQuestionService;

    @Autowired
    public CodingQuestionController(CodingQuestionService codingQuestionService) {
        this.codingQuestionService = codingQuestionService;
    }

    @Operation(summary = "Answer Coding Question",
            description = """
                    Accepts a coding question and returns an AI-generated answer.
                    The response includes a concise answer, a detailed explanation,
                    and a code example where applicable.
                    Requires CODER or ADMIN role.
                    """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Answer generated",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CodingAnswerOutDto.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed for request body",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ValidationErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "CODER or ADMIN role required")
    })
    @PostMapping("/coding-question")
    public CodingAnswerOutDto answerCodingQuestion(@RequestBody @Valid ChatInDto request) {
        return codingQuestionService.answerQuestion(request.question());
    }
}
